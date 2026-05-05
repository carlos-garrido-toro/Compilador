package ArbolSintactico;

// Para los case del switch
public class NodoCaso extends NodoBase {
    public NodoExpresion valor; // El número o char (null si es DEFAULT)
    public NodoBloque bloque;   // Las sentencias dentro del case

    public NodoCaso(NodoExpresion valor, NodoBloque bloque, int linea, int columna) {
        super(linea, columna);
        this.valor = valor;
        this.bloque = bloque;
    }

    @Override
    public String toString() {
        return "Case(" + (valor == null ? "Default" : valor.toString()) + ")";
    }
}
