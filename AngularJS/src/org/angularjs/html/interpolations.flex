package org.angularjs.html;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlTokenType;

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

WS=[ \n\r\t\f]+

%%

<YYINITIAL>     "{{"            { yybegin(INTERPOLATION); return myType; }
<INTERPOLATION> "}}"            { yybegin(YYINITIAL); return myType; }
<INTERPOLATION> [^]             { return AngularJSElementTypes.EMBEDDED_CONTENT; }
<YYINITIAL>     {WS}            { return XmlTokenType.XML_REAL_WHITE_SPACE;}
<YYINITIAL>     [^]             { return myType; }
