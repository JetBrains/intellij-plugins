package org.angularjs.html;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import com.intellij.lang.javascript.JSElementTypes;

%%

%unicode
//%debug
%class _AngularJSInterpolationsLexer
%implements FlexLexer
%type IElementType

%{
  private IElementType myType;
  void setType(IElementType type) {
    myType = type;
  }
%}

%function advance
%state INTERPOLATION

%%

<YYINITIAL> "{{"     { yybegin(INTERPOLATION); return myType; }
<INTERPOLATION> "}}" { yybegin(YYINITIAL); return myType; }
<INTERPOLATION> .    { return JSElementTypes.EMBEDDED_CONTENT; }
<YYINITIAL> .        { return myType; }