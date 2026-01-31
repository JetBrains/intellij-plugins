// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.lang.WhitespacesBinders;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.NlsContexts.ParsingError;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlElementType;
import com.intellij.xml.util.HtmlUtil;
import com.jetbrains.plugins.jade.JadeBundle;
import com.jetbrains.plugins.jade.JadeLanguage;
import com.jetbrains.plugins.jade.lexer.IndentUtil;
import com.jetbrains.plugins.jade.psi.JadeElementTypes;
import com.jetbrains.plugins.jade.psi.stubs.JadeStubElementTypes;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

import static com.intellij.openapi.util.text.StringUtil.notNullize;
import static com.intellij.psi.xml.XmlTokenType.XML_DATA_CHARACTERS;
import static com.intellij.psi.xml.XmlTokenType.XML_REAL_WHITE_SPACE;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.CASE;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.COLON;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.COMMENTS;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.COND_KEYWORD;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.DEFAULT_KEYWORD;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.DOCTYPE_KEYWORD;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.DOT;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.ELSE_KEYWORD;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.EOL;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.EQ;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.EXTENDS_KEYWORD;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.FILE_PATH;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.FILTER_NAME;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.IDENTIFIER;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.INCLUDE_KEYWORD;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.INDENT;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.JS_CODE_BLOCK_PATCHED;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.JS_EACH_EXPR;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.JS_EXPR;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.JS_META_CODE;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.JS_MIXIN_PARAMS;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.JS_MIXIN_PARAMS_VALUES;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.LPAREN;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.MIXIN_KEYWORD;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.NEQ;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.PIPE;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.PLUS;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.TAG_CLASS;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.TAG_ID;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.TAG_NAME;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.TEXT;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.WHEN;
import static com.jetbrains.plugins.jade.psi.JadeTokenTypes.YIELD_KEYWORD;


public class JadeParser implements PsiParser {
  private static final Logger LOG = Logger.getInstance(JadeParser.class);

  private final int myTabSize;

  private PsiBuilder myBuilder;

  public JadeParser(final CodeStyleSettings settings) {
    CommonCodeStyleSettings.IndentOptions indentOptions = settings.getCommonSettings(JadeLanguage.INSTANCE).getIndentOptions();
    myTabSize = indentOptions != null ? indentOptions.TAB_SIZE : new CommonCodeStyleSettings.IndentOptions().TAB_SIZE;
  }

  @Override
  public @NotNull ASTNode parse(final @NotNull IElementType root, final @NotNull PsiBuilder builder) {
    myBuilder = builder;

    final PsiBuilder.Marker fileMarker = builder.mark();
    final PsiBuilder.Marker document = builder.mark();

    while (!builder.eof()) {
      parseTopLevel(-1);
    }

    document.done(JadeElementTypes.DOCUMENT);
    fileMarker.done(root);
    return builder.getTreeBuilt();
  }

  private void parseTopLevel(int parentIndent) {
    if (myBuilder.getTokenType() == null) return;

    passExcessEOLsAndIndents();
    final int blockIndent = getCurrentIndent();

    while (!myBuilder.eof()) {
      passExcessEOLsAndIndents();
      int currentIndent = getCurrentIndent();
      if (currentIndent <= parentIndent) {
        break;
      }
      if (currentIndent < blockIndent) {
        // might be pipeless error, but NO
        break;
      }

      passEOLsAndIndents();
      parseNode(currentIndent);
    }
  }

