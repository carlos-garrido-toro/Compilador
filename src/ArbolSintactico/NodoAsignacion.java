package ArbolSintactico;

public class NodoAsignacion extends NodoSentencia {
    public String identificador;
    public NodoExpresion expresion;

    public NodoAsignacion(String id, NodoExpresion expr, int linea, int columna) {
        super(linea, columna);
        this.identificador = id;
        this.expresion = expr;
    }

    @Override
    public String toString() {
        return identificador + " = " + expresion.toString();
    }
}