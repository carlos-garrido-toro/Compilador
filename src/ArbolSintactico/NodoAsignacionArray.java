package ArbolSintactico;

public class NodoAsignacionArray extends NodoSentencia {
    public String nombre;
    public NodoExpresion indice;
    public NodoExpresion valor;

    public NodoAsignacionArray(String nombre, NodoExpresion indice, NodoExpresion valor, int linea, int columna) {
        super(linea, columna);
        this.nombre = nombre;
        this.indice = indice;
        this.valor = valor;
    }

    @Override
    public String toString() {
        return "AsigArray: " + nombre + "[...] = ...";
    }
}