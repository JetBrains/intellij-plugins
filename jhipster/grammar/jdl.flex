package com.intellij.jhipster.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.intellij.jhipster.psi.JdlTokenTypes.*;

import it.unimi.dsi.fastutil.ints.IntArrayList;
%%

%{
  public _JdlLexer() {
    this((java.io.Reader)null);
  }

  private final IntArrayList myStateStack = new IntArrayList();

  protected void resetInternal() {
    myStateStack.clear();
  }

  private void pushState(int newState) {
    myStateStack.add(yystate());
    yybegin(newState);
  }

  private void popState() {
    if (myStateStack.isEmpty()) return;

    int state = myStateStack.removeInt(myStateStack.size() - 1);
    yybegin(state);
  }
%}

%public
%class _JdlLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

IDENTIFIER=([:letter:]|[_])([:letter:]|[._\-0-9])*

ESCAPE_SEQUENCE=\\[^\r\n]
DOUBLE_QUOTED_STRING=\"([^\\\"\r\n]|{ESCAPE_SEQUENCE})*(\"|\\)?

ESCAPED_SLASH=\\\/
REGEX_STRING=\/([^/]|{ESCAPED_SLASH})*(\/)?[a-zA-Z]*

INTEGER_LITERAL=0|[1-9][0-9]*

DIGIT=[0-9]
DOUBLE_LITERAL=(({FLOATING_POINT_LITERAL1})|({FLOATING_POINT_LITERAL2})|({FLOATING_POINT_LITERAL3}))
FLOATING_POINT_LITERAL1=({DIGIT})+"."({DIGIT})*({EXPONENT_PART})?
FLOATING_POINT_LITERAL2="."({DIGIT})+({EXPONENT_PART})?
FLOATING_POINT_LITERAL3=({DIGIT})+({EXPONENT_PART})
EXPONENT_PART=[Ee]["+""-"]?({DIGIT})*

LINE_COMMENT="//".*
BLOCK_COMMENT="/"\*([^*]|\*+[^*/])*(\*+"/")?

WHITE_SPACE = [\t ]+
NEWLINE = \r\n|[\r\n]

%eof{
    resetInternal();
%eof}

%state BRACES_BODY

%%

// hard keywords
<YYINITIAL, BRACES_BODY> {
    "true"                               { return TRUE; }
    "false"                              { return FALSE; }

    "with"                               { return WITH_KEYWORD; }
    "except"                             { return EXCEPT_KEYWORD; }
    "to"                                 { return TO_KEYWORD; }
    "use"                                { return USE_KEYWORD; }
    "for"                                { return FOR_KEYWORD; }
}

<BRACES_BODY> {
    {IDENTIFIER}                         { return IDENTIFIER; }
}

// soft keywords
<YYINITIAL> {
    "application"                        { return APPLICATION_KEYWORD; }
    "entity"                             { return ENTITY_KEYWORD; }
    "enum"                               { return ENUM_KEYWORD; }
    "deployment"                         { return DEPLOYMENT_KEYWORD; }
    "relationship"                       { return RELATIONSHIP_KEYWORD; }
}

<YYINITIAL, BRACES_BODY> {
    ","                                  { return COMMA; }
    ":"                                  { return COLON; }
    "*"                                  { return WILDCARD; }
    "["                                  { return LBRACKET; }
    "]"                                  { return RBRACKET; }
    "("                                  { return LPARENTH; }
    ")"                                  { return RPARENTH; }
    "{"                                  { pushState(BRACES_BODY); return LBRACE; }
    "}"                                  { popState(); return RBRACE; }
    "@"                                  { return STRUDEL; }
    "="                                  { return ASSIGN; }

    {IDENTIFIER}                         { return IDENTIFIER; }
    {INTEGER_LITERAL}                    { return INTEGER_NUMBER; }
    {DOUBLE_LITERAL}                     { return DOUBLE_NUMBER; }
    {DOUBLE_QUOTED_STRING}               { return DOUBLE_QUOTED_STRING; }
    {LINE_COMMENT}                       { return LINE_COMMENT; }
    {BLOCK_COMMENT}                      { return BLOCK_COMMENT; }
    {NEWLINE}                            { return NEWLINE; }
    {WHITE_SPACE}                        { return WHITE_SPACE; }
    {REGEX_STRING}                       { return REGEX_STRING; }
}

[^] {
    if (myStateStack.isEmpty()) {
      return BAD_CHARACTER;
    }

    yypushback(yylength());
    popState();
}