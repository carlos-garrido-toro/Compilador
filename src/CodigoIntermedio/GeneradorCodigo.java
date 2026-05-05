package CodigoIntermedio;

import ArbolSintactico.*;
import Simbolos.*;

public class GeneradorCodigo {

    private GeneradorC3A gen;
    private TablaSimbolos ts;

    public GeneradorCodigo(TablaSimbolos ts) {
        this.gen = new GeneradorC3A();
        this.ts = ts;
    }

    public void generar(NodoBloque raiz) {
        // PASADA 1: GENERAR SUBRUTINAS
        // DEBUG
        // System.out.println("--- [PASADA 1] Generando Funciones ---");
        for (NodoSentencia s : raiz.sentencias) {
            if (s instanceof NodoFuncion) {
                // DEBUG
                // System.out.println("   -> Generando FUNCION: " + ((NodoFuncion)s).nombre);
                tratarSentencia(s);
            }
        }

        // PASADA 2: GENERAR MAIN
        // DEBUG
        // System.out.println("--- [PASADA 2] Generando Main ---");

        gen.agregar(TipoInstruccion.PMB, null, null, "main");
        ts.entraBloc();

        for (NodoSentencia s : raiz.sentencias) {
            if (s instanceof NodoFuncion) continue;

            if (s instanceof NodoBloque) {
                // DEBUG
                // System.out.println("   -> Generando BLOQUE MAIN");
                tratarSentencia(s);
            }
            else if (s instanceof NodoDeclaracion || s instanceof NodoAsignacion) {
                tratarSentencia(s);
            }
            else {
                // DEBUG
                // System.out.println("   -> [IGNORADO EN MAIN]: " + s.getClass().getSimpleName());
            }
        }

        ts.salirBloc();
        gen.agregar(TipoInstruccion.RETURN, null, null, null);
        // DEBUG
        // System.out.println("--- Generación Completada ---");
    }

    public GeneradorC3A getGenerador() {
        return gen;
    }

    // PROCESAMIENTO DE SENTENCIAS

    private void tratarSentencia(NodoSentencia nodo) {
        if (nodo == null) return;

        if (nodo instanceof NodoDeclaracion) {
            NodoDeclaracion decl = (NodoDeclaracion) nodo;
            Descripcion d = new Descripcion(Descripcion.Categoria.DVAR, decl.tipo);
            if (decl.tipo == TSB.TS_ARRAY) d.tamano = decl.tamanoArray;
            ts.poner(decl.identificador, d);

            if (decl.inicializacion != null) {
                String temp = tratarExpresion(decl.inicializacion);
                gen.agregar(TipoInstruccion.COPY, temp, decl.identificador);
            }
        }
        else if (nodo instanceof NodoAsignacion) {
            NodoAsignacion asig = (NodoAsignacion) nodo;
            String temp = tratarExpresion(asig.expresion);
            gen.agregar(TipoInstruccion.COPY, temp, asig.identificador);
        }
        else if (nodo instanceof NodoAsignacionArray) {
            NodoAsignacionArray naa = (NodoAsignacionArray) nodo;
            String tIndice = tratarExpresion(naa.indice);
            String tValor = tratarExpresion(naa.valor);
            gen.agregar(TipoInstruccion.IND_ASS, tValor, tIndice, naa.nombre);
        }
        else if (nodo instanceof NodoBloque) {
            ts.entraBloc();
            for (NodoSentencia s : ((NodoBloque) nodo).sentencias) {
                tratarSentencia(s);
            }
            ts.salirBloc();
        }
        else if (nodo instanceof NodoIf) {
            tratarIf((NodoIf) nodo);
        }
        else if (nodo instanceof NodoWhile) {
            tratarWhile((NodoWhile) nodo);
        }
        else if (nodo instanceof NodoFor) {
            tratarFor((NodoFor) nodo);
        }
        else if (nodo instanceof NodoSwitch) {
            tratarSwitch((NodoSwitch) nodo);
        }
        else if (nodo instanceof NodoFuncion) {
            tratarFuncion((NodoFuncion) nodo);
        }
        else if (nodo instanceof NodoReturn) {
            NodoReturn ret = (NodoReturn) nodo;
            if (ret.expresion != null) {
                String temp = tratarExpresion(ret.expresion);
                gen.agregar(TipoInstruccion.RETURN, temp);
            } else {
                gen.agregar(TipoInstruccion.RETURN, null);
            }
        }
        else if (nodo instanceof NodoSentenciaLlamada) {
            tratarExpresion(((NodoSentenciaLlamada) nodo).llamada);
        }

        else if (nodo instanceof NodoLeer) {
            NodoLeer leer = (NodoLeer) nodo;
            String destino = tratarExpresion(leer.objetivo);

            // Usamos el tipo que el Semántico guardó en el nodo
            if (leer.objetivo.tipo == TSB.TS_CHAR) {
                gen.agregar(TipoInstruccion.READ_CHAR, null, null, destino);
            } else {
                gen.agregar(TipoInstruccion.READ, null, null, destino);
            }
        }

        else if (nodo instanceof NodoImprimir) {
            NodoImprimir imp = (NodoImprimir) nodo;
            String temp = tratarExpresion(imp.expresion);

            // Si el semántico indica que es CHAR, usamos PRINT_CHAR.
            if (imp.expresion.tipo == TSB.TS_CHAR) {
                gen.agregar(TipoInstruccion.PRINT_CHAR, temp, null, null);
            } else {
                gen.agregar(TipoInstruccion.PRINT, temp, null, null);
            }
        }
    }

