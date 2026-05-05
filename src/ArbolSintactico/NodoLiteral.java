package ArbolSintactico;
import Simbolos.TSB;

public class NodoLiteral extends NodoExpresion {
    public Object valor;
    public TSB tipo;

    public NodoLiteral(Object valor, TSB tipo, int linea, int columna) {
        super(linea, columna);
        this.valor = valor;
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return "Lit(" + valor + ":" + tipo + ")";
    }
}