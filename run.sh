#!/bin/bash

#  VERIFICAMOS LOS ARGUMENTOS
if [ -z "$1" ]; then
    echo "Uso: ./run.sh <archivo_fuente>"
    echo "--> Por ejemplo: ./run.sh TESTS/testCalculadora.txt"
    exit 1
fi

INPUT_FILE="$1"

# Verificamos que el archivo de entrada existe
if [ ! -f "$INPUT_FILE" ]; then
    echo "Error: El archivo de entrada '$INPUT_FILE' no existe."
    exit 1
fi

# DEFINIMOS LAS RUTAS

SRC_DIR="src"
OUT_DIR="out/production/Compilador"
LIB_DIR="lib"
OUTPUT_FILES="ArchivoCompilacion"
ASM_FILE="5_codigo_asm.s"
RUNTIME="runtime.c"

# Limpiamos el archivo ensamblador anterior
rm -rf "$OUTPUT_FILES"
mkdir -p "$OUTPUT_FILES"

# COMPILAMOS EL PROYECTO
# Compilamos Main.java y sus dependencias.
# -d: dónde guardar los .class
# -sourcepath: dónde buscar los .java
# -cp: librerías (CUP/JFlex)
javac -d "$OUT_DIR" \
      -sourcepath "$SRC_DIR" \
      -cp "$LIB_DIR/*" \
      "$SRC_DIR/Main.java"

# Si javac falla
if [ $? -ne 0 ]; then
    echo "Error de compilación en Java. Debe corregir el código."
    exit 1
fi

# EJECUCIÓN DEL FRONTEND/BACKEND
# Ejecutamos pasando el archivo de entrada
java -cp "$OUT_DIR:$LIB_DIR/*" Main "$INPUT_FILE"

# Si Java falla
if [ $? -ne 0 ]; then
    echo "Error durante la ejecución del compilador."
    exit 1
fi

# Verificamos que se haya generado el ensamblador
if [ ! -f "$OUTPUT_FILES/$ASM_FILE" ]; then
    echo "Error: El compilador finalizó pero no generó '$OUTPUT_FILES/$ASM_FILE'."
    exit 1
fi

# GENERACIÓN DEL EJECUTABLE
# Copiamos el runtime a la carpeta de salida para que Docker lo vea
cp "$RUNTIME" "$OUTPUT_FILES/"

# Ejecutamos Docker montando la carpeta ArchivoCompilacion
# $PWD para la ruta absoluta del volumen
docker run --rm -it \
    --platform linux/amd64 \
    -v "$PWD/$OUTPUT_FILES":/app \
    mi-runner-x86 \
    sh -c "gcc -m32 -o programa $ASM_FILE runtime.c -no-pie && echo 'Ejecutando:' && ./programa"
