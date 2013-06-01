package com.jetbrains.lang.dart;

import java.util.*;
import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import static com.jetbrains.lang.dart.DartTokenTypes.*;
import static com.jetbrains.lang.dart.DartTokenTypesSets.*;

%%
%{
    private static final class State {
        final int lBraceCount;
        final int state;

        public State(int state, int lBraceCount) {
            this.state = state;
            this.lBraceCount = lBraceCount;
        }

        @Override
        public String toString() {
            return "yystate = " + state + (lBraceCount == 0 ? "" : "lBraceCount = " + lBraceCount);
        }
    }

    private final Stack<State> states = new Stack<State>();
    private int lBraceCount;

    private int commentStart;
    private int commentDepth;

    private void pushState(int state) {
        states.push(new State(yystate(), lBraceCount));
        lBraceCount = 0;
        yybegin(state);
    }

    private void popState() {
        State state = states.pop();
        lBraceCount = state.lBraceCount;
        yybegin(state.state);
    }

    public _DartLexer() {
      this((java.io.Reader)null);
    }
%}

%class _DartLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}

%xstate QUO_STRING THREE_QUO_STRING APOS_STRING THREE_APOS_STRING SHORT_TEMPLATE_ENTRY LONG_TEMPLATE_ENTRY

DIGIT=[0-9]
LETTER=[a-z]|[A-Z]
WHITE_SPACE=[ \n\r\t\f]+
SINGLE_LINE_COMMENT="/""/"[^\r\n]*
SINGLE_LINE_DOC_COMMENT="/""/""/"[^\r\n]*
MULTI_LINE_STYLE_COMMENT=("/*"[^"*"]{COMMENT_TAIL})|"/*"  //TODO doccomment may nest
COMMENT_TAIL=([^"*"]*("*"+[^"*""/"])?)*("*"+"/")?
DOC_COMMENT="/*""*"+("/"|([^"/""*"]{COMMENT_TAIL}))?  // TODO brackets

/*
    Raw strings + common
*/
RAW_SINGLE_QUOTED_STRING=(@|"r")({QUOTED_LITERAL} | {DOUBLE_QUOTED_LITERAL})
RAW_TRIPLE_QUOTED_STRING=(@|"r")({TRIPLE_QUOTED_LITERAL}|{TRIPLE_APOS_LITERAL})

