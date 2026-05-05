#include <stdio.h>
#include <stdlib.h>

// IMPRIMIR

void print_int(int n) {
    printf("%d", n);
    fflush(stdout); // Forza a mostrarlo en pantalla inmediatamente
}

void print_char(int c) {
    printf("%c", c);
    fflush(stdout);
}

// LECTURA

// Buffer auxiliar para leer texto del teclado
char buffer[256];

int read_int() {


    // Lee una linea entera hasta que pulses ENTER
    if (fgets(buffer, 256, stdin) == NULL) {
        return 0; // Error o fin de fichero
    }

    // Convierte el texto a numero
    return atoi(buffer);
}

int read_char() {
    //  Lee una linea entera hasta que pulses ENTER
    if (fgets(buffer, 256, stdin) == NULL) {
        return 0;
    }

    // Devuelve el primer caracter escrito
    // buffer[0] es la letra, buffer[1] seria el salto de linea
    return (int)buffer[0];
}