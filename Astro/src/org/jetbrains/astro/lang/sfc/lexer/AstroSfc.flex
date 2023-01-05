package org.jetbrains.astro.lang.sfc.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.Nullable;

import static com.intellij.util.ArrayUtil.*;

%%

%unicode

%{
    private IElementType readReturnTokenType;
    private int readReturnState;
    private char[] readUntilBoundary;

    public _AstroLexer() {
      this((java.io.Reader)null);
    }

    private boolean inBuffer(@Nullable String text, int offset) {
      if (text == null) {
        return false;
      }
      int curPos = zzMarkedPos - 1 + offset;
      if (text.length() > zzBuffer.length() - curPos) {
        return false;
      }
      for (int i = 0; i < text.length(); i++) {
        if (zzBuffer.charAt(i + curPos) != text.charAt(i)) {
          return false;
        }
      }
      return true;
    }


    private void readUntil(boolean finishAtBoundary, char... chars) {
      if (zzMarkedPos == zzEndRead) return;
      char ch;
      do {
        ch = zzBuffer.charAt(zzMarkedPos++);
      } while (zzMarkedPos < zzEndRead
               && !contains(chars, ch)
               && !(finishAtBoundary && contains(readUntilBoundary, ch)));
    }

    private boolean contains(char[] chars, char ch) {
      for (int i = 0 ; i < chars.length; i++) {
        if (chars[i] == ch) return true;
      }
      return false;
    }

%}

%class _AstroLexer
%public
%implements FlexLexer
%function advance
%type IElementType
//%debug

%state FRONTMATTER_OPENED
%state FRONTMATTER_CLOSED
%state EXPRESSION

%state FRONTMATTER_OPEN
%state FRONTMATTER_CLOSE
%state READ_STRING
%state READ_COMMENT_OR_REGEXP
%state READ_MULTILINE_COMMENT
%state FINISH_READ

%state START_TAG_NAME
%state END_TAG_NAME


ALPHA=[:letter:]
DIGIT=[0-9]
WHITE_SPACE_CHARS=[ \n\r\t\f\u2028\u2029\u0085]+

TAG_NAME=({ALPHA}|"_"|":")({ALPHA}|{DIGIT}|"_"|":"|"."|"-")*
/* see http://www.w3.org/TR/html5/syntax.html#syntax-attribute-name */
ATTRIBUTE_NAME=([^ \n\r\t\f\"\'<>/=])+

DTD_REF= "\"" [^\"]* "\"" | "'" [^']* "'"
DOCTYPE= "<!" (D|d)(O|o)(C|c)(T|t)(Y|y)(P|p)(E|e)
HTML= (H|h)(T|t)(M|m)(L|l)
PUBLIC= (P|p)(U|u)(B|b)(L|l)(I|i)(C|c)
END_COMMENT="-->"

CONDITIONAL_COMMENT_CONDITION=({ALPHA})({ALPHA}|{WHITE_SPACE_CHARS}|{DIGIT}|"."|"("|")"|"|"|"!"|"&")*
%%

<YYINITIAL> {
  "---" {
        yypushback(3);
        yybegin(FRONTMATTER_OPEN);
        return XmlTokenType.XML_COMMENT_CHARACTERS;
      }
  [{}] {
        yybegin(FRONTMATTER_CLOSED);
        yypushback(1);
        return XmlTokenType.XML_DATA_CHARACTERS;
      }
  \<[a-zA-Z] {
        yybegin(FRONTMATTER_CLOSED);
        yypushback(2);
        return XmlTokenType.XML_DATA_CHARACTERS;
      }
}

<FRONTMATTER_OPENED> {
  "---" {
        yypushback(3);
        yybegin(FRONTMATTER_CLOSE);
        return XmlTokenType.XML_DATA_CHARACTERS;
      }
}

<YYINITIAL, FRONTMATTER_OPENED> {
  ['\"`] {
          if (yystate() == YYINITIAL) {
            readReturnTokenType = XmlTokenType.XML_DATA_CHARACTERS;
            readReturnState = FRONTMATTER_CLOSED;
          } else {
            readReturnTokenType = null;
            readReturnState = FRONTMATTER_OPENED;
          }
          yypushback(1);
          yybegin(READ_STRING);
        }
  "/" {
        if (yystate() == YYINITIAL) {
          readReturnTokenType = XmlTokenType.XML_DATA_CHARACTERS;
          readReturnState = FRONTMATTER_CLOSED;
        } else {
          readReturnTokenType = null;
          readReturnState = FRONTMATTER_OPENED;
        }
        readUntilBoundary = EMPTY_CHAR_ARRAY;
        yybegin(READ_COMMENT_OR_REGEXP);
      }
  <<EOF>> {
        yybegin(FRONTMATTER_CLOSED);
        return XmlTokenType.XML_DATA_CHARACTERS;
      }
  [^] {

      }
}

<FRONTMATTER_CLOSED> {
  [^]+ { return XmlTokenType.XML_BAD_CHARACTER; }
}

<FRONTMATTER_OPEN> {
  "---" {
          yybegin(FRONTMATTER_OPENED);
          return AstroTokenTypes.FRONTMATTER_SEPARATOR;
        }
  [^] {
        yypushback(1);
        yybegin(YYINITIAL);
      }
}

<FRONTMATTER_CLOSE> {
  "---" {
          yybegin(FRONTMATTER_CLOSED);
          return AstroTokenTypes.FRONTMATTER_SEPARATOR;
        }
  [^] {
        yypushback(1);
        yybegin(FRONTMATTER_OPENED);
      }
}


<READ_COMMENT_OR_REGEXP> {
  "/" {
        readUntil(false, '\r', '\n');
        yybegin(FINISH_READ);
      }
  "*" {
        yybegin(READ_MULTILINE_COMMENT);
      }
  [^] {
        readUntil(true, '/', '\r', '\n');
        yybegin(FINISH_READ);
      }
}

<READ_MULTILINE_COMMENT> {
  "*/" {
        yybegin(FINISH_READ);//comment
      }
  <<EOF>> {
        yybegin(FINISH_READ);
      }
  [^] {

      }
}

<READ_STRING> {
  "'" {
        readUntil(false, '\'', '\r', '\n');
        yybegin(FINISH_READ);
      }
  "\"" {
        readUntil(false, '"', '\r', '\n');
        yybegin(FINISH_READ);
      }
  "`" {
        readUntil(false, '`');
        yybegin(FINISH_READ);
      }
}

<FINISH_READ> {
  [^] {
        yypushback(1);
        yybegin(readReturnState);
        if (readReturnTokenType != null) {
          return readReturnTokenType;
        }
      }
  <<EOF>> {
        yybegin(readReturnState);
        if (readReturnTokenType != null) {
          return readReturnTokenType;
        }
      }
}

[^] { return XmlTokenType.XML_BAD_CHARACTER; }
