package ArbolSintactico;
import java.util.ArrayList;
import java.util.List;

public class NodoLlamada extends NodoExpresion {
    public String nombre;
    public List<NodoExpresion> argumentos;

    public NodoLlamada(String nombre, List<NodoExpresion> args, int linea, int columna) {
        super(linea, columna);
        this.nombre = nombre;
        this.argumentos = args != null ? args : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Llamada: " + nombre + "()";
    }
}