/* It's an automatically generated code. Do not modify it. */
package com.intellij.tapestry.psi;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.lexer.FlexLexer;

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
%eof{  return;
%eof}

IDENT=[:jletter:][:jletterdigit:]*

%%

{IDENT} { return TelElementTypes.TAP5_EL_IDENTIFIER; }
"${"    { return TelElementTypes.TAP5_EL_START; }
"}"     { return TelElementTypes.TAP5_EL_END; }
":"     { return TelElementTypes.TAP5_EL_COLON; }
"."     { return TelElementTypes.TAP5_EL_DOT; }

[^] { return TokenType.BAD_CHARACTER; }
