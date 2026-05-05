package ArbolSintactico;
import Simbolos.TSB;

public class NodoDeclaracion extends NodoSentencia {
    public String identificador;
    public TSB tipo;
    public NodoExpresion inicializacion; // Puede ser null si es "int a;"

    public int tamanoArray; // 0 si es variable normal, >0 si es array

    public NodoDeclaracion(String id, TSB tipo, int linea, int columna) {
        super(linea, columna);
        this.identificador = id;
        this.tipo = tipo;
        this.inicializacion = null;

        this.tamanoArray = 0; // Por defecto escalar
    }

    @Override
    public String toString() {
        return "Decl(" + tipo + " " + identificador + ")";
    }
}
