import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java_cup.runtime.Symbol;

import Parser.Parser;
import Scanner.Scanner;
import ArbolSintactico.*;
import Errores.GestorErrores;
import Semantico.AnalizadorSemantico;
import CodigoIntermedio.GeneradorCodigo;
import CodigoIntermedio.Instruccion;

public class Main {

    // Configuración de rutas
    static final String CARPETA_SALIDA = "ArchivoCompilacion";
    static final String FILE_TOKENS = CARPETA_SALIDA + "/1_tokens.txt";
    static final String FILE_AST    = CARPETA_SALIDA + "/1b_arbol_sintactico.txt"; // Nuevo archivo
    static final String FILE_TS     = CARPETA_SALIDA + "/2_tabla_simbolos.txt";
    static final String FILE_TV_TP  = CARPETA_SALIDA + "/3_tablas_backend.txt";
    static final String FILE_C3A    = CARPETA_SALIDA + "/4_codigo_intermedio.txt";
    static final String FILE_ASM    = CARPETA_SALIDA + "/5_codigo_asm.s";
    static final String FILE_ERRORES= CARPETA_SALIDA + "/0_errores.txt";

    public static void main(String[] args) {
        String ficheroEntrada = null;

        if (args.length > 0) {
            ficheroEntrada = args[0]; // Usamos el archivo que nos pasen por consola
        } else {
            System.err.println(" No has especificado archivo para compilar");
            System.exit(1);
        }


        System.out.println(" Procesando archivo: " + ficheroEntrada);

        // PREPARAR CARPETA DE SALIDA
        File carpeta = new File(CARPETA_SALIDA);
        if (!carpeta.exists()) carpeta.mkdir();
        // DEBUG
        // System.out.println(" -- Iniciando Compilación --");
        GestorErrores.reset();

        try {
            // GENERACIÓN DE TOKENS (Front-End)
            // DEBUG
            // System.out.println(" Generando fichero de tokens...");
            generarFicheroTokens(ficheroEntrada);

            //  PARSING Y AST
            // Reiniciamos el scanner para el parsing real
            Scanner scanner = new Scanner(new FileReader(ficheroEntrada));
            Parser p = new Parser(scanner);
            Symbol s = p.parse(); // Ejecuta el análisis sintáctico

            NodoBloque raiz = null;
            if (s.value instanceof NodoBloque) {
                raiz = (NodoBloque) s.value;
                // DEBUG
                // System.out.println(" Generando fichero del Árbol Sintáctico...");
                StringBuilder sbAST = new StringBuilder();
                // DEBUG
                //sbAST.append("=== ÁRBOL SINTÁCTICO ABSTRACTO (AST) ===\n");
                construirAST(raiz, "", sbAST);

                escribirArchivo(FILE_AST, sbAST.toString());
            }



            // ANÁLISIS SEMÁNTICO Y TABLA DE SÍMBOLOS
            // DEBUG
            // System.out.println(" Ejecutando Análisis Semántico...");
            AnalizadorSemantico semantico = new AnalizadorSemantico();
            if (raiz != null) {
                semantico.analizar(raiz);
            }

            // Guardamos la Tabla de Símbolos completa
            escribirArchivo(FILE_TS, semantico.getTablaSimbolos().toString());

            // Si hay errores semánticos, generamos informe y salimos
            if (GestorErrores.hayErrores()) {
                generarFicheroErrores();
                System.err.println(" Compilación detenida por errores.");
                System.exit(1);
            }

            // GENERACIÓN DE CÓDIGO INTERMEDIO (Back-End)
            // DEBUG
            //System.out.println("Generando Código Intermedio (C3@)...");
            GeneradorCodigo generador = new GeneradorCodigo(semantico.getTablaSimbolos());
            generador.generar(raiz);

            // Guardar Fichero C3A
            StringBuilder sbC3A = new StringBuilder();
            for (Instruccion inst : generador.getGenerador().getInstrucciones()) {
                sbC3A.append(inst.toString()).append("\n");
            }
            escribirArchivo(FILE_C3A, sbC3A.toString());

            // Guardar Tablas Backend (Variables y Procedimientos)
            String infoTablas = obtenerInfoTablasBackend(generador);
            escribirArchivo(FILE_TV_TP, infoTablas);

            // GENERACIÓN DE ENSAMBLADOR
            // DEBUG
            // System.out.println("Generando Ensamblador...");

            Ensamblador.GeneradorEnsamblador ens = new Ensamblador.GeneradorEnsamblador(
                    generador.getGenerador().getInstrucciones(),
                    semantico.getTablaSimbolos(),
                    FILE_ASM
            );
            ens.generar();

            System.out.println(" COMPILACIÓN EXITOSA");
            System.out.println(" Todos los ficheros generados en: " + CARPETA_SALIDA);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(" Error fatal en el compilador.");
        }
    }

