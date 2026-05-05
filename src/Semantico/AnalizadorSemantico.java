package Semantico;

import ArbolSintactico.*;
import Simbolos.*;
import Errores.GestorErrores;
import java.util.ArrayList; // Necesario para argumentos
import java.util.HashMap;
import java.util.Map;

public class AnalizadorSemantico {

    private TablaSimbolos ts;
    private int desplazamientoActual = 0;

    // COMUNICACIÓN CON BACKEND
    public static Map<String, Integer> argumentosOffset = new HashMap<>();
    public static Map<String, Integer> globalesDeclaradas = new HashMap<>();

    public AnalizadorSemantico() {
        this.ts = new TablaSimbolos();
    }

    private int getTamano(TSB tipo) {
        if (tipo == TSB.TS_ENTER) return 4;
        if (tipo == TSB.TS_BOOL)  return 4;
        if (tipo == TSB.TS_CHAR)  return 4;
        return 0;
    }

    // (Int <-> Char)
    private boolean sonCompatibles(TSB t1, TSB t2) {
        if (t1 == t2) return true;
        if (t1 == TSB.TS_ENTER && t2 == TSB.TS_CHAR) return true;
        if (t1 == TSB.TS_CHAR && t2 == TSB.TS_ENTER) return true;
        return false;
    }

    public void analizar(NodoBloque raiz) {
        this.desplazamientoActual = 0;
        argumentosOffset.clear();
        globalesDeclaradas.clear();

        for (NodoSentencia s : raiz.sentencias) {
            analizarSentencia(s);
        }
        // DEBUG
        //System.out.println("--- Análisis Semántico Finalizado ---");
    }

    private void analizarSentencia(NodoSentencia nodo) {
        if (nodo == null) return;

        if (nodo instanceof NodoDeclaracion) {
            procesarDeclaracion((NodoDeclaracion) nodo);
        }
        else if (nodo instanceof NodoAsignacion) {
            procesarAsignacion((NodoAsignacion) nodo);
        }
        else if (nodo instanceof NodoAsignacionArray) {
            procesarAsignacionArray((NodoAsignacionArray) nodo);
        }
        else if (nodo instanceof NodoFuncion) {
            procesarFuncion((NodoFuncion) nodo);
        }
        else if (nodo instanceof NodoBloque) {
            ts.entraBloc();
            for (NodoSentencia s : ((NodoBloque) nodo).sentencias) analizarSentencia(s);
            ts.salirBloc();
        }
        else if (nodo instanceof NodoIf) {
            NodoIf nif = (NodoIf) nodo;
            TSB tCond = evaluarExpresion(nif.condicion);
            if (tCond != TSB.TS_BOOL && tCond != TSB.TS_ERROR) {
                GestorErrores.errorSemantico(nif.linea +1 , 0, "La condición del IF debe ser de tipo booleano.");
            }
            analizarSentencia(nif.bloqueIf);
            if (nif.bloqueElse != null) analizarSentencia(nif.bloqueElse);
        }
        else if (nodo instanceof NodoWhile) {
            NodoWhile nw = (NodoWhile) nodo;
            TSB tCond = evaluarExpresion(nw.condicion);
            if (tCond != TSB.TS_BOOL && tCond != TSB.TS_ERROR) {
                GestorErrores.errorSemantico(nw.linea+1 , 0, "La condición del WHILE debe ser de tipo booleano.");
            }
            analizarSentencia(nw.cuerpo);
        }
        else if (nodo instanceof NodoFor) {
            NodoFor nf = (NodoFor) nodo;
            ts.entraBloc();
            analizarSentencia(nf.inicializacion);

            TSB tCond = evaluarExpresion(nf.condicion);
            if (tCond != TSB.TS_BOOL && tCond != TSB.TS_ERROR) {
                GestorErrores.errorSemantico(nf.linea+1 , 0, "La condición del FOR debe ser de tipo booleano.");
            }

            analizarSentencia(nf.incremento);
            analizarSentencia(nf.cuerpo);
            ts.salirBloc();
        }
        else if (nodo instanceof NodoSwitch) {
            procesarSwitch((NodoSwitch) nodo);
        }
        else if (nodo instanceof NodoImprimir) {
            evaluarExpresion(((NodoImprimir) nodo).expresion);
        }
        else if (nodo instanceof NodoLeer) {
            evaluarExpresion(((NodoLeer) nodo).objetivo);
        }
        else if (nodo instanceof NodoSentenciaLlamada) {
            procesarLlamada(((NodoSentenciaLlamada) nodo).llamada);
        }
        else if (nodo instanceof NodoReturn) {
            NodoReturn ret = (NodoReturn) nodo;
            if (ret.expresion != null) evaluarExpresion(ret.expresion);
        }
    }

