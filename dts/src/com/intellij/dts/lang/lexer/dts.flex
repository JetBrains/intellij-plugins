package com.intellij.dts.lang.lexer;

import java.util.Stack;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;

import com.intellij.dts.lang.psi.DtsTypes;

%%

%{
    private final Stack<Integer> stack = new Stack<>();

    private void pushState(int state) {
        stack.push(yystate());
        yybegin(state);
    }

    private void popState() {
        assert !stack.empty();
        yybegin(stack.pop());
    }

    private void resetState() {
        stack.clear();
        yybegin(YYINITIAL);
    }
%}

%{
    private int parenCount = 0;

    private void beginExpr() {
        parenCount = 0;
        pushState(WAITING_EXPR);
    }

    private void openParen() {
        parenCount++;
    }

    private void closeParen() {
        if (parenCount == 0) {
            popState();
        } else {
            parenCount--;
        }
    }
%}

%class DtsLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}

EOL               = "\n" | "\r" | "\r\n"
EOL_ESC           = "\\"{LINE_WS}*{EOL}
LINE_WS           = [\ \t]

CPP_DIRECTIVES    = "include" | "ifdef" | "ifndef" | "endif" | "define" | "undef"

// node or property names
NAME              = [a-zA-Z0-9,._+*#?@-]+
PATH              = "/" | (("/"{NAME})+"/"?)

// c like identifier for macro names
IDENTIFIER        = [a-zA-Z_][0-9a-zA-Z_]*

BYTE              = [a-fA-F0-9]{2}
INT               = ([0-9]+|0[xX][0-9a-fA-F]+)(U|L|UL|LL|ULL)?
STRING            = ([^\"]|\\.)+
CHAR              = [^']|(\\((x[0-9a-fA-F]{1,2})|([0-7][0-8]{0,2})|[^x0-7]))

COMMENT_EOL       = "//"[^\r\n]*
COMMENT_C         = "/*"([^*]|"*"[^/])*"*/"

%state WAITING_CELL WAITING_BYTE WAITING_VALUE WAITING_BITS WAITING_EXPR WAITING_STRING WAITING_CHAR WAITING_HANDLE

%%

{COMMENT_C}                                         { return DtsTypes.COMMENT_C; }
{COMMENT_EOL}                                       { return DtsTypes.COMMENT_EOL; }

// c prerpocssor statments
"#"{LINE_WS}*{CPP_DIRECTIVES} (.+ | {EOL_ESC})*     { return DtsTypes.PP_STATEMENT; }

// dts include preprocesser statment
"/include/"{LINE_WS}*"\""{STRING}"\""               { return DtsTypes.PP_STATEMENT; }

<YYINITIAL> {
    "/dts-v1/"                                      { return DtsTypes.V1; }
    "/plugin/"                                      { return DtsTypes.PLUGIN; }
    "/memreserve/"                                  { pushState(WAITING_CELL); return DtsTypes.MEMRESERVE; }
    "/delete-node/"                                 { return DtsTypes.DELETE_NODE; }
    "/delete-property/"                             { return DtsTypes.DELETE_PROP; }
    "/omit-if-no-ref/"                              { return DtsTypes.OMIT_NODE; }

    ";"                                             { return DtsTypes.SEMICOLON; }

    "="                                             { pushState(WAITING_VALUE); return DtsTypes.ASSIGN; }

    {NAME}                                          { return DtsTypes.NAME; }
}

<YYINITIAL, WAITING_VALUE> {
    "/bits/"                                        { pushState(WAITING_BITS); return DtsTypes.BITS; }

    ","                                             { return DtsTypes.COMMA; }
    "/"                                             { return DtsTypes.SLASH; }
    "&"                                             { pushState(WAITING_HANDLE); return DtsTypes.HANDLE; }
    "{"                                             { return DtsTypes.LBRACE; }
    "}"                                             { return DtsTypes.RBRACE; }
    "("                                             { return DtsTypes.LPAREN; }
    ")"                                             { return DtsTypes.RPAREN; }
    "["                                             { pushState(WAITING_BYTE); return DtsTypes.LBRAC; }
    "]"                                             { return DtsTypes.RBRAC; }
    "<"                                             { pushState(WAITING_CELL); return DtsTypes.LANGL; }
    ">"                                             { return DtsTypes.RANGL; }

    "\""                                            { pushState(WAITING_STRING); return DtsTypes.DQUOTE; }

    {NAME}":"                                       { return DtsTypes.LABEL; }
    {IDENTIFIER}                                    { return DtsTypes.NAME; }
}

<WAITING_STRING> {
    {STRING}                                        { return DtsTypes.STRING_VALUE; }
    "\""                                            { popState(); return DtsTypes.DQUOTE; }
}

<WAITING_CHAR> {
    {CHAR}                                          { return DtsTypes.CHAR_VALUE; }
    "'"                                             { popState(); return DtsTypes.SQUOTE; }
}

<WAITING_BITS> {
    {INT}                                           { popState(); return DtsTypes.INT; }
}

<WAITING_HANDLE> {
    "{"                                             { return DtsTypes.LBRACE; }
    "}"                                             { popState(); return DtsTypes.RBRACE; }

    {IDENTIFIER}                                    { popState(); return DtsTypes.NAME; }
    {PATH}                                          { return DtsTypes.PATH; }

    [^]                                             { popState(); yypushback(1); }
}

<WAITING_CELL> {
    "("                                             { beginExpr(); return DtsTypes.LPAREN; }
    ">"                                             { popState(); return DtsTypes.RANGL; }
    "'"                                             { pushState(WAITING_CHAR); return DtsTypes.SQUOTE; }

    "&"                                             { pushState(WAITING_HANDLE); return DtsTypes.HANDLE; }

    {INT}                                           { return DtsTypes.INT; }
    {NAME}":"                                       { return DtsTypes.LABEL; }
    {IDENTIFIER}                                    { return DtsTypes.NAME; }
}

<WAITING_BYTE> {
    "]"                                             { popState(); return DtsTypes.RBRAC; }

    {BYTE}                                          { return DtsTypes.BYTE; }
    {NAME}":"                                       { return DtsTypes.LABEL; }
}

<WAITING_EXPR> {
    "+"                                             { return DtsTypes.ADD; }
    "-"                                             { return DtsTypes.SUB; }
    "*"                                             { return DtsTypes.MUL; }
    "/"                                             { return DtsTypes.DIV; }
    "%"                                             { return DtsTypes.MOD; }

    "&"                                             { return DtsTypes.AND; }
    "|"                                             { return DtsTypes.OR; }
    "^"                                             { return DtsTypes.XOR; }
    "~"                                             { return DtsTypes.NOT; }
    "<<"                                            { return DtsTypes.LSH; }
    ">>"                                            { return DtsTypes.RSH; }

    "&&"                                            { return DtsTypes.L_AND; }
    "||"                                            { return DtsTypes.L_OR; }
    "!"                                             { return DtsTypes.L_NOT; }

    "<"                                             { return DtsTypes.LES; }
    ">"                                             { return DtsTypes.GRT; }
    "<="                                            { return DtsTypes.LEQ; }
    ">="                                            { return DtsTypes.GEQ; }
    "=="                                            { return DtsTypes.EQ; }
    "!="                                            { return DtsTypes.NEQ; }

    ":"                                             { return DtsTypes.COLON; }
    "?"                                             { return DtsTypes.TERNARY; }

    ","                                             { return DtsTypes.COMMA; }

    {INT}                                           { return DtsTypes.INT; }
    {IDENTIFIER}                                    { return DtsTypes.NAME; }

    "'"                                             { pushState(WAITING_CHAR); return DtsTypes.SQUOTE; }
    "("                                             { openParen(); return DtsTypes.LPAREN; }
    ")"                                             { closeParen(); return DtsTypes.RPAREN; }
}

({LINE_WS} | {EOL})+                                { return TokenType.WHITE_SPACE; }

[^] {
    if (yystate() != YYINITIAL) {
      resetState();
      yypushback(1);
    } else {
      return TokenType.BAD_CHARACTER;
    }
}