    // Genera el fichero 1_tokens.txt leyendo el archivo independientemente
    private static void generarFicheroTokens(String fichero) {
        try {
            Scanner scannerTokens = new Scanner(new FileReader(fichero));
            StringBuilder sb = new StringBuilder();
            while (true) {
                Symbol s = scannerTokens.next_token();
                if (s.sym == 0) break; // EOF

                // Formato: Linea/Columna | ID | Valor
                sb.append(String.format("Línea %d, Col %d: ID=%d, Valor=%s\n",
                        s.left, s.right, s.sym, s.value));
            }
            escribirArchivo(FILE_TOKENS, sb.toString());
        } catch (Exception e) {
            System.err.println("Error generando tokens: " + e.getMessage());
        }
    }

    // Genera el fichero 0_errores.txt
    private static void generarFicheroErrores() {
        escribirArchivo(FILE_ERRORES, GestorErrores.getInformeErrores());
    }

    // Escribe contenido en un archivo
    private static void escribirArchivo(String ruta, String contenido) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ruta))) {
            writer.write(contenido);
        } catch (IOException e) {
            System.err.println("Error escribiendo archivo " + ruta + ": " + e.getMessage());
        }
    }

    // Simula la obtención de tablas TV/TP
    private static String obtenerInfoTablasBackend(GeneradorCodigo gen) {
        StringBuilder sb = new StringBuilder();

        // TABLA DE VARIABLES
        // (t0, t1...)
        int totalTemps = gen.getGenerador().getContadorTemporales();
        int totalLabels = gen.getGenerador().getContadorEtiquetas();

        sb.append("===================================================\n");
        sb.append("           TABLA DE VARIABLES (TV) - C3@           \n");
        sb.append("===================================================\n");
        sb.append(String.format(" %-25s | %s\n", "RECURSO", "RANGO / CANTIDAD"));
        sb.append("---------------------------|-----------------------\n");

        if (totalTemps > 0) {
            sb.append(String.format(" %-25s | t0 ... t%d (%d total)\n",
                    "Temporales Usados", (totalTemps - 1), totalTemps));
        } else {
            sb.append(String.format(" %-25s | (Ninguno)\n", "Temporales Usados"));
        }

        if (totalLabels > 0) {
            sb.append(String.format(" %-25s | L0 ... L%d (%d total)\n",
                    "Etiquetas de Salto", (totalLabels - 1), totalLabels));
        } else {
            sb.append(String.format(" %-25s | (Ninguna)\n", "Etiquetas de Salto"));
        }
        sb.append("\n");

        // TABLA DE PROCEDIMIENTOS
        // Recorremos las instrucciones buscando 'PMB'
        sb.append("===================================================\n");
        sb.append("         TABLA DE PROCEDIMIENTOS (TP) - C3@        \n");
        sb.append("===================================================\n");
        sb.append(String.format(" %-20s | %-20s\n", "NOMBRE PROC/FUNC", "POSICIÓN (Instr #)"));
        sb.append("----------------------|----------------------------\n");

        boolean encontroFunciones = false;
        int indiceInstruccion = 0;

        // Iteramos sobre la lista de instrucciones generadas
        for (Instruccion inst : gen.getGenerador().getInstrucciones()) {

            // Si encontramos un PMB, es el inicio de una función
            if (inst.tipo == CodigoIntermedio.TipoInstruccion.PMB) {
                String nombreFunc = (inst.destino != null) ? inst.destino : "Desconocido";

                sb.append(String.format(" %-20s | Instrucción #%d\n",
                        nombreFunc, indiceInstruccion));
                encontroFunciones = true;
            }
            indiceInstruccion++;
        }

        if (!encontroFunciones) {
            sb.append(" (No se detectaron procedimientos o funciones)\n");
        }

        return sb.toString();
    }

    private static void construirAST(Object nodo, String indent, StringBuilder sb) {
        if (nodo == null) {
            sb.append(indent).append("└── (null/vacío)\n");
            return;
        }

        if (nodo instanceof NodoBloque) {
            NodoBloque bloque = (NodoBloque) nodo;
            sb.append(indent).append("└── BLOQUE\n");
            for (NodoSentencia sent : bloque.sentencias) {
                construirAST(sent, indent + "    ", sb);
            }
        }
        else if (nodo instanceof NodoAsignacion) {
            NodoAsignacion asig = (NodoAsignacion) nodo;
            sb.append(indent).append("└── ASIGNACION: ").append(asig.identificador).append("\n");
            construirAST(asig.expresion, indent + "    ", sb);
        }
        else if (nodo instanceof NodoDeclaracion) {
            NodoDeclaracion decl = (NodoDeclaracion) nodo;
            sb.append(indent).append("└── DECLARACION: ").append(decl.tipo).append(" ").append(decl.identificador);
            if (decl.inicializacion != null) {
                sb.append(" = \n");
                construirAST(decl.inicializacion, indent + "    ", sb);
            } else {
                sb.append("\n");
            }
        }
        else if (nodo instanceof NodoOperacion) {
            NodoOperacion op = (NodoOperacion) nodo;
            sb.append(indent).append("└── OP: ").append(op.operacion).append("\n");
            construirAST(op.izquierda, indent + "    ", sb);
            construirAST(op.derecha, indent + "    ", sb);
        }
        else if (nodo instanceof NodoImprimir) {
            NodoImprimir imp = (NodoImprimir) nodo;
            sb.append(indent).append("└── IMPRIMIR\n");
            construirAST(imp.expresion, indent + "    ", sb);
        }
        else if (nodo instanceof NodoFuncion) {
            NodoFuncion func = (NodoFuncion) nodo;
            sb.append(indent).append("└── FUNCION: ").append(func.tipoRetorno).append(" ").append(func.nombre).append("\n");
            construirAST(func.cuerpo, indent + "    ", sb);
        }
        else if (nodo instanceof NodoLlamada) {
            NodoLlamada call = (NodoLlamada) nodo;
            sb.append(indent).append("└── LLAMADA: ").append(call.nombre).append("()\n");
        }
        else if (nodo instanceof NodoSentenciaLlamada) {
            NodoSentenciaLlamada sentCall = (NodoSentenciaLlamada) nodo;
            sb.append(indent).append("└── SENTENCIA LLAMADA\n");
            construirAST(sentCall.llamada, indent + "    ", sb);
        }
        else if (nodo instanceof NodoAsignacionArray) {
            NodoAsignacionArray arr = (NodoAsignacionArray) nodo;
            sb.append(indent).append("└── ASIG ARRAY: ").append(arr.nombre).append("\n");
            construirAST(arr.indice, indent + "    │   ", sb);
            construirAST(arr.valor, indent + "        ", sb);
        }
        else if (nodo instanceof NodoAccesoArray) {
            NodoAccesoArray acc = (NodoAccesoArray) nodo;
            sb.append(indent).append("└── ACCESO ARRAY: ").append(acc.nombre).append("\n");
            construirAST(acc.indice, indent + "    ", sb);
        }
        else if (nodo instanceof NodoUnario) {
            NodoUnario un = (NodoUnario) nodo;
            sb.append(indent).append("└── ").append(un.operador).append("\n");
            construirAST(un.expresion, indent + "    ", sb);
        }
        else if (nodo instanceof NodoIf) {
            NodoIf nif = (NodoIf) nodo;
            sb.append(indent).append("└── IF\n");
            construirAST(nif.condicion, indent + "    ", sb);
            construirAST(nif.bloqueIf, indent + "    ", sb);
            if(nif.bloqueElse != null) {
                sb.append(indent).append("└── ELSE\n");
                construirAST(nif.bloqueElse, indent + "    ", sb);
            }
        }
        else if (nodo instanceof NodoWhile) {
            NodoWhile nWhile = (NodoWhile) nodo;
            sb.append(indent).append("└── WHILE\n");
            construirAST(nWhile.condicion, indent + "    ", sb);
            construirAST(nWhile.cuerpo, indent + "    ", sb);
        }
        else if (nodo instanceof NodoFor) {
            NodoFor nFor = (NodoFor) nodo;
            sb.append(indent).append("└── FOR\n");

            // Inicialización
            sb.append(indent).append("    ├── [Init] \n");
            construirAST(nFor.inicializacion, indent + "    │   ", sb);

            // Condición
            sb.append(indent).append("    ├── [Cond] \n");
            construirAST(nFor.condicion, indent + "    │   ", sb);

            // Incremento
            sb.append(indent).append("    ├── [Step] \n");
            construirAST(nFor.incremento, indent + "    │   ", sb);

            // Cuerpo
            sb.append(indent).append("    └── [Body] \n");
            construirAST(nFor.cuerpo, indent + "        ", sb);
        }
        else if (nodo instanceof NodoSwitch) {
            NodoSwitch nSwitch = (NodoSwitch) nodo;
            sb.append(indent).append("└── SWITCH\n");

            // Variables evaluadas
            sb.append(indent).append("    ├── [Expr] \n");
            construirAST(nSwitch.discriminante, indent + "    │   ", sb);

            // Casos
            sb.append(indent).append("    └── [Casos] \n");

            for (NodoCaso caso : nSwitch.casos) {
                // Distinguir entre CASE valor: y DEFAULT:
                if (caso.valor != null) {
                    // Sacamos el valor literal si es posible, si no, imprime la expresión
                    String valStr = (caso.valor instanceof NodoLiteral)
                            ? ((NodoLiteral)caso.valor).valor.toString()
                            : caso.valor.toString();
                    sb.append(indent).append("        ├── CASE ").append(valStr).append(":\n");
                } else {
                    sb.append(indent).append("        ├── DEFAULT:\n");
                }

                // Imprimir el bloque de código dentro del caso
                if (caso.bloque != null) {
                    // Iteramos las sentencias del bloque del caso
                    for (NodoSentencia s : caso.bloque.sentencias) {
                        construirAST(s, indent + "        │   ", sb);
                    }
                }
            }
        }
        else if (nodo instanceof NodoLiteral) {
            NodoLiteral lit = (NodoLiteral) nodo;
            sb.append(indent).append("└── Lit(").append(lit.valor).append(":").append(lit.tipo).append(")\n");
        }
        else if (nodo instanceof NodoIdentificador) {
            NodoIdentificador id = (NodoIdentificador) nodo;
            sb.append(indent).append("└── Id(").append(id.nombre).append(")\n");
        }
        else {
            sb.append(indent).append("└── ").append(nodo.toString()).append("\n");
        }
    }
}