package org.intellij.terraform.hil.psi;

import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;
import static org.intellij.terraform.hil.HILElementTypes.*;
import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;

@SuppressWarnings({"ALL"})
%%

%public
%class _HILLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

WHITE_SPACE=\s

NUMBER=(0x)?[0-9]+(\.[0-9]+)?([eE][-+]?[0-9]+)?
ID=([0-9a-zA-Z_][0-9a-zA-Z_]*)|([a-zA-Z_][0-9a-zA-Z\-_*]*)

HIL_START=(\$\{\~?)
HIL_STOP=(\~?\})
TEMPLATE_START=(\%\{\~?)

IL_STRING_ELEMENT=([^\"\'\$\{\}]|\\[^\r\n])+
STRING_ELEMENT=([^\"\'\$\{\}\\]|\\[^\r\n\\])+

%state STRING, INTERPOLATION

%{
  public _HILLexer() {
    this((java.io.Reader)null);
  }
  int stringStart = -1;
  int hil = 0;
  private void hil_inc() {
    hil++;
  }
  private int hil_dec() {
    assert hil > 0;
    hil--;
    return hil;
  }
  private IElementType eods() {
    yybegin(YYINITIAL); zzStartRead = stringStart; return DOUBLE_QUOTED_STRING;
  }
  private IElementType eoil() {
    hil=0; return eods();
  }

%}

%%

<STRING> {
  {HIL_START} { hil_inc(); yybegin(INTERPOLATION); }
  {TEMPLATE_START} { hil_inc(); yybegin(INTERPOLATION); }
  \"          { return eods(); }
  \\\\ {}
  {STRING_ELEMENT} {}
  \$ {}
  \{ {}
  \} {}
  \' {}
  <<EOF>> { return eods(); }
  [^] { return eods(); }
}

<INTERPOLATION> {
  {HIL_START} {hil_inc();}
  {HIL_STOP} {if (hil_dec() <= 0) yybegin(STRING); }
  {IL_STRING_ELEMENT} {}
  \' {}
  \" {}
  \$ {}
  \{ {}
  <<EOF>> { return eoil(); }
}

<YYINITIAL>   \"  { stringStart = zzStartRead; yybegin(STRING); }

<YYINITIAL> {
  {WHITE_SPACE}               { return WHITE_SPACE; }

  {TEMPLATE_START}            { return TEMPLATE_START; }
  {HIL_START}                 { return INTERPOLATION_START; }
  {HIL_STOP}                  { return R_CURLY; }

  "("                         { return L_PAREN; }
  ")"                         { return R_PAREN; }
  "["                         { return L_BRACKET; }
  "]"                         { return R_BRACKET; }
  "{"                         { return L_CURLY; }
  "}"                         { return R_CURLY; }
  ","                         { return COMMA; }
  "="                         { return EQUALS; }
  "..."                       { return OP_ELLIPSIS; }
  "."                         { return OP_DOT; }
  "+"                         { return OP_PLUS; }
  "-"                         { return OP_MINUS; }
  "*"                         { return OP_MUL; }
  "/"                         { return OP_DIV; }
  "%"                         { return OP_MOD; }
  "!"                         { return OP_NOT; }
  "=="                        { return OP_EQUAL; }
  "!="                        { return OP_NOT_EQUAL; }
  "<"                         { return OP_LESS; }
  ">"                         { return OP_GREATER; }
  "<="                        { return OP_LESS_OR_EQUAL; }
  ">="                        { return OP_GREATER_OR_EQUAL; }
  "&&"                        { return OP_AND_AND; }
  "||"                        { return OP_OR_OR; }
  ":"                         { return OP_COLON; }
  "?"                         { return OP_QUEST; }
  "true"                      { return TRUE; }
  "false"                     { return FALSE; }
  "null"                      { return NULL; }

  {NUMBER}                    { return NUMBER; }
  {ID}                        { return ID; }

}

[^] { return BAD_CHARACTER; }
