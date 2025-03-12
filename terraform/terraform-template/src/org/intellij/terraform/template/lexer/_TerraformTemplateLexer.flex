package org.intellij.terraform.template.lexer;

import com.intellij.psi.tree.IElementType;
import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static org.intellij.terraform.hil.HILElementTypes.*;
import static org.intellij.terraform.hil.psi.template.TftplTokenTypes.DATA_LANGUAGE_TOKEN_UNPARSED;
%%

%{
  public _TerraformTemplateLexer() {
    this(null);
  }

  private int braceBalance = 0;
  private boolean isLastBrace() { return braceBalance <= 0; }
  private void incrementBraceBalance() { braceBalance++; }
  private void decrementBraceBalance() { braceBalance--; assert braceBalance >= 0 : "Invalid braces balance, consider lexer adjustments"; }
  private void resetBraceBalance() { braceBalance = 0; }
%}

%public
%class _TerraformTemplateLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

%state TEMPLATE_SEGMENT_SELECTOR DATA_LANGUAGE_SEGMENT TEMPLATE_LANGUAGE_SEGMENT IL_EXPRESSION QUOTED_STRING

WHITE_SPACE=[\s\R]+

NUMBER=(0x)?[0-9]+(\.[0-9]+)?([eE][-+]?[0-9]+)?
ID=([0-9a-zA-Z_][0-9a-zA-Z_]*)|([a-zA-Z_][0-9a-zA-Z\-_*]*)

HIL_START=(\$\{\~?)
HIL_STOP=(\~?\})
TEMPLATE_START=(\%\{\~?)
QUOTE = \"
IL_STRING_ELEMENT=\"([^\"])?\"

%eof{
  resetBraceBalance();
%eof}

%%
<YYINITIAL, TEMPLATE_SEGMENT_SELECTOR> {
  {QUOTE}                           {
                                      yybegin(QUOTED_STRING);
                                    }
  {HIL_START}                       {
                                      incrementBraceBalance();
                                      yybegin(TEMPLATE_LANGUAGE_SEGMENT);
                                      return INTERPOLATION_START;
                                    }
  {TEMPLATE_START}                  {
                                      incrementBraceBalance();
                                      yybegin(TEMPLATE_LANGUAGE_SEGMENT);
                                      return TEMPLATE_START;
                                    }
  [^]                               {
                                      yybegin(DATA_LANGUAGE_SEGMENT);
                                    }
}

<DATA_LANGUAGE_SEGMENT> {
  {QUOTE}                           {
                                      yybegin(QUOTED_STRING);
                                    }
  {HIL_START}|{TEMPLATE_START}      {
                                      yypushback(yylength());
                                      resetBraceBalance();
                                      yybegin(TEMPLATE_SEGMENT_SELECTOR);
                                      return DATA_LANGUAGE_TOKEN_UNPARSED;
                                    }
  [^]                               { }
  <<EOF>>                           {
                                      resetBraceBalance();
                                      yybegin(YYINITIAL);
                                      return DATA_LANGUAGE_TOKEN_UNPARSED;
                                    }
}

<TEMPLATE_LANGUAGE_SEGMENT> {
  {HIL_STOP}                        {
                                      decrementBraceBalance();
                                      if (isLastBrace()) {
                                        resetBraceBalance();
                                        yybegin(YYINITIAL);
                                      }
                                      return R_CURLY;
                                    }

  "~"                               { return TILDA; }
  "("                               { return L_PAREN; }
  ")"                               { return R_PAREN; }
  "["                               { return L_BRACKET; }
  "]"                               { return R_BRACKET; }
  ","                               { return COMMA; }
  "="                               { return EQUALS; }
  "..."                             { return OP_ELLIPSIS; }
  "."                               { return OP_DOT; }
  "::"                              { return COLON_COLON; }
  "+"                               { return OP_PLUS; }
  "-"                               { return OP_MINUS; }
  "*"                               { return OP_MUL; }
  "/"                               { return OP_DIV; }
  "%"                               { return OP_MOD; }
  "=="                              { return OP_EQUAL; }
  "!="                              { return OP_NOT_EQUAL; }
  "!"                               { return OP_NOT; }
  "<"                               { return OP_LESS; }
  ">"                               { return OP_GREATER; }
  "<="                              { return OP_LESS_OR_EQUAL; }
  ">="                              { return OP_GREATER_OR_EQUAL; }
  "&&"                              { return OP_AND_AND; }
  "||"                              { return OP_OR_OR; }
  ":"                               { return OP_COLON; }
  "?"                               { return OP_QUEST; }
  "true"                            { return TRUE; }
  "false"                           { return FALSE; }
  "null"                            { return NULL; }
  "for"                             { return FOR_KEYWORD; }
  "in"                              { return IN_KEYWORD; }
  "endfor"                          { return ENDFOR_KEYWORD; }
  "if"                              { return IF_KEYWORD; }
  "else"                            { return ELSE_KEYWORD; }
  "endif"                           { return ENDIF_KEYWORD; }
  {NUMBER}                          { return NUMBER; }
  {ID}                              { return ID; }
  {WHITE_SPACE}                     { return WHITE_SPACE; }
  {IL_STRING_ELEMENT}               { return DOUBLE_QUOTED_STRING; }

  [^]                               { return BAD_CHARACTER; }
}

<QUOTED_STRING> {
  {QUOTE}                           {
                                      yybegin(DATA_LANGUAGE_SEGMENT);
                                    }
  [^]                               { }
  <<EOF>>                           {
                                      resetBraceBalance();
                                      yybegin(YYINITIAL);
                                      return DATA_LANGUAGE_TOKEN_UNPARSED;
                                    }
}