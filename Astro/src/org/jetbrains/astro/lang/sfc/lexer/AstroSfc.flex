package org.jetbrains.astro.lang.sfc.lexer;

import com.intellij.lang.javascript.JSLexerUtil;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lexer.FlexLexer;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.util.ArrayUtil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

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

    private int findFirstUnescapedChar(char... chars) {
      for (int i = zzStartRead; i < zzMarkedPos; i++) {
        char ch = zzBuffer.charAt(i);
        if (ch == '\\') {
          i++;
        } else if (contains(chars, ch)) {
          return i;
        }
      }
      return zzMarkedPos;
    }

    private boolean contains(char[] chars, char ch) {
      for (int i = 0 ; i < chars.length; i++) {
        if (chars[i] == ch) return true;
      }
      return false;
    }

    private IElementType readReturnTokenType;
    private int readReturnState;
    private char[] readUntilBoundary;

    private static final int KIND_EXPRESSION = 0;
    private static final int KIND_NESTED_BRACES = 1;
    private static final int KIND_SPREAD_OR_SHORTHAND_ATTRIBUTE_EXPRESSION = 2;
    private static final int KIND_ATTRIBUTE_EXPRESSION = 3;
    private static final int KIND_TEMPLATE_LITERAL_EXPRESSION = 4;
    private static final int KIND_TEMPLATE_LITERAL_ATTRIBUTE = 5;
    private static final int KIND_EXPRESSION_PARENTHESIS = 6;
    private static final int KIND_NON_EXPRESSION_PARENTHESIS = 7;
    private static final int KIND_HTML_CONTENT = 8;
    private static final int KIND_START_TAG = 9;
    private static final int KIND_END_TAG = 10;

    public IntArrayList expressionStack = new IntArrayList(15);

    public boolean isRestartable() {
      return expressionStack.isEmpty();
    }

    private boolean isWithinAttributeExpression() {
      var elements = expressionStack.elements();
      for (int i = expressionStack.size() - 1; i >= 0; i--) {
        var element = elements[i];
        if (element == KIND_SPREAD_OR_SHORTHAND_ATTRIBUTE_EXPRESSION
            || element == KIND_ATTRIBUTE_EXPRESSION
            || element == KIND_TEMPLATE_LITERAL_ATTRIBUTE) {
          return true;
        }
      }
      return false;
    }

    private boolean backqouteForcesTemplateLiteralEnd() {
      boolean foundTemplateLiteralExpression = false;
      var elements = expressionStack.elements();
      for (int i = expressionStack.size() - 1; i >= 0; i--) {
        switch (elements[i]) {
          case KIND_TEMPLATE_LITERAL_EXPRESSION -> {
            foundTemplateLiteralExpression = true;
          }
          case KIND_TEMPLATE_LITERAL_ATTRIBUTE -> {
            return foundTemplateLiteralExpression;
          }
          case KIND_ATTRIBUTE_EXPRESSION -> {
            return false;
          }
        }
      }
      return foundTemplateLiteralExpression;
    }

    private boolean templateLiteralSupportsEscape() {
      var elements = expressionStack.elements();
      for (int i = expressionStack.size() - 1; i >= 0; i--) {
        switch (elements[i]) {
          case KIND_TEMPLATE_LITERAL_ATTRIBUTE, KIND_ATTRIBUTE_EXPRESSION -> {
            return false;
          }
        }
      }
      return true;
    }

    private boolean shouldTrackParentheses() {
      if (expressionStack.isEmpty()) return false;
      var lastElement = expressionStack.peekInt(0);
      return lastElement == KIND_EXPRESSION_PARENTHESIS || lastElement == KIND_NON_EXPRESSION_PARENTHESIS;
    }

    private void readString(@Nullable IElementType returnTokenType, int nextState) {
      yybegin(READ_STRING);
      readReturnState = nextState;
      readUntilBoundary = EMPTY_CHAR_ARRAY;
      readReturnTokenType = returnTokenType;
      yypushback(1);
    }

    private IElementType finishReadString(IElementType expressionTokenType) {
      yybegin(readReturnState);
      if (readReturnState == DIV_OR_GT) {
        return expressionTokenType;
      } else {
        return readReturnTokenType;
      }
    }

    private void readCommentOrRegExp(@Nullable IElementType returnTokenType, int nextState, char... regExpBoundary) {
      yybegin(COMMENT_OR_REGEXP);
      readReturnState = nextState;
      readUntilBoundary = regExpBoundary;
      readReturnTokenType = returnTokenType;
    }

    private IElementType finishReadCommentOrRegexp(IElementType expressionTokenType) {
      yybegin(readReturnState);
      if (readReturnState == EXPRESSION_INITIAL) {
        return expressionTokenType;
      } else {
        return readReturnTokenType;
      }
    }

    private void backToInitial() {
      if (expressionStack.isEmpty() || expressionStack.peekInt(0) == KIND_HTML_CONTENT) {
        yybegin(HTML_INITIAL);
      } else {
        yybegin(EXPRESSION_INITIAL);
      }
    }

    private void processClosedTag(boolean isEmpty) {
      if (expressionStack.isEmpty()) {
        // This is a case in which lexer is restarted
        // on a closing tag, which ends with empty
        // expression stack
        yybegin(HTML_INITIAL);
        return;
      }
      var tagKind = expressionStack.popInt();
      if (tagKind == KIND_START_TAG) {
        expressionStack.push(KIND_HTML_CONTENT);
        yybegin(HTML_INITIAL);
      } else if (tagKind == KIND_END_TAG) {
        if (expressionStack.isEmpty()) {
          yybegin(HTML_INITIAL);
        } else {
          while (!expressionStack.isEmpty() && expressionStack.popInt() != KIND_HTML_CONTENT) {};
          if (expressionStack.isEmpty()) {
            yybegin(HTML_INITIAL);
          } else {
            var current = expressionStack.peekInt(0);
            if (current == KIND_HTML_CONTENT) {
              yybegin(HTML_INITIAL);
            } else if (current == KIND_EXPRESSION
                      || current == KIND_EXPRESSION_PARENTHESIS
                      || current == KIND_NON_EXPRESSION_PARENTHESIS
                      || current == KIND_TEMPLATE_LITERAL_EXPRESSION) {
              yybegin(EXPRESSION_INITIAL);
            } else {
              throw new IllegalStateException("Wrong kind on stack: " + current);
            }
          }
        }
      } else {
        throw new IllegalStateException("Wrong kind on stack: " + tagKind);
      }
    }

    private boolean canBeGenericArgumentList() throws IOException {
       return saveStateDoRestoreState(() -> JSLexerUtil.canBeGenericArgumentList(this, false));
    }

    private <T> T saveStateDoRestoreState(ThrowableComputable<T, IOException> action) throws IOException {
      int currentPos = zzCurrentPos;
      int markedPos = zzMarkedPos;
      int startRead = zzStartRead;
      boolean atEOF = zzAtEOF;
      boolean atBOL = zzAtBOL;
      int endRead = zzEndRead;
      int lexicalState = zzLexicalState;
      int state = zzState;
      IntArrayList _expressionStack = expressionStack.clone();
      try {
        return action.compute();
      }
      finally {
        zzCurrentPos = currentPos;
        zzMarkedPos = markedPos;
        zzStartRead = startRead;
        zzAtEOF = atEOF;
        zzAtBOL = atBOL;
        zzEndRead = endRead;
        zzLexicalState = lexicalState;
        zzState = state;
        expressionStack = _expressionStack;
      }
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
%state COMMENT_OR_REGEXP
%state MULTILINE_COMMENT
%state READ_TAG_ATTR_EXPRESSION
%state FINISH_READ

%state EXPRESSION_INITIAL
%state DIV_OR_GT
%state NON_EXPRESSION_PAR
%state AFTER_DOT
%state AFTER_ELVIS
%state STRING_TEMPLATE
%state STRING_TEMPLATE_DOLLAR


ALPHA=[:letter:]
DIGIT=[0-9]
WHITE_SPACE_CHARS=[ \n\r\t\f\u2028\u2029\u0085]+


DTD_REF= "\"" [^\"]* "\"" | "'" [^']* "'"
DOCTYPE= "<!" (D|d)(O|o)(C|c)(T|t)(Y|y)(P|p)(E|e)
HTML= (H|h)(T|t)(M|m)(L|l)
PUBLIC= (P|p)(U|u)(B|b)(L|l)(I|i)(C|c)

CONDITIONAL_COMMENT_CONDITION=({ALPHA})({ALPHA}|{WHITE_SPACE_CHARS}|{DIGIT}|"."|"("|")"|"|"|"!"|"&")*

CHAR_ENTITY="&"([a-zA-Z][a-zA-Z0-9]*)";" | "&#"{DIGIT}+";" | "&#"(x|X)({DIGIT}|[a-fA-F])+";"

//JSX
IDENTIFIER_START=[:jletter:]|\\u{HEX_DIGIT}{4}|\\u\{{HEX_DIGIT}+\}
IDENTIFIER_PART={IDENTIFIER_START}|[:jletterdigit:]
IDENTIFIER={IDENTIFIER_START} {IDENTIFIER_PART}*
PRIVATE_IDENTIFIER="#"{IDENTIFIER}

NONZERO_DIGIT=[1-9]
DIGIT=[0-9]
HEX_DIGIT=[0-9A-Fa-f]
BINARY_DIGIT=[0-1]
OCTAL_DIGIT=[0-7]
NUMERIC_SEPARATOR=_

DECIMAL_DIGITS=({DIGIT}({NUMERIC_SEPARATOR}?{DIGIT})*)
HEX_DIGITS=({HEX_DIGIT}({NUMERIC_SEPARATOR}?{HEX_DIGIT})*)
OCTAL_DIGITS=({OCTAL_DIGIT}({NUMERIC_SEPARATOR}?{OCTAL_DIGIT})*)
BINARY_DIGITS=({BINARY_DIGIT}({NUMERIC_SEPARATOR}?{BINARY_DIGIT})*)

INTEGER_LITERAL_NO_LEGACY={DECIMAL_INTEGER_LITERAL}|{BINARY_INTEGER_LITERAL}|{OCTAL_INTEGER_LITERAL}|{HEX_INTEGER_LITERAL}
INTEGER_LITERAL={INTEGER_LITERAL_NO_LEGACY}|{OBSOLETE_OCTAL_INTEGER_LITERAL}
BIG_INTEGER_LITERAL=({INTEGER_LITERAL_NO_LEGACY})n
DECIMAL_INTEGER_LITERAL=(0|(({NONZERO_DIGIT})(({NUMERIC_SEPARATOR})?({DECIMAL_DIGITS}))?))
HEX_INTEGER_LITERAL=0[Xx]({HEX_DIGITS})
OCTAL_INTEGER_LITERAL=0[Oo]({OCTAL_DIGITS})
BINARY_INTEGER_LITERAL=0[Bb]({BINARY_DIGITS})
OBSOLETE_OCTAL_INTEGER_LITERAL=0({OCTAL_DIGIT})+

FLOAT_LITERAL=({FLOATING_POINT_LITERAL1})|({FLOATING_POINT_LITERAL2})|({FLOATING_POINT_LITERAL3})|({FLOATING_POINT_LITERAL4})
FLOATING_POINT_LITERAL1=({DECIMAL_INTEGER_LITERAL})"."({DECIMAL_DIGITS})?({EXPONENT_PART})?
FLOATING_POINT_LITERAL2="."({DECIMAL_DIGITS})({EXPONENT_PART})?
FLOATING_POINT_LITERAL3=({DECIMAL_INTEGER_LITERAL})({EXPONENT_PART})
FLOATING_POINT_LITERAL4=({DECIMAL_INTEGER_LITERAL})
EXPONENT_PART=[Ee]["+""-"]?({DECIMAL_DIGITS})

LINE_TERMINATOR_SEQUENCE=\R
STRING_TEMPLATE_CHAR=[^\\$`]
ESCAPE_SEQUENCE=\\[^\r\n]
GROUP = "[" ( [^\]\\] | \\. )* "]"

REGEXP_LITERAL="/"([^\*\\/\r\n\[]|{ESCAPE_SEQUENCE}|{GROUP})([^\\/\r\n\[]|{ESCAPE_SEQUENCE}|{GROUP})*("/"[gimxsuyd]*)?

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
        yybegin(EXPRESSION_INITIAL);
        return JSTokenTypes.XML_LBRACE;
      }
  {WHITE_SPACE_CHARS} {
        return XmlTokenType.XML_REAL_WHITE_SPACE;
      }
  {CHAR_ENTITY} {
        return XmlTokenType.XML_CHAR_ENTITY_REF;
      }
  [^] {
        return XmlTokenType.XML_DATA_CHARACTERS;
      }
}

<HTML_INITIAL, EXPRESSION_INITIAL, DIV_OR_GT> {
  "<?" {
        if (yystate() != HTML_INITIAL && isWithinAttributeExpression()) {
          yypushback(yylength() - 1);
          return JSTokenTypes.LT;
        }
        yybegin(PROCESSING_INSTRUCTION);
        return XmlTokenType.XML_PI_START;
      }
  {DOCTYPE} {
        if (yystate() != HTML_INITIAL && isWithinAttributeExpression()) {
          yypushback(yylength() - 1);
          return JSTokenTypes.LT;
        }
        yybegin(DOC_TYPE);
        return XmlTokenType.XML_DOCTYPE_START;
      }
  "<!--" {
        yybegin(COMMENT);
        return XmlTokenType.XML_COMMENT_START;
      }
  \<[>a-zA-Z] {
        if (yystate() != HTML_INITIAL && isWithinAttributeExpression()) {
          yypushback(yylength() - 1);
          return JSTokenTypes.LT;
        }
        expressionStack.push(KIND_START_TAG);
        yybegin(START_TAG_NAME);
        yypushback(1);
        return XmlTokenType.XML_START_TAG_START;
      }

  \<\/[>a-zA-Z] {
        if (yystate() != HTML_INITIAL && isWithinAttributeExpression()) {
          yypushback(yylength() - 1);
          return JSTokenTypes.LT;
        }
        expressionStack.push(KIND_END_TAG);
        yybegin(END_TAG_NAME);
        yypushback(1);
        return XmlTokenType.XML_END_TAG_START;
      }
}

<EXPRESSION_INITIAL> {
  "<" {
        return JSTokenTypes.LT;
      }
  {REGEXP_LITERAL} {
        int boundary;
        if (isWithinAttributeExpression()) {
          boundary = findFirstUnescapedChar('}');
        } else {
          boundary = findFirstUnescapedChar('{', '}', '\'', '"', '`');
        }
        if (boundary < zzMarkedPos) {
          zzMarkedPos = boundary;
        }
        return JSTokenTypes.REGEXP_LITERAL;
      }
}

<EXPRESSION_INITIAL, DIV_OR_GT> {
  "{" {
        expressionStack.push(KIND_NESTED_BRACES);
        yybegin(EXPRESSION_INITIAL);
        return JSTokenTypes.LBRACE;
      }
  "/*" | "//" {
        yypushback(1);
        if (isWithinAttributeExpression()) {
          readCommentOrRegExp(null, EXPRESSION_INITIAL, '}');
        } else {
          readCommentOrRegExp(null, EXPRESSION_INITIAL, '{', '}', '\'', '"', '`');
        }
      }
  "`" {
        if (backqouteForcesTemplateLiteralEnd()) {
          while (!expressionStack.isEmpty() && expressionStack.popInt() != KIND_TEMPLATE_LITERAL_EXPRESSION) {}
          yypushback(1);
          yybegin(STRING_TEMPLATE);
        } else {
          yybegin(STRING_TEMPLATE);
          return JSTokenTypes.BACKQUOTE;
        }
      }

  [\"'] {
        readString(null, DIV_OR_GT);
      }
  "}" {
        while (!expressionStack.isEmpty()) {
          var popped = expressionStack.popInt();
          switch(popped) {
            case KIND_NESTED_BRACES -> {
               yybegin(EXPRESSION_INITIAL);
               return JSTokenTypes.RBRACE;
            }
            case KIND_SPREAD_OR_SHORTHAND_ATTRIBUTE_EXPRESSION -> {
               yybegin(TAG_ATTRIBUTES_POST_SHORTHAND);
               return JSTokenTypes.XML_RBRACE;
            }
            case KIND_ATTRIBUTE_EXPRESSION -> {
               yybegin(TAG_ATTRIBUTES);
               return JSTokenTypes.XML_RBRACE;
            }
            case KIND_EXPRESSION -> {
               yybegin(HTML_INITIAL);
               return JSTokenTypes.XML_RBRACE;
            }
            case KIND_TEMPLATE_LITERAL_EXPRESSION -> {
               yybegin(STRING_TEMPLATE);
               return JSTokenTypes.RBRACE;
            }
            case KIND_EXPRESSION_PARENTHESIS, KIND_NON_EXPRESSION_PARENTHESIS -> {
               // drop unbalanced parenthesis
            }
            default -> throw new IllegalStateException("Wrong value on stack: " + popped);
          }
        }
        yybegin(HTML_INITIAL);
        return JSTokenTypes.XML_RBRACE;
      }
}

<START_TAG_NAME, END_TAG_NAME> {
  [^ \n\r\t\f/>]+ {
        yybegin(BEFORE_TAG_ATTRIBUTES);
        return XmlTokenType.XML_NAME;
      }
}

<BEFORE_TAG_ATTRIBUTES>
  {WHITE_SPACE_CHARS} {
        yybegin(TAG_ATTRIBUTES);
        return XmlTokenType.XML_WHITE_SPACE;
      }

<TAG_ATTRIBUTES> {
  "{" {
        expressionStack.push(KIND_SPREAD_OR_SHORTHAND_ATTRIBUTE_EXPRESSION);
        yybegin(EXPRESSION_INITIAL);
        return JSTokenTypes.XML_LBRACE;
      }
  "=" {
        yybegin(ATTRIBUTE_VALUE_START);
        return XmlTokenType.XML_EQ;
      }
  [^{ \n\r\t\f/=<>]+ {
        if (inBuffer("{", 1)) {
          // If attribute name contains '{' everything up to it is ignored.
          return XmlTokenType.XML_COMMENT_CHARACTERS;
        }
        return XmlTokenType.XML_NAME;
      }
  {WHITE_SPACE_CHARS} {
        return XmlTokenType.XML_WHITE_SPACE;
      }
}

<TAG_ATTRIBUTES_POST_SHORTHAND> {
  [/=>] { yypushback(1); yybegin(TAG_ATTRIBUTES); }
  {WHITE_SPACE_CHARS} { yybegin(TAG_ATTRIBUTES); return XmlTokenType.XML_WHITE_SPACE; }
  // This stuff is actually concatenated with the shorthand attribute contents,
  // but I think it's better to just show bad character here
  [^] {
        return XmlTokenType.XML_BAD_CHARACTER;
      }
}

<ATTRIBUTE_VALUE_START> {
  ">" { processClosedTag(false); return XmlTokenType.XML_TAG_END; }
  "'" { yybegin(ATTRIBUTE_VALUE_SQ); return XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER; }
  "\"" { yybegin(ATTRIBUTE_VALUE_DQ); return XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER; }
  "`" {
          expressionStack.push(KIND_TEMPLATE_LITERAL_ATTRIBUTE);
          yybegin(STRING_TEMPLATE);
          return JSTokenTypes.BACKQUOTE;
        }
  "{" {
        expressionStack.push(KIND_ATTRIBUTE_EXPRESSION);
        yybegin(EXPRESSION_INITIAL);
        return JSTokenTypes.XML_LBRACE;
      }
  [^ \n\r\t\f'\">`{]([^ \n\r\t\f\>]|(\/[^\>]))* {
        yybegin(TAG_ATTRIBUTES);
        return XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN;
      }
  <<EOF>> {
          yybegin(HTML_INITIAL);
          return XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN;
        }
}

<START_TAG_NAME, END_TAG_NAME, BEFORE_TAG_ATTRIBUTES, TAG_ATTRIBUTES> {
  "<"  { processClosedTag(false); yypushback(1); }
  "/>" { processClosedTag(true); return XmlTokenType.XML_EMPTY_ELEMENT_END; }
  ">" { processClosedTag(false); return XmlTokenType.XML_TAG_END; }
  [^] { yybegin(HTML_INITIAL); expressionStack.popInt(); yypushback(1); }
}
<PROCESSING_INSTRUCTION> {
  "?"? ">" { yybegin(HTML_INITIAL);return XmlTokenType.XML_PI_END; }
  ([^\?\>] | (\?[^\>]))+ { return XmlTokenType.XML_PI_TARGET; }
}

<DOC_TYPE> {
  {HTML} { return XmlTokenType.XML_NAME; }
  {PUBLIC} { return XmlTokenType.XML_DOCTYPE_PUBLIC; }
  {DTD_REF} { return XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN;}
  ">" { yybegin(HTML_INITIAL);return XmlTokenType.XML_DOCTYPE_END; }
}

<COMMENT> {
  "["     { yybegin(C_COMMENT_START); return XmlTokenType.XML_CONDITIONAL_COMMENT_START; }
  "<!["   { yybegin(C_COMMENT_END); return XmlTokenType.XML_CONDITIONAL_COMMENT_END_START; }
  "-->" | "<!-->" { backToInitial(); return XmlTokenType.XML_COMMENT_END; }
  "<!--"   { return XmlTokenType.XML_BAD_CHARACTER; }
  "<!--->" | "--!>" { backToInitial(); return XmlTokenType.XML_BAD_CHARACTER; }
  ">" {
    // according to HTML spec (http://www.w3.org/html/wg/drafts/html/master/syntax.html#comments)
    // comments should start with <!-- and end with -->. The comment <!--> is not valid, but should terminate
    // comment token. Please note that it's not true for XML (http://www.w3.org/TR/REC-xml/#sec-comments)
    int loc = getTokenStart();
    char prev = zzBuffer.charAt(loc - 1);
    char prevPrev = zzBuffer.charAt(loc - 2);
    if (prev == '-' && prevPrev == '-') {
      backToInitial();
      return XmlTokenType.XML_BAD_CHARACTER;
    }
    return XmlTokenType.XML_COMMENT_CHARACTERS;
  }
  [^] { return XmlTokenType.XML_COMMENT_CHARACTERS; }
}

<C_COMMENT_START> {
  "]>" { yybegin(COMMENT); return XmlTokenType.XML_CONDITIONAL_COMMENT_START_END; }
  [^] { yybegin(COMMENT); return XmlTokenType.XML_COMMENT_CHARACTERS; }
}

<C_COMMENT_END> {
  "]" { yybegin(COMMENT); return XmlTokenType.XML_CONDITIONAL_COMMENT_END; }
  [^] { yybegin(COMMENT); return XmlTokenType.XML_COMMENT_CHARACTERS; }
}

<C_COMMENT_START,C_COMMENT_END> {
  {CONDITIONAL_COMMENT_CONDITION} {
        return XmlTokenType.XML_COMMENT_CHARACTERS;
      }
  "-->" { backToInitial(); return XmlTokenType.XML_COMMENT_END; }
}

<ATTRIBUTE_VALUE_DQ> {
  "\"" { yybegin(TAG_ATTRIBUTES); return XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER; }
  [^\"] { return XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN; }
}

<ATTRIBUTE_VALUE_SQ> {
  "'" { yybegin(TAG_ATTRIBUTES); return XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER; }
  [^'] { return XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN; }
}

<COMMENT_OR_REGEXP> {
  "/" {
        readUntil(false, '\r', '\n');
        var result = finishReadCommentOrRegexp(JSTokenTypes.END_OF_LINE_COMMENT);
        if (result != null) return result;
      }
  "*" {
        yybegin(MULTILINE_COMMENT);
      }
  [^] {
        zzMarkedPos--;
        readUntil(true, '/', '\r', '\n');
        var result = finishReadCommentOrRegexp(JSTokenTypes.REGEXP_LITERAL);
        if (result != null) return result;
      }
}

<MULTILINE_COMMENT> {
  "*/" {
        var result = finishReadCommentOrRegexp(JSTokenTypes.C_STYLE_COMMENT);
        if (result != null) return result;
      }
  [^*]+|[*] {
        // consume
      }
  <<EOF>> {
        var result = finishReadCommentOrRegexp(JSTokenTypes.C_STYLE_COMMENT);
        if (result != null) return result;
      }
}

<READ_STRING> {
  "'" {
        if (backqouteForcesTemplateLiteralEnd()) {
          readUntil(false, '\'', '`', '\r', '\n');
          if (inBuffer("`", 0)) {
            zzMarkedPos--;
          }
        } else {
          readUntil(false, '\'', '\r', '\n');
        }
        var result = finishReadString(JSTokenTypes.SINGLE_QUOTE_STRING_LITERAL);
        if (result != null) return result;
      }
  "\"" {
        if (backqouteForcesTemplateLiteralEnd()) {
          readUntil(false, '"', '`', '\r', '\n');
          if (inBuffer("`", 0)) {
            zzMarkedPos--;
          }
        } else {
          readUntil(false, '"', '\r', '\n');
        }
        var result = finishReadString(JSTokenTypes.STRING_LITERAL);
        if (result != null) return result;
      }
}

// JSX expression rules
<EXPRESSION_INITIAL> {
  "<![CDATA[" { return JSTokenTypes.CDATA_START; }
}

<EXPRESSION_INITIAL,DIV_OR_GT> {
  {INTEGER_LITERAL}     { yybegin(DIV_OR_GT); return JSTokenTypes.NUMERIC_LITERAL; }
  {BIG_INTEGER_LITERAL} { yybegin(DIV_OR_GT); return JSTokenTypes.NUMERIC_LITERAL; }
  {FLOAT_LITERAL}       { yybegin(DIV_OR_GT); return JSTokenTypes.NUMERIC_LITERAL; }
  "true"          { yybegin(DIV_OR_GT); return JSTokenTypes.TRUE_KEYWORD; }
  "false"         { yybegin(DIV_OR_GT); return JSTokenTypes.FALSE_KEYWORD; }
  "null"          { yybegin(DIV_OR_GT); return JSTokenTypes.NULL_KEYWORD; }
  "undefined"     { yybegin(DIV_OR_GT); return JSTokenTypes.UNDEFINED_KEYWORD; }
  "break"         { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.BREAK_KEYWORD; }
  "case"          { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.CASE_KEYWORD; }
  "catch"         { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.CATCH_KEYWORD; }
  "continue"      { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.CONTINUE_KEYWORD; }
  "debugger"      { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.DEBUGGER_KEYWORD; }
  "default"       { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.DEFAULT_KEYWORD; }
  "delete"        { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.DELETE_KEYWORD; }
  "do"            { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.DO_KEYWORD; }
  "else"          { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.ELSE_KEYWORD; }
  "finally"       { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.FINALLY_KEYWORD; }
  "for"           { yybegin(NON_EXPRESSION_PAR); return JSTokenTypes.FOR_KEYWORD; }
  "function"      { yybegin(DIV_OR_GT); return JSTokenTypes.FUNCTION_KEYWORD; }
  "if"            { yybegin(NON_EXPRESSION_PAR); return JSTokenTypes.IF_KEYWORD; }
  "in"            { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.IN_KEYWORD; }
  "out"           { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.OUT_KEYWORD; }
  "instanceof"    { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.INSTANCEOF_KEYWORD; }
  "new"           { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.NEW_KEYWORD; }
  "return"        { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.RETURN_KEYWORD; }
  "switch"        { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.SWITCH_KEYWORD; }
  "this"          { yybegin(DIV_OR_GT); return JSTokenTypes.THIS_KEYWORD; }
  "throw"         { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.THROW_KEYWORD; }
  "try"           { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.TRY_KEYWORD; }
  "typeof"        { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.TYPEOF_KEYWORD; }
  "var"           { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.VAR_KEYWORD; }
  "void"          { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.VOID_KEYWORD; }
  "while"         { yybegin(NON_EXPRESSION_PAR); return JSTokenTypes.WHILE_KEYWORD; }
  "with"          { yybegin(NON_EXPRESSION_PAR); return JSTokenTypes.WITH_KEYWORD; }
  "class"         { yybegin(DIV_OR_GT); return JSTokenTypes.CLASS_KEYWORD; }
  "const"         { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.CONST_KEYWORD; }
  "enum"          { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.ENUM_KEYWORD; }
  "export"        { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.EXPORT_KEYWORD; }
  "decorator"     { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.DECORATOR_KEYWORD; }
  "let"           { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.LET_KEYWORD; }
  "yield"         { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.YIELD_KEYWORD; }
  "extends"       { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.EXTENDS_KEYWORD; }
  "import"        { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.IMPORT_KEYWORD; }
  "super"         { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.SUPER_KEYWORD; }
  "implements"    { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.IMPLEMENTS_KEYWORD; }
  "interface"     { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.INTERFACE_KEYWORD; }
  "type"          { yybegin(DIV_OR_GT); return JSTokenTypes.TYPE_KEYWORD; }
  "package"       { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.PACKAGE_KEYWORD; }
  "private"       { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.PRIVATE_KEYWORD; }
  "protected"     { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.PROTECTED_KEYWORD; }
  "public"        { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.PUBLIC_KEYWORD; }
  "static"        { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.STATIC_KEYWORD; }
  "accessor"      { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.ACCESSOR_KEYWORD; }
  "module"        { yybegin(DIV_OR_GT); return JSTokenTypes.MODULE_KEYWORD; }
  "declare"       { yybegin(DIV_OR_GT); return JSTokenTypes.DECLARE_KEYWORD; }
  "any"           { yybegin(DIV_OR_GT); return JSTokenTypes.ANY_KEYWORD; }
  "number"        { yybegin(DIV_OR_GT); return JSTokenTypes.NUMBER_KEYWORD; }
  "string"        { yybegin(DIV_OR_GT); return JSTokenTypes.STRING_KEYWORD; }
  "boolean"       { yybegin(DIV_OR_GT); return JSTokenTypes.BOOLEAN_KEYWORD; }
  "require"       { yybegin(DIV_OR_GT); return JSTokenTypes.REQUIRE_KEYWORD; }
  "symbol"        { yybegin(DIV_OR_GT); return JSTokenTypes.SYMBOL_KEYWORD; }
  "bigint"        { yybegin(DIV_OR_GT); return JSTokenTypes.BIGINT_KEYWORD; }
  "never"         { yybegin(DIV_OR_GT); return JSTokenTypes.NEVER_KEYWORD; }
  "unknown"       { yybegin(DIV_OR_GT); return JSTokenTypes.UNKNOWN_KEYWORD; }
  "object"        { yybegin(DIV_OR_GT); return JSTokenTypes.OBJECT_TYPE_KEYWORD; }
  "abstract"      { yybegin(DIV_OR_GT); return JSTokenTypes.ABSTRACT_KEYWORD; }
  "readonly"      { yybegin(DIV_OR_GT); return JSTokenTypes.READONLY_KEYWORD; }
  "global"        { yybegin(DIV_OR_GT); return JSTokenTypes.GLOBAL_KEYWORD; }
  "keyof"         { yybegin(DIV_OR_GT); return JSTokenTypes.KEYOF_KEYWORD; }
  "infer"         { yybegin(DIV_OR_GT); return JSTokenTypes.INFER_KEYWORD; }
  "awaited"       { yybegin(DIV_OR_GT); return JSTokenTypes.AWAITED_KEYWORD; }
  "intrinsic"     { yybegin(DIV_OR_GT); return JSTokenTypes.INTRINSIC_KEYWORD; }
  "::"            { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.COLON_COLON; }
  "..."           { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.DOT_DOT_DOT; }
  ".."            { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.DOT_DOT; }
  "namespace"     { yybegin(DIV_OR_GT); return JSTokenTypes.NAMESPACE_KEYWORD; }
  "override"      { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.OVERRIDE_KEYWORD; }
  "is"            { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.IS_KEYWORD; }
  "asserts"       { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.ASSERTS_KEYWORD; }
  "get"           { yybegin(DIV_OR_GT); return JSTokenTypes.GET_KEYWORD; }
  "set"           { yybegin(DIV_OR_GT); return JSTokenTypes.SET_KEYWORD; }
  "of"            { yybegin(DIV_OR_GT); return JSTokenTypes.OF_KEYWORD; }
  "await"         { yybegin(EXPRESSION_INITIAL);  return JSTokenTypes.AWAIT_KEYWORD; }
  "async"         { yybegin(DIV_OR_GT); return JSTokenTypes.ASYNC_KEYWORD; }
  "as"            { yybegin(DIV_OR_GT); return JSTokenTypes.AS_KEYWORD; }
  "satisfies"     { yybegin(DIV_OR_GT); return JSTokenTypes.SATISFIES_KEYWORD; }
  "from"          { yybegin(DIV_OR_GT); return JSTokenTypes.FROM_KEYWORD; }
  "assert"        { yybegin(DIV_OR_GT); return JSTokenTypes.ASSERT_KEYWORD; }
  "@"             { return JSTokenTypes.AT; }
  "==="           { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.EQEQEQ; }
  "!=="           { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.NEQEQ; }
  "++"            { return JSTokenTypes.PLUSPLUS; }
  "--"            { return JSTokenTypes.MINUSMINUS; }
  "=="            { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.EQEQ; }
  "!="            { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.NE; }
  "&lt;"          { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.LT; }
  ">"             { yybegin(DIV_OR_GT); return JSTokenTypes.GT; }
  "&gt;"          { yybegin(DIV_OR_GT); return JSTokenTypes.GT; }
  "&lt;="         { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.LE; }
  "&gt;="         { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.GE; }
  "&lt;&lt;"      { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.LTLT; }
  "&"             { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.AND; }
  "&amp;"         { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.AND; }
  "&&="           { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.AND_AND_EQ; }
  "&&"            { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.ANDAND; }
  "&amp;&amp;"    { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.ANDAND; }
  "|"             { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.OR; }
  "||="           { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.OR_OR_EQ; }
  "||"            { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.OROR; }
  "??"            { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.QUEST_QUEST; }
  "??="           { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.QUEST_QUEST_EQ; }
  "+="            { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.PLUSEQ; }
  "-="            { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.MINUSEQ; }
  "&="            { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.ANDEQ; }
  "&amp;="        { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.ANDEQ; }
  "|="            { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.OREQ; }
  "^="            { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.XOREQ; }
  "%="            { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.PERCEQ; }
  "&lt;&lt;="     { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.LTLTEQ; }
  ">>="           { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.GTGTEQ; }
  "&gt;&gt;="     { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.GTGTEQ; }
  ">>>="          { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.GTGTGTEQ; }
  "&gt;&gt;&gt;=" { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.GTGTGTEQ; }
  "["             { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.LBRACKET; }
  "]"             { yybegin(DIV_OR_GT); return JSTokenTypes.RBRACKET; }
  ";"             { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.SEMICOLON; }
  ","             { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.COMMA; }
  "=>"            { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.EQGT;}
  "->"            { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.MINUSGT; }
  "="             { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.EQ; }
  "!"             { return JSTokenTypes.EXCL; }
  "~"             { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.TILDE; }
  "?"             { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.QUEST; }
  ":"             { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.COLON; }
  "+"             { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.PLUS; }
  "-"             { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.MINUS; }
  "*"             { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.MULT; }
  "|>"            { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.PIPE; }
  "/"             { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.DIV; }
  "^"             { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.XOR; }
  "%"             { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.PERC; }
  "#"             { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.SHARP; } // standalone SHARP is needed for JSDoc
  "."             { yybegin(AFTER_DOT); return JSTokenTypes.DOT; }
  "?."{DIGIT}     { yypushback(yylength()-1); yybegin(EXPRESSION_INITIAL); return JSTokenTypes.QUEST; }
  "?."            { yybegin(AFTER_ELVIS); return JSTokenTypes.ELVIS; }
}

<DIV_OR_GT> {
  "<"   { yypushback(yylength()); yybegin(EXPRESSION_INITIAL); }
  "**"  { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.MULTMULT; }
  "<="  { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.LE; }
  "*="  { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.MULTEQ; }
  "/="  { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.DIVEQ; }
  "<<=" { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.LTLTEQ; }
  "**=" { yybegin(EXPRESSION_INITIAL); return JSTokenTypes.MULTMULTEQ; }
  "<<"  {
        yybegin(EXPRESSION_INITIAL);
        if (canBeGenericArgumentList()) {
           yypushback(yylength() - 1);
           return JSTokenTypes.LT;
        }
        return JSTokenTypes.LTLT;
      }
}

<EXPRESSION_INITIAL,DIV_OR_GT> {
  "(" {
        if (shouldTrackParentheses()) {
          expressionStack.push(KIND_EXPRESSION_PARENTHESIS);
        }
        yybegin(EXPRESSION_INITIAL);
        return JSTokenTypes.LPAR;
      }

  ")" {
        boolean wasExpr;
        if (expressionStack.isEmpty()) {
          wasExpr = true;
        } else {
          var peek = expressionStack.peekInt(0);
          if (peek == KIND_NON_EXPRESSION_PARENTHESIS) {
            wasExpr = false;
            expressionStack.popInt();
          } else {
            wasExpr = true;
            if (peek == KIND_EXPRESSION_PARENTHESIS) {
              expressionStack.popInt();
            }
          }
        }
        yybegin(wasExpr ? DIV_OR_GT : EXPRESSION_INITIAL);
        return JSTokenTypes.RPAR;
      }
}

<NON_EXPRESSION_PAR> "("  {
        expressionStack.push(KIND_NON_EXPRESSION_PARENTHESIS);
        yybegin(EXPRESSION_INITIAL);
        return JSTokenTypes.LPAR;
      }

<EXPRESSION_INITIAL,DIV_OR_GT,AFTER_DOT,AFTER_ELVIS> {
    {PRIVATE_IDENTIFIER} { yybegin(DIV_OR_GT); return JSTokenTypes.PRIVATE_IDENTIFIER; }
    {IDENTIFIER}         { yybegin(DIV_OR_GT); return JSTokenTypes.IDENTIFIER; }
}

<NON_EXPRESSION_PAR,AFTER_DOT,AFTER_ELVIS> [^] { yypushback(yylength()); yybegin(EXPRESSION_INITIAL); }

<STRING_TEMPLATE> {
    ( {STRING_TEMPLATE_CHAR} | (\\[^`\r\n]) | "\\" {LINE_TERMINATOR_SEQUENCE} )+
                { return JSTokenTypes.STRING_TEMPLATE_PART; }
    \\[`]       {
                  if (!templateLiteralSupportsEscape()) {
                    yypushback(1);
                  }
                  return JSTokenTypes.STRING_TEMPLATE_PART;
                }
    "$"         { return JSTokenTypes.STRING_TEMPLATE_PART; }
    /* don't merge with { to have parents paired */
    "$" / "{"   { yybegin(STRING_TEMPLATE_DOLLAR); return JSTokenTypes.DOLLAR; }
    "`" {
        if (!expressionStack.isEmpty() && expressionStack.peekInt(0) == KIND_TEMPLATE_LITERAL_ATTRIBUTE) {
          expressionStack.popInt();
          yybegin(TAG_ATTRIBUTES);
        } else {
          yybegin(DIV_OR_GT);
        }
        return JSTokenTypes.BACKQUOTE;
      }
}
<STRING_TEMPLATE_DOLLAR> "{" {
        expressionStack.push(KIND_TEMPLATE_LITERAL_EXPRESSION);
        yybegin(EXPRESSION_INITIAL);
        return JSTokenTypes.LBRACE;
      }

{WHITE_SPACE_CHARS} { return XmlTokenType.XML_WHITE_SPACE; }

[^] { return XmlTokenType.XML_BAD_CHARACTER; }