  public void parseNode(int nodeIndent) {
    IElementType tokenType = myBuilder.getTokenType();
    if (tokenType == null) {
      return;
    }

    if (tokenType == JS_CODE_BLOCK_PATCHED) {
      // Just rely on the lexer choice
      myBuilder.advanceLexer();
    }
    else if (tokenType == TAG_NAME
        || tokenType == DOT
        || tokenType == TAG_ID) {
      parseTag(nodeIndent);
    }
    else if (tokenType == TEXT && parseTag(nodeIndent)) {
        // do nothing
      }
      else if (tokenType == COLON) {
        parseFilter(nodeIndent);
      }
      else if (tokenType == PLUS) {
        parseMixinInvocation(nodeIndent);
      }
      else if (tokenType == JS_META_CODE) {
        myBuilder.advanceLexer();
        //      parseJSLine(nodeIndent);
      }
      else if (tokenType == EQ || tokenType == NEQ) {
        parseJSTextLine();
      }
      else if (tokenType == PIPE) {
        parsePipedLine();
      }
      else if (tokenType == TEXT) {
        parsePlainTextLine(myBuilder);
      }
      else if (COMMENTS.contains(tokenType)) {
        parseComment(nodeIndent);
      }
      else if (tokenType == COND_KEYWORD) {
        parseConditionalStatement(nodeIndent);
      }
      else if (tokenType == JS_EACH_EXPR) {
        parseForStatement(nodeIndent);
      }
      else if (tokenType == CASE) {
        parseCaseStatement();
      }
      else if (tokenType == INCLUDE_KEYWORD
               || tokenType == EXTENDS_KEYWORD) {
        parseIncludeStatement(nodeIndent);
      }
      else if (tokenType == MIXIN_KEYWORD) {
        parseMixinDeclaration(nodeIndent);
      }
      else if (tokenType == DOCTYPE_KEYWORD) {
        parseDoctypeValue(nodeIndent);
      }
      else if (tokenType == YIELD_KEYWORD) {
        parseYieldStatement(nodeIndent);
      }
      else {
        error(JadeBundle.message("pug.parser.error.unknown-token"));
        myBuilder.advanceLexer();
      }
  }

  /**
   * @return false if starts with TEXT, but it's not a valid interpolation, true otherwise
   */
  private boolean parseTag(int nodeIndent) {
    PsiBuilder.Marker marker = myBuilder.mark();

    if (parseTagOrMixinInternals(nodeIndent)) {
      marker.done(JadeElementTypes.TAG);
      return true;
    }
    else {
      marker.rollbackTo();
      return false;
    }
  }

  /**
   * @return false if starts with TEXT, but it's not a valid interpolation, true otherwise
   */
  private boolean parseTagOrMixinInternals(int nodeIndent) {
    boolean isStyleTag = false;

    // Probably interpolation
    if (myBuilder.getTokenType() == TEXT) {
      if (!parseInterpolatedTagName()) return false;
    }

    while (!myBuilder.eof()) {
      IElementType tokenType = myBuilder.getTokenType();
      if (tokenType == EOL
          || tokenType == INDENT
          || tokenType == COLON
          || (tokenType == DOT && myBuilder.lookAhead(1) != TAG_CLASS)
          || tokenType == EQ || tokenType == NEQ) {
        break;
      }
      if (tokenType == LPAREN) {
        TagParsing.parseAttributeList(myBuilder);
        continue;
      }
      if (tokenType == DOT || tokenType == TAG_ID) {
        parseTagIdOrClassName(tokenType);
        continue;
      }
      if (tokenType == TAG_NAME) {
        isStyleTag = HtmlUtil.STYLE_TAG_NAME.equals(myBuilder.getTokenText());
      }
      if (tokenType == TEXT) {
        parsePlainTextLine(myBuilder);
      } else {
        myBuilder.advanceLexer();
      }
    }

    if (myBuilder.eof()) return true;
    IElementType tokenType = myBuilder.getTokenType();
    if (tokenType == COLON) {
      myBuilder.advanceLexer();

      parseNode(nodeIndent);
    }
    else if (tokenType == DOT) {
      myBuilder.advanceLexer();
      parseBlock(nodeIndent, isStyleTag);
    }
    else if (tokenType == EQ || tokenType == NEQ) {
      myBuilder.advanceLexer();
      expectToken(JS_EXPR);
      myBuilder.advanceLexer();
      parseTopLevel(nodeIndent);
    }
    else {
      parseTopLevel(nodeIndent);
    }

    return true;
  }

