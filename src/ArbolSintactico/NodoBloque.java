package ArbolSintactico;
import java.util.ArrayList;
import java.util.List;

public class NodoBloque extends NodoSentencia {
    public List<NodoSentencia> sentencias;

    public NodoBloque(int linea, int columna) {
        super(linea, columna);
        this.sentencias = new ArrayList<>();
    }

    public void agregar(NodoSentencia sentencia) {
        this.sentencias.add(sentencia);
    }

    @Override
    public String toString() {
        return "Bloque{...}";
    }
}