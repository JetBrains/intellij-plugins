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

WHITE_SPACE_CHARS=[ \n\r\t\f]+

%%

<YYINITIAL> "{{"                { yybegin(INTERPOLATION); return myType; }
<INTERPOLATION> "}}"            { yybegin(YYINITIAL); return myType; }
<INTERPOLATION> .|"\n"          { return AngularJSElementTypes.EMBEDDED_CONTENT; }
<YYINITIAL> {WHITE_SPACE_CHARS} { return XmlTokenType.XML_REAL_WHITE_SPACE;}
<YYINITIAL> .|"\n"              { return myType; }
