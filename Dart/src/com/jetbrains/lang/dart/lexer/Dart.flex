package com.jetbrains.lang.dart.lexer;

import java.util.*;
import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import static com.jetbrains.lang.dart.DartTokenTypes.*;
import static com.jetbrains.lang.dart.DartTokenTypesSets.*;
import static com.jetbrains.lang.dart.lexer.DartLexer.*;

@SuppressWarnings("DuplicateBranchesInSwitch")
%%
%{
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

  protected final Stack<State> myStateStack = new Stack<>();
  protected int myLeftBraceCount;

  private void pushState(int state) {
    myStateStack.push(new State(yystate(), myLeftBraceCount));
    myLeftBraceCount = 0;
    yybegin(state);
  }

  private void popState() {
    State state = myStateStack.pop();
    myLeftBraceCount = state.lBraceCount;
    yybegin(state.state);
  }

  _DartLexer() {
    this(null);
  }
%}

%class _DartLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{
  myLeftBraceCount = 0;
  myStateStack.clear();
%eof}

%xstate MULTI_LINE_COMMENT_STATE QUO_STRING THREE_QUO_STRING APOS_STRING THREE_APOS_STRING SHORT_TEMPLATE_ENTRY LONG_TEMPLATE_ENTRY

DIGIT=[0-9]
HEX_DIGIT=[0-9a-fA-F]
LETTER=[a-z]|[A-Z]
WHITE_SPACE=[ \n\t\f]+
PROGRAM_COMMENT="#""!"[^\n]*
SINGLE_LINE_COMMENT="/""/"[^\n]*
SINGLE_LINE_DOC_COMMENT="/""/""/"[^\n]*
SINGLE_LINE_COMMENTED_COMMENT="/""/""/""/"[^\n]*

MULTI_LINE_DEGENERATE_COMMENT = "/*" "*"+ "/"
MULTI_LINE_COMMENT_START      = "/*"
MULTI_LINE_DOC_COMMENT_START  = "/**"
MULTI_LINE_COMMENT_END        = "*/"