  private boolean parseInterpolatedTagName() {
    final PsiBuilder.Marker interpolatedName = myBuilder.mark();

    parseOneTextElement();
    if (!expectToken(JS_EXPR)) {
      interpolatedName.drop();
      return false;
    }
    myBuilder.advanceLexer();
    if (!expectToken(TEXT, JadeBundle.message("pug.parser.token.interpolation-closing-brace"))) {
      interpolatedName.drop();
      return false;
    } else {
      parseOneTextElement();
    }
    interpolatedName.done(JadeElementTypes.TAG_INTERP_NAME);

    return true;
  }

  private void parseTagIdOrClassName(IElementType tokenType) {
    final PsiBuilder.Marker attrMarker = myBuilder.mark();
    myBuilder.mark().done(JadeElementTypes.FAKE_ATTR_NAME);
    final PsiBuilder.Marker valueMarker = myBuilder.mark();

    if (tokenType == TAG_ID) {
      myBuilder.advanceLexer();
    }
    else {
      PsiBuilder.Marker m = myBuilder.mark();
      myBuilder.advanceLexer();
      LOG.assertTrue(myBuilder.getTokenType() == TAG_CLASS);
      myBuilder.advanceLexer();
      m.done(JadeElementTypes.CLASS);
    }

    valueMarker.done(JadeElementTypes.ATTRIBUTE_VALUE);
    attrMarker.done(JadeElementTypes.ATTRIBUTE);
  }

  private void parseFilter(int nodeIndent) {
    LOG.assertTrue(myBuilder.getTokenType() == COLON);
    PsiBuilder.Marker marker = myBuilder.mark();

    while (!myBuilder.eof()) {
      IElementType tokenType = myBuilder.getTokenType();
      if (tokenType == EOL || tokenType == INDENT) {
        break;
      }
      myBuilder.advanceLexer();
    }

    parseBlock(nodeIndent, false);

    marker.done(JadeElementTypes.FILTER);
  }

  private void parseMixinInvocation(int nodeIndent) {
    LOG.assertTrue(myBuilder.getTokenType() == PLUS);
    PsiBuilder.Marker marker = myBuilder.mark();

    myBuilder.advanceLexer();
    // Expect TEXT (interpolation) or TAG_NAME
    if (myBuilder.getTokenType() != TEXT) {
      expectToken(TAG_NAME, JadeBundle.message("pug.parser.token.mixin-name"));
    }

    parseTagOrMixinInternals(nodeIndent);

    marker.done(JadeElementTypes.MIXIN);
  }

  private void parseJSTextLine() {
    LOG.assertTrue(myBuilder.getTokenType() == EQ || myBuilder.getTokenType() == NEQ);
    PsiBuilder.Marker marker = myBuilder.mark();

    myBuilder.advanceLexer();
    if (expectToken(JS_EXPR)) {
      myBuilder.advanceLexer();
    }

    marker.done(JadeElementTypes.JS_EXPR);
  }

  private void parsePipedLine() {
    LOG.assertTrue(myBuilder.getTokenType() == PIPE);
    PsiBuilder.Marker marker = myBuilder.mark();

    myBuilder.advanceLexer();
    // smth with interpolation possibly
    while (notEolOrEof()) {
      final IElementType type = myBuilder.getTokenType();
      if (type == TEXT) {
        parsePlainTextLine(myBuilder);
      } else {
        if (myBuilder.getTokenType() != JS_EXPR) {
          expectToken(JS_EXPR, JadeBundle.message("pug.parser.token.text-or-interpolation"));
        }
        myBuilder.advanceLexer();
      }
    }

    marker.done(JadeElementTypes.PIPED_TEXT);
  }

  public static void parsePlainTextLine(final @NotNull PsiBuilder myBuilder) {
    LOG.assertTrue(myBuilder.getTokenType() == TEXT);
    PsiBuilder.Marker marker = myBuilder.mark();
    marker.setCustomEdgeTokenBinders(WhitespacesBinders.GREEDY_LEFT_BINDER, WhitespacesBinders.GREEDY_RIGHT_BINDER);

    while (myBuilder.getTokenType() == TEXT) {
      markTextPart(myBuilder);
    }

    marker.done(XmlElementType.XML_TEXT);
  }

