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
ASSIGN=("="|":="|"::="|"?="|"!="|"+=")
FILENAME_CHARACTER=[^:\ \r\n\t]
COMMAND=[^\r\n]+

%state SEPARATOR DEPENDENCIES COMMANDS VARIABLE

%%

<YYINITIAL> {COMMENT}          { yybegin(YYINITIAL); return COMMENT; }
<YYINITIAL> {EOL}              { yybegin(YYINITIAL); return WHITE_SPACE; }

<YYINITIAL> {FILENAME_CHARACTER}+   { yybegin(SEPARATOR); return IDENTIFIER; }

<SEPARATOR> {COLON}             { yybegin(DEPENDENCIES); return COLON; }
<SEPARATOR> {ASSIGN}            { yybegin(VARIABLE); return ASSIGN; }
<SEPARATOR> {SPACES}            { yybegin(SEPARATOR); return WHITE_SPACE; }

<DEPENDENCIES> {FILENAME_CHARACTER}+   { yybegin(DEPENDENCIES); return IDENTIFIER; }
<DEPENDENCIES> {EOL}                   { yybegin(YYINITIAL); return EOL; }
<DEPENDENCIES> {SPACES}                { yybegin(DEPENDENCIES); return WHITE_SPACE; }

<YYINITIAL> {TAB}+                     { yybegin(COMMANDS); return WHITE_SPACE; }
<COMMANDS> {COMMAND}                   { yybegin(YYINITIAL); return COMMAND; }

<VARIABLE> {VARIABLE_VALUE}+           { yybegin(VARIABLE); return VARIABLE_VALUE; }
<VARIABLE> {EOL}                       { yybegin(YYINITIAL); return EOL; }

[^] { return BAD_CHARACTER; }
