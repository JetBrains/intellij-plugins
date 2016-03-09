package org.angularjs.html;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import org.angularjs.lang.parser.AngularJSElementTypes;

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

<YYINITIAL> "{{"       { yybegin(INTERPOLATION); return myType; }
<INTERPOLATION> "}}"   { yybegin(YYINITIAL); return myType; }
<INTERPOLATION> .|"\n" { return AngularJSElementTypes.EMBEDDED_CONTENT; }
<YYINITIAL> .|"\n"     { return myType; }
