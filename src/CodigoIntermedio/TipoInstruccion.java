package CodigoIntermedio;

public enum TipoInstruccion {
    // Aritméticas y Lógicas
    ADD, SUB, MUL, DIV, MOD, // +, -, *, /, %
    AND, OR, NOT,            // &&, ||, !
    NEG,                     // - unario (a = -b)

    // Copia y Asignación
    COPY,           // a = b

    // Saltos y Etiquetas
    LABEL,          // L1:
    GOTO,           // goto L1
    IF_EQ, IF_NE,   // ==, !=
    IF_LT, IF_GT,   // <, >
    IF_LE, IF_GE,   // <=, >=

    // Subprogramas
    PARAM_S,        // param_s (simple/scalar)
    CALL,           // call p, num_params
    PMB,            // pmb (preámbulo de función)
    RETURN,         // ret

    // Arrays
    IND_VAL,        // t1 = a[i] (Lectura indirecta)
    IND_ASS,        // a[i] = t1 (Escritura indirecta)

    // I/O
    PRINT, PRINT_CHAR, READ, READ_CHAR
}