 /* It's an automatically generated code. Do not modify it. */
package com.intellij.plugins.drools.lang.lexer;

import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.*;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypeSets.*;
import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import it.unimi.dsi.fastutil.ints.IntArrayList;

%%
%{
  public _DroolsLexer() {
    this((java.io.Reader)null);
  }
  private final IntArrayList myStateStack = new IntArrayList();

  private void pushState(int newState) {
   myStateStack.add(yystate());
   yybegin(newState);
  }

  private void popState() {
   if (myStateStack.isEmpty()) return;
   int state = myStateStack.removeInt(myStateStack.size() - 1);
   yybegin(state);
  }

  protected void resetInternal() {
    myStateStack.clear();
  }
%}

%public
%class _DroolsLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

%state LHS_STATE
%state RHS_STATE
%state QUERY_STATE
%state FQN_STATE

WHITE_SPACE_CHAR=[\ \n\r\t\f]
SINGLE_LINE_COMMENT="/""/"[^\r\n]*
SINGLE_LINE_COMMENT_DEPR="#"[^\r\n]*
MULTI_LINE_COMMENT=("/*"{COMMENT_TAIL})|"/*"
COMMENT_TAIL=([^"*"]*("*"+[^"*""/"])?)*("*"+"/")?
END_OF_LINE_COMMENT="/""/"[^\r\n]*

NEW_LINE = \r | \n | \r\n

LETTER = [:letter:]
DIGIT = [:digit:]
ESCAPE_SEQUENCE=\\[^\r\n]

JAVA_IDENTIFIER=("$" | "_")? {LETTER} ({DIGIT} | "_" | {LETTER})*
STRING_IDENTIFIER=  ({DIGIT}| {LETTER})*

HEX_DIGIT = [0-9A-Fa-f]
INT_DIGIT = [0-9]
OCT_DIGIT = [0-7]

NUM_INT = "0" | ([1-9] {INT_DIGIT}*)
NUM_HEX = ("0x" | "0X") {HEX_DIGIT}+
NUM_OCT = "0" {OCT_DIGIT}+

FLOAT_EXPONENT = [eE] [+-]? {DIGIT}+
NUM_FLOAT = ( (({DIGIT}* "." {DIGIT}+) | ({DIGIT}+ "." {DIGIT}*)) {FLOAT_EXPONENT}?) | ({DIGIT}+ {FLOAT_EXPONENT})

