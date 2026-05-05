package CodigoIntermedio;

import java.util.ArrayList;
import java.util.List;

public class GeneradorC3A {
    public List<Instruccion> instrucciones;
    private int contadorTemp = 0;
    private int contadorEtiquetas = 0;

    public GeneradorC3A() {
        this.instrucciones = new ArrayList<>();
    }

    // Generación de Nombres
    public String nuevaTemporal() {
        return "t" + (contadorTemp++);
    }

    public String nuevaEtiqueta() {
        return "L" + (contadorEtiquetas++);
    }

    // Agregar Instrucciones
    public void agregar(Instruccion inst) {
        instrucciones.add(inst);
    }

    // Diferentes 'metodos' para facilitar el uso y no tener que estar poniendo null
    public void agregar(TipoInstruccion tipo, String op1, String op2, String destino) {
        instrucciones.add(new Instruccion(tipo, op1, op2, destino));
    }

    public void agregar(TipoInstruccion tipo, String op1, String destino) {
        instrucciones.add(new Instruccion(tipo, op1, destino));
    }

    public void agregar(TipoInstruccion tipo, String destino) {
        instrucciones.add(new Instruccion(tipo, destino));
    }

    public void imprimir() {
        // DEBUG
        // System.out.println("\n=== CÓDIGO INTERMEDIO (C3@) ===");
        for (Instruccion i : instrucciones) {
            if (i.tipo != TipoInstruccion.LABEL && i.tipo != TipoInstruccion.PMB) {
                System.out.print("    ");
            }
            System.out.println(i);
        }
        // DEBUG
        // System.out.println("\n=== FIN DE GENERACIÓN CÓDIGO INTERMEDIO (C3@) === \n");
    }

    public List<Instruccion> getInstrucciones() {
        return this.instrucciones;
    }

    public int getContadorTemporales() {
        return this.contadorTemp;
    }

    public int getContadorEtiquetas() {
        return this.contadorEtiquetas;
    }

}