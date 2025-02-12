package com.intellij.lang.actionscript.parsing;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.actionscript.ActionScriptStubElementTypes;
import com.intellij.lang.javascript.*;
import com.intellij.lang.javascript.parsing.FunctionParser;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSAttributeNameValuePairImpl;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.tree.IElementType;

/**
 * @author Konstantin.Ulitin
 */
public final class ActionScriptFunctionParser extends FunctionParser<ActionScriptParser> {

  private static final Logger LOG = Logger.getInstance(ActionScriptFunctionParser.class);

  ActionScriptFunctionParser(ActionScriptParser parser) {
    super(parser);
  }

  @Override
  public boolean parseFunctionName(boolean functionKeywordWasOmitted, Context context) {
    if (parseGetSetAndNameAfterFunctionKeyword(context)) return true;
    return super.parseFunctionName(functionKeywordWasOmitted, context);
  }

  @Override
  public void parseFunctionIdentifier() {
    if (!JSKeywordSets.PROPERTY_NAMES.contains(builder.getTokenType())) {
      LOG.error(builder.getTokenText());
    }

    parser.getStatementParser().parsePossiblyQualifiedName();
  }

  @Override
  public IElementType getFunctionDeclarationElementType() {
    return ActionScriptStubElementTypes.ACTIONSCRIPT_FUNCTION;
  }

  @Override
  public boolean parseAttributesList() {
    final PsiBuilder.Marker modifierList = builder.mark();

    boolean seenNs = false;
    boolean seenAnyAttributes = false;

    try {
      boolean hasSomethingInAttrList = true;
      boolean hadConditionalCompileBlock = false;

      boolean doNotAllowAttributes = false;
      while (hasSomethingInAttrList) {
        hasSomethingInAttrList = false;

        while (builder.getTokenType() == JSTokenTypes.LBRACKET) {
          if (doNotAllowAttributes) {
            builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.declaration"));
            break;
          }

          PsiBuilder.Marker attribute = builder.mark();

          builder.advanceLexer();

          IElementType tokenType = builder.getTokenType();
          if (tokenType == JSTokenTypes.RBRACKET) {
            builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.identifier"));
          }
          else if (tokenType == null || !isIdentifierToken(tokenType)) {
            attribute.drop();
            return false;
          }
          else {
            builder.advanceLexer();
          }

          while (builder.getTokenType() != JSTokenTypes.RBRACKET) {
            parseAttributeBody();

            if (builder.eof()) {
              attribute.done(JSStubElementTypes.ATTRIBUTE);
              builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.rbracket"));
              return true;
            }
          }

          builder.advanceLexer();
          attribute.done(JSStubElementTypes.ATTRIBUTE);
          hasSomethingInAttrList = true;
        }

        if (builder.getTokenType() == JSTokenTypes.INCLUDE_KEYWORD) {
          hasSomethingInAttrList = true;
          parser.getStatementParser().parseIncludeDirective();
        }

        if (builder.getTokenType() == JSTokenTypes.USE_KEYWORD && !doNotAllowAttributes) {
          hasSomethingInAttrList = true;
          parser.getStatementParser().parseUseNamespaceDirective();
        }

        if (builder.getTokenType() == JSTokenTypes.IDENTIFIER && !seenNs) {
          PsiBuilder.Marker identifier = builder.mark();
          hasSomethingInAttrList = true;
          seenNs = true;
          PsiBuilder.Marker marker = builder.mark();
          builder.advanceLexer();
          marker.done(JSElementTypes.REFERENCE_EXPRESSION);

          IElementType tokenType = builder.getTokenType();

          if (!hadConditionalCompileBlock) {
            if (tokenType == JSTokenTypes.COLON_COLON &&
                parser.getExpressionParser().proceedWithNamespaceReference(identifier, false)) {
              (identifier = identifier.precede()).done(JSElementTypes.REFERENCE_EXPRESSION);
              identifier.precede().done(JSElementTypes.CONDITIONAL_COMPILE_VARIABLE_REFERENCE);
              hadConditionalCompileBlock = true;
              seenNs = false;
            }
            else if (tokenType == JSTokenTypes.DOT) {
              while (builder.getTokenType() == JSTokenTypes.DOT) {
                builder.advanceLexer();
                boolean identifierToken = isIdentifierToken(builder.getTokenType());
                if (identifierToken) {
                  builder.advanceLexer();
                }
                identifier.done(JSElementTypes.REFERENCE_EXPRESSION);
                identifier = identifier.precede();
                if (!identifierToken) {
                  builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.name"));
                  break;
                }
              }
              identifier.drop();
            }
            else {
              identifier.drop();
            }
          }
          else {
            identifier.drop();
          }
        }

        IElementType tokenType;
        while (JSTokenTypes.MODIFIERS.contains(tokenType = builder.getTokenType())
               || tokenType == JSTokenTypes.GET_KEYWORD || tokenType == JSTokenTypes.SET_KEYWORD) {
          doNotAllowAttributes = true;
          seenAnyAttributes = true;
          hasSomethingInAttrList = true;
          if (builder.getTokenType() == JSTokenTypes.NATIVE_KEYWORD ||
              builder.getTokenType() == JSTokenTypes.DECLARE_KEYWORD) {
            builder.putUserData(methodsEmptinessKey, MethodEmptiness.ALWAYS);
          }
          builder.advanceLexer();
        }

        if (builder.eof()) {
          return true;
        }
      }
    }
    finally {
      final IElementType currentTokenType = builder.getTokenType();

      if (seenNs &&
          !seenAnyAttributes &&
          isNonAttrListOwner(currentTokenType)
      ) {
        modifierList.rollbackTo();
      }
      else {
        modifierList.done(getAttributeListElementType());
      }
    }
    return true;
  }

