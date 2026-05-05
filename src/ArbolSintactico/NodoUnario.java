package ArbolSintactico;

public class NodoUnario extends NodoExpresion {
    public enum Operador { NOT, MENOS_UNARIO } // MENOS_UNARIO para negar en vez de restar

    public Operador operador;
    public NodoExpresion expresion;

    public NodoUnario(Operador op, NodoExpresion expr, int linea, int columna) {
        super(linea, columna);
        this.operador = op;
        this.expresion = expr;
    }

    @Override
    public String toString() {
        return "Unario(" + operador + ")";
    }
}