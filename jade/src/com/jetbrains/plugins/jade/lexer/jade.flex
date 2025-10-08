package com.jetbrains.plugins.jade.lexer;

import java.util.Stack;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import com.jetbrains.plugins.jade.psi.JadeTokenTypes;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.openapi.util.text.StringUtil;

/* Auto generated File */
%%

%class _JadeLexer
%implements FlexLexer
%unicode
%public

%function advance
%type IElementType

%{

  private static class BlockInfo {
    int indent;
    IElementType tokenType;
    int parentState;
    State state;

    public BlockInfo(int indent, IElementType tokenType, int parentState, State state) {
      this.indent = indent;
      this.tokenType = tokenType;
      this.parentState = parentState;
      this.state = state;
    }

    public enum State {
      PREDICTED,
      CREATED,
      STARTED_NO_INDENT,
      STARTED
    }
  }

  private CharSequence tagName;
  protected int myTabSize;

  private final Stack<Integer> stateToGo = new Stack<>();
  private final Stack<String> parenthesisStack = new Stack<>();
  private final Stack<BlockInfo> blockStack = new Stack<>();
  private StringBuilder valueTail = new StringBuilder();

  private char attributeNameQuoteType = '"';

  private boolean isMixin = false;
  private IElementType myForcedWhitespaceType;
  private int whiteSpacesToEat;


  protected void resetInternal(int tabSize) {
    stateToGo.clear();
    parenthesisStack.clear();
    blockStack.clear();

    tagName = null;
    myTabSize = tabSize;
  }

  private void indentDone() {
    if (yystate() == YYINITIAL) {
      yybegin(INDENT_DONE);
    }
  }

  private int calcNextLineIndent() {
    return IndentUtil.calcIndent(zzBuffer, getTokenStart() + 1, myTabSize);
  }

  private int calcCurrentLineIndent(int offsetFromTokenStart) {
    int loc = getTokenStart() + offsetFromTokenStart;
    while (loc >= 0 && zzBuffer.charAt(loc) != '\n') {
      loc--;
    }
    return IndentUtil.calcIndent(zzBuffer, loc + 1, myTabSize);
  }

  private void pushBackUpTo(char c) {
    int loc = getTokenEnd();
    while (zzBuffer.charAt(loc - 1) != c) {
      loc--;
    }
    yypushback(getTokenEnd() - loc + 1);
  }

  private void pushBackWhile(String chars) {
    int i = 0;
    while (chars.indexOf(zzBuffer.charAt(getTokenEnd() - i - 1)) != -1) {
      i++;
    }
    yypushback(i);
    whiteSpacesToEat = i;
  }

  private IElementType startBlock(IElementType type, BlockInfo.State state) {
    return startBlock(type, state, calcCurrentLineIndent(0));
  }

  private IElementType startBlock(IElementType type, BlockInfo.State state, int blockIndent) {
    if (!blockStack.isEmpty() && blockStack.peek().state == BlockInfo.State.PREDICTED) {
      pushTopBlockState();
      return blockStack.peek().tokenType;
    }
    blockStack.push(new BlockInfo(blockIndent, type, yystate(), state));

    if (state == BlockInfo.State.STARTED || state == BlockInfo.State.STARTED_NO_INDENT) {
      yybegin(BLOCK);
    }
    return type;
  }

  private void pushTopBlockState() {
    BlockInfo topBlock = blockStack.peek();
    if (topBlock == null) return;
    if (topBlock.state == BlockInfo.State.STARTED_NO_INDENT ||
        topBlock.state == BlockInfo.State.CREATED) {
      topBlock.state = BlockInfo.State.STARTED;
    }
    else if (topBlock.state == BlockInfo.State.PREDICTED) {
      topBlock.state = BlockInfo.State.CREATED;
    }
  }

  private IElementType endBlockOrContinueOnNewline() {
    final int nextLineIndent = calcNextLineIndent();
    boolean deletedSomething = false;
    int stateToGo = 0;

    while (!blockStack.isEmpty()) {
      BlockInfo topBlock = blockStack.peek();

      if (topBlock.state == BlockInfo.State.STARTED && nextLineIndent >= topBlock.indent
        || topBlock.state == BlockInfo.State.STARTED_NO_INDENT && nextLineIndent > topBlock.indent) {
        if (deletedSomething)
          break;

        updateBlockIndent(topBlock, nextLineIndent);
        return topBlock.tokenType;
      }

      blockStack.pop();
      deletedSomething = true;

      stateToGo = getParentStateToGo(topBlock.parentState);
    }

    return optionalPrecedingWhitespace(JadeTokenTypes.EOL, stateToGo, JadeTokenTypes.INDENT);
  }

  private int getParentStateToGo(int state) {
    if (state != BLOCK) {
      return YYINITIAL;
    }
    return state;
  }

  private IElementType getCurrentBlockTokenType() {
    if (blockStack.isEmpty())
      return JadeTokenTypes.TEXT;
    else
      return blockStack.peek().tokenType;
  }

  private void updateBlockIndent(BlockInfo block, int indent) {
    if (block.state == BlockInfo.State.CREATED || block.state == BlockInfo.State.STARTED_NO_INDENT) {
      pushTopBlockState();
      block.indent = indent;
    }
  }

  private IElementType doRegularEol() {
    yybegin(YYINITIAL);
    tagName = null;
    return JadeTokenTypes.EOL;
  }

  private IElementType processEol() {
    tagName = null;
    if (!blockStack.isEmpty()) {
      BlockInfo topBlock = blockStack.peek();
      if (topBlock.state == BlockInfo.State.CREATED || topBlock.state == BlockInfo.State.STARTED_NO_INDENT) {
        int nextLineIndent = calcNextLineIndent();
        if (nextLineIndent > topBlock.indent) {
          updateBlockIndent(topBlock, nextLineIndent);
          yybegin(PRECEDING_INDENT);
          return JadeTokenTypes.EOL;
        }
        else {
          blockStack.pop();
        }
      }
    }

    return doRegularEol();
  }

  private IElementType optionalPrecedingWhitespace(IElementType tokenType, int state) {
    return optionalPrecedingWhitespace(tokenType, state, TokenType.WHITE_SPACE);
  }

  private IElementType optionalPrecedingWhitespace(IElementType tokenType, int state, IElementType forcedWhitespaceType) {
    myForcedWhitespaceType = forcedWhitespaceType;
    if (Character.isWhitespace(zzBuffer.charAt(getTokenEnd()-1))) {
      stateToGo.push(state);
      yybegin(PRECEDING_WHITESPACE);
      pushBackWhile(" \t\f");
      return tokenType;
    }
    else {
      yybegin(state);
      return tokenType;
    }
  }

  private boolean isScriptOrStyleTag() {
    String s;
    return tagName != null && ((s = tagName.toString()).equals("script") || s.equals("style"));
  }

  private IElementType processClosingBracket(String exitBracket, IElementType exitBracketType, IElementType defaultType) {
    String bracket = yytext().toString();
    String paired = "";
    if (bracket.equals(")")) paired = "(";
    if (bracket.equals("]")) paired = "[";
    if (bracket.equals("}")) paired = "{";
    // We need to treat conditional as brackets to ensure that
    // we continue lexing the attribute value correctly
    if (bracket.equals(":")) {
      paired = "?";
      if (parenthesisStack.isEmpty() || !parenthesisStack.peek().equals(paired)) {
        return defaultType;
      }
    }

    if (parenthesisStack.isEmpty()) {
      if (bracket.equals(exitBracket)) {
        return exitBracketType;
      }
      return JadeTokenTypes.BAD_CHARACTER;
    }
    if (!parenthesisStack.peek().equals(paired)) {
      return JadeTokenTypes.BAD_CHARACTER;
    }
    parenthesisStack.pop();

    return defaultType;
  }

  private void beginTag() {
    if (yystate() != TAG) {
      isMixin = false;
    }
    yybegin(TAG);
  }

  private void clearValueTail() {
    valueTail = new StringBuilder();
  }

  private boolean isEmptyTail() {
    return valueTail.length() == 0;
  }

  private void updateValueTail() {
    valueTail.append(yytext());
    if (valueTail.length() > 20) {
      valueTail = new StringBuilder(valueTail.substring(valueTail.length() - 10));
    }
  }

  private boolean canFinishValue() {
    if (!parenthesisStack.isEmpty()) {
      return false;
    }

    if (isEmptyTail()) {
      return false;
    }

    String tail = valueTail.toString();
    if (tail.endsWith("+")) return false;
    if (tail.endsWith("*")) return false;
    if (tail.endsWith("/")) return false;
    if (tail.endsWith("-")) return false;
    if (tail.endsWith("^")) return false;
    if (tail.endsWith("&")) return false;
    if (tail.endsWith("|")) return false;
    if (tail.endsWith("%")) return false;
    if (tail.endsWith("<")) return false;
    if (tail.endsWith(">")) return false;
    if (tail.endsWith("=")) return false;
    if (tail.endsWith("~")) return false;
    if (tail.endsWith("new")) return false;
    if (tail.endsWith("delete")) return false;
    if (tail.endsWith("return")) return false;
    if (tail.endsWith("?")) return false;
    if (tail.endsWith(":")) return false;
    return true;
  }
%}

