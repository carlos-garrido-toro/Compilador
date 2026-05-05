# Compilador - Asignatura de Compiladores

Implementación completa de un compilador que traduce código fuente de alto nivel a código máquina ejecutable (IA32). El proyecto abarca todas las fases del proceso de compilación: análisis léxico, sintáctico, semántico y generación de código ensamblador.

## Características

- **Front-End (Análisis)**
  - Análisis Léxico con JFlex (Generador de escáneres)
  - Análisis Sintáctico con CUP (Parser LALR(1))
  - Construcción de Árbol Sintáctico Abstracto (AST)
  - Análisis Semántico con Sistema de Tipos Subyacentes Básicos (TSB)
  - Tabla de Símbolos con gestión de ámbitos (Scopes)

- **Back-End (Síntesis)**
  - Generación de Código Intermedio (C3@ - Código de Tres Direcciones)
  - Generación de Código Ensamblador (IA32, sintaxis AT&T)
  - Integración con Runtime en C para I/O
  - Enlazado automático con GCC

- **Recuperación de Errores**
  - Panic Mode para recuperación sintáctica
  - Mensajes de error detallados en español
  - Detección múltiple de errores en una pasada

## Arquitectura

```
Código Fuente (.txt)
        ↓
    Scanner (JFlex)
        ↓
    Parser (CUP)
        ↓
    AST (Abstract Syntax Tree)
        ↓
    Análisis Semántico
        ↓
    Generador Código Intermedio (C3@)
        ↓
    Generador Ensamblador (IA32)
        ↓
    GCC Linker
        ↓
    Ejecutable
```

## Lenguaje Soportado

### Tipos de Datos
- `int` - Enteros 32-bit
- `char` - Caracteres ASCII
- `bool` - Valores lógicos (true/false)
- Arrays unidimensionales: `int arr[10];`

### Estructuras de Control
- `if/else` - Condicionales
- `while` - Bucles iterativos
- `for` - Bucles completos
- `switch/case` - Selección múltiple

### Operadores
- **Aritméticos**: `+`, `-`, `*`, `/`, `%`
- **Relacionales**: `==`, `!=`, `<`, `>`, `<=`, `>=`
- **Lógicos**: `&&`, `||`, `!`
- **Unarios**: `-`, `!`

### Entrada/Salida
```
leer(variable);      // Lee de entrada estándar
imprimir(expresion); // Imprime a salida estándar
```
## Requisitos

### Linux con Arquitectura x86 (32/64-bit)
- **Java JDK 8+** - Para compilar el compilador
  - Ubuntu/Debian: `sudo apt install openjdk-11-jdk`
- **GCC (GNU Compiler Collection)** - Para ensamblado y enlazado
  - Ubuntu/Debian: `sudo apt install gcc`

### macOS Intel (x86-64)
- **Java JDK 8+** - Para compilar el compilador
  - `brew install openjdk@11`
- **GCC (GNU Compiler Collection)** - Para ensamblado y enlazado
  - `brew install gcc`

### macOS con Procesador ARM

**Requisitos:**
- **Java JDK 8+** - Para compilar el compilador
  - `brew install openjdk@11`
- **Docker Desktop** - Para la compilación final (gcc + linking)
  - Descargar de: https://www.docker.com/products/docker-desktop
  - Tener Docker Desktop abierto/ejecutándose en background

**Por qué Docker:**
El compilador genera código IA32 (Intel 32-bit), incompatible con procesadores ARM. El script `run.sh` detecta automáticamente esto y usa Docker solo para la fase final (ensamblado y enlazado con gcc).

**Flujo automático:**
```
1. Java compila el compilador (en ARM)
2. Se genera código ensamblador .s (en ARM)
3. Docker Desktop compila con gcc (en contenedor x86 compatible)
4. Ejecutable final
```

### Windows (x86-64)
- **Java JDK 8+** - Para compilar el compilador
  - Descargar de: https://www.oracle.com/java/technologies/downloads/
- **GCC (GNU Compiler Collection)** - Para ensamblado y enlazado
  - Opción 1: MinGW - https://www.mingw-w64.org/
  - Opción 2: TDM-GCC - https://jmeubank.github.io/tdm-gcc/
  - **Importante:** Añadir carpeta `bin` de GCC al PATH del sistema

### Flag -m32 (automático)
- Los scripts `run.sh` y `run.bat` incluyen `-m32` automáticamente para asegurar compatibilidad con sistemas 64-bit

## Compilación y Ejecución

### Linux y macOS Intel (x86-64)

