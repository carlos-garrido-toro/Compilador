package ArbolSintactico;
import Simbolos.TSB;
import java.util.ArrayList;
import java.util.List;

public class NodoFuncion extends NodoSentencia {
    public String nombre;
    public TSB tipoRetorno;
    public List<NodoDeclaracion> parametros;
    public NodoBloque cuerpo;

    public NodoFuncion(String nombre, TSB tipo, List<NodoDeclaracion> params, NodoBloque cuerpo, int linea, int columna) {
        super(linea, columna);
        this.nombre = nombre;
        this.tipoRetorno = tipo;
        this.parametros = params != null ? params : new ArrayList<>();
        this.cuerpo = cuerpo;
    }

    @Override
    public String toString() {
        return "Funcion: " + tipoRetorno + " " + nombre + "(params: " + parametros.size() + ")";
    }
}