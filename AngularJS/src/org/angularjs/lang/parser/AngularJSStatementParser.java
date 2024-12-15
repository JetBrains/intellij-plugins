// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angularjs.lang.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.JavaScriptParserBundle;
import com.intellij.lang.javascript.parsing.StatementParser;
import com.intellij.psi.tree.IElementType;
import org.angularjs.lang.lexer.AngularJSTokenTypes;

class AngularJSStatementParser extends StatementParser<AngularJSParser> {
  private final AngularJSParser myParser;

  public AngularJSStatementParser(AngularJSParser parser) {
    super(parser);
    myParser = parser;
  }

  @Override
  public void parseStatement() {
    final IElementType firstToken = builder.getTokenType();
    if (firstToken == JSTokenTypes.LBRACE) {
      parseExpressionStatement();
      checkForSemicolon();
      return;
    }
    if (isIdentifierToken(firstToken)) {
      final IElementType nextToken = builder.lookAhead(1);
      if (nextToken == JSTokenTypes.IN_KEYWORD) {
        parseInStatement();
        return;
      }
    }
    if (tryParseNgIfStatement()) {
      return;
    }
    if (firstToken == JSTokenTypes.LET_KEYWORD) {
      if (builder.lookAhead(2) != JSTokenTypes.EQ) {
        parseNgForStatement();
        return;
      }
      parseExpressionStatement();
      return;
    }
    if (builder.getTokenType() == JSTokenTypes.LPAR) {
      if (parseInStatement()) {
        return;
      }
    }
    super.parseStatement();
  }

  private void parseNgForStatement() {
    PsiBuilder.Marker statement = builder.mark();
    if (!myParser.getExpressionParser().parseForExpression()) {
      statement.drop();
      return;
    }
    checkForSemicolon();
    statement.done(JSElementTypes.EXPRESSION_STATEMENT);
  }

  private boolean parseInStatement() {
    PsiBuilder.Marker statement = builder.mark();
    if (!myParser.getExpressionParser().parseInExpression()) {
      statement.drop();
      return false;
    }
    statement.done(JSElementTypes.EXPRESSION_STATEMENT);
    return true;
  }

  private boolean tryParseNgIfStatement() {
    PsiBuilder.Marker ngIf = builder.mark();
    myParser.getExpressionParser().parseExpression();
    if (builder.getTokenType() != JSTokenTypes.SEMICOLON) {
      ngIf.rollbackTo();
      return false;
    }
    builder.advanceLexer();
    if (builder.getTokenType() == JSTokenTypes.LET_KEYWORD) {
      myParser.getExpressionParser().parseHashDefinition();
      builder.advanceLexer();
    }
    if (builder.getTokenType() != AngularJSTokenTypes.THEN && builder.getTokenType() != JSTokenTypes.ELSE_KEYWORD) {
      ngIf.rollbackTo();
      return false;
    }

    parseBranch(AngularJSTokenTypes.THEN);
    if (builder.getTokenType() == JSTokenTypes.SEMICOLON) {
      builder.advanceLexer();
    }
    parseBranch(JSTokenTypes.ELSE_KEYWORD);
    ngIf.done(JSElementTypes.IF_STATEMENT);
    checkForSemicolon();
    if (builder.getTokenType() == JSTokenTypes.LET_KEYWORD) {
      myParser.getExpressionParser().parseHashDefinition();
    }
    return true;
  }

  private void parseBranch(IElementType branchType) {
    if (builder.getTokenType() == branchType) {
      builder.advanceLexer();
      if (builder.getTokenType() == JSTokenTypes.IDENTIFIER) {
        myParser.buildTokenElement(JSElementTypes.REFERENCE_EXPRESSION);
      }
      else {
        builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.identifier"));
      }
    }
  }
}