  @Override
  public IElementType getAttributeListElementType() {
    return ActionScriptStubElementTypes.ACTIONSCRIPT_ATTRIBUTE_LIST;
  }

  public void parseAttributeBody() {
    final boolean haveLParen = checkMatches(builder, JSTokenTypes.LPAR, "javascript.parser.message.expected.lparen");

    while (haveLParen) {
      boolean hasName = JSAttributeNameValuePairImpl.IDENTIFIER_TOKENS_SET.contains(builder.getTokenType());

      if (builder.getTokenType() == JSTokenTypes.COMMA) {
        builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.identifier.or.value"));
        break;
      }
      IElementType tokenType = builder.getTokenType();
      if (tokenType == JSTokenTypes.RBRACKET) break;
      if (tokenType == JSTokenTypes.RPAR) break;

      PsiBuilder.Marker attributeNameValuePair = builder.mark();
      builder.advanceLexer();

      if (hasName && builder.getTokenType() != JSTokenTypes.COMMA && builder.getTokenType() != JSTokenTypes.RPAR) {
        checkMatches(builder, JSTokenTypes.EQ, "javascript.parser.message.expected.equal");

        IElementType type = builder.getTokenType();
        if (type != JSTokenTypes.COMMA && type != JSTokenTypes.RBRACKET && type != JSTokenTypes.RPAR) {
          if (type == JSTokenTypes.IDENTIFIER) {
            PsiBuilder.Marker ident = builder.mark();
            builder.advanceLexer();
            IElementType nextTokenType = builder.getTokenType();
            ident.rollbackTo();
            if (!JSTokenTypes.STRING_LITERALS.contains(nextTokenType)) {
              parser.getExpressionParser().parseSimpleExpression();
            }
            else {
              builder.advanceLexer();
            }
          }
          else {
            builder.advanceLexer();
          }
        }
        else {
          builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.value"));
        }
      }

      attributeNameValuePair.done(JSStubElementTypes.ATTRIBUTE_NAME_VALUE_PAIR);
      if (builder.getTokenType() != JSTokenTypes.COMMA) break;
      builder.advanceLexer();

      if (builder.eof()) {
        builder.error(JavaScriptParserBundle.message("javascript.parser.message.expected.rparen"));
        return;
      }
    }

    if (haveLParen) {
      checkMatches(builder, JSTokenTypes.RPAR, "javascript.parser.message.expected.rparen");
    }
    else {
      builder.advanceLexer();
    }
  }

  private static boolean isNonAttrListOwner(IElementType currentTokenType) {
    return currentTokenType != JSTokenTypes.VAR_KEYWORD &&
           currentTokenType != JSTokenTypes.CONST_KEYWORD &&
           currentTokenType != JSTokenTypes.FUNCTION_KEYWORD &&
           currentTokenType != JSTokenTypes.CLASS_KEYWORD &&
           currentTokenType != JSTokenTypes.INTERFACE_KEYWORD &&
           currentTokenType != JSTokenTypes.NAMESPACE_KEYWORD;
  }

  @Override
  public void parseFunctionExpressionAttributeList() {
    final PsiBuilder.Marker mark = builder.mark();
    IElementType type = builder.getTokenType();
    if (type == JSTokenTypes.GET_KEYWORD
        || type == JSTokenTypes.SET_KEYWORD) {
      builder.advanceLexer();
    }
    mark.done(getAttributeListElementType());
  }

  @Override
  protected IElementType getFunctionExpressionElementType() {
    return ActionScriptStubElementTypes.ACTIONSCRIPT_FUNCTION_EXPRESSION;
  }

  @Override
  public IElementType getParameterType() {
    return ActionScriptStubElementTypes.ACTIONSCRIPT_PARAMETER;
  }
}
