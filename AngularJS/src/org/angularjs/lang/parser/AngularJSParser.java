package org.angularjs.lang.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.javascript.*;
import com.intellij.lang.javascript.parsing.ExpressionParser;
import com.intellij.lang.javascript.parsing.FunctionParser;
import com.intellij.lang.javascript.parsing.JavaScriptParser;
import com.intellij.lang.javascript.parsing.StatementParser;
import com.intellij.psi.tree.IElementType;
import org.angularjs.lang.lexer.AngularJSTokenTypes;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSParser extends JavaScriptParser<AngularJSParser.AngularJSExpressionParser, StatementParser, FunctionParser> {
  public AngularJSParser(PsiBuilder builder) {
    super(JavaScriptSupportLoader.JAVASCRIPT_1_5, builder);
    myExpressionParser = new AngularJSExpressionParser();
    myStatementParser = new StatementParser<AngularJSParser>(this) {
      @Override
      protected void doParseStatement(boolean canHaveClasses) {
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
        if (builder.getTokenType() == JSTokenTypes.LPAR) {
          if (parseInStatement()) {
            return;
          }
        }
        super.doParseStatement(canHaveClasses);
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
    };
  }

  public void parseAngular(IElementType root) {
    final PsiBuilder.Marker rootMarker = builder.mark();
    while (!builder.eof()) {
      getStatementParser().parseStatement();
    }
    rootMarker.done(root);
  }

  protected class AngularJSExpressionParser extends ExpressionParser<AngularJSParser> {
    public AngularJSExpressionParser() {
      super(AngularJSParser.this);
    }

    @Override
    protected boolean parseUnaryExpression() {
      final IElementType tokenType = builder.getTokenType();
      if (tokenType == JSTokenTypes.OR) {
        builder.advanceLexer();
        if (!parseFilter()) {
          builder.error("expected filter");
        }
        return true;
      }
      if (tokenType == AngularJSTokenTypes.ONE_TIME_BINDING) {
        final PsiBuilder.Marker expr = builder.mark();
        builder.advanceLexer();
        if (!super.parseUnaryExpression()) {
          builder.error(JSBundle.message("javascript.parser.message.expected.expression"));
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
      if (firstToken == JSTokenTypes.IDENTIFIER && builder.lookAhead(1) == JSTokenTypes.AS_KEYWORD) {
        return parseAsExpression();
      }
      return super.parsePrimaryExpression();
    }

    private boolean parseAsExpression() {
      PsiBuilder.Marker expr = builder.mark();
      buildTokenElement(JSElementTypes.REFERENCE_EXPRESSION);
      builder.advanceLexer();
      parseExplicitIdentifierWithError();
      expr.done(AngularJSElementTypes.AS_EXPRESSION);
      return true;
    }

    private void parseExplicitIdentifierWithError() {
      if (isIdentifierToken(builder.getTokenType())) {
        parseExplicitIdentifier();
      } else {
        builder.error(JSBundle.message("javascript.parser.message.expected.identifier"));
      }
    }

    @Override
    protected int getCurrentBinarySignPriority(boolean allowIn, boolean advance) {
      if (builder.getTokenType() == JSTokenTypes.OR) return 10;
      return super.getCurrentBinarySignPriority(allowIn, advance);
    }

    private boolean parseFilter() {
      final PsiBuilder.Marker mark = builder.mark();
      buildTokenElement(JSElementTypes.REFERENCE_EXPRESSION);
      PsiBuilder.Marker arguments = null;
      while (builder.getTokenType() == JSTokenTypes.COLON) {
        arguments = arguments == null ? builder.mark() : arguments;
        builder.advanceLexer();
        if (!super.parseUnaryExpression()) {
          builder.error(JSBundle.message("javascript.parser.message.expected.expression"));
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
      mark.done(JSElementTypes.LITERAL_EXPRESSION);
      final String errorMessage = validateLiteralText(literal.toString());
      if (errorMessage != null) {
        builder.error(errorMessage);
      }
      return true;
    }

    public boolean parseInExpression() {
      final PsiBuilder.Marker expr = builder.mark();
      if (isIdentifierToken(builder.getTokenType())) {
        parseExplicitIdentifier();
      } else {
        final PsiBuilder.Marker keyValue = builder.mark();
        parseKeyValue();
        if (builder.getTokenType() != JSTokenTypes.IN_KEYWORD) {
          expr.rollbackTo();
          return false;
        } else {
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
      parseExplicitIdentifierWithError();
      if (builder.getTokenType() == JSTokenTypes.COMMA) {
        builder.advanceLexer();
      } else {
        builder.error(JSBundle.message("javascript.parser.message.expected.comma"));
      }
      parseExplicitIdentifierWithError();
      comma.done(JSElementTypes.COMMA_EXPRESSION);
      if (builder.getTokenType() == JSTokenTypes.RPAR) {
        builder.advanceLexer();
      } else {
        builder.error(JSBundle.message("javascript.parser.message.expected.rparen"));
      }
    }

    private void parseExplicitIdentifier() {
      final PsiBuilder.Marker def = builder.mark();
      buildTokenElement(JSElementTypes.REFERENCE_EXPRESSION);
      def.done(JSElementTypes.DEFINITION_EXPRESSION);
    }
  }
}