```bash
# Primera vez: dar permisos de ejecución
chmod +x run.sh

# Compilar y ejecutar
./run.sh nombrePrograma.txt
```

### macOS con Procesador ARM

El script lo hace automáticamente

```bash
# 1. Abre Docker Desktop (una sola vez)
# 2. En la terminal:
chmod +x run.sh
./run.sh nombrePrograma.txt
```

### Windows (x86-64)

```cmd
REM No requiere permisos especiales
.\run.bat nombrePrograma.txt
```

## Estructura del Proyecto

```
Practica-Compiladores-2025/
├── Compilador/              # Código Java del compilador
│   ├── AnalizadorSemantico.java
│   ├── GeneradorCodigo.java
│   ├── GeneradorEnsamblador.java
│   ├── GestorErrores.java
│   ├── TablaSimbolos.java
│   └── ...
├── ArbolSintactico/         # Nodos del AST
│   ├── NodoBase.java
│   ├── NodoExpresion.java
│   ├── NodoSentencia.java
│   └── ...
├── CodigoIntermedio/        # Representación intermedia
│   ├── Instruccion.java
│   ├── GeneradorCodigo.java
│   └── TipoInstruccion.java
├── Simbolos/                # Gestión de símbolos
│   ├── TablaSimbolos.java
│   ├── Descripcion.java
│   └── Categoria.java
├── TESTS/                   # Casos de prueba
│   ├── testTipos.txt
│   ├── testOperaciones.txt
│   ├── testErroresSemanticos.txt
│   └── ...
├── scanner.flex             # Especificación JFlex
├── parser.cup               # Especificación CUP
├── runtime.c                # Librería de I/O en C
├── run.sh                   # Script para Linux/macOS
├── run.bat                  # Script para Windows
└── DOCUMENTACION.pdf        # Documentación completa (38 páginas)
```

## Casos de Prueba

El proyecto incluye múltiples casos de prueba que validan:

### Casos Correctos
- **testTipos.txt** - Sistema de tipos y estructuras de datos
- **testOperaciones.txt** - Control de flujo y funciones
- **testOperadores.txt** - Expresiones complejas
- **testExpresionesAritmeticasLogicas.txt** - Evaluación de expresiones
- **testCalculadora.txt** - Programa interactivo completo

### Casos de Error
- **testErroresSemanticos.txt** - Variables no declaradas, incompatibilidad de tipos
- **testErroresMixtos.txt** - Recuperación de errores en cascada
- **testErrorCalculadora.txt** - Robustez ante errores sintácticos

Ejecutar un test:
```bash
./run.sh TESTS/testTipos.txt
```

## Documentación Detallada

Para información exhaustiva sobre:
- Diseño de la gramática
- Implementación de la Tabla de Símbolos
- Estrategias de generación de código
- Análisis semántico y sistema de tipos
- Recuperación de errores

Ver **DOCUMENTACION.pdf**

## Componentes Principales

### 1. Análisis Léxico (JFlex)
- Reconocimiento de tokens: palabras clave, identificadores, literales
- Gestión de líneas y columnas para reportes de error
- Secuencias de escape en caracteres

### 2. Análisis Sintáctico (CUP)
- Parser LALR(1) con precedencia de operadores
- Recuperación en modo pánico (Panic Mode)
- Construcción on-the-fly del AST

### 3. Análisis Semántico
- Validación de tipos estricta
- Gestión de ámbitos (Global, Local, Parámetros)
- Cálculo de direcciones de memoria (Offsets)
- Detección de errores: variables no declaradas, tipos incompatibles, etc.

### 4. Generación de Código Intermedio
- Representación en Código de Tres Direcciones (C3@)
- Variables temporales y etiquetas simbólicas
- Gestión automática de temporales

### 5. Generación de Ensamblador
- Traducción a IA32 (sintaxis AT&T)
- Gestión del Stack Frame
- Integración con runtime.c para I/O

## Optimizaciones Implementadas

- **Flattening de AST**: Eliminación de anidamiento innecesario
- **Recuperación inteligente de errores**: Prevención de cascada de falsos errores
- **Compatibilidad x64**: Flag -m32 para entornos de 64 bits
- **Limpieza automática**: Scripts que limpian archivos compilados antes de cada ejecución


## Autor

Carlos Garrido del Toro  
Ingeniería Informática - UIB (Universidad de las Islas Baleares)  
Asignatura: Compiladores - Curso 2025-2026

Proyecto académico

---
**Para más detalles, consultar DOCUMENTACION.pdf**
