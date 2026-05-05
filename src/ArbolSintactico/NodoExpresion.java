package ArbolSintactico;
import Simbolos.TSB;

public abstract class NodoExpresion extends NodoBase {
    // El tipo que resulta de esta expresión (se rellena en fase semántica)
    public TSB tipoResultado = TSB.TS_NULL;
    public TSB tipo = TSB.TS_NULL;

    // Si es un valor constante o una variable
    public boolean esConstante = false;

    public NodoExpresion(int linea, int columna) {
        super(linea, columna);
    }
}