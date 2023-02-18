/* Generated code. Do not modify it. */
package com.intellij.lang.ognl.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import java.util.*;
import java.lang.reflect.Field;
import org.jetbrains.annotations.NotNull;

import com.intellij.psi.TokenType;
import com.intellij.lang.ognl.OgnlTypes;

%%

%{
  public _OgnlLexer(){
    this((java.io.Reader)null);
  }

  private static final class State {
    final int lBraceCount;
    final int state;

    private State(int state, int lBraceCount) {
        this.state = state;
        this.lBraceCount = lBraceCount;
    }

    @Override
    public String toString() {
        return "yystate = " + state + (lBraceCount == 0 ? "" : "lBraceCount = " + lBraceCount);
    }
  }

  private final Stack<State> myStateStack = new Stack<>();
  private int myLeftBraceCount;

  protected void resetInternal() {
    myLeftBraceCount = 0;
    myStateStack.clear();
  }

  private void pushState(int newState) {
    myStateStack.push(new State(yystate(), myLeftBraceCount));
    myLeftBraceCount = 0;
    yybegin(newState);
  }

  private void popState() {
    if (myStateStack.empty()) return;

    State state = myStateStack.pop();
    myLeftBraceCount = state.lBraceCount;
    yybegin(state.state);
  }
%}

%unicode
%class _OgnlLexer
%public
%implements FlexLexer
%function advance
%type IElementType
%eof{
  resetInternal();
%eof}

ALPHA=[:letter:]
DIGIT=[0-9]
WHITE_SPACE_CHAR=[\ \n\r\t\f]

IDENTIFIER={ALPHA} [:jletterdigit:]*

INTEGER_LITERAL=(0|([1-9]({DIGIT})*))
BIG_INTEGER_LITERAL=({INTEGER_LITERAL})(["h""H"]?)
DOUBLE_LITERAL=({FLOATING_POINT_LITERAL1})|({FLOATING_POINT_LITERAL2})|({FLOATING_POINT_LITERAL3})
BIG_DECIMAL_LITERAL=({DOUBLE_LITERAL})(["b""B"]?)

FLOATING_POINT_LITERAL1=({DIGIT})+"."({DIGIT})*({EXPONENT_PART})?
FLOATING_POINT_LITERAL2="."({DIGIT})+({EXPONENT_PART})?
FLOATING_POINT_LITERAL3=({DIGIT})+({EXPONENT_PART})
EXPONENT_PART=[Ee]["+""-"]?({DIGIT})*

