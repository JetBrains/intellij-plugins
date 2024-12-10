package org.angular2.lang.html.lexer;

import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lexer.FlexLexer;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlTokenType;
import org.angular2.codeInsight.blocks.Angular2HtmlBlockUtils;
import org.angular2.lang.expr.parser.Angular2EmbeddedExprTokenType;
import org.angular2.lang.html.Angular2TemplateSyntax;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
%%

%unicode

%{
  private boolean tokenizeExpansionForms;
  private boolean enableBlockSyntax;
  private boolean enableLetSyntax;

  private String interpolationStart;
  private String interpolationEnd;

  public int expansionFormNestingLevel;
  public int interpolationStartPos;
  private boolean inInterpolationComment;
  private Character interpolationQuote;

  public String blockName;
  public int parameterIndex;
  public int parameterStart;
  public int blockParenLevel;

  public _Angular2HtmlLexer(@NotNull Angular2TemplateSyntax templateSyntax,
                            @Nullable Pair<String, String> interpolationConfig) {
    this(null);
    if (interpolationConfig == null) {
      interpolationStart = "{{";
      interpolationEnd = "}}";
    } else {
      interpolationStart = interpolationConfig.first;
      interpolationEnd = interpolationConfig.second;
    }
    this.tokenizeExpansionForms = templateSyntax.getTokenizeExpansionForms();
    this.enableBlockSyntax = templateSyntax.getEnableBlockSyntax();
    this.enableLetSyntax = templateSyntax.getEnableLetSyntax();
  }

  private boolean tryConsumeInterpolationBoundary(String boundary) {
    if (inBuffer(boundary, 0)) {
      zzMarkedPos += boundary.length() - 1;
      interpolationStartPos = -1;
      return true;
    }
    return false;
  }

  private boolean inBuffer(String text, int offset) {
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

  private void processInterpolationEntity() {
    CharSequence entity = yytext();
    char ch;
    if (StringUtil.equals(entity, "&quot;") || StringUtil.equals(entity, "&#34;") || StringUtil.equals(entity, "&#x22;")) {
      ch = '\"';
    } else if (StringUtil.equals(entity, "&apos;") || StringUtil.equals(entity, "&#39;") || StringUtil.equals(entity, "&#x27;")) {
      ch = '\'';
    } else {
      return;
    }
    processQuoteWithinInterpolation(ch);
  }

  private boolean processInterpolationChar(int nextStateIfEnd) {
    if (interpolationQuote == null && inBuffer(interpolationEnd, 0)) {
      yybegin(nextStateIfEnd);
      yypushback(1);
      return true;
    }
    if (interpolationStartPos <= 0) {
      interpolationStartPos = zzStartRead;
      inInterpolationComment = false;
    }
    if (inInterpolationComment) return false;

    processQuoteWithinInterpolation(zzBuffer.charAt(zzMarkedPos - 1));
    return false;
  }

  private void processQuoteWithinInterpolation(char ch) {
    if (interpolationQuote != null) {
      if (interpolationQuote == ch) {
        interpolationQuote = null;
      }
    } else {
      if (ch == '\"' || ch == '\'' || ch =='`') {
        interpolationQuote = ch;
      }
    }
  }

  private boolean isWithinInterpolation() {
    return zzLexicalState == INTERPOLATION
      || zzLexicalState == INTERPOLATION_DQ
      || zzLexicalState == INTERPOLATION_SQ;
  }

  public int getExpansionFormNestingLevel() {
    return expansionFormNestingLevel;
  }

  public void setExpansionFormNestingLevel(int level) {
    expansionFormNestingLevel = level;
  }

  private void consumeLetString() {
    char quote = zzBuffer.charAt(zzMarkedPos - 1);
    if (quote != '\'' && quote != '"') throw new IllegalStateException("Wrong quote style: " + quote);
    while (zzMarkedPos < zzEndRead) {
      char ch = zzBuffer.charAt(zzMarkedPos);
      if (ch == '\\' && zzMarkedPos + 1 < zzEndRead) {
        zzMarkedPos += 2;
      } else if (ch == quote){
        zzMarkedPos++;
        break;
      } else {
        zzMarkedPos++;
      }
    }
  }

%}

%class _Angular2HtmlLexer
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

%state EXPANSION_FORM_CONTENT
%state EXPANSION_FORM_CASE_END
%state INTERPOLATION
%state UNTERMINATED_INTERPOLATION
%state INTERPOLATION_END
%state INTERPOLATION_SQ
%state INTERPOLATION_DQ
%state UNTERMINATED_INTERPOLATION_SQ
%state UNTERMINATED_INTERPOLATION_DQ
%state INTERPOLATION_END_SQ
%state INTERPOLATION_END_DQ

%state BLOCK_NAME
%state BLOCK_PARAMETERS_START
%state BLOCK_PARAMETER
%state BLOCK_PARAMETER_END
%state BLOCK_PARAMETERS_END
%state BLOCK_START

%state LET_WHITESPACE
%state LET_NAME
%state LET_EQ
%state LET_VALUE
%state LET_VALUE_END

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
    return Angular2HtmlTokenTypes.INTERPOLATION_START;
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
    return Angular2HtmlTokenTypes.INTERPOLATION_START;
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

<INTERPOLATION_DQ, INTERPOLATION_SQ> [^] {
  if (processInterpolationChar(yystate() == INTERPOLATION_DQ ? INTERPOLATION_END_DQ : INTERPOLATION_END_SQ)) {
    return Angular2EmbeddedExprTokenType.INTERPOLATION_EXPR;
  }
}

<INTERPOLATION_END_DQ, INTERPOLATION_END_SQ> [^] {
  yybegin(yystate() == INTERPOLATION_END_DQ ? ATTRIBUTE_VALUE_DQ : ATTRIBUTE_VALUE_SQ);
  if (tryConsumeInterpolationBoundary(interpolationEnd)) {
    return Angular2HtmlTokenTypes.INTERPOLATION_END;
  }
  return XmlTokenType.XML_BAD_CHARACTER;
}
<EXPANSION_FORM_CASE_END> [^] {
  expansionFormNestingLevel--;
  yypushback(1);
  yybegin(EXPANSION_FORM_CONTENT);
}
<EXPANSION_FORM_CONTENT> "," { return XmlTokenType.XML_COMMA; }
<EXPANSION_FORM_CONTENT> "{" {
  expansionFormNestingLevel++;
  yybegin(YYINITIAL);
  return Angular2HtmlTokenTypes.EXPANSION_FORM_CASE_START;
}
<EXPANSION_FORM_CONTENT> "}" {
  yybegin(YYINITIAL);
  return Angular2HtmlTokenTypes.EXPANSION_FORM_END;
}
<EXPANSION_FORM_CONTENT> {WHITE_SPACE_CHARS} { return XmlTokenType.XML_WHITE_SPACE; }
<EXPANSION_FORM_CONTENT> [^ \n\r\t\f\u2028\u2029\u0085,{}] { return XmlTokenType.XML_DATA_CHARACTERS; }

"&lt;" |
"&gt;" |
"&apos;" |
"&quot;" |
"&nbsp;" |
"&amp;" |
"&#"{DIGIT}+";" |
"&#"(x|X)({DIGIT}|[a-fA-F])+";" { if (!isWithinInterpolation()) return XmlTokenType.XML_CHAR_ENTITY_REF; else processInterpolationEntity(); }
"&"{TAG_NAME}";" { if (!isWithinInterpolation()) return XmlTokenType.XML_ENTITY_REF_TOKEN; else processInterpolationEntity(); }

<INTERPOLATION> [^] {
  if (processInterpolationChar(INTERPOLATION_END)) {
    return Angular2EmbeddedExprTokenType.INTERPOLATION_EXPR;
  }
}
<INTERPOLATION_END> [^] {
  yybegin(YYINITIAL);
  if (tryConsumeInterpolationBoundary(interpolationEnd)) {
    return Angular2HtmlTokenTypes.INTERPOLATION_END;
  }
  return XmlTokenType.XML_BAD_CHARACTER;
}
<INTERPOLATION, INTERPOLATION_SQ, INTERPOLATION_DQ> \\[^] {
  // consume escaped char
}
<INTERPOLATION, INTERPOLATION_SQ, INTERPOLATION_DQ> \/\/ {
  // comment start
  inInterpolationComment = true;
}
<UNTERMINATED_INTERPOLATION> ([^<&\$# \n\r\t\f]|(\\#)) { return XmlTokenType.XML_DATA_CHARACTERS; }

<YYINITIAL> "@"[a-zA-Z0-9_] {
  if (enableLetSyntax && inBuffer("let", 0)) {
    zzMarkedPos += 2;
    yybegin(LET_WHITESPACE);
    return Angular2HtmlTokenTypes.BLOCK_NAME;
  } else if (enableBlockSyntax) {
    yypushback(2);
    yybegin(BLOCK_NAME);
  } else {
    return XmlTokenType.XML_DATA_CHARACTERS;
  }
}
<YYINITIAL> "@" {
  if (enableBlockSyntax) {
    blockName = "";
    return Angular2HtmlTokenTypes.BLOCK_NAME;
  } else {
    return XmlTokenType.XML_DATA_CHARACTERS;
  }
}
<BLOCK_NAME> "@"[a-zA-Z0-9_\t ]*[a-zA-Z0-9_] {
  blockName = Angular2HtmlBlockUtils.INSTANCE.toCanonicalBlockName(yytext().toString());
  yybegin(BLOCK_PARAMETERS_START);
  return Angular2HtmlTokenTypes.BLOCK_NAME;
}
<BLOCK_PARAMETERS_START, BLOCK_START> {WHITE_SPACE_CHARS} { return XmlTokenType.XML_WHITE_SPACE; }

<BLOCK_PARAMETERS_START> {
  "(" {
      yybegin(BLOCK_PARAMETER);
      blockParenLevel = 1;
      parameterIndex = 0;
      parameterStart = zzMarkedPos;
      return Angular2HtmlTokenTypes.BLOCK_PARAMETERS_START;
  }
}
<BLOCK_PARAMETERS_START, BLOCK_START> {
  ")" {
      yybegin(BLOCK_START);
      return Angular2HtmlTokenTypes.BLOCK_PARAMETERS_END;
  }
  "{" {
    yybegin(YYINITIAL);
    return Angular2HtmlTokenTypes.BLOCK_START;
  }
  [^] {
    yypushback(1);
    yybegin(YYINITIAL);
  }
}
<BLOCK_PARAMETER> {
  ")" {
     if (--blockParenLevel <= 0) {
       yypushback(1);
       yybegin(BLOCK_PARAMETERS_END);
       if (parameterStart < zzMarkedPos)
          return Angular2EmbeddedExprTokenType.createBlockParameter(blockName, parameterIndex);
     }
  }
  "(" {
     blockParenLevel++;
  }
  "@" {
       // Angular 2 expression cannot contain an `@` character
       yypushback(1);
       yybegin(YYINITIAL);
       if (parameterStart < zzMarkedPos)
          return Angular2EmbeddedExprTokenType.createBlockParameter(blockName, parameterIndex);
      }
  ";" {
      yypushback(1);
      blockParenLevel = 1;
      yybegin(BLOCK_PARAMETER_END);
      if (parameterStart < zzMarkedPos)
         return Angular2EmbeddedExprTokenType.createBlockParameter(blockName, parameterIndex++);
      else
         parameterIndex++;
    }
  "\\"[^]
  | "'"([^\\\']|\\[^])*("'"|\\)?
  | "\""([^\\\"]|\\[^])*("\""|\\)?
  | "`"([^\\`]|\\[^])*("`"|\\)? {}
  [^] {}
  <<EOF>> {
    yybegin(YYINITIAL);
    if (parameterStart < zzMarkedPos)
       return Angular2EmbeddedExprTokenType.createBlockParameter(blockName, parameterIndex);
  }
}
<BLOCK_PARAMETER_END> {
  ";" {
    parameterStart = zzMarkedPos;
    yybegin(BLOCK_PARAMETER);
    return Angular2HtmlTokenTypes.BLOCK_SEMICOLON;
  }
}
<BLOCK_PARAMETERS_END> {
  ")" {
      yybegin(BLOCK_START);
      return Angular2HtmlTokenTypes.BLOCK_PARAMETERS_END;
  }
}
<LET_WHITESPACE> {
  {WHITE_SPACE_CHARS} {
    yybegin(LET_NAME);
    return XmlTokenType.XML_WHITE_SPACE;
  }
  [^] {
    yybegin(YYINITIAL);
    yypushback(1);
  }
}
<LET_NAME> {
  [a-zA-Z$_][a-zA-Z$_0-9]* {
    yybegin(LET_EQ);
  }
  [^] {
    yybegin(YYINITIAL);
    yypushback(1);
  }
}
<LET_EQ> {
  [\x09-\x20\xA0]* "=" { // tab-space, nbsp
    yybegin(LET_VALUE);
  }
  [^] {
    yybegin(YYINITIAL);
    yypushback(1);
    return Angular2EmbeddedExprTokenType.createBlockParameter("let", 0);
  }
}
<LET_VALUE> {
  [[^;'\"]\R]* {
    // consume
  }
  ['\"] {
    consumeLetString();
  }
  ";" {
    yybegin(LET_VALUE_END);
    yypushback(1);
    return Angular2EmbeddedExprTokenType.createBlockParameter("let", 0);
  }
  <<EOF>> {
    yybegin(YYINITIAL);
    return Angular2EmbeddedExprTokenType.createBlockParameter("let", 0);
  }
}
<LET_VALUE_END> {
  ";" {
    yybegin(YYINITIAL);
    return Angular2HtmlTokenTypes.BLOCK_SEMICOLON;
  }
  [^] {
    yybegin(YYINITIAL);
    yypushback(1);
  }
}

<YYINITIAL> ([^<&\$# \n\r\t\f]|(\\#)) {
  if (tryConsumeInterpolationBoundary(interpolationStart)) {
    if (inBuffer(interpolationEnd, 1)) {
      yybegin(INTERPOLATION_END);
    } else {
      yybegin(INTERPOLATION);
    }
    return Angular2HtmlTokenTypes.INTERPOLATION_START;
  }
  switch (zzBuffer.charAt(zzStartRead)) {
    case '{':
      if (tokenizeExpansionForms) {
        yybegin(EXPANSION_FORM_CONTENT);
        return Angular2HtmlTokenTypes.EXPANSION_FORM_START;
      }
    case '}':
      if (expansionFormNestingLevel > 0) {
        yybegin(EXPANSION_FORM_CASE_END);
        return Angular2HtmlTokenTypes.EXPANSION_FORM_CASE_END;
      } else if (enableBlockSyntax) {
        return Angular2HtmlTokenTypes.BLOCK_END;
      }
  }
  return XmlTokenType.XML_DATA_CHARACTERS;
}
<YYINITIAL, UNTERMINATED_INTERPOLATION> [^] { return XmlTokenType.XML_DATA_CHARACTERS; }
[^] { return XmlTokenType.XML_BAD_CHARACTER; }
