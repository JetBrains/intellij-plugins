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
MULTILINECOMMENT="#"[^\r\n\\]*(("\\"\r?\n[^\r\n\\]*|"\\"[^\r\n\\]*))+
FUNCTIONS=("error"|"warning"|"info"|"shell"|"subst"|"patsubst"|"strip"|"findstring"|
  "filter"|"filter-out"|"sort"|"word"|"wordlist"|"words"|"firstword"|"lastword"|"dir"|"notdir"|"suffix"|
  "basename"|"addsuffix"|"addprefix"|"join"|"wildcard"|"realpath"|"abspath"|"if"|"or"|"and"|
  "foreach"|"file"|"call"|"value"|"eval"|"origin"|"flavor"|"guile")
MACRO="@"[^@ \r\n]+"@"
ASSIGN=("="|":="|"::="|"?="|"!="|"+=")

CHARS = [0-9a-zA-Z.!\-?\"%@/_\[\]+~*\^&+`'<>]

STRING="\""[^\"]*"\""|"'"[^']*"'"

%state INCLUDES SOURCE SOURCE_FORCED DEFINE DEFINEBODY CONDITIONALS FUNCTION EXPORT EXPORTVAR

%%

\\"#"                  { return CHARS; }
{DOCCOMMENT}           { return DOC_COMMENT; }
{MULTILINECOMMENT}     { return COMMENT; }
{COMMENT}              { return COMMENT; }
^{MACRO}               { return MACRO; }

^\t+               { return TAB; }
\t+                { return WHITE_SPACE; }
{EOL}              { return EOL; }
{SPACES}           { return WHITE_SPACE; }
\\:                { return CHARS; }
:                  { return COLON; }
","                { return COMMA; }
{ASSIGN}           { return ASSIGN; }
{BACKSLASHCRLF}    { return SPLIT; }
"|"                { return PIPE; }
";"                { return SEMICOLON; }
"include"          { return KEYWORD_INCLUDE; }
"-include"         { return KEYWORD_INCLUDE; }
"sinclude"         { return KEYWORD_INCLUDE; }
"vpath"            { return KEYWORD_VPATH; }
"define"           { return KEYWORD_DEFINE; }
"undefine"         { return KEYWORD_UNDEFINE; }
"endef"            { return KEYWORD_ENDEF; }
"ifeq"             { return KEYWORD_IFEQ; }
"ifneq"            { return KEYWORD_IFNEQ; }
"ifdef"            { return KEYWORD_IFDEF; }
"ifndef"           { return KEYWORD_IFNDEF; }
"else"             { return KEYWORD_ELSE; }
"endif"            { return KEYWORD_ENDIF; }
"override"         { return KEYWORD_OVERRIDE; }
"export"           { return KEYWORD_EXPORT; }
"private"          { return KEYWORD_PRIVATE; }
"$"                { return DOLLAR; }
{FUNCTIONS}        { return FUNCTION_NAME; }
"("                { return OPEN_PAREN; }
")"                { return CLOSE_PAREN; }
"{"                { return OPEN_CURLY; }
"}"                { return CLOSE_CURLY; }
\\\"               { return CHARS; }
{STRING}           { return STRING; }
{CHARS}+           { return CHARS; }
\\\\               { return CHARS; }
\\                 { return CHARS; }

[^] { return BAD_CHARACTER; }