RAW_SINGLE_QUOTED_STRING= "r" ((\" ([^\"\n])* \"?) | ("'" ([^\'\n])* \'?))
RAW_TRIPLE_QUOTED_STRING= "r" ({RAW_TRIPLE_QUOTED_LITERAL} | {RAW_TRIPLE_APOS_LITERAL})

RAW_TRIPLE_QUOTED_LITERAL = {THREE_QUO}  ([^\"] | \"[^\"] | \"\"[^\"])* {THREE_QUO}?
RAW_TRIPLE_APOS_LITERAL   = {THREE_APOS} ([^\'] | \'[^\'] | \'\'[^\'])* {THREE_APOS}?

THREE_QUO =  (\"\"\")
THREE_APOS = (\'\'\')

SHORT_TEMPLATE_ENTRY=\${IDENTIFIER_NO_DOLLAR}
LONG_TEMPLATE_ENTRY_START=\$\{

IDENTIFIER_START_NO_DOLLAR={LETTER}|"_"
IDENTIFIER_START={IDENTIFIER_START_NO_DOLLAR}|"$"
IDENTIFIER_PART_NO_DOLLAR={IDENTIFIER_START_NO_DOLLAR}|{DIGIT}
IDENTIFIER_PART={IDENTIFIER_START}|{DIGIT}
IDENTIFIER={IDENTIFIER_START}{IDENTIFIER_PART}*
IDENTIFIER_NO_DOLLAR={IDENTIFIER_START_NO_DOLLAR}{IDENTIFIER_PART_NO_DOLLAR}*

NUMERIC_LITERAL = {NUMBER} | {HEX_NUMBER}
NUMBER = ({DIGIT}+ ("." {DIGIT}+)? {EXPONENT}?) | ("." {DIGIT}+ {EXPONENT}?)
EXPONENT = [Ee] ["+""-"]? {DIGIT}*
HEX_NUMBER = 0 [Xx] {HEX_DIGIT}*

%%

<YYINITIAL> "{"                { return LBRACE; }
<YYINITIAL> "}"                { return RBRACE; }
<LONG_TEMPLATE_ENTRY> "{"      { myLeftBraceCount++; return LBRACE; }
<LONG_TEMPLATE_ENTRY> "}"      {
                                   if (myLeftBraceCount == 0) {
                                     popState();
                                     return LONG_TEMPLATE_ENTRY_END;
                                   }
                                   myLeftBraceCount--;
                                   return RBRACE;
                               }

<YYINITIAL, LONG_TEMPLATE_ENTRY> {WHITE_SPACE}                   { return WHITE_SPACE;             }

// single-line comments
<YYINITIAL, LONG_TEMPLATE_ENTRY> {SINGLE_LINE_COMMENTED_COMMENT} { return SINGLE_LINE_COMMENT; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> {SINGLE_LINE_DOC_COMMENT}       { return SINGLE_LINE_DOC_COMMENT; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> {SINGLE_LINE_COMMENT}           { return SINGLE_LINE_COMMENT;     }
<YYINITIAL>                      {PROGRAM_COMMENT}               { return SINGLE_LINE_COMMENT;     }

// multi-line comments
<YYINITIAL, LONG_TEMPLATE_ENTRY> {MULTI_LINE_DEGENERATE_COMMENT} { return MULTI_LINE_COMMENT;      } // without this rule /*****/ is parsed as doc comment and /**/ is parsed as not closed doc comment

// next rules return temporary IElementType's that are rplaced with DartTokenTypesSets#MULTI_LINE_COMMENT or DartTokenTypesSets#MULTI_LINE_DOC_COMMENT in com.jetbrains.lang.dart.lexer.DartLexer
<YYINITIAL, LONG_TEMPLATE_ENTRY> {MULTI_LINE_DOC_COMMENT_START}  { pushState(MULTI_LINE_COMMENT_STATE); return MULTI_LINE_DOC_COMMENT_START;                                                             }
<YYINITIAL, LONG_TEMPLATE_ENTRY> {MULTI_LINE_COMMENT_START}      { pushState(MULTI_LINE_COMMENT_STATE); return MULTI_LINE_COMMENT_START;                                                                 }

<MULTI_LINE_COMMENT_STATE>       {MULTI_LINE_COMMENT_START}      { pushState(MULTI_LINE_COMMENT_STATE); return MULTI_LINE_COMMENT_BODY;                                                                  }
<MULTI_LINE_COMMENT_STATE>       [^]                             {                                      return MULTI_LINE_COMMENT_BODY;                                                                  }
<MULTI_LINE_COMMENT_STATE>       {MULTI_LINE_COMMENT_END}        { popState();                          return yystate() == MULTI_LINE_COMMENT_STATE
                                                                                                               ? MULTI_LINE_COMMENT_BODY // inner comment closed
                                                                                                               : MULTI_LINE_COMMENT_END; }

// reserved words
<YYINITIAL, LONG_TEMPLATE_ENTRY> "assert"               { return ASSERT; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "break"                { return BREAK; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "case"                 { return CASE; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "catch"                { return CATCH; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "class"                { return CLASS; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "const"                { return CONST; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "continue"             { return CONTINUE; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "default"              { return DEFAULT; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "do"                   { return DO; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "else"                 { return ELSE; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "enum"                 { return ENUM; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "extends"              { return EXTENDS; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "false"                { return FALSE; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "final"                { return FINAL; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "finally"              { return FINALLY; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "for"                  { return FOR; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "if"                   { return IF; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "in"                   { return IN; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "is"                   { return IS; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "new"                  { return NEW; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "null"                 { return NULL; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "rethrow"              { return RETHROW; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "return"               { return RETURN; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "super"                { return SUPER; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "switch"               { return SWITCH; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "this"                 { return THIS; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "throw"                { return THROW; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "true"                 { return TRUE; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "try"                  { return TRY; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "var"                  { return VAR; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "void"                 { return VOID; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "while"                { return WHILE; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "with"                 { return WITH; }


// BUILT_IN_IDENTIFIER (can be used as normal identifiers)
<YYINITIAL, LONG_TEMPLATE_ENTRY> "abstract"             { return ABSTRACT; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "as"                   { return AS; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "covariant"            { return COVARIANT; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "deferred"             { return DEFERRED; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "export"               { return EXPORT; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "external"             { return EXTERNAL; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "factory"              { return FACTORY; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "get"                  { return GET; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "implements"           { return IMPLEMENTS; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "import"               { return IMPORT; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "library"              { return LIBRARY; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "mixin"                { return MIXIN; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "operator"             { return OPERATOR; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "part"                 { return PART; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "set"                  { return SET; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "static"               { return STATIC; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "typedef"              { return TYPEDEF; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "sync"                 { return SYNC; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "async"                { return ASYNC; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "await"                { return AWAIT; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "yield"                { return YIELD; }

// next are not listed in spec, but they seem to have the same sense as BUILT_IN_IDENTIFIER: somewhere treated as keywords, but can be used as normal identifiers
<YYINITIAL, LONG_TEMPLATE_ENTRY> "on"                   { return ON; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "of"                   { return OF; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "native"               { return NATIVE; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "show"                 { return SHOW; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "hide"                 { return HIDE; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "late"                 { return LATE; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "required"             { return REQUIRED; }

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
<YYINITIAL, LONG_TEMPLATE_ENTRY> "!="               { return NEQ; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "."                { return DOT; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> ".."               { return DOT_DOT; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "?.."              { return QUEST_DOT_DOT; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "..."              { return DOT_DOT_DOT; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "...?"             { return DOT_DOT_DOT_QUEST; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> ","                { return COMMA; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> ":"                { return COLON; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> ">"                { return GT; }
//<YYINITIAL, LONG_TEMPLATE_ENTRY> ">="               { return GT_EQ;    } breaks mixin app parsing
//<YYINITIAL, LONG_TEMPLATE_ENTRY> ">>"               { return GT_GT;    } breaks generics parsing
//<YYINITIAL, LONG_TEMPLATE_ENTRY> ">>="              { return GT_GT_EQ; } breaks mixin app parsing
<YYINITIAL, LONG_TEMPLATE_ENTRY> "<"                { return LT; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "<="               { return LT_EQ; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "<<"               { return LT_LT; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "<<="              { return LT_LT_EQ; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "?"                { return QUEST; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "?."               { return QUEST_DOT; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "??"               { return QUEST_QUEST; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "??="              { return QUEST_QUEST_EQ; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "|"                { return OR; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "|="               { return OR_EQ; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "||"               { return OR_OR; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "||="              { return OR_OR_EQ; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "^"                { return XOR; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "^="               { return XOR_EQ; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "&"                { return AND; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "&="               { return AND_EQ; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "&&"               { return AND_AND; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "&&="              { return AND_AND_EQ; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "@"                { return AT; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> "#"                { return HASH; }

<YYINITIAL, LONG_TEMPLATE_ENTRY> {NUMERIC_LITERAL} { return NUMBER; }

// raw strings
<YYINITIAL, LONG_TEMPLATE_ENTRY> {RAW_TRIPLE_QUOTED_STRING} { return RAW_TRIPLE_QUOTED_STRING; }
<YYINITIAL, LONG_TEMPLATE_ENTRY> {RAW_SINGLE_QUOTED_STRING} { return RAW_SINGLE_QUOTED_STRING; }

// string start
<YYINITIAL, LONG_TEMPLATE_ENTRY>      \"                  { pushState(QUO_STRING);        return OPEN_QUOTE;    }
<YYINITIAL, LONG_TEMPLATE_ENTRY>      \'                  { pushState(APOS_STRING);       return OPEN_QUOTE;    }
<YYINITIAL, LONG_TEMPLATE_ENTRY>      {THREE_QUO}         { pushState(THREE_QUO_STRING);  return OPEN_QUOTE;    }
<YYINITIAL, LONG_TEMPLATE_ENTRY>      {THREE_APOS}        { pushState(THREE_APOS_STRING); return OPEN_QUOTE;    }
// correct string end
<QUO_STRING>                          \"                  { popState();                   return CLOSING_QUOTE; }
<APOS_STRING>                         \'                  { popState();                   return CLOSING_QUOTE; }
<THREE_QUO_STRING>                    {THREE_QUO}         { popState();                   return CLOSING_QUOTE; }
<THREE_APOS_STRING>                   {THREE_APOS}        { popState();                   return CLOSING_QUOTE; }
<QUO_STRING, APOS_STRING>             \n                  { popState();                   return WHITE_SPACE;   } // not closed single-line string literal. Do not return BAD_CHARACTER here because red highlighting of bad \n looks awful
// string content
<QUO_STRING>                          ([^\\\"\n\$] | (\\ [^\n]))*   {                return REGULAR_STRING_PART; }
<APOS_STRING>                         ([^\\\'\n\$] | (\\ [^\n]))*   {                return REGULAR_STRING_PART; }
<THREE_QUO_STRING>                    ([^\\\"\$])*                  {                return REGULAR_STRING_PART; }
<THREE_QUO_STRING>                    (\"[^\"]) | (\"\"[^\"])       { yypushback(1); return REGULAR_STRING_PART; } // pushback because we could capture '\' that escapes something
<THREE_APOS_STRING>                   ([^\\\'\$])*                  {                return REGULAR_STRING_PART; }
<THREE_APOS_STRING>                   (\'[^\']) | (\'\'[^\'])       { yypushback(1); return REGULAR_STRING_PART; } // pushback because we could capture '\' that escapes something
<THREE_QUO_STRING, THREE_APOS_STRING> (\\[^])                       {                return REGULAR_STRING_PART; } // escape sequence
// bad string interpolation (no identifier after '$')
<QUO_STRING, APOS_STRING, THREE_QUO_STRING, THREE_APOS_STRING> \$   { return SHORT_TEMPLATE_ENTRY_START; }
// short string interpolation
<QUO_STRING, APOS_STRING, THREE_QUO_STRING, THREE_APOS_STRING> {SHORT_TEMPLATE_ENTRY}      { pushState(SHORT_TEMPLATE_ENTRY);
                                                                                             yypushback(yylength() - 1);
                                                                                             return SHORT_TEMPLATE_ENTRY_START;}
// long string interpolation
<QUO_STRING, APOS_STRING, THREE_QUO_STRING, THREE_APOS_STRING> {LONG_TEMPLATE_ENTRY_START} { pushState(LONG_TEMPLATE_ENTRY);
                                                                                             return LONG_TEMPLATE_ENTRY_START; }
// Only *this* keyword is itself an expression valid in this position
// *null*, *true* and *false* are also keywords and expression, but it does not make sense to put them
// in a string template for it'd be easier to just type them in without a dollar
<SHORT_TEMPLATE_ENTRY> "this"          { popState(); return THIS; }
<SHORT_TEMPLATE_ENTRY> {IDENTIFIER_NO_DOLLAR}    { popState(); return IDENTIFIER; }

<YYINITIAL, MULTI_LINE_COMMENT_STATE, QUO_STRING, THREE_QUO_STRING, APOS_STRING, THREE_APOS_STRING, SHORT_TEMPLATE_ENTRY, LONG_TEMPLATE_ENTRY> [^] { return BAD_CHARACTER; }
