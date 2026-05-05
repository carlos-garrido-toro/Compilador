package Ensamblador;

import CodigoIntermedio.*;
import Simbolos.*;
import Semantico.AnalizadorSemantico;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneradorEnsamblador {

    private List<Instruccion> instrucciones;
    private TablaSimbolos ts;
    private PrintWriter out;
    private Map<String, Integer> offsetsTemporales;
    private int currentOffsetLocal = 0;

    private String funcionActual = "";

    public GeneradorEnsamblador(List<Instruccion> instrucciones, TablaSimbolos ts, String ficheroSalida) {
        this.instrucciones = instrucciones;
        this.ts = ts;
        this.offsetsTemporales = new HashMap<>();
        try {
            this.out = new PrintWriter(new FileWriter(ficheroSalida));
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void generar() {
        escribirCabecera();

        for (Instruccion inst : instrucciones) {
            out.println("    # " + inst.toString());
            tratarInstruccion(inst);
        }

        out.println("fin_main:");
        out.println("    movl $0, %ebx");
        out.println("    movl $1, %eax");
        out.println("    int $0x80");

        out.close();
        // DEBUG
        // System.out.println("--- Ensamblador Generado Correctamente ---");
    }

    private void escribirCabecera() {
        out.println(".section .data");

        // GENERACIÓN DINÁMICA DE GLOBALES
        // Leemos las globales que detectó el Semántico
        for (Map.Entry<String, Integer> entry : AnalizadorSemantico.globalesDeclaradas.entrySet()) {
            String nombre = entry.getKey();
            int tamanoBytes = entry.getValue();

            if (tamanoBytes > 4) {
                // Es un array o estructura grande
                out.println(nombre + ": .space " + tamanoBytes);
            } else {
                // Es un entero, char o bool simple
                out.println(nombre + ": .long 0");
            }
        }

        out.println("");
        out.println(".section .text");
        out.println(".globl main");
        out.println("");
    }

    private void tratarInstruccion(Instruccion inst) {
        switch (inst.tipo) {
            case LABEL:
                out.println(inst.destino + ":");
                break;

            case PMB:
                funcionActual = inst.destino;
                out.println("    " + inst.destino + ":");
                out.println("    pushl %ebp");
                out.println("    movl %esp, %ebp");
                out.println("    subl $256, %esp");
                currentOffsetLocal = 0;
                offsetsTemporales.clear();
                break;

            case RETURN:
                if (inst.op1 != null) {
                    out.println("    movl " + getOperando(inst.op1) + ", %eax");
                } else if (inst.destino != null) {
                    out.println("    movl " + getOperando(inst.destino) + ", %eax");
                }
                out.println("    leave");
                out.println("    ret");
                break;

            case CALL:
                out.println("    call " + inst.op1);
                try {
                    int n = Integer.parseInt(inst.op2);
                    if (n > 0) out.println("    addl $" + (n*4) + ", %esp");
                } catch(Exception e){}
                if (inst.destino != null) out.println("    movl %eax, " + getOperando(inst.destino));
                break;

            case PARAM_S:
                String p = (inst.op1 != null) ? inst.op1 : inst.destino;
                out.println("    pushl " + getOperando(p));
                break;

            case COPY:
                out.println("    movl " + getOperando(inst.op1) + ", %eax");
                out.println("    movl %eax, " + getOperando(inst.destino));
                break;
            case ADD:
                out.println("    movl " + getOperando(inst.op1) + ", %eax");
                out.println("    addl " + getOperando(inst.op2) + ", %eax");
                out.println("    movl %eax, " + getOperando(inst.destino));
                break;
            case SUB:
                out.println("    movl " + getOperando(inst.op1) + ", %eax");
                out.println("    subl " + getOperando(inst.op2) + ", %eax");
                out.println("    movl %eax, " + getOperando(inst.destino));
                break;
            case MUL:
                out.println("    movl " + getOperando(inst.op1) + ", %eax");
                out.println("    imull " + getOperando(inst.op2) + ", %eax");
                out.println("    movl %eax, " + getOperando(inst.destino));
                break;
            case DIV:
                out.println("    movl " + getOperando(inst.op1) + ", %eax");
                out.println("    cltd");
                out.println("    idivl " + getOperando(inst.op2));
                out.println("    movl %eax, " + getOperando(inst.destino));
                break;
            case MOD:
                out.println("    movl " + getOperando(inst.op1) + ", %eax");
                out.println("    cltd");
                out.println("    idivl " + getOperando(inst.op2));
                out.println("    movl %edx, " + getOperando(inst.destino));
                break;
            case AND:
                out.println("    movl " + getOperando(inst.op1) + ", %eax");
                out.println("    andl " + getOperando(inst.op2) + ", %eax");
                out.println("    movl %eax, " + getOperando(inst.destino));
                break;
            case OR:
                out.println("    movl " + getOperando(inst.op1) + ", %eax");
                out.println("    orl " + getOperando(inst.op2) + ", %eax");
                out.println("    movl %eax, " + getOperando(inst.destino));
                break;
            case NOT:
                out.println("    movl " + getOperando(inst.op1) + ", %eax");
                out.println("    cmpl $0, %eax");
                out.println("    movl $0, %eax");
                out.println("    sete %al");
                out.println("    movl %eax, " + getOperando(inst.destino));
                break;

            case GOTO: out.println("    jmp " + inst.destino); break;

            case IF_LT:
                out.println("    movl " + getOperando(inst.op1) + ", %eax");
                out.println("    cmpl " + getOperando(inst.op2) + ", %eax");
                out.println("    jl " + inst.destino);
                break;
            case IF_GT:
                out.println("    movl " + getOperando(inst.op1) + ", %eax");
                out.println("    cmpl " + getOperando(inst.op2) + ", %eax");
                out.println("    jg " + inst.destino);
                break;
            case IF_EQ:
                out.println("    movl " + getOperando(inst.op1) + ", %eax");
                out.println("    cmpl " + getOperando(inst.op2) + ", %eax");
                out.println("    je " + inst.destino);
                break;
            case IF_NE:
                out.println("    movl " + getOperando(inst.op1) + ", %eax");
                out.println("    cmpl " + getOperando(inst.op2) + ", %eax");
                out.println("    jne " + inst.destino);
                break;
            case IF_LE:
                out.println("    movl " + getOperando(inst.op1) + ", %eax");
                out.println("    cmpl " + getOperando(inst.op2) + ", %eax");
                out.println("    jle " + inst.destino);
                break;
            case IF_GE:
                out.println("    movl " + getOperando(inst.op1) + ", %eax");
                out.println("    cmpl " + getOperando(inst.op2) + ", %eax");
                out.println("    jge " + inst.destino);
                break;

            case IND_VAL:
                out.println("    movl " + getOperando(inst.op2) + ", %edi");
                String baseVal = getOperando(inst.op1);
                if (baseVal.contains("%ebp")) out.println("    leal " + baseVal + ", %edx");
                else out.println("    movl $" + baseVal + ", %edx");
                out.println("    movl (%edx, %edi, 4), %eax");
                out.println("    movl %eax, " + getOperando(inst.destino));
                break;

            case IND_ASS:
                out.println("    movl " + getOperando(inst.op2) + ", %edi");
                out.println("    movl " + getOperando(inst.op1) + ", %eax");
                String baseAss = getOperando(inst.destino);
                if (baseAss.contains("%ebp")) out.println("    leal " + baseAss + ", %edx");
                else out.println("    movl $" + baseAss + ", %edx");
                out.println("    movl %eax, (%edx, %edi, 4)");
                break;

            case PRINT:
                String v = (inst.destino != null) ? inst.destino : inst.op1;
                out.println("    pushl " + getOperando(v));
                out.println("    call print_int");
                out.println("    addl $4, %esp");
                break;
            case PRINT_CHAR:
                String c = (inst.destino != null) ? inst.destino : inst.op1;
                out.println("    pushl " + getOperando(c));
                out.println("    call print_char");
                out.println("    addl $4, %esp");
                break;
            case READ:
                out.println("    call read_int");
                out.println("    movl %eax, " + getOperando(inst.destino));
                break;
            case READ_CHAR:
                out.println("    call read_char");
                out.println("    movl %eax, " + getOperando(inst.destino));
                break;
        }
    }

    private String getOperando(String op) {
        if (op == null) return "$0";
        if (op.matches("-?\\d+")) return "$" + op;

        // ARGUMENTOS
        String keyArg = funcionActual + "_" + op;
        if (AnalizadorSemantico.argumentosOffset.containsKey(keyArg)) {
            return AnalizadorSemantico.argumentosOffset.get(keyArg) + "(%ebp)";
        }

        // GLOBALES
        // Comprobamos si está en el mapa de globales que rellenó el Semántico
        if (AnalizadorSemantico.globalesDeclaradas.containsKey(op)) {
            return op; // Devuelve el nombre tal cual para la sección .data
        }

        // VARIABLES LOCALES / TEMPORALES
        if (!offsetsTemporales.containsKey(op)) {
            currentOffsetLocal += 4;
            offsetsTemporales.put(op,- currentOffsetLocal);
        }
        return offsetsTemporales.get(op) + "(%ebp)";
    }
}