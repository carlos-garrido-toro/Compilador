package ArbolSintactico;

public class NodoAccesoArray extends NodoExpresion {
    public String nombre;
    public NodoExpresion indice;

    public NodoAccesoArray(String nombre, NodoExpresion indice, int linea, int columna) {
        super(linea, columna);
        this.nombre = nombre;
        this.indice = indice;
    }

    @Override
    public String toString() {
        return "AccesoArray: " + nombre + "[...]";
    }
}