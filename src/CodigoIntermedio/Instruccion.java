package CodigoIntermedio;

public class Instruccion {
    public TipoInstruccion tipo;
    public String op1;
    public String op2;
    public String destino;

    public Instruccion(TipoInstruccion tipo, String op1, String op2, String destino) {
        this.tipo = tipo;
        this.op1 = op1;
        this.op2 = op2;
        this.destino = destino;
    }

    // Constructor para operaciones unarias o copias (x = -y)
    public Instruccion(TipoInstruccion tipo, String op1, String destino) {
        this(tipo, op1, null, destino);
    }

    // Constructor para saltos o etiquetas (goto L1)
    public Instruccion(TipoInstruccion tipo, String destino) {
        this(tipo, null, null, destino);
    }

    @Override
    public String toString() {
        switch (tipo) {
            case ADD: return destino + " = " + op1 + " + " + op2;
            case SUB: return destino + " = " + op1 + " - " + op2;
            case MUL: return destino + " = " + op1 + " * " + op2;
            case DIV: return destino + " = " + op1 + " / " + op2;
            case MOD: return destino + " = " + op1 + " % " + op2;

            case COPY: return destino + " = " + op1;
            case NEG:  return destino + " = -" + op1;
            case NOT:  return destino + " = not " + op1;

            case LABEL: return destino + ": skip"; // Según PDF pág 6
            case GOTO:  return "goto " + destino;

            case IF_EQ: return "if " + op1 + " == " + op2 + " goto " + destino;
            case IF_LT: return "if " + op1 + " < " + op2 + " goto " + destino;
            case IF_GT: return "if " + op1 + " > " + op2 + " goto " + destino;
            case IF_LE: return "if " + op1 + " <= " + op2 + " goto " + destino;
            case IF_GE: return "if " + op1 + " >= " + op2 + " goto " + destino;
            case IF_NE: return "if " + op1 + " != " + op2 + " goto " + destino;

            case PARAM_S: return "param_s " + destino;
            case CALL:    return "call " + op1 + ", " + op2; // op1=nombre, op2=numParams
            case PMB:     return "pmb " + destino;
            case RETURN:  return "ret " + (destino!=null ? destino : "");

            case IND_VAL: return destino + " = " + op1 + "[" + op2 + "]";
            case IND_ASS: return op1 + "[" + op2 + "] = " + destino;

            default: return tipo + " " + op1 + ", " + op2 + ", " + destino;
        }
    }
}