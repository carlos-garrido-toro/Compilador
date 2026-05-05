package ArbolSintactico;

public class NodoIf extends NodoSentencia {
    public NodoExpresion condicion;
    public NodoBloque bloqueIf;
    public NodoBloque bloqueElse;

    public NodoIf(NodoExpresion condicion, NodoBloque bloqueIf, NodoBloque bloqueElse, int linea, int columna) {
        super(linea, columna);
        this.condicion = condicion;
        this.bloqueIf = bloqueIf;
        this.bloqueElse = bloqueElse;
    }

    @Override
    public String toString() {
        return "If";
    }
}