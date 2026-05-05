package ArbolSintactico;

public class NodoLeer extends NodoSentencia {
    public NodoExpresion objetivo; // Dónde guardamos lo leído (Variable o Array)

    public NodoLeer(NodoExpresion objetivo, int linea, int columna) {
        super(linea, columna);
        this.objetivo = objetivo;
    }

    @Override
    public String toString() {
        return "Leer";
    }
}