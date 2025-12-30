package org.angular2.lang.expr.lexer;

import com.intellij.psi.tree.IElementType;
import com.intellij.lexer.FlexLexer;
import com.intellij.util.containers.IntStack;

import org.angular2.codeInsight.blocks.Angular2HtmlBlockUtilsKt;

import static com.intellij.lang.javascript.JSTokenTypes.*;
import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static org.angular2.lang.expr.lexer.Angular2TokenTypes.*;

%%

%{
  private char quote;

  private String blockName;
  private int blockParamIndex;
  private boolean enableVoidKeyword;

  private IntStack myStateStack = new IntStack(5);

  public _Angular2Lexer(Angular2Lexer.Config config) {
    this((java.io.Reader)null);
    enableVoidKeyword = config.getSyntax().getEnableVoidKeyword();
    if (config instanceof Angular2Lexer.BlockParameter blockParameter) {
      blockName = blockParameter.getName();
      blockParamIndex = blockParameter.getIndex();
    }
  }

  public final void clearState() {
    myStateStack.clear();
  }

  public boolean isRestartableState() {
    return (yystate() == YYINITIAL || yystate() == YYEXPRESSION) && myStateStack.size() == 0;
  }

  private boolean shouldStartWithParameter() {
    return blockName != null && (blockParamIndex > 0 || !Angular2HtmlBlockUtilsKt.getBLOCKS_WITH_PRIMARY_EXPRESSION().contains(blockName));
  }

  private void pushState(int nextState) {
    myStateStack.push(nextState);
  }

  private int popState() {
    if (myStateStack.size() > 0) {
      int nextState = myStateStack.pop();
      yybegin(nextState);
      return nextState;
    }
    return -1;
  }

%}

%unicode
//%debug
%class _Angular2Lexer
%implements FlexLexer
%type IElementType

%function advance

WHITE_SPACE=([ \t\n\r\u000B\u00A0]|\\\n)+

DIGIT=[0-9]
NUMBER=({DIGIT}+)|({FP_LITERAL1})|({FP_LITERAL2})|({FP_LITERAL3})|({FP_LITERAL4})
FP_LITERAL1=({DIGIT})+"."({DIGIT})*({EXPONENT_PART})?
FP_LITERAL2="."({DIGIT})+({EXPONENT_PART})?
FP_LITERAL3=({DIGIT})+({EXPONENT_PART})
FP_LITERAL4=({DIGIT})+
EXPONENT_PART=[Ee]["+""-"]?({DIGIT})*
COMMENT="//"[^]*

ALPHA=[:letter:]
TAG_NAME=({ALPHA}|"_"|":")({ALPHA}|{DIGIT}|"_"|":"|"."|"-")*

