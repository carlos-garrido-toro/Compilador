package ArbolSintactico;

public class NodoIdentificador extends NodoExpresion {
    public String nombre;

    public NodoIdentificador(String nombre, int linea, int columna) {
        super(linea, columna);
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return "Id(" + nombre + ")";
    }
}
