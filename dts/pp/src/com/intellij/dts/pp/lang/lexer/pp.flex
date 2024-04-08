package com.intellij.dts.pp.lang.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;

import com.intellij.dts.pp.lang.PpTokenTypes;

%%

%{
    private final PpTokenTypes tokenTypes;
%}

%init{
    this.tokenTypes = tokenTypes;
%init}

%class PpLexer
%implements FlexLexer
%ctorarg PpTokenTypes tokenTypes
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}

EOL               = "\n" | "\r" | "\r\n"
EOL_ESC           = "\\"{LINE_WS}*{EOL}
LINE_WS           = [\ \t]
SYMBOL            = [a-zA-Z_][a-zA-Z_0-9]+

PATH_CHAR         = [^>\"\n] | {EOL_ESC}
INCLUDE_PATH      = ("<"{PATH_CHAR}*">"?) | (\"{PATH_CHAR}*\"?)

%state WAITING_SYMBOL WAITING_DEFINE_SYMBOL WAITING_DEFINE_VALUE WAITING_INCLUDE WAITING_EXPRESSION

%%

<YYINITIAL> {
    "#"{LINE_WS}*"include"  { yybegin(WAITING_INCLUDE); return tokenTypes.getIncludeDirective(); }
    "#"{LINE_WS}*"ifdef"    { yybegin(WAITING_SYMBOL); return tokenTypes.getIfdefDirective(); }
    "#"{LINE_WS}*"if"       { yybegin(WAITING_EXPRESSION); return tokenTypes.getIfDirective(); }
    "#"{LINE_WS}*"ifndef"   { yybegin(WAITING_SYMBOL); return tokenTypes.getIfndefDirective(); }
    "#"{LINE_WS}*"elif"     { yybegin(WAITING_EXPRESSION); return tokenTypes.getElifDirective(); }
    "#"{LINE_WS}*"elifdef"  { yybegin(WAITING_SYMBOL); return tokenTypes.getElifdefDirective(); }
    "#"{LINE_WS}*"elifndef" { yybegin(WAITING_SYMBOL); return tokenTypes.getElifndefDirective(); }
    "#"{LINE_WS}*"else"     { return tokenTypes.getElseDirective(); }
    "#"{LINE_WS}*"endif"    { return tokenTypes.getEndifDirective(); }
    "#"{LINE_WS}*"define"   { yybegin(WAITING_DEFINE_SYMBOL); return tokenTypes.getDefineDirective(); }
    "#"{LINE_WS}*"undef"    { yybegin(WAITING_SYMBOL); return tokenTypes.getUndefDirective(); }
}

<WAITING_SYMBOL> {
    {SYMBOL}                { return tokenTypes.getSymbol(); }
}

<WAITING_DEFINE_SYMBOL> {
    {SYMBOL}                { yybegin(WAITING_DEFINE_VALUE); return tokenTypes.getSymbol(); }
}

<WAITING_INCLUDE> {
    {INCLUDE_PATH}          { return tokenTypes.getIncludePath(); }
}

<WAITING_DEFINE_VALUE> {
    [^]+                    { return tokenTypes.getDefineValue(); }
}

<WAITING_EXPRESSION> {
    [^]+                    { return tokenTypes.getExpression(); }
}

({LINE_WS} | {EOL_ESC})+    { return TokenType.WHITE_SPACE; }

[^]                         { return TokenType.BAD_CHARACTER; }
