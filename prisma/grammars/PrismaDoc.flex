package org.intellij.prisma.lang.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.*;
import static org.intellij.prisma.lang.psi.PrismaDocTokenTypes.*;
%%

%{
  _PrismaDocLexer() {
    this(null);
  }
%}

%class _PrismaDocLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType

%xstate COMMENT_BODY
%xstate AFTER_CRLF
%xstate AFTER_ASTERISK

WHITE_SPACE_NO_CRLF=[\ \t\f]

%%

<YYINITIAL> {
  "/**"                                                             { yybegin(COMMENT_BODY); return DOC_COMMENT_START; }
  [^]                                                               { return BAD_CHARACTER;  /* can't happen */ }
}

<COMMENT_BODY> {
  "*/"                                                              { return zzMarkedPos == zzEndRead ? DOC_COMMENT_END : DOC_COMMENT_BODY; }
  {WHITE_SPACE_NO_CRLF}* (\n+ {WHITE_SPACE_NO_CRLF}*)+              { yybegin(AFTER_CRLF); return WHITE_SPACE; }
  .                                                                 { return DOC_COMMENT_BODY; }
}

<AFTER_CRLF> {
  "*/" | [^"*"]                                                     { yypushback(yylength()); yybegin(COMMENT_BODY); break; }
  "*"                                                               { yybegin(AFTER_ASTERISK); return DOC_COMMENT_LEADING_ASTERISK; }
}

<AFTER_ASTERISK> {
  {WHITE_SPACE_NO_CRLF}                                             { yybegin(COMMENT_BODY); return WHITE_SPACE; }
  [^]                                                               { yypushback(yylength()); yybegin(COMMENT_BODY); }
}
