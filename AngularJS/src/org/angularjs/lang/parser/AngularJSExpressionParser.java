// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angularjs.lang.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lang.javascript.JSStubElementTypes;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.JavaScriptParserBundle;
import com.intellij.lang.javascript.parsing.ExpressionParser;
import com.intellij.psi.tree.IElementType;
import org.angularjs.AngularJSBundle;
import org.angularjs.lang.lexer.AngularJSTokenTypes;

public class AngularJSExpressionParser extends ExpressionParser<AngularJSParser> {
  private final AngularJSParser myParser;
  private final AngularJSMessageFormatParser myAngularJSMessageFormatParser;

  public AngularJSExpressionParser(AngularJSParser parser) {
    super(parser);
    myParser = parser;
    myAngularJSMessageFormatParser = new AngularJSMessageFormatParser(parser);
  }

  @Override
  protected boolean parseUnaryExpression() {
    final IElementType tokenType = builder.getTokenType();
    if (tokenType == JSTokenTypes.OR) {
      builder.advanceLexer();
      if (!parseFilter()) {
        builder.error(AngularJSBundle.message("angularjs.parser.message.expected.filter"));
      }
      return true;
    }
    if (tokenType == AngularJSTokenTypes.ONE_TIME_BINDING) {
      final PsiBuilder.Marker expr = builder.mark();
      builder.advanceLexer();
      if (!super.parseUnaryExpression()) {
        builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.expression"));
      }
      expr.done(JSElementTypes.PREFIX_EXPRESSION);
      return true;
    }
    return super.parseUnaryExpression();
  }

  @Override
  public boolean parsePrimaryExpression() {
    final IElementType firstToken = builder.getTokenType();
    if (firstToken == JSTokenTypes.STRING_LITERAL) {
      return parseStringLiteral(firstToken);
    }
    if (firstToken == JSTokenTypes.LET_KEYWORD) {
      parseHashDefinition();
      return true;
    }
    if (isIdentifierToken(firstToken)) {
      if (myAngularJSMessageFormatParser.parseMessage()) {
        return true;
      }
      int cur = -1;
      IElementType prev = builder.rawLookup(-1);
      while (prev != null && builder.isWhitespaceOrComment(prev)) {
        prev = builder.rawLookup(--cur);
      }
      if (prev == JSTokenTypes.AS_KEYWORD) {
        parseExplicitIdentifierWithError();
        return true;
      }
    }
    return super.parsePrimaryExpression();
  }

  private void parseExplicitIdentifierWithError() {
    if (isIdentifierToken(builder.getTokenType())) {
      final PsiBuilder.Marker def = builder.mark();
      myParser.buildTokenElement(JSElementTypes.REFERENCE_EXPRESSION);
      def.done(JSStubElementTypes.DEFINITION_EXPRESSION);
    }
    else {
      builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.identifier"));
    }
  }

  @Override
  protected boolean isReferenceQualifierSeparator(IElementType tokenType) {
    return tokenType == AngularJSTokenTypes.ELVIS ||
           tokenType == AngularJSTokenTypes.ASSERT_NOT_NULL ||
           super.isReferenceQualifierSeparator(tokenType);
  }

  @Override
  protected int getCurrentBinarySignPriority(boolean allowIn, boolean advance) {
    if (builder.getTokenType() == JSTokenTypes.OR) return 10;
    if (builder.getTokenType() == JSTokenTypes.AS_KEYWORD) {
      if (advance) builder.advanceLexer();
      return 10;
    }
    return super.getCurrentBinarySignPriority(allowIn, advance);
  }

  private boolean parseFilter() {
    final PsiBuilder.Marker mark = builder.mark();
    myParser.buildTokenElement(JSElementTypes.REFERENCE_EXPRESSION);
    PsiBuilder.Marker arguments = null;
    while (builder.getTokenType() == JSTokenTypes.COLON) {
      arguments = arguments == null ? builder.mark() : arguments;
      builder.advanceLexer();
      if (!super.parseUnaryExpression()) {
        builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.expression"));
      }
    }
    if (arguments != null) {
      arguments.done(JSElementTypes.ARGUMENT_LIST);
    }
    mark.done(AngularJSElementTypes.FILTER_EXPRESSION);
    return true;
  }