    private void procesarFuncion(NodoFuncion func) {
        Descripcion d = new Descripcion(Descripcion.Categoria.DPROC, func.tipoRetorno);

        // Guardar tipos de argumentos para validar llamadas después
        d.listaTiposArgs = new ArrayList<>();
        for (NodoDeclaracion param : func.parametros) {
            d.listaTiposArgs.add(param.tipo);
        }

        ts.poner(func.nombre, d);

        int oldDesp = this.desplazamientoActual;
        this.desplazamientoActual = 0;

        ts.entraBloc();
        int currentArgOffset = 8;

        for (NodoDeclaracion param : func.parametros) {
            Descripcion dp = new Descripcion(Descripcion.Categoria.DARG, param.tipo);
            if (!ts.poner(param.identificador, dp)) {
                GestorErrores.errorSemantico(func.linea+1 , 0, "Parámetro duplicado: " + param.identificador);
            }
            String claveMapa = func.nombre + "_" + param.identificador;
            argumentosOffset.put(claveMapa, currentArgOffset);
            currentArgOffset += 4;
        }

        analizarSentencia(func.cuerpo);
        d.ocupVL = this.desplazamientoActual;
        ts.salirBloc();
        this.desplazamientoActual = oldDesp;
    }

    private void procesarDeclaracion(NodoDeclaracion decl) {
        Descripcion d = new Descripcion(Descripcion.Categoria.DVAR, decl.tipo);
        int tamanoTotal = getTamano(decl.tipo);

        if (decl.tamanoArray > 0) {
            tamanoTotal = tamanoTotal * decl.tamanoArray;
            d.tamano = decl.tamanoArray;
        }

        d.direccion = this.desplazamientoActual;
        this.desplazamientoActual += tamanoTotal;

        if (ts.nivelActual == 0) {
            globalesDeclaradas.put(decl.identificador, tamanoTotal);
        }

        if (!ts.poner(decl.identificador, d)) {
            GestorErrores.errorSemantico(decl.linea+1 , decl.columna+1 , "Variable duplicada: " + decl.identificador);
        }

        if (decl.inicializacion != null && decl.tamanoArray == 0) {
            TSB tInit = evaluarExpresion(decl.inicializacion);
            if (!sonCompatibles(decl.tipo, tInit) && tInit != TSB.TS_ERROR) {
                GestorErrores.errorSemantico(decl.linea+1 , decl.columna+1 , "Tipos incompatibles en inicialización.");
            }
        }
    }

    private void procesarAsignacion(NodoAsignacion asig) {
        Descripcion d = ts.get(asig.identificador);
        if (d == null) {
            GestorErrores.errorSemantico(asig.linea+1 , asig.columna+1 , "Variable no declarada: " + asig.identificador);
            return;
        }
        if (d.tamano != null && d.tamano > 0) {
            GestorErrores.errorSemantico(asig.linea+1 , asig.columna+1 , "No se puede asignar directamente a un array completo. Usa índices.");
            return;
        }

        TSB tipoExpr = evaluarExpresion(asig.expresion);
        if (!sonCompatibles(d.tsb, tipoExpr) && tipoExpr != TSB.TS_ERROR) {
            GestorErrores.errorSemantico(asig.linea+1, asig.columna+1, "Tipos incompatibles en asignación.");
        }
    }

    private void procesarAsignacionArray(NodoAsignacionArray asig) {
        Descripcion d = ts.get(asig.nombre);
        if (d == null) {
            GestorErrores.errorSemantico(asig.linea+1, asig.columna+1, "Array no declarado: " + asig.nombre);
            // No hacemos return para seguir evaluando hijos
        } else if (d.tamano == null || d.tamano == 0) {
            GestorErrores.errorSemantico(asig.linea+1, asig.columna+1, "La variable '" + asig.nombre + "' no es un array.");
        }

        TSB tIndice = evaluarExpresion(asig.indice);
        if (tIndice != TSB.TS_ENTER && tIndice != TSB.TS_ERROR) {
            GestorErrores.errorSemantico(asig.linea+1, asig.columna+1, "El índice del array debe ser entero.");
        }

        TSB tValor = evaluarExpresion(asig.valor);
        if (d != null && !sonCompatibles(d.tsb, tValor) && tValor != TSB.TS_ERROR) {
            GestorErrores.errorSemantico(asig.linea+1, asig.columna+1, "Tipos incompatibles en asignación a array.");
        }
    }

