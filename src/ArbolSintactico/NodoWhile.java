package ArbolSintactico;

public class NodoWhile extends NodoSentencia {
    public NodoExpresion condicion;
    public NodoSentencia cuerpo;

    public NodoWhile(NodoExpresion cond, NodoSentencia cuerpo, int linea, int columna) {
        super(linea, columna);
        this.condicion = cond;
        this.cuerpo = cuerpo;
    }

    @Override
    public String toString() {
        return "While(...)";
    }
}