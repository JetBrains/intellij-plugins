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
SPACES=" "+
BACKSLASHCRLF="\\"(\r|\n|\r\n)
COMMENT="#"[^\r\n]*
VARIABLE_VALUE=[^\r\n]*[^\\\r\n]
COLON=":"
SEMICOLON=";"
PIPE="|"
ASSIGN=("="|":="|"::="|"?="|"!="|"+=")

FILENAME_CHARACTER=[^:=!?#\ \r\n\t]
VARIABLE_USAGE="$("[^)]*")"
CONDITION_CHARACTER=[^#\r\n]

%state PREREQUISITES INCLUDES SOURCE DEFINE DEFINEBODY CONDITIONALS

%%

{COMMENT}              { return COMMENT; }

<YYINITIAL> {
    \t+                { yybegin(SOURCE); return TAB; }
    {EOL}              { return WHITE_SPACE; }
    {SPACES}           { return WHITE_SPACE; }
    {COLON}            { yybegin(PREREQUISITES); return COLON; }
    {ASSIGN}           { yybegin(SOURCE); return ASSIGN; }
    "include"          { yybegin(INCLUDES); return KEYWORD_INCLUDE; }
    "-include"         { yybegin(INCLUDES); return KEYWORD_INCLUDE; }
    "sinclude"         { yybegin(INCLUDES); return KEYWORD_INCLUDE; }
    "vpath"            { yybegin(INCLUDES); return KEYWORD_VPATH; }
    "define"           { yybegin(DEFINE); return KEYWORD_DEFINE; }
    "undefine"         { yybegin(INCLUDES); return KEYWORD_UNDEFINE; }
    "ifeq"             { yybegin(CONDITIONALS); return KEYWORD_IFEQ; }
    "ifneq"            { yybegin(CONDITIONALS); return KEYWORD_IFNEQ; }
    "ifdef"            { yybegin(CONDITIONALS); return KEYWORD_IFDEF; }
    "ifndef"           { yybegin(CONDITIONALS); return KEYWORD_IFNDEF; }
    "else"             { return KEYWORD_ELSE; }
    "endif"            { return KEYWORD_ENDIF; }
    "override"         { return KEYWORD_OVERRIDE; }
    "export"           { return KEYWORD_EXPORT; }
    "private"          { return KEYWORD_PRIVATE; }
    {FILENAME_CHARACTER}+   { return IDENTIFIER; }
}

<PREREQUISITES> {
    "override"              { yybegin(YYINITIAL); return KEYWORD_OVERRIDE; }
    "export"                { yybegin(YYINITIAL); return KEYWORD_EXPORT; }
    "private"               { yybegin(YYINITIAL); return KEYWORD_PRIVATE; }
    {BACKSLASHCRLF}         { return SPLIT; }
    {PIPE}                  { return PIPE; }
    {SEMICOLON}             { yybegin(SOURCE); return SEMICOLON; }
    {VARIABLE_USAGE}        { return IDENTIFIER; }
    {FILENAME_CHARACTER}+   { return IDENTIFIER; }
    {EOL}                   { yybegin(YYINITIAL); return EOL; }
    <<EOF>>                 { yypushback(yylength()); yybegin(YYINITIAL); return EOL; }
    {SPACES}|\t+            { return WHITE_SPACE; }
}

<INCLUDES> {
    {FILENAME_CHARACTER}+   { return IDENTIFIER; }
    {EOL}                   { yybegin(YYINITIAL); return EOL; }
    <<EOF>>                 { yypushback(yylength()); yybegin(YYINITIAL); return EOL; }
    {SPACES}|\t+            { return WHITE_SPACE; }
}


<SOURCE> {
    {SPACES}|\t+            { return WHITE_SPACE; }
    {BACKSLASHCRLF}         { return SPLIT; }
    {VARIABLE_VALUE}        { return LINE; }
    {EOL}                   { yybegin(YYINITIAL); return WHITE_SPACE; }
}

<DEFINE> {
    {SPACES}|\t+                { return WHITE_SPACE; }
    {EOL}                       { yybegin(DEFINEBODY); return WHITE_SPACE; }
    {ASSIGN}                    { return ASSIGN; }
    {FILENAME_CHARACTER}+       { return IDENTIFIER; }
}

<DEFINEBODY> {
    "endef"                { yybegin(YYINITIAL); return KEYWORD_ENDEF; }
    {BACKSLASHCRLF}        { return SPLIT; }
    {VARIABLE_VALUE}       { return VARIABLE_VALUE_LINE; }
    {EOL}                  { return WHITE_SPACE; }
}

<CONDITIONALS> {
    {SPACES}                   { return WHITE_SPACE; }
    {CONDITION_CHARACTER}+     { yybegin(YYINITIAL); return CONDITION; }
    {EOL}                      { yybegin(YYINITIAL); return WHITE_SPACE; }
}

[^] { return BAD_CHARACTER; }
