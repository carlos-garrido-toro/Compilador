package Scanner;

import java_cup.runtime.Symbol;
import Parser.sym;
import java.io.*;
import Errores.GestorErrores;

%%

// OPCIONES Y DECLARACIONES

%class Scanner
%public
%unicode
%line
%column
%cup

// El fin del fichero devuelve EOF de CUP
%eofval{
  return new Symbol(sym.EOF);
%eofval}

// MACROS
Lletra      = [A-Za-z_]
Caracter    = "'" ([^'\\] | "\\" [ntr\\'\"]) "'"
Digit       = [0-9]
Alphanum    = {Lletra}|{Digit}
Entero      = 0 | [1-9]{Digit}*

/* Real        = ({Digit}+ "." {Digit}*) | ("." {Digit}+) */

Id          = {Lletra}({Alphanum})*
Espai       = [ \t\r\n]+

%%

// REGLAS LÉXICAS

// PALABRAS RESERVADAS
"main"      { return new Symbol(sym.MAIN, yyline, yycolumn, yytext()); }
"if"        { return new Symbol(sym.IF, yyline, yycolumn, yytext()); }
"else"      { return new Symbol(sym.ELSE, yyline, yycolumn, yytext()); }
"while"     { return new Symbol(sym.WHILE, yyline, yycolumn, yytext()); }
"for"       { return new Symbol(sym.FOR, yyline, yycolumn, yytext()); }
"switch"    { return new Symbol(sym.SWITCH, yyline, yycolumn, yytext()); }
"case"      { return new Symbol(sym.CASE, yyline, yycolumn, yytext()); }
"default"   { return new Symbol(sym.DEFAULT, yyline, yycolumn, yytext()); }
"break"     { return new Symbol(sym.BREAK, yyline, yycolumn, yytext()); }
"return"    { return new Symbol(sym.RETURN, yyline, yycolumn, yytext()); }
"leer"      { return new Symbol(sym.LEER, yyline, yycolumn, yytext()); }
"imprimir"  { return new Symbol(sym.IMPRIMIR, yyline, yycolumn, yytext()); }

// TIPOS DE DATOS
"int"       { return new Symbol(sym.T_INT, yyline, yycolumn, yytext()); }
/* "float"     { return new Symbol(sym.T_FLOAT, yyline, yycolumn, yytext()); } */ /* <-- ELIMINADO */
"char"      { return new Symbol(sym.T_CHAR, yyline, yycolumn, yytext()); }
"bool"      { return new Symbol(sym.T_BOOL, yyline, yycolumn, yytext()); }
"void"      { return new Symbol(sym.T_VOID, yyline, yycolumn, yytext()); }


// CONSTANTES LÓGICAS
"true"      { return new Symbol(sym.TRUE, yyline, yycolumn, yytext()); }
"false"     { return new Symbol(sym.FALSE, yyline, yycolumn, yytext()); }

// OPERADORES RELACIONALES
"=="        { return new Symbol(sym.IGUAL_QUE, yyline, yycolumn); }
"!="        { return new Symbol(sym.DIFERENTE, yyline, yycolumn); }
"<="        { return new Symbol(sym.MENOR_IGUAL, yyline, yycolumn); }
">="        { return new Symbol(sym.MAYOR_IGUAL, yyline, yycolumn); }
"<"         { return new Symbol(sym.MENOR, yyline, yycolumn); }
">"         { return new Symbol(sym.MAYOR, yyline, yycolumn); }

// OPERADORES LOGICOS
"&&"        { return new Symbol(sym.AND, yyline, yycolumn); }
"||"        { return new Symbol(sym.OR, yyline, yycolumn); }
"!"         { return new Symbol(sym.NOT, yyline, yycolumn); }

// OPERADORES ARITMETICOS
"+"         { return new Symbol(sym.SUMA, yyline, yycolumn); }
"-"         { return new Symbol(sym.RESTA, yyline, yycolumn); }
"*"         { return new Symbol(sym.MULT, yyline, yycolumn); }
"/"         { return new Symbol(sym.DIV, yyline, yycolumn); }
"%"         { return new Symbol(sym.MOD, yyline, yycolumn); }
"="         { return new Symbol(sym.ASSIGN, yyline, yycolumn); }

// DELIMITADORES Y SIGNOS
"("         { return new Symbol(sym.LPAREN, yyline, yycolumn); }
")"         { return new Symbol(sym.RPAREN, yyline, yycolumn); }
"{"         { return new Symbol(sym.LLAVE_IZQ, yyline, yycolumn); }
"}"         { return new Symbol(sym.LLAVE_DER, yyline, yycolumn); }
"["         { return new Symbol(sym.CORCH_IZQ, yyline, yycolumn); }
"]"         { return new Symbol(sym.CORCH_DER, yyline, yycolumn); }
";"         { return new Symbol(sym.PUNTO_COMA, yyline, yycolumn); }
","         { return new Symbol(sym.COMA, yyline, yycolumn); }
":"         { return new Symbol(sym.DOS_PUNTOS, yyline, yycolumn); }

// VALORES (LITERALES E IDS)

{Entero}    { return new Symbol(sym.NUMERO, yyline, yycolumn, Integer.parseInt(yytext())); }

/* {Real}      { return new Symbol(sym.REAL, yyline, yycolumn, Double.parseDouble(yytext())); } */ /* <-- ELIMINADO */

{Caracter}  {
    String str = yytext();
    char c;
    // Si la longitud es 4 (como '\n'), es un caracter de escape
    if (str.length() == 4 && str.charAt(1) == '\\') {
         char escape = str.charAt(2);
         switch(escape) {
             case 'n': c = '\n'; break; // Salto de linea (10)
             case 't': c = '\t'; break; // Tabulador (9)
             case 'r': c = '\r'; break;
             case '0': c = '\0'; break;
             default:  c = escape;      // Caso '\\' o '\''
         }
    } else {
         // Caso normal 'a', '1', etc. (Longitud 3)
         c = str.charAt(1);
    }
    // Devolvemos el valor ASCII como ENTERO
    return new Symbol(sym.LIT_CHAR, yyline, yycolumn, (int)c);
}

{Id}        { return new Symbol(sym.ID, yyline, yycolumn, yytext()); }


/* --- COMENTARIOS Y ESPACIOS --- */
{Espai}     { /* Ignoramos */ }
"//".* { /* Ignorar comentario de linea */ }

/* --- ERROR --- */
.           {
    // yyline empieza en 0, sumamos 1 para que el usuario vea "Linea 1"
    GestorErrores.errorLexico((yyline+1), (yycolumn+1), "Carácter ilegal: " + yytext());
}