  private void parseOneTextElement() {
    LOG.assertTrue(myBuilder.getTokenType() == TEXT);
    PsiBuilder.Marker marker = myBuilder.mark();
    marker.setCustomEdgeTokenBinders(WhitespacesBinders.GREEDY_LEFT_BINDER, WhitespacesBinders.GREEDY_RIGHT_BINDER);
    markTextPart(myBuilder);
    marker.done(XmlElementType.XML_TEXT);
  }

  private static void markTextPart(final @NotNull PsiBuilder myBuilder) {
    LOG.assertTrue(myBuilder.getTokenType() == TEXT);
    final IElementType tokenType =
      Character.isWhitespace(Objects.requireNonNull(myBuilder.getTokenText()).charAt(0)) ? XML_REAL_WHITE_SPACE : XML_DATA_CHARACTERS;
    final PsiBuilder.Marker mark = myBuilder.mark();
    myBuilder.advanceLexer();
    mark.collapse(tokenType);
  }

  private boolean notEolOrEof() {
    return !myBuilder.eof() && !isEol(myBuilder.getTokenType());
  }

  private void parseComment(int nodeIndent) {
    LOG.assertTrue(COMMENTS.contains(myBuilder.getTokenType()));
    PsiBuilder.Marker marker = myBuilder.mark();

    final int secondLineIndent = IndentUtil.calcSecondLineIndent(myBuilder.getTokenText(), myTabSize);
    myBuilder.advanceLexer();
    while (!myBuilder.eof()) {
      passExcessEOLsAndIndents();
      int currentIndent = getCurrentIndent();
      if (secondLineIndent != -1 && currentIndent >= secondLineIndent) {
        LOG.warn("should not be here");
        myBuilder.advanceLexer();
        continue;
      }
      if (currentIndent <= nodeIndent) {
        break;
      }

      myBuilder.advanceLexer();
      pipelessText(nodeIndent);
      marker.done(JadeElementTypes.COMMENT);
      return;
    }

    marker.done(JadeElementTypes.COMMENT);
  }

  private void parseConditionalStatement(int nodeIndent) {
    LOG.assertTrue(myBuilder.getTokenType() == COND_KEYWORD);
    PsiBuilder.Marker marker = myBuilder.mark();
    PsiBuilder.Marker headerMarker = myBuilder.mark();

    myBuilder.advanceLexer();
    if (!expectToken(JS_EXPR)) {
      headerMarker.drop();
      marker.done(JadeElementTypes.CONDITIONAL_STATEMENT);
      return;
    }
    myBuilder.advanceLexer();
    headerMarker.done(JadeElementTypes.CONDITIONAL_HEADER);

    if (!isEol(myBuilder.getTokenType())) {
      expectToken(INDENT);
      marker.done(JadeElementTypes.CONDITIONAL_STATEMENT);
      return;
    }

    parseSmthWithElse(nodeIndent, JadeElementTypes.CONDITIONAL_BODY, JadeElementTypes.CONDITIONAL_ELSE);
    marker.done(JadeElementTypes.CONDITIONAL_STATEMENT);
  }