CHARACTER_LITERAL="'"([^\\\'\r\n]|{ESCAPE_SEQUENCE})*("'"|\\)?
STRING_LITERAL=\"([^\\\"\r\n]|{ESCAPE_SEQUENCE})*(\"|\\)?

%eof{
  resetInternal();
%eof}

%%
<YYINITIAL> "when" {{ yybegin(LHS_STATE);  return WHEN;}}
<YYINITIAL> "query" {{ yybegin(QUERY_STATE);  return QUERY;}}

<LHS_STATE, QUERY_STATE> {
    "or"                          {  return      OR; }
    "and"                         {  return      AND; }
    "if"                          {  return      IF; }
    "do"                          {  return      DO; }
    "break"                       {  return      BREAK; }
    "then" {yybegin(YYINITIAL); return THEN; }
    "end" {yybegin(YYINITIAL); return END;}
}

<FQN_STATE> {
  "."                         { return DOT; }
  {JAVA_IDENTIFIER}           {  return JAVA_IDENTIFIER ; }
}

<YYINITIAL, LHS_STATE, QUERY_STATE> {
{WHITE_SPACE_CHAR}+                       { return com.intellij.psi.TokenType.WHITE_SPACE;}

"package"                     {  return      PACKAGE; }
"null"                        {  return     NULL ;  }
"false"                       {  return     FALSE ;  }
"true"                        {  return     TRUE ;  }
"lock-on-active"              {  return     LOCK_ON_ACTIVE; }
"date-effective"              {  return     DATE_EFFECTIVE; }
"date-expires"                {  return      DATE_EXPIRES; }
"no-loop"                     {  return      NO_LOOP; }
"auto-focus"                  {  return      AUTO_FOCUS; }
"activation-group"            {  return      ACTIVATION_GROUP; }
"agenda-group"                {  return      AGENDA_GROUP; }
"ruleflow-group"              {  return      RULEFLOW_GROUP; }
"entry-point"                 {  return      ENTRY_POINT; }
"duration"                    {  return      DURATION; }
"timer"                       {  return      TIMER; }
"calendars"                   {  return      CALENDARS; }
"import"                      {  return      IMPORT; }
"dialect"                     {  return      DIALECT; }
"salience"                    {  return      SALIENCE; }
"enabled"                     {  return      ENABLED; }
"attributes"                  {  return      ATTRIBUTES; }
"rule"                        {  return      RULE; }
"extends"                     {  return      EXTENDS; }
"then"                        {  return      THEN; }
"template"                    {  return      TEMPLATE; }
"declare"                     {  return      DECLARE; }
"function"                    {  return      FUNCTION; }
"global"                      {  return      GLOBAL; }
"eval"                        {  return      EVAL; }
"not"                         {  return      NOT; }
"in"                          {  return      IN; }
"exists"                      {  return      EXISTS; }
"forall"                      {  return      FORALL; }
"accumulate"                  {  return      ACCUMULATE; }
"collect"                     {  return      COLLECT; }
"from"                        {  return      FROM; }
"action"                      {  return      ACTION; }
"reverse"                     {  return      REVERSE; }
"result"                      {  return      RESULT; }
"end"                         {  return      END; }
"over"                        {  return      OVER; }
"init"                        {  return      INIT; }
"unit"                        {  return      UNIT; }
"modify"                      {  return      MODIFY; }
"update"                      {  return      UPDATE; }
"enum"                        {  return      ENUM; }
"retract"                     {  return      RETRACT; }
"insert"                      {  return      INSERT; }
"insertLogical"               {  return      INSERT_LOGICAL; }
"contains"                    {  return      CONTAINS; }
"memberOf"                    {  return      MEMBEROF; }
"matches"                     {  return      MATCHES; }
"soundslike"                  {  return      SOUNDSLIKE; }
"isA"                         {  return      IS_A; }
"window"                      {  return      WINDOW; }

"this"                        {  return      THIS; }

"{"                           {  return       LBRACE; }
"}"                           {  return       RBRACE; }
"["                           {  return       LBRACKET; }
"]"                           {  return       RBRACKET; }
"("                           {  return       LPAREN; }
")"                           {  return       RPAREN; }

";"                           {  return       SEMICOLON; }
":"                           {  return       COLON; }
","                           {  return       COMMA; }
"?"                           {  return       QUEST; }

"+"                           {  return       OP_PLUS; }
"-"                           {  return       OP_MINUS; }
"*"                           {  return       OP_MUL; }
"/"                           {  return       OP_DIV; }
"%"                           {  return       OP_REMAINDER; }
"@"                           {  return       OP_AT; }

"="                           {  return       OP_ASSIGN; }
"+="                          {  return       OP_PLUS_ASSIGN; }
"-="                          {  return       OP_MINUS_ASSIGN; }
"*="                          {  return       OP_MUL_ASSIGN; }
"/="                          {  return       OP_DIV_ASSIGN; }
"&="                          {  return       OP_BIT_AND_ASSIGN; }
"|="                          {  return       OP_BIT_OR_ASSIGN; }
"!."                          {  return       NULL_DOT; }
"^="                          {  return       OP_BIT_XOR_ASSIGN; }
"%="                          {  return       OP_REMAINDER_ASSIGN; }
"<<="                         {  return       OP_SL_ASSIGN; }
">>="                         {  return       OP_SR_ASSIGN; }
">>>="                        {  return       OP_BSR_ASSIGN; }
"=="                          {  return       OP_EQ; }
"!="                          {  return       OP_NOT_EQ; }
"!"                           {  return       OP_NOT; }
"~"                           {  return       OP_COMPLEMENT; }
"++"                          {  return       OP_PLUS_PLUS; }
"--"                          {  return       OP_MINUS_MINUS; }

"||"                          {  return       OP_COND_OR; }
"&&"                          {  return       OP_COND_AND; }
"|"                           {  return       OP_BIT_OR; }
"&"                           {  return       OP_BIT_AND; }
"^"                           {  return       OP_BIT_XOR; }

"<"                           {  return       OP_LESS; }
"<="                          {  return       OP_LESS_OR_EQUAL; }
">"                           {  return       OP_GREATER; }
">="                          {  return       OP_GREATER_OR_EQUAL; }

"boolean"                     {  return       BOOLEAN; }
"char"                        {  return       CHAR; }
"byte"                        {  return       BYTE; }
"short"                       {  return       SHORT; }
"int"                         {  return       INT; }
"long"                        {  return       LONG; }
"float"                       {  return       FLOAT; }
"double"                      {  return       DOUBLE; }
"void"                        {  return       VOID; }
"."                            { return DOT; }

{JAVA_IDENTIFIER}             {  pushState(FQN_STATE); return JAVA_IDENTIFIER ; }

{SINGLE_LINE_COMMENT}         {  return     SINGLE_LINE_COMMENT ; }
{SINGLE_LINE_COMMENT_DEPR}    {  return     SINGLE_LINE_COMMENT_DEPR ; }
{MULTI_LINE_COMMENT}          {  return     MULTI_LINE_COMMENT ; }
{STRING_LITERAL}              {  return     STRING_TOKEN ; }
{CHARACTER_LITERAL}           {  return     CHARACTER_LITERAL; }
{NUM_INT} | {NUM_HEX}         {  return     INT_TOKEN ; }
{NUM_FLOAT}                   {  return     FLOAT_TOKEN ; }
{STRING_IDENTIFIER}           {  return     STRING_IDENTIFIER ; }
}

[^] {
        if (myStateStack.isEmpty()) {
          return TokenType.BAD_CHARACTER;
        }

        yypushback(yylength());
        popState();
    }