QUOTED_LITERAL="'" ([^\\\'\r\n] | {ESCAPE_SEQUENCE} | (\\[\r\n]))* ("'"|\\)?
DOUBLE_QUOTED_LITERAL=\"([^\\\"\r\n]|{ESCAPE_SEQUENCE}|(\\[\r\n]))*?(\"|\\)?
ESCAPE_SEQUENCE=\\[^\r\n]

ANY_ESCAPE_SEQUENCE = \\[^]

THREE_QUO = (\"\"\")
ONE_TWO_QUO = (\"[^\"]) | (\"\\[^]) | (\"\"[^\"]) | (\"\"\\[^])
QUO_STRING_CHAR = [^\\\"] | {ANY_ESCAPE_SEQUENCE} | {ONE_TWO_QUO}
TRIPLE_QUOTED_LITERAL = {THREE_QUO} {QUO_STRING_CHAR}* {THREE_QUO}?

THREE_APOS = (\'\'\')
ONE_TWO_APOS = ('[^']) | ('\\[^]) | (''[^']) | (''\\[^])
APOS_STRING_CHAR = [^\\'] | {ANY_ESCAPE_SEQUENCE} | {ONE_TWO_APOS}
TRIPLE_APOS_LITERAL = {THREE_APOS} {APOS_STRING_CHAR}* {THREE_APOS}?

/*
    Strings with templates
*/

REGULAR_QUO_STRING_PART=[^\\\"\$]+
REGULAR_APOS_STRING_PART=[^\\\'\$]+
SHORT_TEMPLATE_ENTRY=\${IDENTIFIER_NO_DOLLAR}
LONELY_DOLLAR=\$
LONG_TEMPLATE_ENTRY_START=\$\{

IDENTIFIER_START_NO_DOLLAR={LETTER}|"_"
IDENTIFIER_START={IDENTIFIER_START_NO_DOLLAR}|"$"
IDENTIFIER_PART_NO_DOLLAR={IDENTIFIER_START_NO_DOLLAR}|{DIGIT}
IDENTIFIER_PART={IDENTIFIER_START}|{DIGIT}
IDENTIFIER={IDENTIFIER_START}{IDENTIFIER_PART}*
IDENTIFIER_NO_DOLLAR={IDENTIFIER_START_NO_DOLLAR}{IDENTIFIER_PART_NO_DOLLAR}*

INTEGER_LITERAL={DECIMAL_INTEGER_LITERAL}|{HEX_INTEGER_LITERAL}
DECIMAL_INTEGER_LITERAL=(0|([1-9]({DIGIT})*))
HEX_INTEGER_LITERAL=0[Xx]([0-9A-Fa-f])*

EXPONENT_PART=[Ee]["+""-"]?({DIGIT})*
FLOATING_POINT_LITERAL1=({DIGIT})+("."({DIGIT})+)?({EXPONENT_PART})?
FLOATING_POINT_LITERAL2="."({DIGIT})+({EXPONENT_PART})?
FLOAT_LITERAL=(({FLOATING_POINT_LITERAL1})|({FLOATING_POINT_LITERAL2}))

%%

<YYINITIAL> "{"                { return LBRACE; }
<YYINITIAL> "}"                { return RBRACE; }
<LONG_TEMPLATE_ENTRY> "{"              { lBraceCount++; return LBRACE; }
<LONG_TEMPLATE_ENTRY> "}"              {
                                           if (lBraceCount == 0) {
                                             popState();
                                             return LONG_TEMPLATE_ENTRY_END;
                                           }
                                           lBraceCount--;
                                           return RBRACE;
                                       }

<YYINITIAL, LONG_TEMPLATE_ENTRY> {WHITE_SPACE}                  { return WHITE_SPACE; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> {MULTI_LINE_STYLE_COMMENT}     { return MULTI_LINE_COMMENT; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> {DOC_COMMENT}                  { return DOC_COMMENT; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> {SINGLE_LINE_DOC_COMMENT}                  { return DOC_COMMENT; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> {SINGLE_LINE_COMMENT}          { return SINGLE_LINE_COMMENT; }

<YYINITIAL, LONG_TEMPLATE_ENTRY> "break"                { return BREAK; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "case"                 { return CASE; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "catch"                { return CATCH; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "const"                { return CONST; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "continue"             { return CONTINUE; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "default"              { return DEFAULT; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "do"                   { return DO; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "else"                 { return ELSE; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "false"                { return FALSE; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "finally"              { return FINALLY; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "final"                { return FINAL; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "for"                  { return FOR; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "if"                   { return IF; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "in"                   { return IN; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "new"                  { return NEW; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "null"                 { return NULL; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "return"               { return RETURN; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "super"                { return SUPER; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "switch"               { return SWITCH; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "this"                 { return THIS; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "throw"                { return THROW; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "true"                 { return TRUE; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "try"                  { return TRY; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "var"                  { return VAR; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "while"                { return WHILE; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "class"                { return CLASS; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "extends"              { return EXTENDS; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "with"                 { return WITH; }

<YYINITIAL, LONG_TEMPLATE_ENTRY> "abstract"             { return ABSTRACT; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "assert"               { return ASSERT; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "factory"              { return FACTORY; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "get"                  { return GET; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "implements"           { return IMPLEMENTS; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "interface"            { return INTERFACE; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "is"                   { return IS; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "as"                   { return AS; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "on"                   { return ON; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "of"                   { return OF; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "negate"               { return NEGATE; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "operator"             { return OPERATOR; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "set"                  { return SET; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "static"               { return STATIC; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "typedef"              { return TYPEDEF; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "native"               { return NATIVE; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "import"               { return IMPORT; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "export"               { return EXPORT; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "show"                 { return SHOW; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "hide"                 { return HIDE; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "part"                 { return PART; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "source"               { return SOURCE; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "library"              { return LIBRARY; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "resource"             { return RESOURCE; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "external"             { return EXTERNAL; }

<YYINITIAL, LONG_TEMPLATE_ENTRY> {IDENTIFIER}           { return IDENTIFIER; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "["                { return LBRACKET; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "]"                { return RBRACKET; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "("                { return LPAREN; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> ")"                { return RPAREN; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> ";"                { return SEMICOLON; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "-"                { return MINUS; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "-="               { return MINUS_EQ; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "--"               { return MINUS_MINUS; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "+"                { return PLUS; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "++"               { return PLUS_PLUS; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "+="               { return PLUS_EQ; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "/"                { return DIV; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "/="               { return DIV_EQ; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "*"                { return MUL; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "*="               { return MUL_EQ; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "~/"               { return INT_DIV; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "~/="              { return INT_DIV_EQ; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "%="               { return REM_EQ; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "%"                { return REM; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "~"                { return BIN_NOT; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "!"                { return NOT; }

<YYINITIAL, LONG_TEMPLATE_ENTRY> "=>"               { return EXPRESSION_BODY_DEF; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "="                { return EQ; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "=="               { return EQ_EQ; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "==="              { return EQ_EQ_EQ; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "!="               { return NEQ; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "!=="              { return NEQ_EQ; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "."                { return DOT; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> ","                { return COMMA; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> ":"                { return COLON; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> ">"                { return GT; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> ">="               { return GT_EQ; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> ">>="              { return GT_GT_EQ; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> ">>>="             { return GT_GT_GT_EQ; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "<"                { return LT; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "<="               { return LT_EQ; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "<<"               { return LT_LT; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "<<="              { return LT_LT_EQ; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "?"                { return QUEST; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "|"                { return OR; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "|="               { return OR_EQ; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "||"               { return OR_OR; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "^"                { return XOR; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "^="               { return XOR_EQ; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "&"                { return AND; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "&="               { return AND_EQ; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "&&"               { return AND_AND; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "@"                { return AT; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "#"                { return HASH; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> {RAW_TRIPLE_QUOTED_STRING} { return RAW_TRIPLE_QUOTED_STRING; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> {RAW_SINGLE_QUOTED_STRING} { return RAW_SINGLE_QUOTED_STRING; }

// String templates

// ", """

<YYINITIAL, LONG_TEMPLATE_ENTRY> {THREE_QUO}          { pushState(THREE_QUO_STRING); return OPEN_QUOTE; }
<THREE_QUO_STRING> \n                  { return REGULAR_STRING_PART; }
<THREE_QUO_STRING> \"                  { return REGULAR_STRING_PART; }
<THREE_QUO_STRING> \\                  { return REGULAR_STRING_PART; }
<THREE_QUO_STRING> {THREE_QUO}         { popState(); return CLOSING_QUOTE; }

<YYINITIAL, LONG_TEMPLATE_ENTRY> \"                          { pushState(QUO_STRING); return OPEN_QUOTE; }
<QUO_STRING> \"                 { popState(); return CLOSING_QUOTE; }
<QUO_STRING> {ESCAPE_SEQUENCE}  { return REGULAR_STRING_PART; }

<QUO_STRING, THREE_QUO_STRING> {REGULAR_QUO_STRING_PART}     { return REGULAR_STRING_PART; }
<QUO_STRING, THREE_QUO_STRING> {SHORT_TEMPLATE_ENTRY}        {
                                                                  pushState(SHORT_TEMPLATE_ENTRY);
                                                                  yypushback(yylength() - 1);
                                                                  return SHORT_TEMPLATE_ENTRY_START;
                                                             }

<QUO_STRING, THREE_QUO_STRING> {LONELY_DOLLAR}               { return REGULAR_STRING_PART; }
<QUO_STRING, THREE_QUO_STRING> {LONG_TEMPLATE_ENTRY_START}   { pushState(LONG_TEMPLATE_ENTRY); return LONG_TEMPLATE_ENTRY_START; }

// ', '''

<YYINITIAL, LONG_TEMPLATE_ENTRY> {THREE_APOS}          { pushState(THREE_APOS_STRING); return OPEN_QUOTE; }
<THREE_APOS_STRING> \n                  { return REGULAR_STRING_PART; }
<THREE_APOS_STRING> \'                  { return REGULAR_STRING_PART; }
<THREE_APOS_STRING> \\                  { return REGULAR_STRING_PART; }
<THREE_APOS_STRING> {THREE_APOS}        { popState(); return CLOSING_QUOTE; }

<YYINITIAL, LONG_TEMPLATE_ENTRY> \'     { pushState(APOS_STRING); return OPEN_QUOTE; }
<APOS_STRING> \'                 { popState(); return CLOSING_QUOTE; }
<APOS_STRING> {ESCAPE_SEQUENCE}  { return REGULAR_STRING_PART; }

<APOS_STRING, THREE_APOS_STRING> {REGULAR_APOS_STRING_PART}    { return REGULAR_STRING_PART; }
<APOS_STRING, THREE_APOS_STRING> {SHORT_TEMPLATE_ENTRY}        {
                                                                  pushState(SHORT_TEMPLATE_ENTRY);
                                                                  yypushback(yylength() - 1);
                                                                  return SHORT_TEMPLATE_ENTRY_START;
                                                             }

<APOS_STRING, THREE_APOS_STRING> {LONELY_DOLLAR}               { return REGULAR_STRING_PART; }
<APOS_STRING, THREE_APOS_STRING> {LONG_TEMPLATE_ENTRY_START}   { pushState(LONG_TEMPLATE_ENTRY); return LONG_TEMPLATE_ENTRY_START; }


// Only *this* keyword is itself an expression valid in this position
// *null*, *true* and *false* are also keywords and expression, but it does not make sense to put them
// in a string template for it'd be easier to just type them in without a dollar
<SHORT_TEMPLATE_ENTRY> "this"          { popState(); return THIS; }
<SHORT_TEMPLATE_ENTRY> {IDENTIFIER_NO_DOLLAR}    { popState(); return IDENTIFIER; }

<YYINITIAL, LONG_TEMPLATE_ENTRY> {INTEGER_LITERAL} | {FLOAT_LITERAL} { return NUMBER; }
.                              { yybegin(YYINITIAL); return BAD_CHARACTER; }