    // ESTRUCTURAS DE CONTROL
    private void tratarIf(NodoIf nodo) {
        String etiquetaElse = gen.nuevaEtiqueta();
        String etiquetaFin = gen.nuevaEtiqueta();
        String cond = tratarExpresion(nodo.condicion);

        gen.agregar(TipoInstruccion.IF_EQ, cond, "0", etiquetaElse);
        tratarSentencia(nodo.bloqueIf);
        gen.agregar(TipoInstruccion.GOTO, etiquetaFin);

        gen.agregar(TipoInstruccion.LABEL, etiquetaElse);
        if (nodo.bloqueElse != null) {
            tratarSentencia(nodo.bloqueElse);
        }
        gen.agregar(TipoInstruccion.LABEL, etiquetaFin);
    }

    private void tratarWhile(NodoWhile nodo) {
        String etiquetaInicio = gen.nuevaEtiqueta();
        String etiquetaFin = gen.nuevaEtiqueta();

        gen.agregar(TipoInstruccion.LABEL, etiquetaInicio);
        String cond = tratarExpresion(nodo.condicion);
        gen.agregar(TipoInstruccion.IF_EQ, cond, "0", etiquetaFin);

        tratarSentencia(nodo.cuerpo);

        gen.agregar(TipoInstruccion.GOTO, etiquetaInicio);
        gen.agregar(TipoInstruccion.LABEL, etiquetaFin);
    }

    private void tratarFor(NodoFor nodo) {
        ts.entraBloc();
        tratarSentencia(nodo.inicializacion);
        String lblInicio = gen.nuevaEtiqueta();
        String lblFin = gen.nuevaEtiqueta();

        gen.agregar(TipoInstruccion.LABEL, lblInicio);
        String cond = tratarExpresion(nodo.condicion);
        gen.agregar(TipoInstruccion.IF_EQ, cond, "0", lblFin);

        tratarSentencia(nodo.cuerpo);
        tratarSentencia(nodo.incremento);

        gen.agregar(TipoInstruccion.GOTO, lblInicio);
        gen.agregar(TipoInstruccion.LABEL, lblFin);
        ts.salirBloc();
    }

    private void tratarSwitch(NodoSwitch nodo) {
        String dicriminante = tratarExpresion(nodo.discriminante);
        String lblFin = gen.nuevaEtiqueta();
        String[] etiquetasCasos = new String[nodo.casos.size()];
        for(int i=0; i<nodo.casos.size(); i++) etiquetasCasos[i] = gen.nuevaEtiqueta();

        for (int i = 0; i < nodo.casos.size(); i++) {
            NodoCaso caso = nodo.casos.get(i);
            if (caso.valor != null) {
                String val = tratarExpresion(caso.valor);
                gen.agregar(TipoInstruccion.IF_EQ, dicriminante, val, etiquetasCasos[i]);
            } else {
                gen.agregar(TipoInstruccion.GOTO, etiquetasCasos[i]);
            }
        }
        gen.agregar(TipoInstruccion.GOTO, lblFin);

        for (int i = 0; i < nodo.casos.size(); i++) {
            NodoCaso caso = nodo.casos.get(i);
            gen.agregar(TipoInstruccion.LABEL, etiquetasCasos[i]);
            if (caso.bloque != null) {
                for(NodoSentencia s : caso.bloque.sentencias) tratarSentencia(s);
            }
            gen.agregar(TipoInstruccion.GOTO, lblFin);
        }
        gen.agregar(TipoInstruccion.LABEL, lblFin);
    }

    //  PROCESAMIENTO DE FUNCIONES
    private void tratarFuncion(NodoFuncion nodo) {
        ts.poner(nodo.nombre, new Descripcion(Descripcion.Categoria.DPROC, nodo.tipoRetorno));
        gen.agregar(TipoInstruccion.LABEL, nodo.nombre);
        gen.agregar(TipoInstruccion.PMB, nodo.nombre);

        ts.entraBloc();
        for(NodoDeclaracion param : nodo.parametros) {
            ts.poner(param.identificador, new Descripcion(Descripcion.Categoria.DVAR, param.tipo));
        }
        tratarSentencia(nodo.cuerpo);
        ts.salirBloc();

        if (nodo.tipoRetorno == TSB.TS_VOID) {
            gen.agregar(TipoInstruccion.RETURN, null);
        }
    }