  private void parseSmthWithElse(int nodeIndent, @NotNull IElementType bodyType, @NotNull IElementType elseType) {
    passExcessEOLsAndIndents();

    int blockIndent = getCurrentIndent();

    PsiBuilder.Marker bodyMarker = null;
    if (blockIndent > nodeIndent) {
      bodyMarker = myBuilder.mark();
    }
    else if (blockIndent == nodeIndent) {
      // for parsing else
      blockIndent++;
    }

    while (!myBuilder.eof()) {
      passExcessEOLsAndIndents();
      int currentIndent = getCurrentIndent();
      if (currentIndent < nodeIndent) {
        if (bodyMarker != null) {
          bodyMarker.done(bodyType);
          bodyMarker = null;
        }
        break;
      }

      if (currentIndent < blockIndent) {
        if (bodyMarker != null) {
          bodyMarker.done(bodyType);
          bodyMarker = null;
        }

        if (myBuilder.lookAhead(1) == ELSE_KEYWORD) {
          myBuilder.advanceLexer();

          PsiBuilder.Marker elseBody = myBuilder.mark();
          myBuilder.advanceLexer();

          passExcessEOLsAndIndents();
          parseTopLevel(currentIndent);

          elseBody.done(elseType);
        }
        break;
      }

      passExcessEOLsAndIndents();
      parseTopLevel(nodeIndent);
    }

    if (bodyMarker != null) {
      bodyMarker.done(bodyType);
    }
  }

  private void parseForStatement(int nodeIndent) {
    LOG.assertTrue(myBuilder.getTokenType() == JS_EACH_EXPR);
    final PsiBuilder.Marker marker = myBuilder.mark();

    myBuilder.advanceLexer();

    if (!isEol(myBuilder.getTokenType())) {
      expectToken(INDENT);
      marker.done(JadeElementTypes.FOR_STATEMENT);
      return;
    }

    parseSmthWithElse(nodeIndent, JadeElementTypes.FOR_BODY, JadeElementTypes.FOR_ELSE);

    marker.done(JadeElementTypes.FOR_STATEMENT);
  }

  private void parseCaseStatement() {
    LOG.assertTrue(myBuilder.getTokenType() == CASE);
    PsiBuilder.Marker marker = myBuilder.mark();

    myBuilder.advanceLexer();
    expectToken(JS_EXPR);
    myBuilder.advanceLexer();

    passExcessEOLsAndIndents();
    expectToken(INDENT);

    int blockIndent = getCurrentIndent();

    while (!myBuilder.eof()) {
      passExcessEOLsAndIndents();
      int currentIndent = getCurrentIndent();
      if (currentIndent < blockIndent) {
        break;
      }

      passEOLsAndIndents();
      parseCaseWhen(blockIndent);
    }

    marker.done(JadeElementTypes.CASE_STATEMENT);
  }

  private void parseCaseWhen(int nodeIndent) {
    PsiBuilder.Marker marker = myBuilder.mark();

    IElementType tokenType = myBuilder.getTokenType();
    if (tokenType != WHEN && tokenType != DEFAULT_KEYWORD) {
      marker.drop();
      expectToken(WHEN, JadeBundle.message("pug.parser.token.when-or-default"));
      myBuilder.advanceLexer();
      return;
    }
    myBuilder.advanceLexer();

    if (tokenType == WHEN) {
      expectToken(JS_EXPR);
      myBuilder.advanceLexer();
    }

    if (myBuilder.getTokenType() == COLON) {
      myBuilder.advanceLexer();
      parseNode(nodeIndent);
    }
    else {
      parseTopLevel(nodeIndent);
    }

    marker.done(JadeElementTypes.WHEN_STATEMENT);
  }

  private void parseIncludeStatement(int nodeIndent) {
    LOG.assertTrue(myBuilder.getTokenType() == EXTENDS_KEYWORD || myBuilder.getTokenType() == INCLUDE_KEYWORD);
    final boolean isIncludeStatement = myBuilder.getTokenType() == INCLUDE_KEYWORD;

    PsiBuilder.Marker marker = myBuilder.mark();
    myBuilder.advanceLexer();

    // looks like include-with-filter statement
    if (isIncludeStatement && myBuilder.getTokenType() == COLON) {
      myBuilder.advanceLexer();

      expectToken(FILTER_NAME);
      myBuilder.advanceLexer();

      if (myBuilder.getTokenType() == LPAREN) {
        final PsiBuilder.Marker tagMarker = myBuilder.mark();
        TagParsing.parseAttributeList(myBuilder);
        tagMarker.done(JadeElementTypes.TAG);
      }
    }

    expectToken(FILE_PATH);
    PsiBuilder.Marker pathMark = myBuilder.mark();
    myBuilder.advanceLexer();
    pathMark.done(JadeElementTypes.FILE_PATH);

    if (isIncludeStatement) {
      parseTopLevel(nodeIndent);
    }

    marker.done(JadeElementTypes.INCLUDE_STATEMENT);
  }

