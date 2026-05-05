package ArbolSintactico;

public class NodoSentenciaLlamada extends NodoSentencia {
    public NodoLlamada llamada;

    public NodoSentenciaLlamada(NodoLlamada llamada, int linea, int columna) {
        super(linea, columna);
        this.llamada = llamada;
    }

    @Override
    public String toString() {
        return "SentenciaLlamada";
    }
}