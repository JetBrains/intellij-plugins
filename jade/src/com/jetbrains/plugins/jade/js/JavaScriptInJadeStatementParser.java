// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.js;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.WhitespacesBinders;
import com.intellij.lang.ecmascript6.parsing.ES6StatementParser;
import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.JavaScriptParserBundle;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.plugins.jade.JadeBundle;
import com.jetbrains.plugins.jade.psi.JadeElementTypes;
import com.jetbrains.plugins.jade.psi.JadeTokenTypes;

/**
 * 1. It allows NO statements after while or for;
 * 2. It allows "each k,v in c"
 */
public class JavaScriptInJadeStatementParser extends ES6StatementParser<JavaScriptInJadeParser> {

  protected JavaScriptInJadeStatementParser(JavaScriptInJadeParser parser) {
    super(parser);
  }

  @Override
  public boolean parseBlock() {
    PsiBuilder.Marker mark = builder.mark();
    parseBlockAndAttachStatementsDirectly();
    mark.done(JSElementTypes.BLOCK_STATEMENT_EAGER);
    return true;
  }

  @Override
  public void parseStatement() {
    final IElementType firstToken = builder.getTokenType();

    // copied from the superclass
    if (firstToken == null) {
      builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.statement"));
      return;
    }

    if (firstToken == JSTokenTypes.WHILE_KEYWORD ||
        firstToken == JSTokenTypes.FOR_KEYWORD ||
        firstToken == JSTokenTypes.EACH_KEYWORD) {
      parseMyIterationStatement();
      return;
    }

    super.parseStatement();
  }

  void parseEachStatement(boolean canGoFurther) {
    LOG.assertTrue(builder.getTokenType() == JSTokenTypes.EACH_KEYWORD || builder.getTokenType() == JSTokenTypes.FOR_KEYWORD);
    final PsiBuilder.Marker statement = builder.mark();
    builder.advanceLexer();

    if (!markVariable(false)) {
      builder.mark().error(JavaScriptParserBundle.message("javascript.parser.message.expected.variable.name"));
      statement.done(JadeTokenTypes.EACH_EXPR);
      return;
    }

    if (builder.getTokenType() == JSTokenTypes.COMMA) {
      builder.advanceLexer();

      if (!markVariable(false)) {
        builder.mark().error(JavaScriptParserBundle.message("javascript.parser.message.expected.variable.name"));
        statement.done(JadeTokenTypes.EACH_EXPR);
        return;
      }
    }

    checkMatches(builder, JSTokenTypes.IN_KEYWORD, "javascript.parser.message.expected.forloop.in.or.semicolon");
    parser.getExpressionParser().parseExpression();

    if (canGoFurther && !isEndReached()) {
      parseStatement();
      if (builder.getTokenType() == JSTokenTypes.ELSE_KEYWORD) {
        builder.advanceLexer();
        parseStatement();
      }
    }
    else if (!canGoFurther && !isEndReached()) {
      builder.mark().error(JadeBundle.message("pug.parser.error.indent-expected"));
    }
    statement.done(JadeTokenTypes.EACH_EXPR);
  }

  void parseMixinParameterList(boolean isDeclaration) {
    final PsiBuilder.Marker statement = builder.mark();
    boolean lParPassed = checkMatches(builder, JSTokenTypes.LPAR, "javascript.parser.message.expected.lparen");
    LOG.assertTrue(lParPassed);

    boolean isFirst = true;
    boolean seenRest = false;
    while (!builder.eof()) {
      IElementType tokenType = builder.getTokenType();

      if (tokenType == JSTokenTypes.RPAR) {
        builder.advanceLexer();
        break;
      }

      if (seenRest) {
        builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.rparen"));
      }

      if (isFirst) {
        isFirst = false;
      }
      else {
        boolean commaPassed = checkMatches(builder, JSTokenTypes.COMMA, "javascript.parser.message.expected.comma.or.rparen");
        if (!commaPassed) {
          break;
        }
      }

      if (isDeclaration) {
        seenRest |= passRest();
        boolean variableMarked = markVariable(true);
        if (!variableMarked) {
          break;
        }
      }
      else {
        boolean expressionParsed = parser.getExpressionParser().parseAssignmentExpression(false);
        if (!expressionParsed) {
          builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.expression"));
          break;
        }
      }
    }

    statement.done(isDeclaration ? JadeElementTypes.MIXIN_PARAMETERS : JadeElementTypes.MIXIN_PARAMETERS_VALUES);
  }

  private boolean passRest() {
    if (builder.getTokenType() == JSTokenTypes.DOT_DOT_DOT) {
      builder.advanceLexer();
      return true;
    }
    return false;
  }

  boolean markVariable(boolean allowInitializer) {
    PsiBuilder.Marker varStatement = builder.mark();
    PsiBuilder.Marker varMarker = builder.mark();

    if (!parseVarName(varMarker)) {
      varStatement.drop();
      return false;
    }

    parser.getTypeParser().tryParseType();

    if (builder.getTokenType() == JSTokenTypes.EQ && allowInitializer) {
      parseVariableInitializer(false);
    }
    varMarker.done(getVariableElementType());
    varMarker.setCustomEdgeTokenBinders(INCLUDE_DOC_COMMENT_AT_LEFT, WhitespacesBinders.DEFAULT_RIGHT_BINDER);

    varStatement.done(JSElementTypes.VAR_STATEMENT);

    return true;
  }

  private void parseMyIterationStatement() {
    final IElementType tokenType = builder.getTokenType();
    if (tokenType == JSTokenTypes.WHILE_KEYWORD) {
      parseMyWhileStatement();
    }
    else if (tokenType == JSTokenTypes.EACH_KEYWORD ||
             (tokenType == JSTokenTypes.FOR_KEYWORD &&
              builder.lookAhead(1) != JSTokenTypes.EACH_KEYWORD &&
              builder.lookAhead(1) != JSTokenTypes.LPAR)) {
      parseEachStatement(true);
    }
    else if (tokenType == JSTokenTypes.FOR_KEYWORD) {
      parseMyForStatement();
    }
  }

  private void parseMyForStatement() {
    LOG.assertTrue(builder.getTokenType() == JSTokenTypes.FOR_KEYWORD);
    final PsiBuilder.Marker statement = builder.mark();
    final boolean forin = parseForLoopHeader();

    if (!isEndReached()) {
      parseStatement();
      if (builder.getTokenType() == JSTokenTypes.ELSE_KEYWORD) {
        builder.advanceLexer();
        parseStatement();
      }
    }
    statement.done(forin ? JSElementTypes.FOR_IN_STATEMENT : JSElementTypes.FOR_STATEMENT);
  }

  private void parseMyWhileStatement() {
    LOG.assertTrue(builder.getTokenType() == JSTokenTypes.WHILE_KEYWORD);
    final PsiBuilder.Marker statement = builder.mark();
    builder.advanceLexer();

    checkMatches(builder, JSTokenTypes.LPAR, "javascript.parser.message.expected.lparen");
    parser.getExpressionParser().parseExpression();
    checkMatches(builder, JSTokenTypes.RPAR, "javascript.parser.message.expected.rparen");

    if (!isEndReached()) {
      parseStatement();
    }

    statement.done(JSElementTypes.WHILE_STATEMENT);
  }

  private boolean isEndReached() {
    return builder.getTokenType() == null;
  }
}
