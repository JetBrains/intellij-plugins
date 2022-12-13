package org.intellij.prisma.lang.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.openapi.util.text.StringUtil;
import static com.intellij.psi.TokenType.*;

import static org.intellij.prisma.lang.psi.PrismaElementTypes.*;
import static org.intellij.prisma.lang.parser.PrismaParserDefinition.*;

%%

%{
  public _PrismaLexer() {
    this((java.io.Reader)null);
  }

  private void handleNewLine() {
      if (yystate() == DECL && StringUtil.containsLineBreak(yytext())) {
          yybegin(YYINITIAL);
      }
  }
%}

%class _PrismaLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType

EOL_WS           = \n | \r | \r\n
LINE_WS          = [\ \t]
WHITE_SPACE_CHAR = {EOL_WS} | {LINE_WS}
WHITE_SPACE      = {WHITE_SPACE_CHAR}+
DIGIT            = [:digit:]

NAME_START       = [a-zA-Z]
NAME_BODY        = [a-zA-Z0-9\-_]
IDENTIFIER       = {NAME_START} ({NAME_BODY})*

STRING_LITERAL   = \"([^\\\"\r\n]|\\[^\r\n])*\"?
NUMERIC_LITERAL  = "-"? {DIGIT}+ ("." {DIGIT}+)?

DOC_COMMENT = "///" .*
LINE_COMMENT = "//" .*

%state DECL, BLOCK

%%

<YYINITIAL> {
    "model"            { yybegin(DECL); return MODEL; }
    "type"             { yybegin(DECL); return TYPE; }
    "enum"             { yybegin(DECL); return ENUM; }
    "generator"        { yybegin(DECL); return GENERATOR; }
    "datasource"       { yybegin(DECL); return DATASOURCE; }
}

"Unsupported"      { return UNSUPPORTED; }

"{"                { yybegin(BLOCK); return LBRACE; }
"}"                { yybegin(YYINITIAL); return RBRACE; }
"("                { return LPAREN; }
")"                { return RPAREN; }
"["                { return LBRACKET; }
"]"                { return RBRACKET; }
"="                { return EQ; }
"."                { return DOT; }
":"                { return COLON; }
"?"                { return QUEST; }
"!"                { return EXCL; }
"@"                { return AT; }
"@@"               { return ATAT; }
","                { return COMMA; }

{IDENTIFIER}       { return IDENTIFIER; }
{NUMERIC_LITERAL}  { return NUMERIC_LITERAL; }
{STRING_LITERAL}   { return STRING_LITERAL; }
{WHITE_SPACE}      { handleNewLine(); return WHITE_SPACE; }

{DOC_COMMENT}      { return DOC_COMMENT; }
{LINE_COMMENT}     { return LINE_COMMENT; }

[^]                { return BAD_CHARACTER; }