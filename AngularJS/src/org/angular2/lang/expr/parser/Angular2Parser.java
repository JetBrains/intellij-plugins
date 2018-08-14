// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
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

  private boolean myIsAction;
  private boolean myIsSimpleBinding;

  public Angular2Parser(PsiBuilder builder) {
    super(JavaScriptSupportLoader.JAVASCRIPT_1_5, builder);
    myExpressionParser = new Angular2ExpressionParser();
    myStatementParser = new Angular2StatementParser(this);
  }

  public void parseAction(IElementType root) {
    parseRoot(root, true, false, () -> getStatementParser().parseChain());
  }

  public void parseBinding(IElementType root) {
    parseRoot(root, false, false, () -> {
      if (!getStatementParser().parseQuote()) {
        getStatementParser().parseChain();
      }
    });
  }

  public void parseTemplateBindings(IElementType root, String templateKey) {
    parseRoot(root, false, false, () -> getStatementParser().parseTemplateBindings(templateKey));
  }

  public void parseInterpolation(IElementType root) {
    parseRoot(root, false, false, () -> getStatementParser().parseChain());
  }

  public void parseSimpleBinding(IElementType root) {
    parseRoot(root, false, true, () -> {
      if (!getStatementParser().parseQuote()) {
        getStatementParser().parseChain();
      }
    });
  }

  private void parseRoot(IElementType root, boolean isAction, boolean isSimpleBinding, Runnable parseAction) {
    myIsAction = isAction;
    myIsSimpleBinding = isSimpleBinding;
    final PsiBuilder.Marker rootMarker = builder.mark();
    parseAction.run();
    rootMarker.done(root);
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
        if (!getExpressionParser().parseExpressionOptional(false, false)) {
          builder.error(JSBundle.message("javascript.parser.message.expected.expression"));
          builder.advanceLexer();
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
          chain.done(EMPTY_STATEMENT);
          break;
        case 1:
          chain.done(EXPRESSION_STATEMENT);
          break;
        default:
          chain.done(CHAIN_STATEMENT);
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
      final PsiBuilder.Marker bindings = builder.mark();
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
      bindings.done(TEMPLATE_BINDINGS_STATEMENT);
    }

    private String parseTemplateBindingKey() {
      final PsiBuilder.Marker key = builder.mark();
      boolean operatorFound = true;
      StringBuilder result = new StringBuilder();
      do {
        if (!isIdentifierName(builder.getTokenType())) {
          if (result.length() > 0) {
            key.done(TEMPLATE_BINDING_KEY);
          } else {
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
          builder.advanceLexer();
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
