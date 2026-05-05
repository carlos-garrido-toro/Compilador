@echo off
setlocal
REM  SCRIPT DE EJECUCION WINDOWS

REM VERIFICAMOS ARGUMENTOS
if "%~1" == "" goto error_args

SET "INPUT_FILE=%~1"

if not exist "%INPUT_FILE%" goto error_file

REM VARIABLES
SET "SRC_DIR=Compilador\src"
SET "OUT_DIR=Compilador\out\production\Compilador"
SET "LIB_DIR=lib"
SET "CARPETA_SALIDA=ArchivoCompilacion"
SET "RUNTIME=runtime.c"
SET "ASM_FILE=5_codigo_asm.s"
SET "NOMBRE_EXE=programa.exe"

REM LIMPIAMOS CARPETA_SALIDA DE EJECUCIONES ANTIGUAS
REM Si existe la carpeta, la borramos
if exist "%CARPETA_SALIDA%" rmdir /S /Q "%CARPETA_SALIDA%"
REM La volvemos a crear vacía
mkdir "%CARPETA_SALIDA%"

REM RECOMPILAMOS JAVA

javac -d "%OUT_DIR%" -sourcepath "%SRC_DIR%" -cp "%LIB_DIR%\*" "%SRC_DIR%\Main.java"

if %errorlevel% neq 0 goto error_javac

REM EJECUTAMOS EL FRONT Y BACK
java -cp "%OUT_DIR%;%LIB_DIR%\*" Main "%INPUT_FILE%"

if %errorlevel% neq 0 goto error_java
if not exist "%CARPETA_SALIDA%\%ASM_FILE%" goto error_not_generated

REM PREPARAMOS GCC

copy /Y %RUNTIME% %CARPETA_SALIDA% > nul
pushd %CARPETA_SALIDA%

REM ENSAMBLADO Y LINKADO DEL GCC

gcc -m32 -o %NOMBRE_EXE% %ASM_FILE% %RUNTIME% -Wl,--defsym=_main=main -Wl,--defsym=print_int=_print_int -Wl,--defsym=print_char=_print_char -Wl,--defsym=read_int=_read_int -Wl,--defsym=read_char=_read_char

if %errorlevel% neq 0 goto error_gcc

REM EJECUCION FINAL
echo.
echo       EJECUTANDO PROGRAMA
%NOMBRE_EXE%
echo.
echo =========================================

REM Volver y salir bien
popd
goto fin

REM ERRORES
:error_args
echo [ERROR] Debes especificar el archivo de entrada.
echo Uso: run.bat TESTS\testCalculadora.txt
goto fin

:error_file
echo [ERROR] El archivo de entrada no existe: %INPUT_FILE%
goto fin

:error_javac
echo.
echo [ERROR] Fallo la compilacion de Java (javac). Revisa tu codigo Java.
goto fin

:error_java
echo.
echo [ERROR] La ejecucion de Java fallo.
goto fin

:error_not_generated
echo.
echo [ERROR] Java termino pero no genero el archivo: %CARPETA_SALIDA%\%ASM_FILE%
goto fin

:error_gcc
echo.
echo [ERROR] Fallo al compilar con GCC.
popd
goto fin

:fin
endlocal