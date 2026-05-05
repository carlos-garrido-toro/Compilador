package ArbolSintactico;
import java.util.List;

public class NodoSwitch extends NodoSentencia {
    public NodoExpresion discriminante; // La variable (ID) del switch
    public List<NodoCaso> casos;

    public NodoSwitch(NodoExpresion discriminante, List<NodoCaso> casos, int linea, int columna) {
        super(linea, columna);
        this.discriminante = discriminante;
        this.casos = casos;
    }

    @Override
    public String toString() {
        return "Switch(...)";
    }
}