    //  PROCESAMIENTO DE EXPRESIONES
    private String tratarExpresion(NodoExpresion nodo) {
        if (nodo == null) return null;

        if (nodo instanceof NodoLiteral) {
            String t = gen.nuevaTemporal();
            String valor = ((NodoLiteral) nodo).valor.toString();
            if (valor.equals("true")) valor = "1";
            else if (valor.equals("false")) valor = "0";
            else if (valor.startsWith("'")) {
                char c = valor.charAt(1);
                // Gestión básica de escapes (si tu lexer los pasa así)
                if (valor.length() >= 4 && valor.charAt(1) == '\\') {
                    char esc = valor.charAt(2);
                    switch (esc) {
                        case 'n': c = 10; break;
                        case 't': c = 9;  break;
                        case '0': c = 0;  break;
                        case 'r': c = 13; break;
                        default:  c = esc;
                    }
                }
                valor = String.valueOf((int)c);
            }
            gen.agregar(TipoInstruccion.COPY, valor, t);
            return t;
        }
        else if (nodo instanceof NodoIdentificador) {
            return ((NodoIdentificador) nodo).nombre;
        }
        else if (nodo instanceof NodoAccesoArray) {
            NodoAccesoArray naa = (NodoAccesoArray) nodo;
            String tIndice = tratarExpresion(naa.indice);
            String tDestino = gen.nuevaTemporal();
            gen.agregar(TipoInstruccion.IND_VAL, naa.nombre, tIndice, tDestino);
            return tDestino;
        }
        else if (nodo instanceof NodoOperacion) {
            return tratarOperacion((NodoOperacion) nodo);
        }
        else if (nodo instanceof NodoUnario) {
            return tratarUnario((NodoUnario) nodo);
        }
        else if (nodo instanceof NodoLlamada) {
            return tratarLlamada((NodoLlamada) nodo);
        }
        return null;
    }

    private String tratarUnario(NodoUnario nodo) {
        String op = tratarExpresion(nodo.expresion);
        String destino = gen.nuevaTemporal();
        if (nodo.operador == NodoUnario.Operador.NOT) {
            gen.agregar(TipoInstruccion.NOT, op, null, destino);
        } else if (nodo.operador == NodoUnario.Operador.MENOS_UNARIO) {
            gen.agregar(TipoInstruccion.SUB, "0", op, destino);
        }
        return destino;
    }

    private String tratarOperacion(NodoOperacion nodo) {
        String op1 = tratarExpresion(nodo.izquierda);
        String op2 = tratarExpresion(nodo.derecha);
        String destino = gen.nuevaTemporal();

        TipoInstruccion instr = null;
        switch (nodo.operacion) {
            case SUMA: instr = TipoInstruccion.ADD; break;
            case RESTA: instr = TipoInstruccion.SUB; break;
            case MULT: instr = TipoInstruccion.MUL; break;
            case DIV: instr = TipoInstruccion.DIV; break;
            case MOD: instr = TipoInstruccion.MOD; break;
            case AND: instr = TipoInstruccion.AND; break;
            case OR:  instr = TipoInstruccion.OR; break;
            case MENOR: case MAYOR: case IGUAL: case DIFERENTE:
            case MENOR_IGUAL: case MAYOR_IGUAL:
                return tratarRelacional(nodo, op1, op2);
        }
        if (instr != null) {
            gen.agregar(instr, op1, op2, destino);
        }
        return destino;
    }

    private String tratarRelacional(NodoOperacion nodo, String op1, String op2) {
        String destino = gen.nuevaTemporal();
        String lblTrue = gen.nuevaEtiqueta();
        String lblFin = gen.nuevaEtiqueta();
        TipoInstruccion salto = null;
        switch(nodo.operacion) {
            case MENOR:       salto = TipoInstruccion.IF_LT; break;
            case MAYOR:       salto = TipoInstruccion.IF_GT; break;
            case IGUAL:       salto = TipoInstruccion.IF_EQ; break;
            case DIFERENTE:   salto = TipoInstruccion.IF_NE; break;
            case MENOR_IGUAL: salto = TipoInstruccion.IF_LE; break;
            case MAYOR_IGUAL: salto = TipoInstruccion.IF_GE; break;
        }
        gen.agregar(salto, op1, op2, lblTrue);
        gen.agregar(TipoInstruccion.COPY, "0", destino);
        gen.agregar(TipoInstruccion.GOTO, lblFin);
        gen.agregar(TipoInstruccion.LABEL, lblTrue);
        gen.agregar(TipoInstruccion.COPY, "1", destino);
        gen.agregar(TipoInstruccion.LABEL, lblFin);
        return destino;
    }

    private String tratarLlamada(NodoLlamada nodo) {
        for (int i = nodo.argumentos.size() - 1; i >= 0; i--) {
            NodoExpresion arg = nodo.argumentos.get(i);
            String tempArg = tratarExpresion(arg);
            gen.agregar(TipoInstruccion.PARAM_S, tempArg);
        }

        String destino = gen.nuevaTemporal();
        gen.agregar(TipoInstruccion.CALL, nodo.nombre, String.valueOf(nodo.argumentos.size()), destino);
        return destino;
    }
}