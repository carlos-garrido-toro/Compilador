package ArbolSintactico;

public class NodoOperacion extends NodoExpresion {

    public enum TipoOperador {
        SUMA, RESTA, MULT, DIV, MOD,
        AND, OR,
        IGUAL, DIFERENTE, MAYOR, MENOR, MAYOR_IGUAL, MENOR_IGUAL
    }

    public TipoOperador operacion;
    public NodoExpresion izquierda;
    public NodoExpresion derecha;

    public NodoOperacion(TipoOperador op, NodoExpresion izq, NodoExpresion der, int linea, int columna) {
        super(linea, columna);
        this.operacion = op;
        this.izquierda = izq;
        this.derecha = der;
    }

    @Override
    public String toString() {
        return "Op(" + operacion + ")";
    }
}