package ArbolSintactico;

public class NodoReturn extends NodoSentencia {
    public NodoExpresion expresion; // Puede ser null si es "return;"

    public NodoReturn(NodoExpresion expresion, int linea, int columna) {
        super(linea, columna);
        this.expresion = expresion;
    }

    @Override
    public String toString() {
        return "Return";
    }
}