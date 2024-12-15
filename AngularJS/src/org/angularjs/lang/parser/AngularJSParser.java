// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angularjs.lang.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.javascript.*;
import com.intellij.lang.javascript.parsing.*;
import com.intellij.psi.tree.IElementType;
import org.angularjs.lang.lexer.AngularJSTokenTypes;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSParser
  extends JavaScriptParser<AngularJSExpressionParser, StatementParser<?>, FunctionParser<?>, JSPsiTypeParser<?>> {

  public AngularJSParser(PsiBuilder builder) {
    super(JavascriptLanguage.INSTANCE, builder);
    myExpressionParser = new AngularJSExpressionParser(this);
    myStatementParser = new StatementParser<>(this) {
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
        if (!getExpressionParser().parseForExpression()) {
          statement.drop();
          return;
        }
        checkForSemicolon();
        statement.done(JSElementTypes.EXPRESSION_STATEMENT);
      }

      private boolean parseInStatement() {
        PsiBuilder.Marker statement = builder.mark();
        if (!getExpressionParser().parseInExpression()) {
          statement.drop();
          return false;
        }
        statement.done(JSElementTypes.EXPRESSION_STATEMENT);
        return true;
      }

      private boolean tryParseNgIfStatement() {
        PsiBuilder.Marker ngIf = builder.mark();
        getExpressionParser().parseExpression();
        if (builder.getTokenType() != JSTokenTypes.SEMICOLON) {
          ngIf.rollbackTo();
          return false;
        }
        builder.advanceLexer();
        if (builder.getTokenType() == JSTokenTypes.LET_KEYWORD) {
          getExpressionParser().parseHashDefinition();
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
          getExpressionParser().parseHashDefinition();
        }
        return true;
      }

      private void parseBranch(IElementType branchType) {
        if (builder.getTokenType() == branchType) {
          builder.advanceLexer();
          if (builder.getTokenType() == JSTokenTypes.IDENTIFIER) {
            buildTokenElement(JSElementTypes.REFERENCE_EXPRESSION);
          }
          else {
            builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.identifier"));
          }
        }
      }
    };
  }

  @Override
  public boolean isIdentifierName(IElementType firstToken) {
    return super.isIdentifierName(firstToken) || firstToken == AngularJSTokenTypes.THEN;
  }

  public void parseAngular(IElementType root) {
    final PsiBuilder.Marker rootMarker = builder.mark();
    while (!builder.eof()) {
      getStatementParser().parseStatement();
    }
    rootMarker.done(root);
  }
}
