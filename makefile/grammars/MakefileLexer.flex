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
DOCCOMMENT="##"[^\r\n]*
COMMENT="#"[^\r\n]*
ERROR="$(error"
WARNING="$(warning"
INFO="$(info"
SHELL="$(shell"
WILDCARD="$(wildcard"
PATHSUBST="$(pathsubst"
MACRO="@"[^@ \r\n]+"@"
VARIABLE_VALUE=[^\r\n#]*[^\\\r\n#]
COLON=":"
DOUBLECOLON="::"
SEMICOLON=";"
PIPE="|"
ASSIGN=("="|":="|"::="|"?="|"!="|"+=")

FILENAME_CHARACTER=[^:=!?#\ \r\n\t]
VARIABLE_USAGE_EXPR="$("[^ )]*")"
CONDITION_CHARACTER=[^#\r\n]

%state PREREQUISITES ELSE INCLUDES SOURCE DEFINE DEFINEBODY CONDITIONALS FUNCTION FUNCTION_PREREQ EXPORT EXPORTVAR

%%

{DOCCOMMENT}           { return DOC_COMMENT; }
{COMMENT}              { return COMMENT; }
^{MACRO}               { return MACRO; }

<FUNCTION> {
  ")"           { yybegin(YYINITIAL); return FUNCTION_END; }
  [^$)]*        { return FUNCTION_PARAM_TEXT; }
  {VARIABLE_USAGE_EXPR} { return VARIABLE_USAGE; }
  {EOL}         { yybegin(YYINITIAL); return EOL; }
}

<FUNCTION_PREREQ> {
  ")"           { yybegin(PREREQUISITES); return FUNCTION_END; }
  [^$)]*        { return FUNCTION_PARAM_TEXT; }
  {VARIABLE_USAGE_EXPR} { return VARIABLE_USAGE; }
  {EOL}         { yybegin(PREREQUISITES); return EOL; }
}

<YYINITIAL> {
    ^\t+               { yybegin(SOURCE); return TAB; }
    \t+                { return WHITE_SPACE; }
    {EOL}              { return WHITE_SPACE; }
    {SPACES}           { return WHITE_SPACE; }
    {DOUBLECOLON}      { yybegin(PREREQUISITES); return DOUBLECOLON; }
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
    "else"             { yybegin(ELSE); return KEYWORD_ELSE; }
    "endif"            { return KEYWORD_ENDIF; }
    "override"         { return KEYWORD_OVERRIDE; }
    "export"           { yybegin(EXPORT); return KEYWORD_EXPORT; }
    "private"          { return KEYWORD_PRIVATE; }
    {ERROR}                { yybegin(FUNCTION); return FUNCTION_ERROR; }
    {WARNING}              { yybegin(FUNCTION); return FUNCTION_WARNING; }
    {INFO}                 { yybegin(FUNCTION); return FUNCTION_INFO; }
    {SHELL}                { yybegin(FUNCTION); return FUNCTION_SHELL; }
    {WILDCARD}             { yybegin(FUNCTION); return FUNCTION_WILDCARD; }
    {PATHSUBST}            { yybegin(FUNCTION); return FUNCTION_PATHSUBST; }
    {VARIABLE_USAGE_EXPR}   { return VARIABLE_USAGE; }
    {FILENAME_CHARACTER}+   { return IDENTIFIER; }
}

<ELSE> {
    {EOL}              { yybegin(YYINITIAL); return EOL; }
    {SPACES}           { return WHITE_SPACE; }
    [^]                { yypushback(yylength()); yybegin(YYINITIAL); return WHITE_SPACE; }
}

<PREREQUISITES> {
    {ERROR}                 { yybegin(FUNCTION_PREREQ); return FUNCTION_ERROR; }
    {WARNING}               { yybegin(FUNCTION_PREREQ); return FUNCTION_WARNING; }
    {INFO}                  { yybegin(FUNCTION_PREREQ); return FUNCTION_INFO; }
    {SHELL}                 { yybegin(FUNCTION_PREREQ); return FUNCTION_SHELL; }
    {WILDCARD}              { yybegin(FUNCTION_PREREQ); return FUNCTION_WILDCARD; }
    {PATHSUBST}             { yybegin(FUNCTION_PREREQ); return FUNCTION_PATHSUBST; }
    "override"              { yybegin(YYINITIAL); return KEYWORD_OVERRIDE; }
    "export"                { yybegin(YYINITIAL); return KEYWORD_EXPORT; }
    "private"               { yybegin(YYINITIAL); return KEYWORD_PRIVATE; }
    {ASSIGN}                { yybegin(SOURCE); return ASSIGN; }
    {BACKSLASHCRLF}         { return SPLIT; }
    {PIPE}                  { return PIPE; }
    {DOUBLECOLON}           { return DOUBLECOLON; }
    {COLON}                 { return COLON; }
    {SEMICOLON}             { yybegin(SOURCE); return SEMICOLON; }
    {VARIABLE_USAGE_EXPR}   { return VARIABLE_USAGE; }
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

<EXPORT> {
    {ERROR}                 { yybegin(FUNCTION); return FUNCTION_ERROR; }
    {WARNING}               { yybegin(FUNCTION); return FUNCTION_WARNING; }
    {INFO}                  { yybegin(FUNCTION); return FUNCTION_INFO; }
    {SHELL}                 { yybegin(FUNCTION); return FUNCTION_SHELL; }
    {WILDCARD}              { yybegin(FUNCTION); return FUNCTION_WILDCARD; }
    {PATHSUBST}             { yybegin(FUNCTION); return FUNCTION_PATHSUBST; }
    {VARIABLE_USAGE_EXPR}   { return VARIABLE_USAGE; }
    {FILENAME_CHARACTER}+   { return IDENTIFIER; }
    {ASSIGN}                { yybegin(EXPORTVAR); return ASSIGN; }
    {EOL}                   { yybegin(YYINITIAL); return EOL; }
    <<EOF>>                 { yypushback(yylength()); yybegin(YYINITIAL); return EOL; }
    {SPACES}|\t+            { return WHITE_SPACE; }
}

<EXPORTVAR> {
    {SPACES}|\t+            { return WHITE_SPACE; }
    {BACKSLASHCRLF}         { return SPLIT; }
    {VARIABLE_VALUE}        { return LINE; }
    {EOL}                   { yybegin(YYINITIAL); return EOL; }
    <<EOF>>                 { yypushback(yylength()); yybegin(YYINITIAL); return EOL; }
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
    ({SPACES}|\t)+{COMMENT}              { return COMMENT; }
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