    private void procesarLlamada(NodoLlamada call) {
        Descripcion d = ts.get(call.nombre);
        if (d == null) {
            GestorErrores.errorSemantico(call.linea+1, call.columna+1, "Función no declarada: " + call.nombre);
            // Evaluamos argumentos para seguir buscando errores dentro
            for(NodoExpresion arg : call.argumentos) evaluarExpresion(arg);
            return;
        }

        // Verificar número de argumentos
        int esperados = (d.listaTiposArgs != null) ? d.listaTiposArgs.size() : 0;
        int recibidos = call.argumentos.size();

        if (esperados != recibidos) {
            GestorErrores.errorSemantico(call.linea +1, call.columna+1,
                    "Número de argumentos incorrecto en llamada a '" + call.nombre +
                            "'. Esperados: " + esperados + ", Recibidos: " + recibidos);
        }

        // Verificar tipos
        for (int i = 0; i < recibidos; i++) {
            TSB tArg = evaluarExpresion(call.argumentos.get(i));
            if (i < esperados) { // Solo comprobamos si estamos dentro del rango
                TSB tParam = d.listaTiposArgs.get(i);
                if (!sonCompatibles(tParam, tArg) && tArg != TSB.TS_ERROR) {
                    GestorErrores.errorSemantico(call.linea +1, call.columna+1,
                            "Tipo de argumento incorrecto en llamada a '" + call.nombre +
                                    "' (arg " + (i+1) + ").");
                }
            }
        }
    }

    private void procesarSwitch(NodoSwitch nodo) {
        TSB tDisc = evaluarExpresion(nodo.discriminante);
        if (tDisc != TSB.TS_ENTER && tDisc != TSB.TS_CHAR && tDisc != TSB.TS_ERROR) {
            GestorErrores.errorSemantico(nodo.linea+1, 0, "El valor del Switch debe ser Entero o Char.");
        }
        for (NodoCaso c : nodo.casos) {
            if (c.valor != null) {
                TSB tCaso = evaluarExpresion(c.valor);
                if (!sonCompatibles(tDisc, tCaso) && tCaso != TSB.TS_ERROR) {
                    GestorErrores.errorSemantico(nodo.linea+1, 0, "Tipo del caso incompatible con el Switch.");
                }
            }
            if (c.bloque != null) {
                ts.entraBloc();
                for (NodoSentencia s : c.bloque.sentencias) analizarSentencia(s);
                ts.salirBloc();
            }
        }
    }

    //  EVALUADOR DE EXPRESIONES