  private void parseMixinDeclaration(int nodeIndent) {
    LOG.assertTrue(myBuilder.getTokenType() == MIXIN_KEYWORD);
    PsiBuilder.Marker marker = myBuilder.mark();

    myBuilder.advanceLexer();
    expectToken(IDENTIFIER, JadeBundle.message("pug.parser.token.mixin-name"));
    myBuilder.advanceLexer();

    if (myBuilder.getTokenType() == JS_MIXIN_PARAMS) {
      myBuilder.advanceLexer();
    }

    passExcessEOLsAndIndents();

    if (notEolOrEof()) {
      expectToken(INDENT);
    }
    // here actually Jade allows CALLING mixin if no contents provided
    else if (getCurrentIndent() <= nodeIndent) {
      marker.rollbackTo();
      parseMixinDeclarationLikeInvocation(nodeIndent);
      return;
    }

    parseTopLevel(nodeIndent);

    marker.done(JadeStubElementTypes.MIXIN_DECLARATION);
  }

  private void parseMixinDeclarationLikeInvocation(int nodeIndent) {
    LOG.assertTrue(myBuilder.getTokenType() == MIXIN_KEYWORD);
    PsiBuilder.Marker marker = myBuilder.mark();

    myBuilder.advanceLexer();
    expectToken(IDENTIFIER, JadeBundle.message("pug.parser.token.mixin-name"));
    myBuilder.advanceLexer();

    if (myBuilder.getTokenType() == JS_MIXIN_PARAMS) {
      myBuilder.remapCurrentToken(JS_MIXIN_PARAMS_VALUES);
      myBuilder.advanceLexer();
    }

    passExcessEOLsAndIndents();

    // decl-like invocation is allowed only w/o a block
    LOG.assertTrue(getCurrentIndent() <= nodeIndent);

    marker.done(JadeElementTypes.MIXIN);
  }

  private void parseBlock(int parentIndent, boolean doNotMakeNode) {
    passExcessEOLsAndIndents();
    int blockIndent = getCurrentIndent();
    if (blockIndent <= parentIndent) {
      return;
    }
    passEOLsAndIndents();

    PsiBuilder.Marker marker = myBuilder.mark();
    while (!myBuilder.eof()) {
      IElementType tokenType = myBuilder.getTokenType();
      if (!isEol(tokenType)) {
        if (tokenType == TEXT) {
          parsePlainTextLine(myBuilder);
        } else {
          myBuilder.advanceLexer();
        }
        continue;
      }

      passExcessEOLsAndIndents();
      int currentIndent = getCurrentIndent();
      if (currentIndent <= parentIndent) {
        break;
      }
      if (tokenType == TEXT) {
        pipelessText(parentIndent);
      }
      if (currentIndent < blockIndent) {
        //myBuilder.advanceLexer();
        closeMarker(marker, JadeElementTypes.BLOCK, !doNotMakeNode);
        return;
      }
      myBuilder.advanceLexer();
    }

    closeMarker(marker, JadeElementTypes.BLOCK, !doNotMakeNode);
  }

  private void parseDoctypeValue(int nodeIndent) {
    LOG.assertTrue(myBuilder.getTokenType() == DOCTYPE_KEYWORD);
    PsiBuilder.Marker marker = myBuilder.mark();
    myBuilder.advanceLexer();

    while (notEolOrEof()) {
      if (myBuilder.getTokenType() == TEXT) {
        parsePlainTextLine(myBuilder);
      } else {
        myBuilder.advanceLexer();
      }
    }

    passExcessEOLsAndIndents();
    if (getCurrentIndent() > nodeIndent) {
      expectToken(EOL);
    }

    marker.done(JadeElementTypes.DOCTYPE);
  }

