package org.jetbrains.vuejs.lang.html.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlTokenType;
import kotlin.Pair;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.vuejs.lang.html.lexer.VueTokenTypes;
%%

%unicode

%{
  private String interpolationStart;
  private String interpolationEnd;

  private int interpolationStartPos;

  public _VueLexer(@Nullable Pair<String, String> interpolationConfig) {
    this((java.io.Reader)null);
    if (interpolationConfig == null) {
      interpolationStart = null;
      interpolationEnd = null;
    } else {
      interpolationStart = interpolationConfig.getFirst();
      interpolationEnd = interpolationConfig.getSecond();
    }
  }

  private boolean tryConsumeInterpolationBoundary(@Nullable String boundary) {
    if (inBuffer(boundary, 0)) {
      zzMarkedPos += boundary.length() - 1;
      interpolationStartPos = -1;
      return true;
    }
    return false;
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

  private boolean tryRollbackInterpolation() {
    if (yystate() == INTERPOLATION) {
      rollbackInterpolation();
      yybegin(UNTERMINATED_INTERPOLATION);
      return true;
    }
    return false;
  }

  private void rollbackInterpolation() {
    if (interpolationStartPos > 0) {
      zzStartRead = interpolationStartPos - 1;
      zzMarkedPos = interpolationStartPos - 1;
      interpolationStartPos = -1;
    } else {
      yypushback(yylength());
    }
  }

  private boolean isWithinInterpolation() {
    return zzLexicalState == INTERPOLATION
      || zzLexicalState == INTERPOLATION_DQ
      || zzLexicalState == INTERPOLATION_SQ;
  }
%}

%class _VueLexer
%public
%implements FlexLexer
%function advance
%type IElementType
//%debug

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

%state INTERPOLATION
%state UNTERMINATED_INTERPOLATION
%state INTERPOLATION_END
%state INTERPOLATION_SQ
%state INTERPOLATION_DQ
%state UNTERMINATED_INTERPOLATION_SQ
%state UNTERMINATED_INTERPOLATION_DQ
%state INTERPOLATION_END_SQ
%state INTERPOLATION_END_DQ

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

<YYINITIAL, INTERPOLATION, UNTERMINATED_INTERPOLATION> "<?" {
  if (!tryRollbackInterpolation()) {
    yybegin(PROCESSING_INSTRUCTION);
    return XmlTokenType.XML_PI_START;
  }
}
<PROCESSING_INSTRUCTION> "?"? ">" { yybegin(YYINITIAL); return XmlTokenType.XML_PI_END; }
<PROCESSING_INSTRUCTION> ([^\?\>] | (\?[^\>]))* { return XmlTokenType.XML_PI_TARGET; }

<YYINITIAL, INTERPOLATION, UNTERMINATED_INTERPOLATION> {DOCTYPE} {
  if (!tryRollbackInterpolation()) {
    yybegin(DOC_TYPE);
    return XmlTokenType.XML_DOCTYPE_START;
  }
}
<DOC_TYPE> {HTML} { return XmlTokenType.XML_NAME; }
<DOC_TYPE> {PUBLIC} { return XmlTokenType.XML_DOCTYPE_PUBLIC; }
<DOC_TYPE> {DTD_REF} { return XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN;}
<DOC_TYPE> ">" { yybegin(YYINITIAL); return XmlTokenType.XML_DOCTYPE_END; }
<YYINITIAL, UNTERMINATED_INTERPOLATION> {WHITE_SPACE_CHARS} { return XmlTokenType.XML_REAL_WHITE_SPACE; }
<INTERPOLATION> {WHITE_SPACE_CHARS} {}
<DOC_TYPE,TAG_ATTRIBUTES,ATTRIBUTE_VALUE_START,PROCESSING_INSTRUCTION, START_TAG_NAME, END_TAG_NAME, TAG_CHARACTERS> {WHITE_SPACE_CHARS} { return XmlTokenType.XML_WHITE_SPACE; }
<YYINITIAL, INTERPOLATION, UNTERMINATED_INTERPOLATION> "<" {TAG_NAME} {
  if (!tryRollbackInterpolation()) {
    yybegin(START_TAG_NAME);
    yypushback(yylength());
  }
}
<START_TAG_NAME, TAG_CHARACTERS> "<" { return XmlTokenType.XML_START_TAG_START; }

<YYINITIAL, INTERPOLATION, UNTERMINATED_INTERPOLATION> "</" {TAG_NAME} {
  if (!tryRollbackInterpolation()) {
    yybegin(END_TAG_NAME); yypushback(yylength());
  }
}
<YYINITIAL, END_TAG_NAME, INTERPOLATION, UNTERMINATED_INTERPOLATION> "</" {
  if (!tryRollbackInterpolation()) {
    return XmlTokenType.XML_END_TAG_START;
  }
}

<YYINITIAL, INTERPOLATION, UNTERMINATED_INTERPOLATION> "<!--" {
  if (!tryRollbackInterpolation()) {
    yybegin(COMMENT);
    return XmlTokenType.XML_COMMENT_START;
  }
}
<COMMENT> "[" { yybegin(C_COMMENT_START); return XmlTokenType.XML_CONDITIONAL_COMMENT_START; }
<COMMENT> "<![" { yybegin(C_COMMENT_END); return XmlTokenType.XML_CONDITIONAL_COMMENT_END_START; }
<COMMENT> {END_COMMENT} | "<!-->" { yybegin(YYINITIAL); return XmlTokenType.XML_COMMENT_END; }
<COMMENT> "<!--" { return XmlTokenType.XML_BAD_CHARACTER; }
<COMMENT> "<!--->" | "--!>" { yybegin(YYINITIAL); return XmlTokenType.XML_BAD_CHARACTER; }
<COMMENT> ">" {
  // according to HTML spec (http://www.w3.org/html/wg/drafts/html/master/syntax.html#comments)
  // comments should start with <!-- and end with -->. The comment <!--> is not valid, but should terminate
  // comment token. Please note that it's not true for XML (http://www.w3.org/TR/REC-xml/#sec-comments)
  int loc = getTokenStart();
  char prev = zzBuffer.charAt(loc - 1);
  char prevPrev = zzBuffer.charAt(loc - 2);
  if (prev == '-' && prevPrev == '-') {
    yybegin(YYINITIAL); return XmlTokenType.XML_BAD_CHARACTER;
  }
  return XmlTokenType.XML_COMMENT_CHARACTERS;
}
<COMMENT> [^] { return XmlTokenType.XML_COMMENT_CHARACTERS; }

<C_COMMENT_START,C_COMMENT_END> {CONDITIONAL_COMMENT_CONDITION} { return XmlTokenType.XML_COMMENT_CHARACTERS; }
<C_COMMENT_START> [^] { yybegin(COMMENT); return XmlTokenType.XML_COMMENT_CHARACTERS; }
<C_COMMENT_START> "]>" { yybegin(COMMENT); return XmlTokenType.XML_CONDITIONAL_COMMENT_START_END; }
<C_COMMENT_START,C_COMMENT_END> {END_COMMENT} { yybegin(YYINITIAL); return XmlTokenType.XML_COMMENT_END; }
<C_COMMENT_END> "]" { yybegin(COMMENT); return XmlTokenType.XML_CONDITIONAL_COMMENT_END; }
<C_COMMENT_END> [^] { yybegin(COMMENT); return XmlTokenType.XML_COMMENT_CHARACTERS; }

<START_TAG_NAME, END_TAG_NAME> {TAG_NAME} { yybegin(BEFORE_TAG_ATTRIBUTES); return XmlTokenType.XML_NAME; }

<BEFORE_TAG_ATTRIBUTES, TAG_ATTRIBUTES, TAG_CHARACTERS> ">" { yybegin(YYINITIAL); return XmlTokenType.XML_TAG_END; }
<BEFORE_TAG_ATTRIBUTES, TAG_ATTRIBUTES, TAG_CHARACTERS> "/>" { yybegin(YYINITIAL); return XmlTokenType.XML_EMPTY_ELEMENT_END; }
<BEFORE_TAG_ATTRIBUTES> {WHITE_SPACE_CHARS} { yybegin(TAG_ATTRIBUTES); return XmlTokenType.XML_WHITE_SPACE;}
<TAG_ATTRIBUTES> {ATTRIBUTE_NAME} { return XmlTokenType.XML_NAME; }
<TAG_ATTRIBUTES> "=" { yybegin(ATTRIBUTE_VALUE_START); return XmlTokenType.XML_EQ; }
<BEFORE_TAG_ATTRIBUTES, TAG_ATTRIBUTES, START_TAG_NAME, END_TAG_NAME> [^] { yybegin(YYINITIAL); yypushback(1); break; }

<TAG_CHARACTERS> [^] { return XmlTokenType.XML_TAG_CHARACTERS; }

<ATTRIBUTE_VALUE_START> ">" { yybegin(YYINITIAL); return XmlTokenType.XML_TAG_END; }
<ATTRIBUTE_VALUE_START> "/>" { yybegin(YYINITIAL); return XmlTokenType.XML_EMPTY_ELEMENT_END; }

<ATTRIBUTE_VALUE_START> [^ \n\r\t\f'\"\>]([^ \n\r\t\f\>]|(\/[^\>]))* { yybegin(TAG_ATTRIBUTES); return XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN; }
<ATTRIBUTE_VALUE_START> "\"" { yybegin(ATTRIBUTE_VALUE_DQ); return XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER; }
<ATTRIBUTE_VALUE_START> "'" { yybegin(ATTRIBUTE_VALUE_SQ); return XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER; }

<ATTRIBUTE_VALUE_DQ, UNTERMINATED_INTERPOLATION_DQ> {
  "\"" { yybegin(TAG_ATTRIBUTES); return XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER; }
  [^] {
  if (yystate() == ATTRIBUTE_VALUE_DQ
      && tryConsumeInterpolationBoundary(interpolationStart)) {
    if (inBuffer(interpolationEnd, 1)) {
      yybegin(INTERPOLATION_END_DQ);
    } else {
      yybegin(INTERPOLATION_DQ);
    }
    return VueTokenTypes.INTERPOLATION_START;
  }
  return XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN;}
}

<ATTRIBUTE_VALUE_SQ, UNTERMINATED_INTERPOLATION_SQ> {
  "'" { yybegin(TAG_ATTRIBUTES); return XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER; }
  [^] {
  if (yystate() == ATTRIBUTE_VALUE_SQ
            && tryConsumeInterpolationBoundary(interpolationStart)) {
    if (inBuffer(interpolationEnd, 1)) {
      yybegin(INTERPOLATION_END_SQ);
    } else {
      yybegin(INTERPOLATION_SQ);
    }
    return VueTokenTypes.INTERPOLATION_START;
  }
  return XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN;}
}

<INTERPOLATION_DQ> "\"" {
  rollbackInterpolation();
  yybegin(UNTERMINATED_INTERPOLATION_DQ);
}

<INTERPOLATION_SQ> "'" {
  rollbackInterpolation();
  yybegin(UNTERMINATED_INTERPOLATION_SQ);
}

<INTERPOLATION_DQ, INTERPOLATION_SQ>   [^] {
  if (inBuffer(interpolationEnd, 0)) {
    yybegin(yystate() == INTERPOLATION_DQ ? INTERPOLATION_END_DQ : INTERPOLATION_END_SQ);
    yypushback(1);
    return VueTokenTypes.INTERPOLATION_EXPR;
  }
  if (interpolationStartPos <= 0) {
    interpolationStartPos = zzStartRead;
  }
}

<INTERPOLATION_END_DQ, INTERPOLATION_END_SQ> [^] {
  yybegin(yystate() == INTERPOLATION_END_DQ ? ATTRIBUTE_VALUE_DQ : ATTRIBUTE_VALUE_SQ);
  if (tryConsumeInterpolationBoundary(interpolationEnd)) {
    return VueTokenTypes.INTERPOLATION_END;
  }
  return XmlTokenType.XML_BAD_CHARACTER;
}

"&lt;" |
"&gt;" |
"&apos;" |
"&quot;" |
"&nbsp;" |
"&amp;" |
"&#"{DIGIT}+";" |
"&#"(x|X)({DIGIT}|[a-fA-F])+";" { if (!isWithinInterpolation()) return XmlTokenType.XML_CHAR_ENTITY_REF; }
"&"{TAG_NAME}";" { if (!isWithinInterpolation()) return XmlTokenType.XML_ENTITY_REF_TOKEN; }

<INTERPOLATION> [^] {
  if (inBuffer(interpolationEnd, 0)) {
    yybegin(INTERPOLATION_END);
    yypushback(1);
    return VueTokenTypes.INTERPOLATION_EXPR;
  }
  if (interpolationStartPos <= 0) {
    interpolationStartPos = zzStartRead;
  }
}
<INTERPOLATION_END> [^] {
  yybegin(YYINITIAL);
  if (tryConsumeInterpolationBoundary(interpolationEnd)) {
    return VueTokenTypes.INTERPOLATION_END;
  }
  return XmlTokenType.XML_BAD_CHARACTER;
}
<UNTERMINATED_INTERPOLATION> ([^<&\$# \n\r\t\f]|(\\#)) { return XmlTokenType.XML_DATA_CHARACTERS; }
<YYINITIAL> ([^<&\$# \n\r\t\f]|(\\#)) {
  if (tryConsumeInterpolationBoundary(interpolationStart)) {
    if (inBuffer(interpolationEnd, 1)) {
      yybegin(INTERPOLATION_END);
    } else {
      yybegin(INTERPOLATION);
    }
    return VueTokenTypes.INTERPOLATION_START;
  }
  return XmlTokenType.XML_DATA_CHARACTERS;
}
<YYINITIAL, UNTERMINATED_INTERPOLATION> [^] { return XmlTokenType.XML_DATA_CHARACTERS; }
[^] { return XmlTokenType.XML_BAD_CHARACTER; }
