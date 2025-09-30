/* It's an automatically generated code. Do not modify it. */
package org.jetbrains.astro.lang.lexer;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.*;
import com.intellij.psi.xml.*;
import com.intellij.lexer.FlexLexer;
import org.jetbrains.astro.lang.parser.AstroElementTypes;
import java.util.ArrayDeque;

%%

%unicode

%{
  public _AstroRawTextLexer() {
    this((java.io.Reader)null);
  }

  private ArrayDeque<Integer> braceStack = new ArrayDeque<>();
%}

%class _AstroRawTextLexer
%public
%implements FlexLexer
%function advance
%type IElementType

%state JS_EXPRESSION
%state JS_TEMPLATE_LITERAL

ALPHA=[:letter:]
DIGIT=[0-9]
WHITE_SPACE_CHARS=[ \n\r\t\f\u2028\u2029\u0085]+

TAG_NAME=({ALPHA}|"_"|":")({ALPHA}|{DIGIT}|"_"|":"|"."|"-")*

%%
<YYINITIAL> {

  {WHITE_SPACE_CHARS} { return XmlTokenType.XML_REAL_WHITE_SPACE; }

  "&lt;" |
  "&gt;" |
  "&apos;" |
  "&quot;" |
  "&nbsp;" |
  "&amp;" |
  "&#"{DIGIT}+";" |
  "&#"[xX]({DIGIT}|[a-fA-F])+";" { return XmlTokenType.XML_CHAR_ENTITY_REF; }
  "&"{TAG_NAME}";" { return XmlTokenType.XML_ENTITY_REF_TOKEN; }

  "{" {
        braceStack.clear();
        braceStack.push(YYINITIAL);
        yybegin(JS_EXPRESSION);
  }

  [^] { return XmlTokenType.XML_DATA_CHARACTERS; }
}
<JS_EXPRESSION> {
  "}" {
        int nextState = braceStack.pop();
        yybegin(nextState);
        if (nextState == YYINITIAL) {
          return AstroElementTypes.ASTRO_EMBEDDED_EXPRESSION;
        }
    }
  "{" {
        braceStack.push(JS_EXPRESSION);
  }
  "\"" ~[\n\r\"] {
  }

  "\'" ~[\n\r\'] {
  }
  "`" {
        yybegin(JS_TEMPLATE_LITERAL);
  }

  [^] { }
  <<EOF>> {
        return AstroElementTypes.ASTRO_EMBEDDED_EXPRESSION;
  }
}
<JS_TEMPLATE_LITERAL> {
  "`" {
        yybegin(JS_EXPRESSION);
  }
  "${" {
        braceStack.push(JS_TEMPLATE_LITERAL);
        yybegin(JS_EXPRESSION);
  }
  [^] { }
  <<EOF>> {
        return AstroElementTypes.ASTRO_EMBEDDED_EXPRESSION;
  }
}