  private void parseYieldStatement(int nodeIndent) {
    LOG.assertTrue(myBuilder.getTokenType() == YIELD_KEYWORD);
    PsiBuilder.Marker marker = myBuilder.mark();

    myBuilder.advanceLexer();

    while (notEolOrEof()) {
      expectToken(EOL);
      myBuilder.advanceLexer();
    }
    passExcessEOLsAndIndents();
    if (getCurrentIndent() > nodeIndent) {
      expectToken(EOL);
    }

    marker.done(JadeElementTypes.YIELD_STATEMENT);
  }

  private void passEOLsAndIndents() {
    IElementType tokenType = myBuilder.getTokenType();
    while (isEol(tokenType)) {
      myBuilder.advanceLexer();
      tokenType = myBuilder.getTokenType();
    }
  }

  private void passExcessEOLsAndIndents() {
    IElementType tokenType = myBuilder.getTokenType();
    IElementType nextTokenType = myBuilder.lookAhead(1);
    while (isEol(tokenType) && isEol(nextTokenType)) {
      myBuilder.advanceLexer();
      tokenType = nextTokenType;
      nextTokenType = myBuilder.lookAhead(1);
    }
  }

  private int getCurrentIndent() {
    if (myBuilder.eof()) {
      return 0;
    }
    return IndentUtil.calcIndent(myBuilder.getTokenText(), 0, myTabSize);
  }

  private boolean expectToken(IElementType tokenType) {
    return expectToken(tokenType, getTokenName(tokenType));
  }

  private boolean expectToken(IElementType tokenType, @Nls String expectedName) {

    if (myBuilder.getTokenType() != tokenType) {
      error(JadeBundle.message("pug.parser.error.expected-x-found-y",
                               StringUtil.capitalize(expectedName),
                               getTokenName(myBuilder.getTokenType())));
      return false;
    }
    return true;
  }

  private static @Nls String getTokenName(@Nullable IElementType tokenType) {
    if (tokenType == null) {
      return JadeBundle.message("pug.parser.token.end-of-file");
    }
    return notNullize(ourTokenNameMap.get(tokenType), tokenType.toString()); //NON-NLS
  }

  private void error(@ParsingError @NotNull String message) {
    myBuilder.mark().error(message);
  }

  private void pipelessText(int indent) {
    boolean seenNonWhitespace = false;

    PsiBuilder.Marker marker = myBuilder.mark();

    while (!myBuilder.eof()) {
      if (!isEol(myBuilder.getTokenType())) {
        seenNonWhitespace = true;
        if (myBuilder.getTokenType() == TEXT) {
          parsePlainTextLine(myBuilder);
        } else {
          myBuilder.advanceLexer();
        }
        continue;
      }

      passExcessEOLsAndIndents();
      if (getCurrentIndent() <= indent) {
        break;
      }
      myBuilder.advanceLexer();
    }

    if (!seenNonWhitespace) {
      marker.drop();
    }
    else {
      marker.error(JadeBundle.message("pug.parser.error.pipeless-text"));
    }
  }

  private static boolean isEol(IElementType tokenType) {
    return tokenType == EOL || tokenType == INDENT;
  }

  private static void closeMarker(PsiBuilder.Marker marker, IElementType type, boolean isDone) {
    if (isDone) {
      marker.done(type);
    }
    else {
      marker.drop();
    }
  }

  private static final Map<IElementType, @Nls String> ourTokenNameMap =
    Map.of(EOL, JadeBundle.message("pug.parser.token.end-of-line"), INDENT, JadeBundle.message("pug.parser.token.indent"), FILE_PATH,
           JadeBundle.message("pug.parser.token.file-path"), FILTER_NAME, JadeBundle.message("pug.parser.token.filter-name"), JS_EXPR,
           JadeBundle.message("pug.parser.token.expression"));
}
