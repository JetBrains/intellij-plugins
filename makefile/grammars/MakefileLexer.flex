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
BACKSLASHCRLF="\\"(\r|\n|\r\n)(" "|\t)*
DOCCOMMENT="##"[^\r\n]*
COMMENT="#"[^\r\n]*
MULTILINECOMMENT="#"[^\r\n\\]*("\\"\r?\n[^\r\n\\]*)+
FUNCTIONS=("error"|"warning"|"info"|"shell"|"subst"|"pathsubst"|"strip"|"findstring"|
  "filter"|"filter-out"|"sort"|"word"|"wordlist"|"words"|"firstword"|"lastword"|"dir"|"notdir"|"suffix"|
  "basename"|"addsuffix"|"addprefix"|"join"|"wildcard"|"realpath"|"abspath"|"if"|"or"|"and"|
  "foreach"|"file"|"call"|"value"|"eval"|"origin"|"flavor"|"guile")
MACRO="@"[^@ \r\n]+"@"
VARIABLE_VALUE=[^\r\n)#]*[^\\\r\n)#]
SOURCE_CODE=[^\r\n#]*[^\\\r\n#]
COLON=":"
DOUBLECOLON="::"
SEMICOLON=";"
COMMA=","
PIPE="|"
ASSIGN=("="|":="|"::="|"?="|"!="|"+=")

VARIABLE_USAGE_EXPR="$("[^ $)]*")"
VARIABLE_USAGE_CURLY_EXPR="${"[^ $}]*"}"
STRING="\""[^\"]*"\""
FILENAME=[^:=!?#$\",()\ \r\n\t]+({VARIABLE_USAGE_EXPR}|[^:=!?#)\ \r\n\t])*
CONDITION_CHARACTER=[^#\r\n]

%state INCLUDES SOURCE SOURCE_FORCED DEFINE DEFINEBODY CONDITIONALS FUNCTION EXPORT EXPORTVAR

%%

{DOCCOMMENT}           { return DOC_COMMENT; }
{MULTILINECOMMENT}     { return COMMENT; }
{COMMENT}              { return COMMENT; }
^{MACRO}               { return MACRO; }

<FUNCTION> {
  {FUNCTIONS}   { return FUNCTION_NAME; }
  ")"           { yybegin(YYINITIAL); return FUNCTION_END; }
  [^$)]*        { return FUNCTION_PARAM_TEXT; }
  {VARIABLE_USAGE_EXPR} { return VARIABLE_USAGE; }
  {EOL}         { yybegin(YYINITIAL); return EOL; }
}

<YYINITIAL> {
    ^\t+               { yybegin(SOURCE); return TAB; }
    \t+                { return WHITE_SPACE; }
    {EOL}              { return EOL; }
    {SPACES}           { return WHITE_SPACE; }
    {DOUBLECOLON}      { return DOUBLECOLON; }
    {COLON}            { return COLON; }
    {COMMA}            { return COMMA; }
    {ASSIGN}           { yybegin(SOURCE); return ASSIGN; }
    {BACKSLASHCRLF}    { return SPLIT; }
    {PIPE}             { return PIPE; }
    {SEMICOLON}        { yybegin(SOURCE_FORCED); return SEMICOLON; }
    "include"          { return KEYWORD_INCLUDE; }
    "-include"         { return KEYWORD_INCLUDE; }
    "sinclude"         { return KEYWORD_INCLUDE; }
    "vpath"            { return KEYWORD_VPATH; }
    "define"           { yybegin(DEFINE); return KEYWORD_DEFINE; }
    "undefine"         { yybegin(INCLUDES); return KEYWORD_UNDEFINE; }
    "ifeq"             { return KEYWORD_IFEQ; }
    "ifneq"            { return KEYWORD_IFNEQ; }
    "ifdef"            { return KEYWORD_IFDEF; }
    "ifndef"           { return KEYWORD_IFNDEF; }
    "else"             { return KEYWORD_ELSE; }
    "endif"            { return KEYWORD_ENDIF; }
    "override"         { return KEYWORD_OVERRIDE; }
    "export"           { yybegin(EXPORT); return KEYWORD_EXPORT; }
    "private"          { return KEYWORD_PRIVATE; }
    "$("               { return FUNCTION_START; }
    {FUNCTIONS}        { return FUNCTION_NAME; }
    "("                { return OPEN_BRACE; }
    ")"                { return FUNCTION_END; }
    {STRING}           { return STRING; }
    {VARIABLE_USAGE_CURLY_EXPR}   { return VARIABLE_USAGE; }
    {FILENAME}         { return IDENTIFIER; }
    "!"                { return IDENTIFIER; }
}

<INCLUDES> {
    {FILENAME}              { return IDENTIFIER; }
    {EOL}                   { yybegin(YYINITIAL); return EOL; }
    <<EOF>>                 { yypushback(yylength()); yybegin(YYINITIAL); return EOL; }
    {SPACES}|\t+            { return WHITE_SPACE; }
}

<EXPORT> {
    "$("                    { yybegin(FUNCTION); return FUNCTION_START; }
    ^[^\t]+                 { yypushback(yylength()); yybegin(YYINITIAL); return WHITE_SPACE; }
    {VARIABLE_USAGE_EXPR}   { return VARIABLE_USAGE; }
    {FILENAME}              { return IDENTIFIER; }
    {ASSIGN}                { yybegin(EXPORTVAR); return ASSIGN; }
    {EOL}                   { yybegin(YYINITIAL); return EOL; }
    <<EOF>>                 { yypushback(yylength()); yybegin(YYINITIAL); return EOL; }
    {SPACES}|\t+            { return WHITE_SPACE; }
}

<EXPORTVAR> {
    ^[^\t]+                 { yypushback(yylength()); yybegin(YYINITIAL); return WHITE_SPACE; }
    {SPACES}|\t+            { return WHITE_SPACE; }
    {BACKSLASHCRLF}         { return SPLIT; }
    {SOURCE_CODE}           { return TEXT; }
    {EOL}                   { yybegin(YYINITIAL); return EOL; }
    <<EOF>>                 { yypushback(yylength()); yybegin(YYINITIAL); return EOL; }
}

<SOURCE> {
    ^\t+                    { return TAB; }
    ^[^\t]+                 { yypushback(yylength()); yybegin(YYINITIAL); return WHITE_SPACE; }
    {SPACES}|\t+            { return WHITE_SPACE; }
    {BACKSLASHCRLF}         { return SPLIT; }
    {SOURCE_CODE}           { return TEXT; }
    {EOL}                   { yybegin(YYINITIAL); return EOL; }
}

<SOURCE_FORCED> {
    ^\t+                    { return TAB; }
    {SPACES}|\t+            { return WHITE_SPACE; }
    {BACKSLASHCRLF}         { return SPLIT; }
    {SOURCE_CODE}           { return TEXT; }
    {EOL}                   { yybegin(YYINITIAL); return EOL; }
}

<DEFINE> {
    {SPACES}|\t+                { return WHITE_SPACE; }
    {EOL}                       { yybegin(DEFINEBODY); return WHITE_SPACE; }
    {ASSIGN}                    { return ASSIGN; }
    {FILENAME}                  { return IDENTIFIER; }
}

<DEFINEBODY> {
    "endef"                { yybegin(YYINITIAL); return KEYWORD_ENDEF; }
    ({SPACES}|\t)+{COMMENT}              { return COMMENT; }
    {BACKSLASHCRLF}        { return SPLIT; }
    {SOURCE_CODE}          { return VARIABLE_VALUE_LINE; }
    {EOL}                  { return WHITE_SPACE; }
}

<CONDITIONALS> {
    {SPACES}                   { return WHITE_SPACE; }
    {CONDITION_CHARACTER}+     { yybegin(YYINITIAL); return CONDITION; }
    {EOL}                      { yybegin(YYINITIAL); return WHITE_SPACE; }
}

[^] { return BAD_CHARACTER; }
