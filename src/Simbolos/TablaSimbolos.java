package Simbolos;

import java.util.HashMap;

public class TablaSimbolos {

    private class Ambito {
        public HashMap<String, Descripcion> tabla; // Los símbolos de este bloque
        public Ambito anterior;                    // Puntero al bloque padre

        public Ambito(Ambito anterior) {
            this.tabla = new HashMap<>();
            this.anterior = anterior;
        }
    }

    // Puntero que señala siempre al bloque donde estamos escribiendo ahora
    private Ambito ambitoActual;

    // Mantenemos contador de nivel para saber la profundidad
    public int nivelActual = 0;

    // Constructor: Inicializa la tabla con el Ámbito Global
    public TablaSimbolos() {
        this.ambitoActual = new Ambito(null);
        this.nivelActual = 0;
    }

    // Entra en un nuevo bloque. Crea un nuevo ámbito hijo y mueve el puntero.
    public void entraBloc() {
        // El nuevo ámbito apunta al actual como "anterior"
        Ambito nuevoAmbito = new Ambito(this.ambitoActual);
        this.ambitoActual = nuevoAmbito;
        this.nivelActual++;
    }

    // Sale del bloque actual. Recupera el puntero al ámbito padre.
    public void salirBloc() {
        if (this.ambitoActual.anterior != null) {
            this.ambitoActual = this.ambitoActual.anterior;
            this.nivelActual--;
        } else {
            System.err.println("Error Compilador: Intentando cerrar el bloque Global.");
        }
    }

     // Añade un símbolo al ámbito ACTUAL. Retorna true si éxito, false si ya existe.
    public boolean poner(String id, Descripcion desc) {
        if (ambitoActual.tabla.containsKey(id)) {
            return false; // Error: Variable ya declarada en este mismo bloque
        }

        // Asignamos el nivel a la descripción antes de guardarla
        desc.nivel = this.nivelActual;

        ambitoActual.tabla.put(id, desc);
        return true;
    }

    // Busca un símbolo. Primero en el ámbito actual, luego sube a los padres.
    public Descripcion get(String id) {
        Ambito temp = this.ambitoActual;

        // Recorremos la cadena de ámbitos hacia arriba
        while (temp != null) {
            if (temp.tabla.containsKey(id)) {
                return temp.tabla.get(id);
            }
            temp = temp.anterior; // Subimos al padre
        }

        return null; // No existe en ningún ámbito visible
    }

    // Método para depuración
    public void imprimir() {
        // DEBUG
        // System.out.println("--- Depuración Tabla Símbolos (Nivel " + nivelActual + ") ---");
        // Imprime el bloque actual
        for (String key : ambitoActual.tabla.keySet()) {
            Descripcion d = ambitoActual.tabla.get(key);
            System.out.println("  " + key + " -> " + d.tsb);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ESTADO FINAL DE LA TABLA DE SÍMBOLOS ===\n");

        Ambito temp = this.ambitoActual;
        int nivelTemp = this.nivelActual;

        // Recorremos desde el ámbito actual hacia arriba (hasta el Global)
        while (temp != null) {
            sb.append("\n-----------------------------------------------------\n");
            if (nivelTemp == 0) {
                sb.append(" BLOQUE GLOBAL (Nivel 0)\n");
            } else {
                sb.append(" BLOQUE LOCAL (Nivel " + nivelTemp + ")\n");
            }
            sb.append("-----------------------------------------------------\n");
            sb.append(String.format("%-15s | %-40s\n", "IDENTIFICADOR", "DETALLES"));
            sb.append("----------------|------------------------------------\n");

            if (temp.tabla.isEmpty()) {
                sb.append(" (Bloque vacío)\n");
            } else {
                for (String id : temp.tabla.keySet()) {
                    Descripcion d = temp.tabla.get(id);
                    sb.append(String.format("%-15s | %s\n", id, formatearDescripcion(d)));
                }
            }

            // Subimos al padre
            temp = temp.anterior;
            nivelTemp--;
        }
        return sb.toString();
    }

    private String formatearDescripcion(Descripcion d) {
        if (d == null) return "null";
        StringBuilder det = new StringBuilder();
        try {
            det.append("Tipo: ").append(d.tsb);
            det.append(", Cat: ").append(d.categoria);

            if (d.nivel == 0) {
                det.append(" [Global]");
            } else {
                det.append(", Offset: ").append(d.direccion);
            }

            // Si es array, mostramos tamaño
            if (d.tamano != null && d.tamano > 0) {
                det.append(", Tamaño: ").append(d.tamano);
            }
        } catch (Exception e) {
            return "Error al leer Descripción: " + d.toString();
        }

        return det.toString();
    }
}