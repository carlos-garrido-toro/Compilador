package Simbolos;

import java.util.ArrayList;
import java.util.List;

public class Descripcion {

    // Categorías posibles de un identificador
    public enum Categoria {
        DVAR,   // Variable
        DPROC,  // Procedimiento o Función
        DARG,   // Argumento/Parámetro
        DTIPO   // Tipo definido
    }

    public Categoria categoria;
    public TSB tsb;        // Tipo de dato (INT, REAL...)
    public int nivel;      // Nivel de profundidad (0=Global, 1=Main...)
    public int direccion;  // Dirección de memoria relativa (Offset)

    public Integer tamano; // Para ARRAYS: número de elementos (ej: 10)

    public int ocupVL = 0; // Ocupación total de variables locales (bytes)

    // Para funciones: Lista de tipos de los parámetros (validar llamadas)
    public List<TSB> listaTiposArgs;

    public Descripcion(Categoria categoria, TSB tsb) {
        this.categoria = categoria;
        this.tsb = tsb;
        this.tamano = 0; // Por defecto 0 (escalar)
        this.listaTiposArgs = new ArrayList<>();

        this.ocupVL = 0;
    }
}