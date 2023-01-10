package org.jetbrains.astro.lang.sfc.lexer;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlTokenType;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import com.intellij.util.containers.Stack;

import static com.intellij.util.ArrayUtil.*;

%%

%unicode

%{

    public _AstroSfcLexer() {
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

    private boolean nextIgnoringWhiteSpaceIs(@NotNull String text) {
      for (int i = zzCurrentPos + 1; i < zzEndRead; i++) {
        char cur = zzBuffer.charAt(i);
        if (Character.isWhitespace(cur))
          continue;
        return inBuffer(text, i - zzCurrentPos);
      }
      return false;
    }

    private boolean nextNonWhitespaceCharIs(char ch) {
      for (int i = zzCurrentPos + 1; i < zzEndRead; i++) {
        char cur = zzBuffer.charAt(i);
        if (Character.isWhitespace(cur))
          continue;
        return cur == ch;
      }
      return false;
    }

    private void readUntil(boolean finishAtBoundary, char... chars) {
      if (zzMarkedPos == zzEndRead) return;
      char ch;
      do {
        ch = zzBuffer.charAt(zzMarkedPos++);
        if (ch == '\\' && zzMarkedPos < zzEndRead) {
          zzMarkedPos++;
          continue;
        }
        if (finishAtBoundary && contains(readUntilBoundary, ch)) {
          zzMarkedPos--;
          return;
        }
      } while (zzMarkedPos < zzEndRead && !contains(chars, ch));
    }

    private boolean contains(char[] chars, char ch) {
      for (int i = 0 ; i < chars.length; i++) {
        if (chars[i] == ch) return true;
      }
      return false;
    }


    private IElementType readReturnTokenType;
    private int readReturnState;
    private IElementType readExprReturnTokenType;
    private int readExprReturnState;
    private char[] readUntilBoundary;

    private static int KIND_EXPRESSION = 0;
    private static int KIND_ATTRIBUTE_EXPRESSION = 1;
    private static int KIND_TEMPLATE_LITERAL = 2;

    private Stack<Integer> expressionStack = new Stack<>();

    private void readTagAttributeExpression(@NotNull IElementType returnTokenType, int nextState) {
      expressionStack.push(KIND_ATTRIBUTE_EXPRESSION);
      readExprReturnState = nextState;
      readExprReturnTokenType = returnTokenType;
      yybegin(READ_TAG_ATTR_EXPRESSION);
    }

    private void readString(@Nullable IElementType returnTokenType, int nextState) {
      yybegin(READ_STRING);
      readReturnState = nextState;
      readUntilBoundary = EMPTY_CHAR_ARRAY;
      readReturnTokenType = returnTokenType;
      yypushback(1);
    }

    private void readCommentOrRegExp(@Nullable IElementType returnTokenType, int nextState, char... regExpBoundary) {
      yybegin(READ_COMMENT_OR_REGEXP);
      readReturnState = nextState;
      readUntilBoundary = regExpBoundary;
      readReturnTokenType = returnTokenType;
    }

%}

%class _AstroSfcLexer
%public
%implements FlexLexer
%function advance
%type IElementType
//%debug

%state FRONTMATTER_OPEN
%state FRONTMATTER_OPENED
%state FRONTMATTER_CLOSE

%state HTML_INITIAL
%state DOC_TYPE
%state COMMENT
%state START_TAG_NAME
%state END_TAG_NAME
%state BEFORE_TAG_ATTRIBUTES
%state TAG_ATTRIBUTES
%state ATTRIBUTE_VALUE_START
%state ATTRIBUTE_VALUE_DQ
%state ATTRIBUTE_VALUE_SQ
%state PROCESSING_INSTRUCTION
%state TAG_CHARACTERS
%state C_COMMENT_START
%state C_COMMENT_END

%state TAG_ATTRIBUTES_POST_SHORTHAND

%state READ_STRING
%state READ_COMMENT_OR_REGEXP
%state READ_MULTILINE_COMMENT
%state READ_TAG_ATTR_EXPRESSION
%state FINISH_READ


ALPHA=[:letter:]
DIGIT=[0-9]
WHITE_SPACE_CHARS=[ \n\r\t\f\u2028\u2029\u0085]+


DTD_REF= "\"" [^\"]* "\"" | "'" [^']* "'"
DOCTYPE= "<!" (D|d)(O|o)(C|c)(T|t)(Y|y)(P|p)(E|e)
HTML= (H|h)(T|t)(M|m)(L|l)
PUBLIC= (P|p)(U|u)(B|b)(L|l)(I|i)(C|c)

CONDITIONAL_COMMENT_CONDITION=({ALPHA})({ALPHA}|{WHITE_SPACE_CHARS}|{DIGIT}|"."|"("|")"|"|"|"!"|"&")*
%%

// Frontmatter handling
<YYINITIAL> {
  "---" {
        yypushback(3);
        yybegin(FRONTMATTER_OPEN);
        return XmlTokenType.XML_COMMENT_CHARACTERS;
      }
  [{}] | \<[a-zA-Z] {
        yybegin(HTML_INITIAL);
        zzMarkedPos = 0;
      }
  ['\"`] {
        readString(XmlTokenType.XML_DATA_CHARACTERS, HTML_INITIAL);
      }
  "/" {
        readCommentOrRegExp(XmlTokenType.XML_DATA_CHARACTERS, HTML_INITIAL);
      }
  [^-{}<\"'`/]+|[-<] {
        // Just consume
      }
  <<EOF>> {
        yybegin(HTML_INITIAL);
        zzMarkedPos = 0;
      }
}

<FRONTMATTER_OPENED> {
  "---" {
          yypushback(3);
          yybegin(FRONTMATTER_CLOSE);
          return AstroSfcTokenTypes.FRONTMATTER_SCRIPT;
        }
  ['\"`] {
        readString(null, FRONTMATTER_OPENED);
      }
  "/" {
        readCommentOrRegExp(null, FRONTMATTER_OPENED);
      }
  [^-\"'`/]+|[-] {
        // Just consume
      }
  <<EOF>> {
          yybegin(HTML_INITIAL);
          return AstroSfcTokenTypes.FRONTMATTER_SCRIPT;
        }
}

<FRONTMATTER_OPEN> {
  "---" {
            yybegin(FRONTMATTER_OPENED);
            return AstroSfcTokenTypes.FRONTMATTER_SEPARATOR;
          }
  [^] {
        yypushback(1);
        yybegin(YYINITIAL);
      }
}

<FRONTMATTER_CLOSE> {
  "---" {
            yybegin(HTML_INITIAL);
            return AstroSfcTokenTypes.FRONTMATTER_SEPARATOR;
          }
  [^] {
        yypushback(1);
        yybegin(FRONTMATTER_OPENED);
      }
}

// Actual HTML code
<HTML_INITIAL> {
  "{" {
        expressionStack.push(KIND_EXPRESSION);
      }
  "/" {
        if (!expressionStack.isEmpty()) {
          readCommentOrRegExp(null, HTML_INITIAL, '{', '}', '\'', '"', '`');
        } else {
          return XmlTokenType.XML_DATA_CHARACTERS;
        }
      }
  [\"'`] {
        if (!expressionStack.isEmpty()) {
          readString(null, HTML_INITIAL);
        } else {
          return XmlTokenType.XML_DATA_CHARACTERS;
        }
      }
  "}" {
          if (expressionStack.isEmpty()) {
            return XmlTokenType.XML_DATA_CHARACTERS;
          }
          while (!expressionStack.isEmpty() && expressionStack.pop() != KIND_EXPRESSION) {
          }
          if (expressionStack.isEmpty()) {
            return AstroSfcTokenTypes.EXPRESSION;
          }
        }
  "<?" {
        yybegin(PROCESSING_INSTRUCTION);
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_PI_START;
      }
  {DOCTYPE} {
        yybegin(DOC_TYPE);
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_DOCTYPE_START;
      }
  "<!--" {
        yybegin(COMMENT);
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_COMMENT_START;
      }
  {WHITE_SPACE_CHARS} {
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_REAL_WHITE_SPACE;
      }
  \<[>a-zA-Z] {
        yybegin(START_TAG_NAME);
        yypushback(1);
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_START_TAG_START;
      }

  \<\/[>a-zA-Z] {
        yybegin(END_TAG_NAME);
        yypushback(1);
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_END_TAG_START;
      }

  "&"([a-zA-Z][a-zA-Z0-9]*)";"
  "&#"{DIGIT}+";" |
  "&#"(x|X)({DIGIT}|[a-fA-F])+";" {
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_CHAR_ENTITY_REF;
      }

  [^{}<&\'\"`/ \n\r\t\f\u2028\u2029\u0085]+|[&<] {
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_DATA_CHARACTERS;
      }

  <<EOF>> {
          if (!expressionStack.isEmpty()) {
            expressionStack.clear();
            return AstroSfcTokenTypes.EXPRESSION;
          }
          return yylength() > 0 ? XmlTokenType.XML_DATA_CHARACTERS : null;
        }
}

<START_TAG_NAME, END_TAG_NAME> {
  [^ \n\r\t\f/>]+ {
        yybegin(BEFORE_TAG_ATTRIBUTES);
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_NAME;
      }
}

<BEFORE_TAG_ATTRIBUTES>
  {WHITE_SPACE_CHARS} {
        yybegin(TAG_ATTRIBUTES);
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_WHITE_SPACE;
      }

<TAG_ATTRIBUTES> {
  "{" {
          readTagAttributeExpression(
             nextIgnoringWhiteSpaceIs("...") ? AstroSfcTokenTypes.SPREAD_ATTRIBUTE : AstroSfcTokenTypes.SHORTHAND_ATTRIBUTE,
             TAG_ATTRIBUTES_POST_SHORTHAND
          );
        }
  "=" {
        yybegin(ATTRIBUTE_VALUE_START);
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_EQ;
      }
  [^{ \n\r\t\f/=>]+ {
        if (!expressionStack.isEmpty()) continue;
        if (inBuffer("{", 1)) {
          // If attribute name contains '{' everything up to it is ignored.
          return XmlTokenType.XML_COMMENT_CHARACTERS;
        }
        return XmlTokenType.XML_NAME;
      }
  {WHITE_SPACE_CHARS} {
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_WHITE_SPACE;
      }
}

<TAG_ATTRIBUTES_POST_SHORTHAND> {
  [/=>] {
        yypushback(1);
        yybegin(TAG_ATTRIBUTES);
      }
  {WHITE_SPACE_CHARS} {
        yybegin(TAG_ATTRIBUTES);
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_WHITE_SPACE;
      }
  // This stuff is actually concatenated with the shorthand attribute contents,
  // but I think it's better to just show bad character here
  [^] {
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_BAD_CHARACTER;
      }
}

<ATTRIBUTE_VALUE_START> {
  ">" {
        yybegin(HTML_INITIAL);
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_TAG_END;
      }
  "'" {
        yybegin(ATTRIBUTE_VALUE_SQ);
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER;
      }
  "\"" {
        yybegin(ATTRIBUTE_VALUE_DQ);
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER;
      }
  "`"[^`]* {
          if (inBuffer("`", 1)) {
            zzMarkedPos++;
          }
          yybegin(TAG_ATTRIBUTES);
          if (!expressionStack.isEmpty()) continue;
          return AstroSfcTokenTypes.TEMPLATE_LITERAL_ATTRIBUTE;
        }
  "{" {
          readTagAttributeExpression(AstroSfcTokenTypes.EXPRESSION_ATTRIBUTE, TAG_ATTRIBUTES);
        }
  [^ \n\r\t\f'\">`{]([^ \n\r\t\f\>]|(\/[^\>]))* {
        yybegin(TAG_ATTRIBUTES);
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN;
      }
  <<EOF>> {
          yybegin(HTML_INITIAL);
          if (!expressionStack.isEmpty()) {
            expressionStack.clear();
            return AstroSfcTokenTypes.EXPRESSION;
          }
          return XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN;
        }
}

<START_TAG_NAME, END_TAG_NAME, BEFORE_TAG_ATTRIBUTES, TAG_ATTRIBUTES> {
  "/>" {
        yybegin(HTML_INITIAL);
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_EMPTY_ELEMENT_END;
      }
  ">" {
        yybegin(HTML_INITIAL);
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_TAG_END;
      }
  [^] {
        yybegin(HTML_INITIAL);
        yypushback(1);
      }
}

<PROCESSING_INSTRUCTION> {
  "?"? ">" {
        yybegin(HTML_INITIAL);
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_PI_END;
      }
  ([^\?\>] | (\?[^\>]))+ {
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_PI_TARGET;
      }
}

<DOC_TYPE> {
  {HTML} { return XmlTokenType.XML_NAME; }
  {PUBLIC} { return XmlTokenType.XML_DOCTYPE_PUBLIC; }
  {DTD_REF} { return XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN;}
  ">" {
        yybegin(HTML_INITIAL);
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_DOCTYPE_END;
      }
}

<COMMENT> {
  "[" {
        yybegin(C_COMMENT_START);
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_CONDITIONAL_COMMENT_START;
      }
  "<![" {
        yybegin(C_COMMENT_END);
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_CONDITIONAL_COMMENT_END_START;
      }
  "-->" | "<!-->" {
        yybegin(HTML_INITIAL);
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_COMMENT_END;
      }
  "<!--" {
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_BAD_CHARACTER;
      }
  "<!--->" | "--!>" {
        yybegin(HTML_INITIAL);
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_BAD_CHARACTER;
      }
  ">" {
    // according to HTML spec (http://www.w3.org/html/wg/drafts/html/master/syntax.html#comments)
    // comments should start with <!-- and end with -->. The comment <!--> is not valid, but should terminate
    // comment token. Please note that it's not true for XML (http://www.w3.org/TR/REC-xml/#sec-comments)
    int loc = getTokenStart();
    char prev = zzBuffer.charAt(loc - 1);
    char prevPrev = zzBuffer.charAt(loc - 2);
    if (prev == '-' && prevPrev == '-') {
      yybegin(HTML_INITIAL);
      if (!expressionStack.isEmpty()) continue;
      return XmlTokenType.XML_BAD_CHARACTER;
    }
    if (!expressionStack.isEmpty()) continue;
    return XmlTokenType.XML_COMMENT_CHARACTERS;
  }
  [^\[\-<>]+|[<] {
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_COMMENT_CHARACTERS;
      }
  <<EOF>> {
          yybegin(HTML_INITIAL);
          if (!expressionStack.isEmpty()) {
            expressionStack.clear();
            return AstroSfcTokenTypes.EXPRESSION;
          }
          return XmlTokenType.XML_COMMENT_CHARACTERS;
        }
}

<C_COMMENT_START> {
  "]>" {
        yybegin(COMMENT);
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_CONDITIONAL_COMMENT_START_END;
      }
  [^] {
        yybegin(COMMENT);
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_COMMENT_CHARACTERS;
      }
}

<C_COMMENT_END> {
  "]" {
        yybegin(COMMENT);
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_CONDITIONAL_COMMENT_END;
      }
  [^] {
        yybegin(COMMENT);
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_COMMENT_CHARACTERS;
      }
}

<C_COMMENT_START,C_COMMENT_END> {
  {CONDITIONAL_COMMENT_CONDITION} {
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_COMMENT_CHARACTERS;
      }
  "-->" {
        yybegin(HTML_INITIAL);
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_COMMENT_END;
      }
}

<ATTRIBUTE_VALUE_DQ> {
  "\"" {
        yybegin(TAG_ATTRIBUTES);
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER;
      }
  [^\"] {
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN;
      }
}

<ATTRIBUTE_VALUE_SQ> {
  "'" {
        yybegin(TAG_ATTRIBUTES);
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER;
      }
  [^'] {
        if (!expressionStack.isEmpty()) continue;
        return XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN;
      }
}

<READ_TAG_ATTR_EXPRESSION> {
  "`" {
        if (expressionStack.peek() == KIND_TEMPLATE_LITERAL) {
          expressionStack.pop();
        } else {
          expressionStack.push(KIND_TEMPLATE_LITERAL);
        }
      }
  "/" {
        if (expressionStack.peek() != KIND_TEMPLATE_LITERAL) {
          readCommentOrRegExp(null, READ_TAG_ATTR_EXPRESSION, '}');
        }
      }
  [\"'] {
        if (expressionStack.peek() != KIND_TEMPLATE_LITERAL) {
          readString(null, READ_TAG_ATTR_EXPRESSION);
        }
      }
  "{" {
        if (expressionStack.peek() == KIND_TEMPLATE_LITERAL) {
          if (inBuffer("${", -1)) {
            expressionStack.push(KIND_EXPRESSION);
          }
        } else {
          expressionStack.push(KIND_EXPRESSION);
        }
      }
  "}" {
          var topExpression = expressionStack.peek();
          if (topExpression == KIND_EXPRESSION || topExpression == KIND_ATTRIBUTE_EXPRESSION) {
            expressionStack.pop();
            if (expressionStack.isEmpty() || topExpression == KIND_ATTRIBUTE_EXPRESSION) {
              yybegin(readExprReturnState);
              if (expressionStack.isEmpty()) {
                if (readExprReturnTokenType == AstroSfcTokenTypes.EXPRESSION_ATTRIBUTE
                    || !nextNonWhitespaceCharIs('=')) {
                  return readExprReturnTokenType;
                } else {
                  return XmlTokenType.XML_NAME;
                }
              }
            }
          }
        }
  [^`/\"'{}]+ {
        // consume
      }
  <<EOF>> {
        yybegin(readExprReturnState);
        expressionStack.clear();
        return readExprReturnTokenType;
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
        zzMarkedPos--;
        readUntil(true, '/', '\r', '\n');
        yybegin(FINISH_READ);
      }
}

<READ_MULTILINE_COMMENT> {
  "*/" {
        yybegin(FINISH_READ);
      }
  [^*]+|[*] {
        // consume
      }
  <<EOF>> {
        yybegin(FINISH_READ);
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

{WHITE_SPACE_CHARS} { return XmlTokenType.XML_WHITE_SPACE; }

[^] { return XmlTokenType.XML_BAD_CHARACTER; }
