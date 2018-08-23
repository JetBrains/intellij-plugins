// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.JSStubElementTypes;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.parsing.*;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.Consumer;

import static org.angular2.lang.expr.lexer.Angular2TokenTypes.*;
import static org.angular2.lang.expr.parser.Angular2ElementTypes.*;

public class Angular2Parser
  extends
  JavaScriptParser<Angular2Parser.Angular2ExpressionParser, Angular2Parser.Angular2StatementParser, FunctionParser, JSPsiTypeParser> {

  /*
  Angular Expression AST mapping

  Binary            - JSBinaryExpression
  BindingPipe       - Angular2Pipe
  Chain             - Angular2Chain
  Conditional       - JSConditionalExpression
  FunctionCall      - JSCallExpression
  ImplicitReceiver  - JSThisExpression
  KeyedRead         - JSIndexedPropertyAccessExpression
  KeyedWrite        - JSIndexedPropertyAccessExpression
  LiteralArray      - JSArrayLiteralExpression
  LiteralMap        - JSObjectLiteralExpression
  LiteralPrimitive  - JSLiteralExpression
  MethodCall        - JSCallExpression
  NonNullAssert     - JSPostfixExpression
  PrefixNot         - JSPrefixExpression
  PropertyRead      - JSReferenceExpression
  PropertyWrite     - JSReferenceExpression
  Quote             - Angular2Quote
  SafeMethodCall    - JSCallExpression
  SafePropertyRead  - JSReferenceExpression
  */


  public static void parseAction(PsiBuilder builder, IElementType root) {
    parseRoot(builder, root, ACTION_STATEMENT, true, false,
              Angular2StatementParser::parseChain);
  }

  public static void parseBinding(PsiBuilder builder, IElementType root) {
    parseRoot(builder, root, BINDING_STATEMENT, false, false, parser -> {
      if (!parser.parseQuote()) {
        parser.parseChain();
      }
    });
  }

  public static void parseTemplateBindings(PsiBuilder builder, IElementType root, String templateKey) {
    parseRoot(builder, root, TEMPLATE_BINDINGS_STATEMENT, false, false,
              parser -> parser.parseTemplateBindings(templateKey));
  }

  public static void parseInterpolation(PsiBuilder builder, IElementType root) {
    parseRoot(builder, root, INTERPOLATION_STATEMENT, false, false, Angular2StatementParser::parseChain);
  }

  public static void parseSimpleBinding(PsiBuilder builder, IElementType root) {
    parseRoot(builder, root, SIMPLE_BINDING_STATEMENT, false, true, parser -> {
      if (!parser.parseQuote()) {
        parser.parseChain();
      }
    });
  }

  private static void parseRoot(PsiBuilder builder, IElementType root, IElementType statementType, boolean isAction, boolean isSimpleBinding,
                                Consumer<Angular2StatementParser> parseAction) {
    final PsiBuilder.Marker rootMarker = builder.mark();
    final PsiBuilder.Marker statementMarker = builder.mark();
    parseAction.consume(new Angular2Parser(builder, isAction, isSimpleBinding).getStatementParser());
    statementMarker.done(statementType);
    rootMarker.done(root);
  }

  private final boolean myIsAction;
  private final boolean myIsSimpleBinding;

  private Angular2Parser(PsiBuilder builder, boolean isAction, boolean isSimpleBinding) {
    super(JavaScriptSupportLoader.JAVASCRIPT_1_5, builder);
    myIsAction = isAction;
    myIsSimpleBinding = isSimpleBinding;
    myExpressionParser = new Angular2ExpressionParser();
    myStatementParser = new Angular2StatementParser(this);
  }

  @Override
  public void parseJS(IElementType root) {
    throw new UnsupportedOperationException();
  }

  protected class Angular2StatementParser extends StatementParser<Angular2Parser> {

    protected Angular2StatementParser(Angular2Parser parser) {
      super(parser);
    }

    public void parseChain() {
      final PsiBuilder.Marker chain = builder.mark();
      int count = 0;
      while (!builder.eof()) {
        count++;
        PsiBuilder.Marker expression = builder.mark();
        if (!getExpressionParser().parseExpressionOptional(false, false)) {
          builder.error(JSBundle.message("javascript.parser.message.expected.expression"));
          builder.advanceLexer();
          expression.drop();
        } else if (myIsAction){
          expression.done(EXPRESSION_STATEMENT);
        } else {
          expression.drop();
        }
        IElementType tokenType = builder.getTokenType();
        if (tokenType == SEMICOLON) {
          if (!myIsAction) {
            builder.error("binding expression cannot contain chained expression");
          }
          while (builder.getTokenType() == SEMICOLON) {
            builder.advanceLexer();
          }
        }
        else if (tokenType != null) {
          builder.error("unexpected token '" + builder.getTokenText() + "'");
        }
      }
      switch (count) {
        case 0:
          if (myIsAction) {
            chain.done(EMPTY_STATEMENT);
          } else {
            chain.done(EMPTY_EXPRESSION);
          }
          break;
        case 1:
          chain.drop();
          break;
        default:
          if (myIsAction) {
            chain.done(CHAIN_STATEMENT);
          } else {
            chain.drop();
          }
      }
    }

    public boolean parseQuote() {
      final PsiBuilder.Marker quote = builder.mark();
      if (!(builder.getTokenType() == IDENTIFIER
            || KEYWORDS.contains(builder.getTokenType()))
          || builder.lookAhead(1) != COLON) {
        quote.drop();
        return false;
      }
      builder.advanceLexer();
      builder.enforceCommentTokens(TokenSet.EMPTY);
      builder.advanceLexer();
      final PsiBuilder.Marker rest = builder.mark();
      while (!builder.eof()) {
        builder.advanceLexer();
      }
      rest.collapse(STRING_LITERAL);
      quote.done(QUOTE_STATEMENT);
      return true;
    }

    public void parseTemplateBindings(String templateKey) {
      boolean firstBinding = true;
      do {
        final PsiBuilder.Marker binding = builder.mark();
        boolean isVar = false;
        String rawKey;
        String key;
        if (firstBinding) {
          rawKey = key = templateKey;
          firstBinding = false;
        }
        else {
          isVar = builder.getTokenType() == LET_KEYWORD;
          if (isVar) builder.advanceLexer();
          rawKey = parseTemplateBindingKey();
          key = isVar ? rawKey : templateKey + StringUtil.capitalize(rawKey);
          if (builder.getTokenType() == COLON) {
            builder.advanceLexer();
          }
        }

        String name = null;
        if (isVar) {
          if (builder.getTokenType() == EQ) {
            builder.advanceLexer();
            name = parseTemplateBindingKey();
          }
          else {
            name = "$implicit";
          }
        }
        else if (builder.getTokenType() == AS_KEYWORD) {
          builder.advanceLexer();
          name = rawKey;
          key = parseTemplateBindingKey();
          isVar = true;
        }
        else if (builder.getTokenType() != LET_KEYWORD
                 && !getExpressionParser().parsePipe()) {
          builder.error(JSBundle.message("javascript.parser.message.expected.expression"));
        }

        binding.done(createTemplateBindingStatement(key, isVar, name));

        if (builder.getTokenType() == AS_KEYWORD && !isVar) {
          final PsiBuilder.Marker localBinding = builder.mark();
          builder.advanceLexer();
          String letName = parseTemplateBindingKey();
          localBinding.done(createTemplateBindingStatement(letName, true, key));
        }
        if (builder.getTokenType() == SEMICOLON
            || builder.getTokenType() == COMMA) {
          builder.advanceLexer();
        }
      }
      while (!builder.eof());
    }

    private String parseTemplateBindingKey() {
      final PsiBuilder.Marker key = builder.mark();
      boolean operatorFound = true;
      StringBuilder result = new StringBuilder();
      do {
        if (!isIdentifierName(builder.getTokenType())) {
          if (result.length() > 0) {
            key.done(TEMPLATE_BINDING_KEY);
          }
          else {
            key.drop();
          }
          builder.error("Identifier or keyword expected");
          builder.advanceLexer();
          return result.toString();
        }
        result.append(builder.getTokenText());
        if (builder.rawLookup(1) == MINUS) {
          builder.advanceLexer();
          result.append(builder.getTokenText());
        }
        else {
          operatorFound = false;
        }
        builder.advanceLexer();
      }
      while (operatorFound);
      key.done(TEMPLATE_BINDING_KEY);
      return result.toString();
    }
  }

  protected class Angular2ExpressionParser extends ExpressionParser<Angular2Parser> {

    public Angular2ExpressionParser() {
      super(Angular2Parser.this);
    }

    @Override
    public boolean parseAssignmentExpression(boolean allowIn) {
      //In Angular EL Pipe is the top level expression instead of Assignment
      return parsePipe();
    }

    @Override
    public void parseScriptExpression(boolean isTypeContext) {
      throw new UnsupportedOperationException();
    }

    public boolean parsePipe() {
      PsiBuilder.Marker pipe = builder.mark();
      if (!parseAssignmentExpressionChecked()) {
        pipe.drop();
        return false;
      }

      while (builder.getTokenType() == OR) {
        if (myIsSimpleBinding) {
          builder.error("host binding expression cannot contain pipes");
        }
        else if (myIsAction) {
          builder.error("action expressions cannot contain pipes");
        }
        builder.advanceLexer();
        if (builder.getTokenType() == IDENTIFIER
            || KEYWORDS.contains(builder.getTokenType())) {
          PsiBuilder.Marker pipeName = builder.mark();
          builder.advanceLexer();
          pipeName.done(REFERENCE_EXPRESSION);
        }
        else {
          builder.error("expected identifier or keyword");
        }
        PsiBuilder.Marker params = builder.mark();
        while (builder.getTokenType() == COLON) {
          builder.advanceLexer();
          if (!parseAssignmentExpressionChecked()) {
            builder.error(JSBundle.message("javascript.parser.message.expected.expression"));
          }
        }
        params.done(ARGUMENT_LIST);
        pipe.done(PIPE_EXPRESSION);
        pipe = pipe.precede();
      }
      pipe.drop();
      return true;
    }

    public boolean parseAssignmentExpressionChecked() {
      final PsiBuilder.Marker expr = builder.mark();
      if (builder.getTokenType() == EQ) {
        builder.error(JSBundle.message("javascript.parser.message.expected.expression"));
        builder.advanceLexer();
        if (!parsePipe()) {
          builder.error(JSBundle.message("javascript.parser.message.expected.expression"));
        }
        expr.done(JSStubElementTypes.ASSIGNMENT_EXPRESSION);
        return true;
      }

      final PsiBuilder.Marker definitionExpr = builder.mark();
      if (!parseConditionalExpression(false)) {
        definitionExpr.drop();
        expr.drop();
        return false;
      }

      if (builder.getTokenType() == EQ) {
        definitionExpr.done(JSStubElementTypes.DEFINITION_EXPRESSION);
        if (!myIsAction) {
          builder.error("binding expressions cannot contain assignments");
        }
        builder.advanceLexer();
        if (!parsePipe()) {
          builder.error(JSBundle.message("javascript.parser.message.expected.expression"));
        }
        expr.done(JSStubElementTypes.ASSIGNMENT_EXPRESSION);
      }
      else {
        definitionExpr.drop();
        expr.drop();
      }
      return true;
    }

    @Override
    public boolean parsePrimaryExpression() {
      final IElementType firstToken = builder.getTokenType();
      if (firstToken == JSTokenTypes.STRING_LITERAL_PART) {
        return parsePartialStringLiteral(firstToken);
      }
      return super.parsePrimaryExpression();
    }

    @Override
    protected int getCurrentBinarySignPriority(boolean allowIn, boolean advance) {
      if (builder.getTokenType() == OR
          || builder.getTokenType() == AS_KEYWORD) {
        return -1;
      }
      return super.getCurrentBinarySignPriority(allowIn, advance);
    }

    @Override
    protected boolean isReferenceQualifierSeparator(IElementType tokenType) {
      return tokenType == ELVIS
             || tokenType == JSTokenTypes.DOT;
    }

    @Override
    protected boolean isPropertyStart(IElementType elementType) {
      if (elementType != IDENTIFIER
          && elementType != STRING_LITERAL
          && !KEYWORDS.contains(elementType)) {
        builder.error("expected identifier, keyword, or string");
        return false;
      }
      return true;
    }

    @Override
    protected boolean parseDialectSpecificMemberExpressionPart(Ref<PsiBuilder.Marker> markerRef) {
      if (builder.getTokenType() == EXCL) {
        builder.advanceLexer();
        markerRef.get().done(POSTFIX_EXPRESSION);
        markerRef.set(markerRef.get().precede());
        return true;
      }
      return false;
    }

    private boolean parsePartialStringLiteral(IElementType firstToken) {
      final PsiBuilder.Marker mark = builder.mark();
      IElementType currentToken = firstToken;
      StringBuilder literal = new StringBuilder();
      while (currentToken == JSTokenTypes.STRING_LITERAL_PART ||
             currentToken == ESCAPE_SEQUENCE ||
             currentToken == INVALID_ESCAPE_SEQUENCE) {
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
  }
}
