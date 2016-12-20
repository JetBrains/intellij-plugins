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
PIPE="|"
ASSIGN=("="|":="|"::="|"?="|"!="|"+=")

FILENAME_CHARACTER=[^:\ \r\n\t]
COMMAND=[^\r\n]+

%state SEPARATOR PREREQUISITES COMMANDS VARIABLE INCLUDES CONDITIONALS

%%

<YYINITIAL> {
    {COMMENT}          { yybegin(YYINITIAL); return COMMENT; }
    {EOL}              { yybegin(YYINITIAL); return WHITE_SPACE; }
    "include"          { yybegin(INCLUDES); return INCLUDE; }
    "ifeq"             { yybegin(CONDITIONALS); return IFEQ; }
    "else"             { yybegin(YYINITIAL); return ELSE_; }
    "endif"            { yybegin(YYINITIAL); return ENDIF; }
}

<YYINITIAL> {FILENAME_CHARACTER}+   { yybegin(SEPARATOR); return IDENTIFIER; }

<SEPARATOR> {COLON}             { yybegin(PREREQUISITES); return COLON; }
<SEPARATOR> {ASSIGN}            { yybegin(VARIABLE); return ASSIGN; }
<SEPARATOR> {SPACES}            { yybegin(SEPARATOR); return WHITE_SPACE; }

<PREREQUISITES> {
    {PIPE}                  { return PIPE; }
    {FILENAME_CHARACTER}+   { return IDENTIFIER; }
    {EOL}                   { yybegin(YYINITIAL); return EOL; }
    {SPACES}                { return WHITE_SPACE; }
}

<YYINITIAL> {TAB}+                     { yybegin(COMMANDS); return WHITE_SPACE; }
<COMMANDS> {COMMAND}                   { yybegin(YYINITIAL); return COMMAND; }

<VARIABLE> {VARIABLE_VALUE}+           { yybegin(VARIABLE); return VARIABLE_VALUE; }
<VARIABLE> {EOL}                       { yybegin(YYINITIAL); return EOL; }

<INCLUDES> {SPACES}                     { yybegin(INCLUDES); return WHITE_SPACE; }
<INCLUDES> {FILENAME_CHARACTER}+        { yybegin(YYINITIAL); return FILENAME; }

<CONDITIONALS> {
    {FILENAME_CHARACTER}+      { yybegin(YYINITIAL); return CONDITION; }
    {SPACES}                   { return WHITE_SPACE; }
    {EOL}                      { yybegin(YYINITIAL); return WHITE_SPACE; }
}

[^] { return BAD_CHARACTER; }