CHARACTER_LITERAL="'"([^\\\'\r\n]|{ESCAPE_SEQUENCE})*("'"|\\)?
STRING_LITERAL=\"([^\\\"\r\n]|{ESCAPE_SEQUENCE})*(\"|\\)?

ESCAPE_SEQUENCE=\\[^\r\n]

%state NESTED_BRACE, EXPR

%%

<YYINITIAL> "%{" {
  pushState(EXPR);
  return OgnlTypes.EXPRESSION_START;
}
<EXPR, NESTED_BRACE> "}" {
  popState();
  if (myLeftBraceCount == 0)  {
    return OgnlTypes.EXPRESSION_END;
  }

  myLeftBraceCount--;
  return OgnlTypes.RBRACE;
}

<YYINITIAL, EXPR, NESTED_BRACE> "{" {
  myLeftBraceCount++;
  pushState(NESTED_BRACE);
  return OgnlTypes.LBRACE;
}

{WHITE_SPACE_CHAR}+   { return TokenType.WHITE_SPACE; }

{INTEGER_LITERAL}     { return OgnlTypes.INTEGER_LITERAL; }
{BIG_INTEGER_LITERAL} { return OgnlTypes.BIG_INTEGER_LITERAL; }
{DOUBLE_LITERAL}      { return OgnlTypes.DOUBLE_LITERAL; }
{BIG_DECIMAL_LITERAL} { return OgnlTypes.BIG_DECIMAL_LITERAL; }

{CHARACTER_LITERAL}   { return OgnlTypes.CHARACTER_LITERAL; }
{STRING_LITERAL}      { return OgnlTypes.STRING_LITERAL; }

"shl"    { return OgnlTypes.SHIFT_LEFT_KEYWORD; }
"shr"    { return OgnlTypes.SHIFT_RIGHT_KEYWORD; }
"ushr"   { return OgnlTypes.SHIFT_RIGHT_LOGICAL_KEYWORD; }

"and"    { return OgnlTypes.AND_KEYWORD; }
"band"   { return OgnlTypes.BAND_KEYWORD; }
"or"     { return OgnlTypes.OR_KEYWORD; }
"bor"    { return OgnlTypes.BOR_KEYWORD; }
"xor"    { return OgnlTypes.XOR_KEYWORD; }
"eq"     { return OgnlTypes.EQ_KEYWORD; }
"neq"    { return OgnlTypes.NEQ_KEYWORD; }
"lt"     { return OgnlTypes.LT_KEYWORD; }
"lte"    { return OgnlTypes.LT_EQ_KEYWORD; }
"gt"     { return OgnlTypes.GT_KEYWORD; }
"gte"    { return OgnlTypes.GT_EQ_KEYWORD; }
"not in" { return OgnlTypes.NOT_IN_KEYWORD; }
"not"    { return OgnlTypes.NOT_KEYWORD; }
"in"     { return OgnlTypes.IN_KEYWORD; }

"new"    { return OgnlTypes.NEW_KEYWORD; }
"true"   { return OgnlTypes.TRUE_KEYWORD; }
"false"  { return OgnlTypes.FALSE_KEYWORD; }
"null"   { return OgnlTypes.NULL_KEYWORD; }
"instanceof" { return OgnlTypes.INSTANCEOF_KEYWORD; }

{IDENTIFIER} { return OgnlTypes.IDENTIFIER; }

"("   { return OgnlTypes.LPARENTH; }
")"   { return OgnlTypes.RPARENTH; }
"["   { return OgnlTypes.LBRACKET; }
"]"   { return OgnlTypes.RBRACKET; }

"!="  { return OgnlTypes.NOT_EQUAL; }
"!"   { return OgnlTypes.NEGATE; }
"=="  { return OgnlTypes.EQUAL; }

"<<"  { return OgnlTypes.SHIFT_LEFT; }
">>>" { return OgnlTypes.SHIFT_RIGHT_LOGICAL; }
">>"  { return OgnlTypes.SHIFT_RIGHT; }

"<="  { return OgnlTypes.LESS_EQUAL; }
">="  { return OgnlTypes.GREATER_EQUAL; }
"<"   { return OgnlTypes.LESS; }
">"   { return OgnlTypes.GREATER; }


"."  { return OgnlTypes.DOT; }
","  { return OgnlTypes.COMMA; }
"?"  { return OgnlTypes.QUESTION; }
":"  { return OgnlTypes.COLON; }
"#"  { return OgnlTypes.HASH; }
"@"  { return OgnlTypes.AT; }
"$"  { return OgnlTypes.DOLLAR; }
"="  { return OgnlTypes.EQ; }

"/"  { return OgnlTypes.DIVISION; }
"*"  { return OgnlTypes.MULTIPLY; }
"-"  { return OgnlTypes.MINUS; }
"+"  { return OgnlTypes.PLUS; }
"%"  { return OgnlTypes.MODULO; }

"&&" { return OgnlTypes.AND_AND; }
"||" { return OgnlTypes.OR_OR; }

"|"  { return OgnlTypes.OR; }
"^"  { return OgnlTypes.XOR; }
"&"  { return OgnlTypes.AND; }
"~"  { return OgnlTypes.NOT; }

[^]  { return TokenType.BAD_CHARACTER; }