package ArbolSintactico;


public class NodoImprimir extends NodoSentencia {
    public NodoExpresion expresion;

    public NodoImprimir(NodoExpresion expr, int linea, int columna) {
        super(linea, columna);
        this.expresion = expr;
    }

    @Override
    public String toString() {
        return "Imprimir(...)";
    }
}