package Errores;

public class GestorErrores {

    private static boolean huboError = false;

    private static StringBuilder logErrores = new StringBuilder();

    public static void errorLexico(int linea, int col, String mensaje) {
        String msg = "[ERROR LÉXICO] Línea " + linea + ", Columna " + col + ": " + mensaje;
        System.err.println(msg);
        huboError = true;

        logErrores.append(msg).append("\n");
    }

    public static void errorSintactico(int linea, int col, String mensaje) {
        String msg = "[ERROR SINTÁCTICO] Línea " + linea + ", Columna " + col + ": " + mensaje;
        System.err.println(msg);
        huboError = true;

        logErrores.append(msg).append("\n");
    }

    public static void errorSemantico(int linea, int col, String mensaje) {
        String msg = "[ERROR SEMÁNTICO] Línea " + linea + ", Columna " + col + ": " + mensaje;
        System.err.println(msg);
        huboError = true;

        logErrores.append(msg).append("\n");
    }

    public static String getInformeErrores() {
        return logErrores.toString();
    }

    // Resetear entre compilaciones
    public static void reset() {
        huboError = false;
        logErrores.setLength(0);
    }

    public static boolean hayErrores() { return huboError; }

    public static void imprimirErrores() {
        System.err.println(logErrores.toString());
    }
}