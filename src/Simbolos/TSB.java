package Simbolos;

public enum TSB {
    TS_ENTER,   // int
    //TS_REAL,    // float (o real)
    TS_CHAR,    // char
    TS_BOOL,    // bool
    TS_VOID,    // void (para procedimientos)
    TS_NULL,    // Error o tipo no deducido
    TS_ARRAY,    // Para arrays
    TS_ERROR    // Para propagar errores
}