IDENT=[_$a-zA-Z][$0-9_a-zA-Z]*
STRING_TEMPLATE_CHAR=[^\\$`] | \\ .
ESCAPE_SEQUENCE=\\[^\r\n]
LINE_TERMINATOR_SEQUENCE=\R

%state YYEXPRESSION
%state YYSTRING
%state YYSTRING_TEMPLATE
%state YYSTRING_TEMPLATE_DOLLAR
%state YYINITIAL_WITH_NONEMPTY_STATE_STACK

%%

<YYINITIAL> {
  {WHITE_SPACE}               { return WHITE_SPACE; }
  "prefetch"|"hydrate"        { if (shouldStartWithParameter()) return BLOCK_PARAMETER_NAME; else { yybegin(YYEXPRESSION); yypushback(yylength());} }
  [a-zA-Z_]+                  { yybegin(YYEXPRESSION); if (shouldStartWithParameter()) return BLOCK_PARAMETER_NAME; else yypushback(yylength()); }
  [^]                         { yypushback(1); yybegin(YYEXPRESSION); }
}

<YYEXPRESSION> {
  "&apos;"                    { yybegin(YYSTRING); quote = '\''; return XML_CHAR_ENTITY_REF; }
  "&quot;"                    { yybegin(YYSTRING); quote = '"'; return XML_CHAR_ENTITY_REF; }
  "'"                         { yybegin(YYSTRING); quote = '\''; return STRING_LITERAL_PART; }
  "\""                        { yybegin(YYSTRING); quote = '"'; return STRING_LITERAL_PART; }
  {NUMBER}                    { return NUMERIC_LITERAL; }
  {WHITE_SPACE}               { return WHITE_SPACE; }
  {COMMENT}                   { return C_STYLE_COMMENT; }

  "var"                       { return VAR_KEYWORD; }
  "let"                       { return LET_KEYWORD; }
  "as"                        { return AS_KEYWORD; }
  "null"                      { return NULL_KEYWORD; }
  "undefined"                 { return UNDEFINED_KEYWORD; }
  "true"                      { return TRUE_KEYWORD; }
  "false"                     { return FALSE_KEYWORD; }
  "if"                        { return IF_KEYWORD; }
  "else"                      { return ELSE_KEYWORD; }
  "this"                      { return THIS_KEYWORD; }
  "typeof"                    { return TYPEOF_KEYWORD; }
  "in"                        { return IN_KEYWORD; }
  "void"                      { if (enableVoidKeyword) return VOID_KEYWORD; else return IDENTIFIER; }

  "as"/(\.)                   { return IDENTIFIER; }
  {IDENT}                     { return IDENTIFIER; }

  "+"                         { return PLUS; }
  "-"                         { return MINUS; }
  "*"                         { return MULT; }
  "**"                        { return MULTMULT; }
  "/"                         { return DIV; }
  "%"                         { return PERC; }
  "^"                         { return XOR; }
  "="                         { return EQ; }
  "==="                       { return EQEQEQ; }
  "!=="                       { return NEQEQ; }
  "=="                        { return EQEQ; }
  "!="                        { return NE; }
  "<"                         { return LT; }
  ">"                         { return GT; }
  "<="                        { return LE; }
  ">="                        { return GE; }
  "&&"                        { return ANDAND; }
  "||"                        { return OROR; }
  "??"                        { return QUEST_QUEST; }
  "&"                         { return AND; }
  "|"                         { return OR; }
  "!"                         { return EXCL; }

  "+="                        { return PLUSEQ; }
  "-="                        { return MINUSEQ; }
  "*="                        { return MULTEQ; }
  "/="                        { return DIVEQ; }
  "%="                        { return PERCEQ; }
  "**="                       { return MULTMULTEQ; }
  "&&="                       { return AND_AND_EQ; }
  "||="                       { return OR_OR_EQ; }
  "??="                       { return QUEST_QUEST_EQ; }

  "("                         { return LPAR; }
  ")"                         { return RPAR; }
  "{"                         {
                                if (myStateStack.size() > 0) pushState(YYEXPRESSION);
                                yybegin(YYEXPRESSION);
                                return LBRACE;
                              }
  "}"                         {
                                int popped = popState();
                                if (popped < 0) {
                                  yybegin(YYEXPRESSION);
                                }
                                return RBRACE;
                              }

  "["                         { return LBRACKET; }
  "]"                         { return RBRACKET; }
  "."                         { return DOT; }
  "?."                        { return ELVIS; }
  ","                         { return COMMA; }
  ";"                         { return SEMICOLON; }
  ":"                         { return COLON; }
  "?"                         { return QUEST; }
  "#"                         { return SHARP; }

  "`"                         { yybegin(YYSTRING_TEMPLATE); return BACKQUOTE; }

  [^]                         { return BAD_CHARACTER; }
}

<YYSTRING> {
  "\\&"{TAG_NAME}";" |
  "\\&#"{DIGIT}+";" |
  "\\&#"(x|X)({DIGIT}|[a-fA-F])+";" |
  [\\][^u\n\r] |
  [\\]u[0-9a-fA-F]{4}         { return ESCAPE_SEQUENCE; }
  [\\]u[^0-9a-fA-F]           { yypushback(1); return INVALID_ESCAPE_SEQUENCE; }
  [\\]u[0-9a-fA-F]{1,3}       { return INVALID_ESCAPE_SEQUENCE; }
  "&apos;"                    { if (quote == '\'') yybegin(YYEXPRESSION); return XML_CHAR_ENTITY_REF; }
  "&quot;"                    { if (quote == '"') yybegin(YYEXPRESSION); return XML_CHAR_ENTITY_REF; }
  "'"                         { if (quote == '\'') yybegin(YYEXPRESSION); return STRING_LITERAL_PART; }
  "\""                        { if (quote == '"') yybegin(YYEXPRESSION); return STRING_LITERAL_PART; }
  "&"{TAG_NAME}";" |
  "&#"(x|X)({DIGIT}|[a-fA-F])+";" |
  "&#"{DIGIT}+";"             { return XML_CHAR_ENTITY_REF; }
  [^&\'\"\n\r\\]+ | "&"       { return STRING_LITERAL_PART; }
  [^]                         { yypushback(yytext().length()); yybegin(YYEXPRESSION); }
}

<YYSTRING_TEMPLATE> {
  ( {STRING_TEMPLATE_CHAR} | {ESCAPE_SEQUENCE} | "\\" {LINE_TERMINATOR_SEQUENCE} )+
                              { return STRING_TEMPLATE_PART; }
  "$"                         { return STRING_TEMPLATE_PART; }
  "$" / "{"                   { /* don't merge with { to have parents paired */
                                yybegin(YYSTRING_TEMPLATE_DOLLAR);
                                return DOLLAR;
                              }
  "`"                         { yybegin(YYEXPRESSION); return BACKQUOTE; }
}

<YYSTRING_TEMPLATE_DOLLAR> {
  "{"                         { pushState(YYSTRING_TEMPLATE); yybegin(YYEXPRESSION); return LBRACE; }
}
