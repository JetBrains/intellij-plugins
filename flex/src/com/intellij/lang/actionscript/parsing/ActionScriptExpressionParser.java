package com.intellij.lang.actionscript.parsing;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.WhitespacesBinders;
import com.intellij.lang.javascript.*;
import com.intellij.lang.javascript.parsing.ExpressionParser;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

/**
 * @author Konstantin.Ulitin
 */
public final class ActionScriptExpressionParser extends ExpressionParser<ActionScriptParser> {
  ActionScriptExpressionParser(ActionScriptParser parser) {
    super(parser);
  }

  @Override
  protected boolean isPropertyStart(IElementType elementType) {
    return JSKeywordSets.AS_IDENTIFIER_TOKENS_SET.contains(elementType) ||
           elementType == JSTokenTypes.STRING_LITERAL ||
           elementType == JSTokenTypes.NUMERIC_LITERAL ||
           elementType == JSTokenTypes.LPAR;
  }

  @Override
  public boolean isPropertyNameStart(IElementType elementType) {
    return JSKeywordSets.PROPERTY_NAMES.contains(elementType);
  }

  @Override
  protected boolean isFunctionPropertyStart(@NotNull PsiBuilder builder) {
    return JSKeywordSets.PROPERTY_NAMES.contains(builder.getTokenType()) && builder.lookAhead(1) == JSTokenTypes.LPAR;
  }

  @Override
  protected boolean parsePropertyNoMarker(PsiBuilder.Marker property) {
    if (builder.getTokenType() == JSTokenTypes.LPAR) {
      parseParenthesizedExpression();
      parsePropertyInitializer(false);
      property.done(JSStubElementTypes.PROPERTY);
      property.setCustomEdgeTokenBinders(INCLUDE_DOC_COMMENT_AT_LEFT, WhitespacesBinders.DEFAULT_RIGHT_BINDER);
      return true;
    }

    return super.parsePropertyNoMarker(property);
  }

  @Override
  protected boolean isReferenceQualifierSeparator(IElementType tokenType) {
    return tokenType == JSTokenTypes.DOT || tokenType == JSTokenTypes.COLON_COLON || tokenType == JSTokenTypes.DOT_DOT;
  }

  @Override
  public boolean parsePrimaryExpression() {
    if (builder.getTokenType() == JSTokenTypes.AT) {
      PsiBuilder.Marker attrReferenceStartMarker = builder.mark();

      builder.advanceLexer();
      PsiBuilder.Marker possibleNamespaceStartMarker = builder.mark();

      if (!builder.eof()) {
        IElementType tokenType = builder.getTokenType();
        if (tokenType == JSTokenTypes.ANY_IDENTIFIER ||
            isIdentifierToken(tokenType)) {
          builder.advanceLexer();

          if (builder.getTokenType() == JSTokenTypes.COLON_COLON) {
            possibleNamespaceStartMarker.done(JSElementTypes.REFERENCE_EXPRESSION);
            possibleNamespaceStartMarker = possibleNamespaceStartMarker.precede();
            proceedWithNamespaceReference(possibleNamespaceStartMarker, true);

            possibleNamespaceStartMarker = null;
          }
        }
        else if (tokenType == JSTokenTypes.LBRACKET) {
          builder.advanceLexer();
          parseExpression();
          checkMatches(builder, JSTokenTypes.RBRACKET, "javascript.parser.message.expected.rbracket");
        }
        else {
          builder.error(JavaScriptCoreBundle.message("javascript.parser.message.expected.identifier"));
        }
      }

      if (possibleNamespaceStartMarker != null) possibleNamespaceStartMarker.drop();
      attrReferenceStartMarker.done(JSElementTypes.REFERENCE_EXPRESSION);

      return true;
    }

    return super.parsePrimaryExpression();
  }

  @Override
  protected int getCurrentBinarySignPriority(boolean allowIn, boolean advance) {
    IElementType tokenType = builder.getTokenType();
    if (tokenType == JSTokenTypes.IS_KEYWORD || tokenType == JSTokenTypes.AS_KEYWORD) {
      if (advance) builder.advanceLexer();
      return 10;
    }
    return super.getCurrentBinarySignPriority(allowIn, advance);
  }
}
