package ArbolSintactico;

public abstract class NodoBase {
    // Para marcar errores necesitamos saber dónde estaba este nodo
    public int linea;
    public int columna;

    public NodoBase(int linea, int columna) {
        this.linea = linea;
        this.columna = columna;
    }

    // Método que usaremos para debug (imprimir el árbol)
    public abstract String toString();
}