    private TSB evaluarExpresion(NodoExpresion nodo) {
        if (nodo == null) return TSB.TS_NULL;

        TSB resultado = TSB.TS_ERROR; // Por defecto error si algo falla

        if (nodo instanceof NodoLiteral) {
            resultado = ((NodoLiteral) nodo).tipo;
        }
        else if (nodo instanceof NodoIdentificador) {
            Descripcion d = ts.get(((NodoIdentificador) nodo).nombre);
            if (d == null) {
                GestorErrores.errorSemantico(nodo.linea +1 ,  nodo.columna +1 , "Variable no declarada: " + ((NodoIdentificador) nodo).nombre);
                resultado = TSB.TS_ERROR;
            } else {
                resultado = d.tsb;
            }
        }
        else if (nodo instanceof NodoAccesoArray) {
            NodoAccesoArray acc = (NodoAccesoArray) nodo;
            Descripcion d = ts.get(acc.nombre);
            if (d == null) {
                GestorErrores.errorSemantico(nodo.linea +1 , nodo.columna+1 , "Array no declarado: " + acc.nombre);
                resultado = TSB.TS_ERROR;
            } else {
                TSB tInd = evaluarExpresion(acc.indice);
                if (tInd != TSB.TS_ENTER && tInd != TSB.TS_ERROR) {
                    GestorErrores.errorSemantico(nodo.linea+1 , nodo.columna+1 , "Índice de array debe ser entero.");
                }
                if (d.tamano == null || d.tamano == 0) {
                    GestorErrores.errorSemantico(nodo.linea+1 , nodo.columna+1 , "La variable '" + acc.nombre + "' no es un array.");
                    resultado = TSB.TS_ERROR;
                } else {
                    resultado = d.tsb;
                }
            }
        }
        else if (nodo instanceof NodoUnario) {
            TSB tExpr = evaluarExpresion(((NodoUnario) nodo).expresion);
            NodoUnario.Operador op = ((NodoUnario) nodo).operador;

            if (tExpr == TSB.TS_ERROR) {
                resultado = TSB.TS_ERROR;
            } else if (op == NodoUnario.Operador.NOT) {
                if (tExpr != TSB.TS_BOOL) {
                    GestorErrores.errorSemantico(nodo.linea+1 , nodo.columna+1 , "Operador NOT requiere booleano.");
                }
                resultado = TSB.TS_BOOL; // Asumimos bool para recuperar
            } else { // MENOS UNARIO
                if (!sonCompatibles(TSB.TS_ENTER, tExpr)) {
                    GestorErrores.errorSemantico(nodo.linea+1 , nodo.columna+1 , "Menos unario requiere numérico.");
                }
                resultado = TSB.TS_ENTER;
            }
        }
        else if (nodo instanceof NodoOperacion) {
            NodoOperacion opNode = (NodoOperacion) nodo;
            TSB tIz = evaluarExpresion(opNode.izquierda);
            TSB tDe = evaluarExpresion(opNode.derecha);
            NodoOperacion.TipoOperador op = opNode.operacion;

            // Lógica de recuperación:
            // Si hay error en hijos, devolvemos el tipo ESPERADO de esta operación
            // para que los nodos padres no sigan quejándose en cadena.

            boolean esAritmetica = (op == NodoOperacion.TipoOperador.SUMA || op == NodoOperacion.TipoOperador.RESTA ||
                    op == NodoOperacion.TipoOperador.MULT || op == NodoOperacion.TipoOperador.DIV ||
                    op == NodoOperacion.TipoOperador.MOD);

            boolean esRelacional = (op == NodoOperacion.TipoOperador.MENOR || op == NodoOperacion.TipoOperador.MAYOR ||
                    op == NodoOperacion.TipoOperador.MENOR_IGUAL || op == NodoOperacion.TipoOperador.MAYOR_IGUAL);

            boolean esIgualdad = (op == NodoOperacion.TipoOperador.IGUAL || op == NodoOperacion.TipoOperador.DIFERENTE);

            boolean esLogica = (op == NodoOperacion.TipoOperador.AND || op == NodoOperacion.TipoOperador.OR);

            if (esAritmetica) {
                if ((!sonCompatibles(TSB.TS_ENTER, tIz) && tIz != TSB.TS_ERROR) ||
                        (!sonCompatibles(TSB.TS_ENTER, tDe) && tDe != TSB.TS_ERROR)) {
                    GestorErrores.errorSemantico(nodo.linea+1 , nodo.columna+1 , "Operación aritmética requiere operandos numéricos.");
                }
                resultado = TSB.TS_ENTER;
            }
            else if (esRelacional) {
                if ((!sonCompatibles(TSB.TS_ENTER, tIz) && tIz != TSB.TS_ERROR) ||
                        (!sonCompatibles(TSB.TS_ENTER, tDe) && tDe != TSB.TS_ERROR)) {
                    GestorErrores.errorSemantico(nodo.linea+1 , nodo.columna+1 , "Operación relacional requiere operandos numéricos.");
                }
                resultado = TSB.TS_BOOL;
            }
            else if (esLogica) {
                if ((tIz != TSB.TS_BOOL && tIz != TSB.TS_ERROR) ||
                        (tDe != TSB.TS_BOOL && tDe != TSB.TS_ERROR)) {
                    GestorErrores.errorSemantico(nodo.linea+1 , nodo.columna+1 , "Operación lógica requiere operandos booleanos.");
                }
                resultado = TSB.TS_BOOL;
            }
            else if (esIgualdad) {
                // Para == y !=, los tipos deben ser compatibles entre sí
                if (!sonCompatibles(tIz, tDe) && tIz != TSB.TS_ERROR && tDe != TSB.TS_ERROR) {
                    GestorErrores.errorSemantico(nodo.linea+1 , nodo.columna+1 , "Tipos incompatibles en comparación de igualdad.");
                }
                resultado = TSB.TS_BOOL;
            }
        }
        else if (nodo instanceof NodoLlamada) {
            procesarLlamada((NodoLlamada) nodo);
            Descripcion d = ts.get(((NodoLlamada) nodo).nombre);
            resultado = (d != null) ? d.tsb : TSB.TS_ERROR;
        }

        nodo.tipo = resultado;
        return resultado;
    }

    public TablaSimbolos getTablaSimbolos() { return this.ts; }
}