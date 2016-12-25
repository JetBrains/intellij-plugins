package name.kropp.intellij.makefile;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import name.kropp.intellij.makefile.psi.MakefileTypes;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static name.kropp.intellij.makefile.psi.MakefileTypes.*;

%%

%{
  public _MakefileLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class _MakefileLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

EOL=[\r\n]+
SPACES=[ \t]+
TAB=\t
COMMENT="#"[^\r\n]*
VARIABLE_VALUE=[^\r\n]
COLON=":"
SEMICOLON=";"
PIPE="|"
ASSIGN=("="|":="|"::="|"?="|"!="|"+=")

FILENAME_CHARACTER=[^:=+!?\ \r\n\t]
COMMAND=[^\r\n]+

%state PREREQUISITES INCLUDES COMMANDS VARIABLE DEFINE DEFINEBODY CONDITIONALS

%%

<YYINITIAL> {
    {COMMENT}          { return COMMENT; }
    {TAB}+             { yybegin(COMMANDS); return WHITE_SPACE; }
    {EOL}              { return WHITE_SPACE; }
    {SPACES}           { return WHITE_SPACE; }
    {COLON}            { yybegin(PREREQUISITES); return COLON; }
    {ASSIGN}           { yybegin(VARIABLE); return ASSIGN; }
    "include"          { yybegin(INCLUDES); return KEYWORD_INCLUDE; }
    "-include"         { yybegin(INCLUDES); return KEYWORD_INCLUDE; }
    "sinclude"         { yybegin(INCLUDES); return KEYWORD_INCLUDE; }
    "define"           { yybegin(DEFINE); return KEYWORD_DEFINE; }
    "undefine"         { yybegin(INCLUDES); return KEYWORD_UNDEFINE; }
    "ifeq"             { yybegin(CONDITIONALS); return KEYWORD_IFEQ; }
    "ifneq"            { yybegin(CONDITIONALS); return KEYWORD_IFNEQ; }
    "ifndef"           { yybegin(CONDITIONALS); return KEYWORD_IFNDEF; }
    "else"             { return KEYWORD_ELSE; }
    "endif"            { return KEYWORD_ENDIF; }
    "override"         { return KEYWORD_OVERRIDE; }
    "export"           { return KEYWORD_EXPORT; }
    {FILENAME_CHARACTER}+   { return IDENTIFIER; }
}

<PREREQUISITES> {
    {PIPE}                  { return PIPE; }
    {SEMICOLON}             { yybegin(COMMANDS); return SEMICOLON; }
    {FILENAME_CHARACTER}+   { return IDENTIFIER; }
    {EOL}                   { yybegin(YYINITIAL); return EOL; }
    <<EOF>>                 { yypushback(yylength()); yybegin(YYINITIAL); return EOL; }
    {SPACES}                { return WHITE_SPACE; }
}

<INCLUDES> {
    {FILENAME_CHARACTER}+   { return IDENTIFIER; }
    {EOL}                   { yybegin(YYINITIAL); return EOL; }
    <<EOF>>                 { yypushback(yylength()); yybegin(YYINITIAL); return EOL; }
    {SPACES}                { return WHITE_SPACE; }
}


<COMMANDS> {COMMAND}                   { yybegin(YYINITIAL); return COMMAND; }

<VARIABLE> {
    {VARIABLE_VALUE}+           { return VARIABLE_VALUE; }
    {EOL}                       { yybegin(YYINITIAL); return WHITE_SPACE; }
}

<DEFINE> {
    {SPACES}                    { return WHITE_SPACE; }
    {EOL}                       { yybegin(DEFINEBODY); return WHITE_SPACE; }
    {ASSIGN}                    { return ASSIGN; }
    {FILENAME_CHARACTER}+       { return IDENTIFIER; }
}

<DEFINEBODY> {
    "endef"                { yybegin(YYINITIAL); return KEYWORD_ENDEF; }
    {VARIABLE_VALUE}+      { return VARIABLE_VALUE_LINE; }
    {EOL}                  { return WHITE_SPACE; }
}

<CONDITIONALS> {
    {FILENAME_CHARACTER}+      { yybegin(YYINITIAL); return CONDITION; }
    {SPACES}                   { return WHITE_SPACE; }
    {EOL}                      { yybegin(YYINITIAL); return WHITE_SPACE; }
}

[^] { return BAD_CHARACTER; }
