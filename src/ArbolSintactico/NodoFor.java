package ArbolSintactico;

public class NodoFor extends NodoSentencia {
    public NodoSentencia inicializacion; // El "i = 0"
    public NodoExpresion condicion;      // El "i < 10"
    public NodoSentencia incremento;     // El "i = i + 1"
    public NodoSentencia cuerpo;         // Lo de dentro de las llaves

    public NodoFor(NodoSentencia inicializacion, NodoExpresion condicion, NodoSentencia incremento, NodoSentencia cuerpo, int linea, int columna) {
        super(linea, columna);
        this.inicializacion = inicializacion;
        this.condicion = condicion;
        this.incremento = incremento;
        this.cuerpo = cuerpo;
    }

    @Override
    public String toString() {
        return "For(...)";
    }
}