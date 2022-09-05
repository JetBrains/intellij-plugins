// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.WhitespacesBinders;
import com.intellij.lang.ecmascript6.ES6StubElementTypes;
import com.intellij.lang.javascript.*;
import com.intellij.lang.javascript.parsing.*;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.Consumer;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.Angular2LangUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import static org.angular2.lang.expr.lexer.Angular2TokenTypes.*;
import static org.angular2.lang.expr.parser.Angular2ElementTypes.*;

public class Angular2Parser extends JavaScriptParser<Angular2Parser.Angular2ExpressionParser,
  Angular2Parser.Angular2StatementParser, FunctionParser, JSPsiTypeParser> {

  /*
  Angular Expression AST mapping

  Binary            - JSBinaryExpression
  BindingPipe       - Angular2PipeExpression
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
    parseRoot(builder, root, createTemplateBindingsStatement(templateKey), false, false,
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

  private static void parseRoot(PsiBuilder builder,
                                IElementType root,
                                IElementType statementType,
                                boolean isAction,
                                boolean isSimpleBinding,
                                Consumer<? super Angular2StatementParser> parseAction) {
    final PsiBuilder.Marker rootMarker = builder.mark();
    final PsiBuilder.Marker statementMarker = builder.mark();
    parseAction.consume(new Angular2Parser(builder, isAction, isSimpleBinding, false).getStatementParser());
    statementMarker.done(statementType);
    rootMarker.done(root);
  }

  public static void parseJS(PsiBuilder builder, IElementType root) {
    new Angular2Parser(builder).parseJS(root);
  }

  private final boolean myIsAction;
  private final boolean myIsSimpleBinding;
  private final boolean myIsJavaScript;

  public Angular2Parser(PsiBuilder builder) {
    this(builder, false, false, true);
  }

  private Angular2Parser(PsiBuilder builder, boolean isAction, boolean isSimpleBinding, boolean isJavaScript) {
    super(DialectOptionHolder.JS_1_5, builder);
    myIsAction = isAction;
    myIsSimpleBinding = isSimpleBinding;
    myIsJavaScript = isJavaScript;
    myExpressionParser = new Angular2ExpressionParser();
    myStatementParser = new Angular2StatementParser(this);
  }

  protected class Angular2StatementParser extends StatementParser<Angular2Parser> {

    protected Angular2StatementParser(Angular2Parser parser) {
      super(parser);
    }

    public void parseChain() {
      assert !myIsJavaScript;
      final PsiBuilder.Marker chain = builder.mark();
      int count = 0;
      while (!builder.eof()) {
        count++;
        PsiBuilder.Marker expression = builder.mark();
        if (!getExpressionParser().parseExpressionOptional(false, false)) {
          builder.error(JavaScriptBundle.message("javascript.parser.message.expected.expression"));
          builder.advanceLexer();
          expression.drop();
        }
        else if (myIsAction) {
          expression.done(EXPRESSION_STATEMENT);
        }
        else {
          expression.drop();
        }
        IElementType tokenType = builder.getTokenType();
        if (tokenType == SEMICOLON) {
          if (!myIsAction) {
            builder.error(Angular2Bundle.message("angular.parse.expression.chained-expression-in-binding"));
          }
          while (builder.getTokenType() == SEMICOLON) {
            builder.advanceLexer();
          }
        }
        else if (tokenType != null) {
          builder.error(Angular2Bundle.message("angular.parse.expression.unexpected-token", builder.getTokenText()));
        }
      }
      switch (count) {
        case 0:
          if (myIsAction) {
            chain.done(EMPTY_STATEMENT);
          }
          else {
            chain.done(EMPTY_EXPRESSION);
          }
          break;
        case 1:
          chain.drop();
          break;
        default:
          if (myIsAction) {
            chain.done(CHAIN_STATEMENT);
          }
          else {
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
          rawKey = parseTemplateBindingKey(isVar);
          key = isVar ? rawKey : templateKey + StringUtil.capitalize(rawKey);
          if (builder.getTokenType() == COLON) {
            builder.advanceLexer();
          }
        }

        String name = null;
        if (isVar) {
          if (builder.getTokenType() == EQ) {
            builder.advanceLexer();
            name = parseTemplateBindingKey(false);
          }
          else {
            name = Angular2LangUtil.$IMPLICIT;
          }
        }
        else if (builder.getTokenType() == AS_KEYWORD) {
          builder.advanceLexer();
          name = rawKey;
          key = parseTemplateBindingKey(true);
          isVar = true;
        }
        else if (builder.getTokenType() != LET_KEYWORD
                 && !getExpressionParser().parsePipe()) {
          builder.error(JavaScriptBundle.message("javascript.parser.message.expected.expression"));
        }

        binding.done(createTemplateBindingStatement(key, isVar, name));

        if (builder.getTokenType() == AS_KEYWORD && !isVar) {
          final PsiBuilder.Marker localBinding = builder.mark();
          builder.advanceLexer();
          String letName = parseTemplateBindingKey(true);
          localBinding.done(createTemplateBindingStatement(letName, true, key));
        }
        if (builder.getTokenType() == SEMICOLON
            || builder.getTokenType() == COMMA) {
          builder.advanceLexer();
        }
      }
      while (!builder.eof());
    }

    private String parseTemplateBindingKey(boolean isVariable) {
      final PsiBuilder.Marker key = builder.mark();
      boolean operatorFound = true;
      StringBuilder result = new StringBuilder();
      do {
        if (!isIdentifierName(builder.getTokenType())) {
          if (result.length() > 0) {
            finishKey(key, isVariable);
          }
          else {
            key.drop();
          }
          builder.error(Angular2Bundle.message("angular.parse.expression.expected-identifier-or-keyword"));
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
      finishKey(key, isVariable);
      return result.toString();
    }
  }

  private static void finishKey(PsiBuilder.Marker key, boolean isVariable) {
    if (isVariable) {
      key.collapse(IDENTIFIER);
      key = key.precede();
      key.done(TEMPLATE_VARIABLE);
      key.precede().done(VAR_STATEMENT);
    }
    else {
      key.done(TEMPLATE_BINDING_KEY);
    }
  }

  protected class Angular2ExpressionParser extends ExpressionParser<Angular2Parser> {

    @NonNls private static final String CHAR_ENTITY_QUOT = "&quot;";
    @NonNls private static final String CHAR_ENTITY_APOS = "&apos;";

    public Angular2ExpressionParser() {
      super(Angular2Parser.this);
    }

    @Override
    public boolean parseAssignmentExpression(boolean allowIn) {
      //In Angular EL Pipe is the top level expression instead of Assignment
      return parsePipe();
    }

    @Override
    public void parseScriptExpression() {
      throw new UnsupportedOperationException();
    }

    public boolean parsePipe() {
      PsiBuilder.Marker pipe = builder.mark();
      PsiBuilder.Marker firstParam = builder.mark();
      if (!parseAssignmentExpressionChecked()) {
        firstParam.drop();
        pipe.drop();
        return false;
      }

      while (builder.getTokenType() == OR) {
        if (myIsSimpleBinding) {
          builder.error(Angular2Bundle.message("angular.parse.expression.pipe-in-host-binding"));
        }
        else if (myIsAction) {
          builder.error(Angular2Bundle.message("angular.parse.expression.pipe-in-action"));
        }
        firstParam.done(PIPE_LEFT_SIDE_ARGUMENT);
        builder.advanceLexer();
        if (builder.getTokenType() == IDENTIFIER
            || KEYWORDS.contains(builder.getTokenType())) {
          PsiBuilder.Marker pipeName = builder.mark();
          builder.advanceLexer();
          pipeName.done(PIPE_REFERENCE_EXPRESSION);
        }
        else {
          builder.error(Angular2Bundle.message("angular.parse.expression.expected-identifier-or-keyword"));
        }
        PsiBuilder.Marker params = builder.mark();
        boolean hasParams = false;
        while (builder.getTokenType() == COLON) {
          builder.advanceLexer();
          if (!parseAssignmentExpressionChecked()) {
            builder.error(JavaScriptBundle.message("javascript.parser.message.expected.expression"));
          }
          else {
            hasParams = true;
          }
        }
        if (hasParams) {
          params.done(PIPE_ARGUMENTS_LIST);
        }
        else {
          params.drop();
        }
        pipe.done(PIPE_EXPRESSION);
        firstParam = pipe.precede();
        pipe = firstParam.precede();
      }
      firstParam.drop();
      pipe.drop();
      return true;
    }

    public boolean parseAssignmentExpressionChecked() {
      final PsiBuilder.Marker expr = builder.mark();
      if (builder.getTokenType() == EQ) {
        builder.error(JavaScriptBundle.message("javascript.parser.message.expected.expression"));
        builder.advanceLexer();
        if (!parsePipe()) {
          builder.error(JavaScriptBundle.message("javascript.parser.message.expected.expression"));
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
        if (!myIsAction && !myIsJavaScript) {
          builder.error(Angular2Bundle.message("angular.parse.expression.assignment-in-binding"));
        }
        builder.advanceLexer();
        if (!parsePipe()) {
          builder.error(JavaScriptBundle.message("javascript.parser.message.expected.expression"));
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
      if (firstToken == JSTokenTypes.STRING_LITERAL_PART
          || isEntityStringStart(firstToken)) {
        return parsePartialStringLiteral(firstToken);
      }
      return super.parsePrimaryExpression();
    }

    private boolean isEntityStringStart(IElementType tokenType) {
      if (tokenType != XML_CHAR_ENTITY_REF) {
        return false;
      }
      String text = builder.getTokenText();
      return text != null && (text.equals(CHAR_ENTITY_QUOT) || text.equals(CHAR_ENTITY_APOS));
    }

    @Override
    protected boolean isIdentifierToken(IElementType tokenType) {
      return !KEYWORDS.contains(tokenType) && super.isIdentifierToken(tokenType);
    }

    @Override
    protected int getCurrentBinarySignPriority(boolean allowIn, boolean advance) {
      if (builder.getTokenType() == OR) {
        return -1;
      }
      return super.getCurrentBinarySignPriority(allowIn, advance);
    }

    @Override
    protected @Nullable IElementType getSafeAccessOperator() {
      return JSTokenTypes.ELVIS;
    }

    @Override
    protected boolean isReferenceQualifierSeparator(IElementType tokenType) {
      return tokenType == JSTokenTypes.DOT || tokenType == getSafeAccessOperator();
    }

    @Override
    protected boolean isPropertyStart(IElementType elementType) {
      if (elementType != IDENTIFIER
          && elementType != STRING_LITERAL
          && !KEYWORDS.contains(elementType)) {
        builder.error(Angular2Bundle.message("angular.parse.expression.expected-identifier-keyword-or-string"));
        return false;
      }
      return true;
    }

    @Override
    protected boolean parseDialectSpecificMemberExpressionPart(Ref<PsiBuilder.Marker> markerRef) {
      if (builder.getTokenType() == EXCL) {
        builder.advanceLexer();
        PsiBuilder.Marker marker = markerRef.get();
        marker.done(JSElementTypes.NOT_NULL_EXPRESSION);
        markerRef.set(marker.precede());
        return true;
      }
      return false;
    }

    @Override
    @AdvancesLexer
    protected boolean parsePropertyNoMarker(PsiBuilder.Marker property) {
      final IElementType firstToken = builder.getTokenType();
      final IElementType secondToken = builder.lookAhead(1);

      if (myJavaScriptParser.isIdentifierName(firstToken) && // Angular, in contrast to ECMAScript, accepts Reserved Words here
          (secondToken == JSTokenTypes.COMMA || secondToken == JSTokenTypes.RBRACE)) {
        final PsiBuilder.Marker ref = builder.mark();
        builder.advanceLexer();
        ref.done(JSElementTypes.REFERENCE_EXPRESSION);
        property.done(ES6StubElementTypes.ES6_PROPERTY);
        return true;
      }

      if (PROPERTY_NAMES.contains(firstToken)) {
        String errorMessage = validateLiteral();
        advancePropertyName(firstToken);
        if (errorMessage != null) {
          builder.error(errorMessage);
        }
      }
      else {
        builder.error(JavaScriptBundle.message("javascript.parser.message.expected.property.name"));
        builder.advanceLexer();
      }

      parsePropertyInitializer(false);

      property.done(JSStubElementTypes.PROPERTY);
      property.setCustomEdgeTokenBinders(INCLUDE_DOC_COMMENT_AT_LEFT, WhitespacesBinders.DEFAULT_RIGHT_BINDER);

      return true;
    }

    private boolean parsePartialStringLiteral(IElementType firstToken) {
      final PsiBuilder.Marker mark = builder.mark();
      IElementType currentToken = firstToken;
      StringBuilder literal = new StringBuilder();
      String text = getCurrentLiteralPartTokenText(currentToken);
      boolean singleQuote = text == null
                            || text.startsWith("'");
      boolean first = true;
      while (text != null
             && (currentToken == JSTokenTypes.STRING_LITERAL_PART
                 || STRING_PART_SPECIAL_SEQ.contains(currentToken))) {
        literal.append(text);
        builder.advanceLexer();
        if (!first
            && ((singleQuote && (text.endsWith("'") && !text.endsWith("\\'")))
                || (!singleQuote && text.endsWith("\"") && !text.endsWith("\\\"")))) {
          break;
        }
        first = false;
        currentToken = builder.getTokenType();
        text = getCurrentLiteralPartTokenText(currentToken);
      }
      mark.done(JSStubElementTypes.LITERAL_EXPRESSION);
      final String errorMessage = validateLiteralText(literal.toString());
      if (errorMessage != null) {
        builder.error(errorMessage);
      }
      return true;
    }

    private String getCurrentLiteralPartTokenText(IElementType currentToken) {
      String text = builder.getTokenText();
      if (text != null && currentToken == XML_CHAR_ENTITY_REF) {
        if (text.equals(CHAR_ENTITY_APOS)) {
          return "'";
        }
        else if (text.equals(CHAR_ENTITY_QUOT)) {
          return "\"";
        }
      }
      return text;
    }
  }
}