LineTerminator = \n
InputCharacter = [^\n]

WhiteSpace = [ \t\f]+
WhiteSpaceOrNewlines = ({WhiteSpace}|{LineTerminator})+

Identifier   = [a-zA-Z_][a-zA-Z0-9_]*
IdentifierEx = [a-zA-Z_][a-zA-Z0-9_\-]*
// Copied roughly from http://www.w3.org/TR/CSS21/grammar.html#scanner
CssIdentifier = -?([_a-zA-Z]|[\240-\377]|\\[0-9a-fA-F]{1,6}(\r\n|[ \t\r\n\f])?|\\[^\r\n\f0-9a-fA-F])([_a-zA-Z0-9-]|[\240-\377]|\\[0-9a-fA-F]{1,6}(\r\n|[ \t\r\n\f])?|\\[^\r\n\f0-9a-fA-F])*
CssName = ([_a-zA-Z0-9-]|[\240-\377]|\\[0-9a-fA-F]{1,6}(\r\n|[ \t\r\n\f])?|\\[^\r\n\f0-9a-fA-F])+

Digit=[0-9]
//CharLiteral="'"([^\'\n]|\\\n)*("'")?
CharLiteral="'"([^\'\n]|\\\n)*"'"
StringContent=([^\\\"\n]|"\\\n"|"\\n"|"\\\"")*
//StringLiteral=\"{StringContent}(\")?
StringLiteral=\"{StringContent}\"
Es6StringLiteral=([a-zA-Z_][a-zA-Z0-9_.]*)?`[^`]*`
Comment = "//" {InputCharacter}*
UnbufferedComment = "//-" {InputCharacter}*
ConditionalStatement = "if"|("else" {WhiteSpace} "if")|"until"|"while"|"unless"
IterationStatement = "each"|"for"
AttributeNameNoParen = ([^ \n\r\t\f\"\'<>/=()\[\],!])+
AttributeNameBrackets =  "[" {AttributeNameNoParen} "]" | {AttributeNameNoParen}
AttributeNameParen =  "(" {AttributeNameBrackets} ")" | {AttributeNameBrackets}
AttributeName = "[" {AttributeNameParen} "]" | {AttributeNameParen}
AttributeNameWithQuotes = {AttributeName} | \"{AttributeName}\" | "'"{AttributeName}"'"
AttributeValueEnd = "@" | ":"| ")" | ({IdentifierEx} | \"{AttributeName}\" | "'"{AttributeName}"'" ({WhiteSpaceOrNewlines}? ("!"? "=" | ")") | {WhiteSpaceOrNewlines}))
Interpolation = ("#" | "!") "{" [^\n}]* "}"

%state INDENT_DONE, TEXT, ATTRIBUTES, MIXIN_ATTRIBUTES, BLOCK, JS_META_BLOCK, TAG, ATTRIBUTE_VALUE, STRING
%state INTERPOLATION, CODE, EXPR, EACH_EXPR, WHEN_EXPR, PRECEDING_WHITESPACE, FILE_PATH, PRECEDING_INDENT, CLASS
%state ATTRIBUTE_NAME_WITH_QUOTES, FILTER, INCLUDE_WITH_FILTER, MIXIN_DECL, JS_MIXIN_PARAMS

%%

<YYINITIAL> {
  {WhiteSpace} { indentDone(); return JadeTokenTypes.INDENT; }
}

<TEXT> {
  {WhiteSpace} { return JadeTokenTypes.TEXT; }
}

<INDENT_DONE, TAG> {
  {WhiteSpace} {
    if (isScriptOrStyleTag()) {
      startBlock(tagName.toString().equals("script") ? JadeTokenTypes.JS_CODE_BLOCK : JadeTokenTypes.STYLE_BLOCK,
                       BlockInfo.State.STARTED, 1000);
      tagName = null;

      return TokenType.WHITE_SPACE;

    } else {
      yybegin(TEXT); return TokenType.WHITE_SPACE;
    }

  }
}

<YYINITIAL, INDENT_DONE> {
  ":"{Identifier} {
    startBlock(JadeTokenTypes.FILTER_CODE, BlockInfo.State.CREATED);
    yypushback(yylength() - 1);
    yybegin(FILTER);
    return JadeTokenTypes.COLON;
  }
}

<FILTER> {
  {IdentifierEx} { return JadeTokenTypes.FILTER_NAME; }
  "(" { stateToGo.push(yystate()); yybegin(ATTRIBUTES); return JadeTokenTypes.LPAREN; }
}

<INCLUDE_WITH_FILTER> {
  ":" { return JadeTokenTypes.COLON; }
  {IdentifierEx} { return JadeTokenTypes.FILTER_NAME; }
  "(" { stateToGo.push(yystate()); yybegin(ATTRIBUTES); return JadeTokenTypes.LPAREN; }
  {WhiteSpace} { yybegin(FILE_PATH); return TokenType.WHITE_SPACE; }
}

<TEXT, BLOCK, JS_META_BLOCK> {
  {WhiteSpace} |
  [^\n\#\\! \t\f]+ |
  "\\"+("#"|"!") { return getCurrentBlockTokenType(); }
  . { return getCurrentBlockTokenType(); }
}

<TEXT, BLOCK, JS_META_BLOCK> {
  {Interpolation} {
    if (yystate() == TEXT || getCurrentBlockTokenType() == JadeTokenTypes.TEXT) {
      yypushback(yylength()-2);
      stateToGo.push(yystate());
      yybegin(INTERPOLATION);
      return JadeTokenTypes.TEXT;
    }
    else {
      return getCurrentBlockTokenType();
    }
  }
}

<INTERPOLATION> {
  "("|"{"|"[" { parenthesisStack.push(yytext().toString()); return JadeTokenTypes.JS_EXPR; }

  ")"|"}"|"]" {
    IElementType result = processClosingBracket("}", JadeTokenTypes.TEXT, JadeTokenTypes.JS_EXPR);
    if (result == JadeTokenTypes.TEXT) {
      yybegin(stateToGo.pop());
    }
    return result;
  }

  [^\n({\[\]})]+ { return JadeTokenTypes.JS_EXPR; }
}

<BLOCK> {
  {LineTerminator} / {WhiteSpace}? ":" {Identifier} {
    endBlockOrContinueOnNewline();
    startBlock(JadeTokenTypes.FILTER_CODE, BlockInfo.State.PREDICTED);
    return doRegularEol();
  }
  {LineTerminator} {WhiteSpace}? {
    boolean hasWhitespace = yylength() > 1;

    IElementType recoverType = endBlockOrContinueOnNewline();
    if (!hasWhitespace && yystate() == PRECEDING_WHITESPACE) {
      yybegin(stateToGo.pop());
    }
    return recoverType;
  }
}

<JS_META_BLOCK> {
  {LineTerminator} {WhiteSpace}? [^- \t\f\n] {
    yypushback(1);
    boolean hasWhitespace = yylength() > 1;

    IElementType recoverType = endBlockOrContinueOnNewline();
    if (!hasWhitespace && yystate() == PRECEDING_WHITESPACE) {
      yybegin(stateToGo.pop());
      yypushback(yylength());
    }
    else if (!blockStack.isEmpty() && calcNextLineIndent() == blockStack.peek().indent) {
      blockStack.pop();
      doRegularEol();
      yypushback(yylength());
    }
    return recoverType;
  }
  {LineTerminator} {WhiteSpace}? [-\n] {
    yypushback(1);
    boolean hasWhitespace = yylength() > 1;

    IElementType recoverType = endBlockOrContinueOnNewline();
    if (!hasWhitespace && yystate() == PRECEDING_WHITESPACE) {
      yybegin(stateToGo.pop());
    }
    return recoverType;
  }

  {LineTerminator} {WhiteSpace}? {
    return JadeTokenTypes.EOL;
  }
}

<MIXIN_ATTRIBUTES, ATTRIBUTES> {
  {WhiteSpace} { return TokenType.WHITE_SPACE; }
  {LineTerminator} { return JadeTokenTypes.EOL; }
  ")" { yybegin(stateToGo.pop()); return JadeTokenTypes.RPAREN; }
}

<MIXIN_ATTRIBUTES> {
  {AttributeNameWithQuotes} {WhiteSpaceOrNewlines}? ("="|"!=") {
    yybegin(ATTRIBUTES);
    yypushback(yylength());
  }

  . {
    stateToGo.push(yystate());
    clearValueTail();
    yybegin(ATTRIBUTE_VALUE);
    yypushback(yylength());
  }
}

<ATTRIBUTES> {
  \"  / {AttributeName}\"  { attributeNameQuoteType = '"'; yybegin(ATTRIBUTE_NAME_WITH_QUOTES); return JadeTokenTypes.DOUBLE_QUOTE; }
  "'" / {AttributeName}"'" { attributeNameQuoteType = '\''; yybegin(ATTRIBUTE_NAME_WITH_QUOTES); return JadeTokenTypes.DOUBLE_QUOTE; }

  {AttributeName} { return JadeTokenTypes.ATTRIBUTE_NAME; }

  "=" { parenthesisStack.clear(); stateToGo.push(yystate()); clearValueTail(); yybegin(ATTRIBUTE_VALUE); return JadeTokenTypes.EQ; }
  "!=" { parenthesisStack.clear(); stateToGo.push(yystate()); clearValueTail(); yybegin(ATTRIBUTE_VALUE); return JadeTokenTypes.NEQ; }
}

<ATTRIBUTE_NAME_WITH_QUOTES> {
  {AttributeName} { return JadeTokenTypes.ATTRIBUTE_NAME; }
  \" {
      if (attributeNameQuoteType == '"') {
        yybegin(ATTRIBUTES);
        return JadeTokenTypes.DOUBLE_QUOTE;
      }
      else {
        return JadeTokenTypes.BAD_CHARACTER;
      }
    }
  "'" {
      if (attributeNameQuoteType == '\'') {
        yybegin(ATTRIBUTES);
        return JadeTokenTypes.DOUBLE_QUOTE;
      }
      else {
        return JadeTokenTypes.BAD_CHARACTER;
      }
    }
  [^] { yybegin(ATTRIBUTES); return JadeTokenTypes.BAD_CHARACTER; }
}

<ATTRIBUTE_VALUE> {
  "("|"{"|"["|"?" { updateValueTail(); parenthesisStack.push(yytext().toString()); return JadeTokenTypes.JS_EXPR; }

  ")"|"}"|"]"|":" {
    updateValueTail();
    IElementType result = processClosingBracket(")", JadeTokenTypes.RPAREN, JadeTokenTypes.JS_EXPR);
    if (result == JadeTokenTypes.RPAREN) {
      stateToGo.pop();
      yybegin(stateToGo.pop());
    }
    return result;
  }
  "," { if (parenthesisStack.isEmpty()) {
          yybegin(stateToGo.pop());
          return JadeTokenTypes.COMMA;
        }
        updateValueTail();
        return JadeTokenTypes.JS_EXPR;
      }
  {WhiteSpace} / {WhiteSpaceOrNewlines}? {AttributeValueEnd} { if (canFinishValue()) {
                                         yybegin(stateToGo.pop());
                                         return TokenType.WHITE_SPACE;
                                       }
                                       return isEmptyTail() ? TokenType.WHITE_SPACE : JadeTokenTypes.JS_EXPR;
                                     }
  {LineTerminator} / {WhiteSpaceOrNewlines}? {AttributeValueEnd} { if (canFinishValue()) {
                                           yybegin(stateToGo.pop());
                                           return JadeTokenTypes.EOL;
                                         }
                                         return isEmptyTail() ? JadeTokenTypes.EOL : JadeTokenTypes.JS_EXPR;
                                       }

  {WhiteSpace} { return isEmptyTail() ? TokenType.WHITE_SPACE : JadeTokenTypes.JS_EXPR; }
  {LineTerminator} { return isEmptyTail() ? JadeTokenTypes.EOL : JadeTokenTypes.JS_EXPR; }

  {CharLiteral}   |
  {StringLiteral} |
  {Es6StringLiteral} |
  . { updateValueTail(); return JadeTokenTypes.JS_EXPR; }
}

<STRING> {
  "\"" {
         yybegin(stateToGo.pop());
         return XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER;
       }
  {StringContent} { return JadeTokenTypes.JS_EXPR; }
}

<YYINITIAL, INDENT_DONE> {
  "doctype"|"!!!" { beginTag(); return JadeTokenTypes.DOCTYPE_KEYWORD; }
  "|" " "? { return optionalPrecedingWhitespace(JadeTokenTypes.PIPE, TEXT); }
  "<" { yybegin(TEXT); return JadeTokenTypes.TEXT; }

  "default"{WhiteSpace}?":" {
    yypushback(yylength() - "default".length());
    yybegin(WHEN_EXPR);
    return JadeTokenTypes.DEFAULT_KEYWORD;
  }
  "default" / {WhiteSpaceOrNewlines} { return JadeTokenTypes.DEFAULT_KEYWORD; }
  {ConditionalStatement} {WhiteSpaceOrNewlines} { return optionalPrecedingWhitespace(JadeTokenTypes.COND_KEYWORD, EXPR); }
  {ConditionalStatement} / "(" { return optionalPrecedingWhitespace(JadeTokenTypes.COND_KEYWORD, EXPR); }
  {IterationStatement} {WhiteSpace} { return optionalPrecedingWhitespace(JadeTokenTypes.JS_EACH_EXPR, EACH_EXPR, JadeTokenTypes.JS_EACH_EXPR); }
  "else" / {WhiteSpaceOrNewlines} { return JadeTokenTypes.ELSE_KEYWORD; }
  "case" {WhiteSpaceOrNewlines} { return optionalPrecedingWhitespace(JadeTokenTypes.CASE, EXPR); }
  "when" {WhiteSpaceOrNewlines} { return optionalPrecedingWhitespace(JadeTokenTypes.WHEN, WHEN_EXPR); }
  "extends" {WhiteSpaceOrNewlines} { return optionalPrecedingWhitespace(JadeTokenTypes.EXTENDS_KEYWORD, FILE_PATH); }
  "include" {WhiteSpaceOrNewlines} { return optionalPrecedingWhitespace(JadeTokenTypes.INCLUDE_KEYWORD, FILE_PATH); }
  "include" / ":" {IdentifierEx} { yybegin(INCLUDE_WITH_FILTER); return JadeTokenTypes.INCLUDE_KEYWORD; }
  "mixin" {WhiteSpaceOrNewlines} { return optionalPrecedingWhitespace(JadeTokenTypes.MIXIN_KEYWORD, MIXIN_DECL); }
  "yield" / {WhiteSpaceOrNewlines} { return JadeTokenTypes.YIELD_KEYWORD; }
  "-" {WhiteSpace}? {
    startBlock(JadeTokenTypes.JS_META_CODE, BlockInfo.State.STARTED);
    yybegin(JS_META_BLOCK);
    return JadeTokenTypes.JS_META_CODE;
  }
  {Identifier} {WhiteSpace} ("="|"!=")
  {
    if (tagName != null && StringUtil.contains(tagName, 0, tagName.length(), ':')) {
      beginTag();
      yypushback(2);
      pushBackWhile(" \t\f");
      return JadeTokenTypes.TAG_NAME;
    }
    else {
      yybegin(CODE);
      return JadeTokenTypes.JS_CODE_BLOCK_PATCHED;
    }
  }
}

<YYINITIAL, INDENT_DONE, TAG> {
  "=" {WhiteSpace}? { return optionalPrecedingWhitespace(JadeTokenTypes.EQ, EXPR); }
  "!=" {WhiteSpace}? { return optionalPrecedingWhitespace(JadeTokenTypes.NEQ, EXPR); }
  {IdentifierEx} |
  {IdentifierEx}":"{IdentifierEx} {
    beginTag();
    if (tagName == null) {
      tagName = yytext();
    }
    return JadeTokenTypes.TAG_NAME;
  }
  {Interpolation} {
    beginTag();
    if (tagName == null) {
      tagName = yytext();
    }

    yypushback(yylength()-2);
    stateToGo.push(yystate());
    yybegin(INTERPOLATION);
    return JadeTokenTypes.TEXT;
  }
  "+"{WhiteSpace}? / ({IdentifierEx} | {Interpolation}) { isMixin = true; return optionalPrecedingWhitespace(JadeTokenTypes.PLUS, TAG); }
}

<CODE> {
  {InputCharacter}* { return JadeTokenTypes.JS_CODE_BLOCK_PATCHED; }
}

<PRECEDING_WHITESPACE> {
  {WhiteSpace} {
    yybegin(stateToGo.pop());
    yypushback(yylength() - whiteSpacesToEat);
    return myForcedWhitespaceType;
  }
}

<PRECEDING_INDENT> {
  {WhiteSpace} {
    BlockInfo topBlock = blockStack.peek();
    yybegin(BLOCK);
    return JadeTokenTypes.INDENT;
  }
  {LineTerminator} { return JadeTokenTypes.EOL; }
}

<EXPR> {
  [^\n\t\f ]+ { return JadeTokenTypes.JS_EXPR; }
  {WhiteSpace} { return JadeTokenTypes.JS_EXPR; }
  {WhiteSpace}"\n" { yypushback(1); return TokenType.WHITE_SPACE; }
}

<EACH_EXPR> {
  [^\n\t\f ]+ { return JadeTokenTypes.JS_EACH_EXPR; }
  {WhiteSpace} { return JadeTokenTypes.JS_EACH_EXPR; }
  {WhiteSpace}"\n" { yypushback(1); return TokenType.WHITE_SPACE; }
}

<WHEN_EXPR> {
  [^:\n\t\f ]+ { return JadeTokenTypes.JS_EXPR; }
  {WhiteSpace} { return JadeTokenTypes.JS_EXPR; }
  {WhiteSpace}"\n" { yypushback(1); return TokenType.WHITE_SPACE; }
  ":" {WhiteSpace}? { return optionalPrecedingWhitespace(JadeTokenTypes.COLON, INDENT_DONE); }
}

<YYINITIAL, INDENT_DONE, TAG> {
  "#"{CssName} { beginTag(); return JadeTokenTypes.TAG_ID; }
  "."{CssIdentifier} { yybegin(CLASS); yypushback(yylength()-1); return JadeTokenTypes.DOT; }
  "(" {
    stateToGo.push(TAG);
    if (isMixin)
      yybegin(MIXIN_ATTRIBUTES);
    else
      yybegin(ATTRIBUTES);
    return JadeTokenTypes.LPAREN;
  }
}

<TAG> {
  "&attributes" / [^a-zA-Z0-9] { isMixin = true; return JadeTokenTypes.ATTRIBUTES_KEYWORD; }
  ":" {WhiteSpace}? { return optionalPrecedingWhitespace(JadeTokenTypes.COLON, INDENT_DONE); }

  ". "{InputCharacter}*"\n" |
  ".\n" {
    if (isScriptOrStyleTag()) {
      yypushback(1);
      startBlock(tagName.toString().equals("script") ? JadeTokenTypes.JS_CODE_BLOCK : JadeTokenTypes.STYLE_BLOCK,
                       BlockInfo.State.CREATED);

      return JadeTokenTypes.DOT;
    } else {
      yypushback(1); startBlock(JadeTokenTypes.TEXT, BlockInfo.State.CREATED); return JadeTokenTypes.DOT;
    }
  }
}

<CLASS> {
  {CssIdentifier} { beginTag(); return JadeTokenTypes.TAG_CLASS; }
}

<FILE_PATH> {
  [^\n\t\f ]+ { return JadeTokenTypes.FILE_PATH; }
  {WhiteSpace} { return JadeTokenTypes.FILE_PATH; }
  {WhiteSpace}"\n" { yypushback(1); return TokenType.WHITE_SPACE; }
}

<MIXIN_DECL> {
  {IdentifierEx} { return JadeTokenTypes.IDENTIFIER; }
  {WhiteSpace} { return TokenType.WHITE_SPACE; }
  "(" { yybegin(JS_MIXIN_PARAMS); parenthesisStack.clear(); return JadeTokenTypes.JS_MIXIN_PARAMS; }
  ")" { return JadeTokenTypes.RPAREN; }
}

<JS_MIXIN_PARAMS> {
  "("|"{"|"[" { parenthesisStack.push(yytext().toString()); return JadeTokenTypes.JS_MIXIN_PARAMS; }
  ")"|"}"|"]" {
    IElementType result = processClosingBracket(")", JadeTokenTypes.TEXT, JadeTokenTypes.JS_MIXIN_PARAMS);
    if (result == JadeTokenTypes.TEXT) {
      yybegin(MIXIN_DECL);
    }
    return JadeTokenTypes.JS_MIXIN_PARAMS;
  }
  {LineTerminator} { return processEol(); }
  {Identifier} |
  {WhiteSpace} |
  . { return JadeTokenTypes.JS_MIXIN_PARAMS; }
  {StringLiteral} |
  {CharLiteral} { indentDone(); return JadeTokenTypes.JS_MIXIN_PARAMS; }
}


<YYINITIAL, INDENT_DONE, ATTRIBUTES, TAG, PRECEDING_WHITESPACE, FILE_PATH, PRECEDING_INDENT, CLASS> {
  {StringLiteral} { indentDone(); return JadeTokenTypes.STRING_LITERAL; }
  {CharLiteral} { indentDone(); return JadeTokenTypes.CHAR_LITERAL; }

}

<YYINITIAL, INDENT_DONE, TAG, FILE_PATH, PRECEDING_INDENT, CLASS, FILTER, MIXIN_DECL> {
  {UnbufferedComment} { return startBlock(JadeTokenTypes.UNBUF_COMMENT, BlockInfo.State.STARTED_NO_INDENT); }
  {Comment} { return startBlock(JadeTokenTypes.COMMENT, BlockInfo.State.STARTED_NO_INDENT); }
}

<YYINITIAL, INDENT_DONE, TEXT, ATTRIBUTES, TAG, STRING, INTERPOLATION, CODE, EXPR, EACH_EXPR, WHEN_EXPR,
 PRECEDING_WHITESPACE, FILE_PATH, PRECEDING_INDENT, CLASS, FILTER, INCLUDE_WITH_FILTER, MIXIN_DECL> {
  {Digit}+ { indentDone(); return JadeTokenTypes.NUMBER; }
  ")" { indentDone(); return JadeTokenTypes.RPAREN; }
  "(" { beginTag(); return JadeTokenTypes.LPAREN; }
  "=" { indentDone(); return JadeTokenTypes.EQ; }
  "/" { indentDone(); return JadeTokenTypes.DIV; }
  "," { indentDone(); return JadeTokenTypes.COMMA; }
  "#" { indentDone(); return JadeTokenTypes.HASH; }
  [a-zA-Z_] { return JadeTokenTypes.IDENTIFIER; }
  "." { return JadeTokenTypes.DOT; }
  "+" { return JadeTokenTypes.PLUS; }

  {LineTerminator} { return processEol(); }
}

[^] { beginTag(); return JadeTokenTypes.BAD_CHARACTER; }
