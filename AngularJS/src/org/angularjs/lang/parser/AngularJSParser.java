package org.angularjs.lang.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lang.javascript.JSStubElementTypes;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.parsing.ExpressionParser;
import com.intellij.lang.javascript.parsing.FunctionParser;
import com.intellij.lang.javascript.parsing.JavaScriptParser;
import com.intellij.lang.javascript.parsing.StatementParser;
import com.intellij.openapi.util.Key;
import com.intellij.psi.tree.IElementType;
import org.angularjs.lang.lexer.AngularJSTokenTypes;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSParser extends JavaScriptParser<ExpressionParser, StatementParser, FunctionParser> {
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
        if (isIdentifierToken(firstToken) && builder.lookAhead(1) == JSTokenTypes.EQ) {
          PsiBuilder.Marker marker = builder.mark();
          parseVarDeclaration(false);
          checkForSemicolon();
          marker.done(JSStubElementTypes.VAR_STATEMENT);
          return;
        }
        super.doParseStatement(canHaveClasses);
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

  private class AngularJSExpressionParser extends ExpressionParser<AngularJSParser> {
    private Key<Boolean> IN_FILTER = Key.create("angular.filter.started");

    public AngularJSExpressionParser() {
      super(AngularJSParser.this);
    }

    @Override
    public boolean parsePrimaryExpression() {
      final IElementType firstToken = builder.getTokenType();
      if (firstToken == JSTokenTypes.STRING_LITERAL) {
        return parseStringLiteral(firstToken);
      }
      return super.parsePrimaryExpression();
    }

    protected boolean parseBitwiseORExpression(final boolean allowIn) {
      PsiBuilder.Marker expr = builder.mark();
      if (!parseBitwiseXORExpression(allowIn)) {
        expr.drop();
        return false;
      }

      if (builder.getUserData(IN_FILTER) == null) {
        while (builder.getTokenType() == JSTokenTypes.OR) {
          builder.advanceLexer();
          if (!parseFilter()) {
            builder.error("expected filter");
          }
          expr.done(JSElementTypes.BINARY_EXPRESSION);
          expr = expr.precede();
        }
      }

      expr.drop();
      return true;
    }

    private boolean parseFilter() {
      final PsiBuilder.Marker mark = builder.mark();
      buildTokenElement(JSElementTypes.REFERENCE_EXPRESSION);
      PsiBuilder.Marker arguments = null;
      while (builder.getTokenType() == JSTokenTypes.COLON) {
        arguments = arguments == null ? builder.mark() : arguments;
        builder.advanceLexer();
        try {
          builder.putUserData(IN_FILTER, true);
          parseExpression();
        } finally {
          builder.putUserData(IN_FILTER, null);
        }
      }
      if (arguments != null) {
        arguments.done(JSElementTypes.ARGUMENT_LIST);
      }
      mark.done(JSElementTypes.CALL_EXPRESSION);
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
  }
}