  private boolean parseStringLiteral(IElementType firstToken) {
    final PsiBuilder.Marker mark = builder.mark();
    IElementType currentToken = firstToken;
    StringBuilder literal = new StringBuilder();
    while (currentToken == JSTokenTypes.STRING_LITERAL ||
           currentToken == AngularJSTokenTypes.ESCAPE_SEQUENCE ||
           currentToken == AngularJSTokenTypes.INVALID_ESCAPE_SEQUENCE) {
      literal.append(builder.getTokenText());
      builder.advanceLexer();
      currentToken = builder.getTokenType();
    }
    mark.done(JSStubElementTypes.LITERAL_EXPRESSION);
    final String errorMessage = validateLiteralText(literal.toString());
    if (errorMessage != null) {
      builder.error(errorMessage);
    }
    return true;
  }

  public boolean parseForExpression() {
    final PsiBuilder.Marker expr = builder.mark();
    parseHashDefinition();

    if (builder.getTokenType() != JSTokenTypes.OF_KEYWORD) {
      expr.drop();
      return true;
    }
    else {
      builder.advanceLexer();
    }
    parseExpression();
    if (builder.lookAhead(1) == AngularJSTokenTypes.TRACK_BY_KEYWORD) {
      builder.advanceLexer();
      builder.advanceLexer();
      if (builder.getTokenType() != JSTokenTypes.COLON) {
        builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.colon"));
      }
      else {
        builder.advanceLexer();
      }
      parseExpression();
    }
    expr.done(AngularJSElementTypes.FOR_EXPRESSION);
    return true;
  }

  protected void parseHashDefinition() {
    final PsiBuilder.Marker def = builder.mark();
    builder.advanceLexer();
    if (builder.getTokenType() != JSTokenTypes.IDENTIFIER) {
      builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.identifier"));
    }
    else {
      myParser.buildTokenElement(JSStubElementTypes.VARIABLE);
    }
    def.done(JSStubElementTypes.VAR_STATEMENT);
  }

  public boolean parseInExpression() {
    final PsiBuilder.Marker expr = builder.mark();
    if (isIdentifierToken(builder.getTokenType())) {
      PsiBuilder.Marker statement = builder.mark();
      myParser.buildTokenElement(JSStubElementTypes.VARIABLE);
      statement.done(JSStubElementTypes.VAR_STATEMENT);
    }
    else {
      final PsiBuilder.Marker keyValue = builder.mark();
      parseKeyValue();
      if (builder.getTokenType() != JSTokenTypes.IN_KEYWORD) {
        expr.rollbackTo();
        return false;
      }
      else {
        keyValue.done(JSElementTypes.PARENTHESIZED_EXPRESSION);
      }
    }
    builder.advanceLexer();
    parseExpression();
    if (builder.getTokenType() == AngularJSTokenTypes.TRACK_BY_KEYWORD) {
      builder.advanceLexer();
      parseExpression();
    }
    expr.done(AngularJSElementTypes.REPEAT_EXPRESSION);
    return true;
  }

  private void parseKeyValue() {
    builder.advanceLexer();
    final PsiBuilder.Marker comma = builder.mark();
    if (isIdentifierToken(builder.getTokenType())) {
      myParser.buildTokenElement(JSStubElementTypes.VARIABLE);
    }
    else {
      builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.identifier"));
    }
    if (builder.getTokenType() == JSTokenTypes.COMMA) {
      builder.advanceLexer();
    }
    else {
      builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.comma"));
    }
    if (isIdentifierToken(builder.getTokenType())) {
      myParser.buildTokenElement(JSStubElementTypes.VARIABLE);
    }
    else {
      builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.identifier"));
    }
    comma.done(JSStubElementTypes.VAR_STATEMENT);
    if (builder.getTokenType() == JSTokenTypes.RPAR) {
      builder.advanceLexer();
    }
    else {
      builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.rparen"));
    }
  }
}
