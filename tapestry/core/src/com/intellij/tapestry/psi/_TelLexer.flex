/* It's an automatically generated code. Do not modify it. */
package com.intellij.tapestry.psi;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.lexer.FlexLexer;
import static com.intellij.tapestry.psi.TelTokenTypes.*;

%%

%{
   public _TelLexer() {
     this((java.io.Reader)null);
   }
 
   private void resetAll() {
   }
%}

%unicode
%class _TelLexer
%implements FlexLexer
%function advance
%type IElementType

IDENT=[:jletter:]([:jletterdigit:]*|-[:jletterdigit:]+)*
INTEGER={SIGN}[:digit:]+
DECIMAL={SIGN}{INTEGER}?\.{INTEGER}
SIGN=[+-]?
WS=(\t|\f|\n|\r|\ )+

%state TEL

%%

<TEL> {
	\}                     { yybegin(YYINITIAL); return TAP5_EL_END; }
	[Ff][Aa][Ll][Ss][Ee]   { return TAP5_EL_BOOLEAN; }
	[Tt][Rr][Uu][Ee]       { return TAP5_EL_BOOLEAN; }
	[Nn][Uu][Ll][Ll]       { return TAP5_EL_NULL; }
	{IDENT}                { return TAP5_EL_IDENTIFIER; }
	\:                     { return TAP5_EL_COLON; }
	\.                     { return TAP5_EL_DOT; }
	\?\.                   { return TAP5_EL_QUESTION_DOT; }
	\.\.                   { return TAP5_EL_RANGE; }
	\,                     { return TAP5_EL_COMMA; }
	\!                     { return TAP5_EL_EXCLAMATION; }
	\(                     { return TAP5_EL_LEFT_PARENTH; }
	\)                     { return TAP5_EL_RIGHT_PARENTH; }
	\[                     { return TAP5_EL_LEFT_BRACKET; }
	\]                     { return TAP5_EL_RIGHT_BRACKET; }
	\'[^']*\'              { return TAP5_EL_STRING; }
	{INTEGER}              { return TAP5_EL_INTEGER; }
	{DECIMAL}              { return TAP5_EL_DECIMAL; }
	{WS}                   { return TokenType.WHITE_SPACE; }
	[^}]                   { return TAP5_EL_BAD_CHAR; }
}

<YYINITIAL> {
	\$\{          { yybegin(TEL); return TAP5_EL_START; }
	[^]           { return TokenType.BAD_CHARACTER; }
}

