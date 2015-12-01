package org.angularjs.lang.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.parsing.ExpressionParser;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Irina.Chernushina on 11/30/2015.
 */
public class AngularJSMessageFormatParser extends ExpressionParser<AngularJSParser> {
  private final static String PLURAL = "plural";
  private final static String SELECT = "select";
  @NonNls public static final String OFFSET_OPTION = "offset";
  private boolean myInsideSelectExpression = false;

  public AngularJSMessageFormatParser(@NotNull AngularJSParser parser) {
    super(parser);
  }

  public boolean parseMessage() {
    if (myInsideSelectExpression) return false;

    final PsiBuilder.Marker expr = builder.mark();
    if (! isIdentifierToken(builder.getTokenType())) {
      return rollback(expr);
    }
    final PsiBuilder.Marker refMark = builder.mark();
    //if (!myJavaScriptParser.getExpressionParser().parseQualifiedTypeName()) {
    myInsideSelectExpression = true;
    try {
      if (!myJavaScriptParser.getExpressionParser().parseUnaryExpression()) {
        return rollback(expr);
      }
    } finally {
      myInsideSelectExpression = false;
    }
    //}
    refMark.done(JSElementTypes.EXPRESSION_STATEMENT);
    if (builder.getTokenType() != JSTokenTypes.COMMA) {
      return rollback(expr);
    }
    builder.advanceLexer();
    final String extensionText = builder.getTokenText();
    if (!isKnownExtension(extensionText) || !isIdentifierToken(builder.getTokenType())) {
      return rollback(expr);
    }

    collapseTokenElement(AngularJSElementTypes.MESSAGE_FORMAT_EXPRESSION_NAME);
    if (builder.getTokenType() != JSTokenTypes.COMMA) {
      return rollback(expr);
    }
    builder.advanceLexer();
    if (SELECT.equals(extensionText)) {
      parseOptionsTail();
    } else {
      parsePluralTail();
    }
    expr.done(AngularJSElementTypes.MESSAGE_FORMAT_EXPRESSION);
    return true;
  }

  // todo continue: other is required option for plural
  public boolean parseInnerMessage() {
    final PsiBuilder.Marker mark = builder.mark();
    PsiBuilder.Marker stringLiteralMark = null;
    while (!builder.eof()) {
      final IElementType type = builder.getTokenType();
      if (JSTokenTypes.LBRACE == type || JSTokenTypes.RBRACE == type) {
        if (stringLiteralMark != null) {
          stringLiteralMark.collapse(JSTokenTypes.STRING_LITERAL);
          stringLiteralMark = null;
        }
        if (JSTokenTypes.LBRACE == type) {
          if (JSTokenTypes.LBRACE == builder.lookAhead(1)) {
            builder.advanceLexer();
            builder.advanceLexer();
            myJavaScriptParser.getExpressionParser().parseExpression();
            if (!isRBraceOrNull(builder.getTokenType()) || !isRBraceOrNull(builder.lookAhead(1))) {
              builder.error("expected }}");
              mark.drop();
              return false;
            }
            builder.advanceLexer();
            builder.advanceLexer();
            continue;
          }
          else {
            builder.error("expected reference");
            mark.drop();
            return false;
          }
        }
        else if (JSTokenTypes.RBRACE == type) {
          mark.done(AngularJSElementTypes.MESSAGE_FORMAT_MESSAGE);
          builder.advanceLexer();
          return true;
        }
      } else {
        if (stringLiteralMark == null) stringLiteralMark = builder.mark();
        builder.advanceLexer();
      }
    }
    if (stringLiteralMark != null) stringLiteralMark.drop();
    mark.drop();
    return false;
  }

  private static boolean isRBraceOrNull(IElementType type) {
    return type == null || JSTokenTypes.RBRACE == type;
  }

  private void parsePluralTail() {
    if (!parseOffsetOption()) return;
    parseOptionsTail();
  }

  private boolean parseOffsetOption() {
    if (isIdentifierToken(builder.getTokenType()) && OFFSET_OPTION.equals(builder.getTokenText())) {
      if (builder.lookAhead(1) != JSTokenTypes.COLON || builder.lookAhead(2) != JSTokenTypes.NUMERIC_LITERAL) {
        return true;
      }
      final PsiBuilder.Marker mark = builder.mark();
      builder.advanceLexer();// offset
      builder.advanceLexer();// colon
      builder.advanceLexer();// numeric literal
      mark.done(AngularJSElementTypes.MESSAGE_FORMAT_OPTION);
      if (builder.getTokenType() != JSTokenTypes.COMMA) {
        builder.error("expected comma");
        return false;
      }
      builder.advanceLexer();
    }
    return true;
  }

  private void parseOptionsTail() {
    boolean key = true;
    while (!builder.eof()) {
      final IElementType type = builder.getTokenType();
      if (key) {
        if (JSTokenTypes.LITERALS.contains(type) || isIdentifierToken(type) || JSTokenTypes.EQ == type) {
          final PsiBuilder.Marker mark = builder.mark();
          int i = 1;
          IElementType forwardType = null;
          // = can be only in the beginning, like =0
          for (; !JSTokenTypes.PARSER_WHITE_SPACE_TOKENS.contains((forwardType = builder.rawLookup(i))); i++);
          if (JSTokenTypes.PARSER_WHITE_SPACE_TOKENS.contains(forwardType) || forwardType == null) {
            for (int j = 0; j < i; j++) {
              builder.advanceLexer();
            }
            mark.collapse(AngularJSElementTypes.MESSAGE_FORMAT_SELECTION_KEYWORD);
            key = false;
          } else {
            mark.drop();
            builder.error("expected selection keyword");
            return;
          }
        } else {
          if (JSTokenTypes.RBRACE == type) {
            builder.advanceLexer();
            return;
          }
          builder.error("expected selection keyword");
          return;
        }
      } else {
        if (JSTokenTypes.LBRACE == type) {
          builder.advanceLexer();
          if (!parseInnerMessage()) return;  //+-
          key = true;
        } else {
          builder.error("expected message");
          return;
        }
      }
    }
  }

  private void collapseTokenElement(IElementType type) {
    final PsiBuilder.Marker mark = builder.mark();
    builder.advanceLexer();
    mark.collapse(type);
  }

  private static boolean isKnownExtension(String text) {
    return PLURAL.equals(text) || SELECT.equals(text);
  }

  private static boolean rollback(PsiBuilder.Marker expr) {
    expr.rollbackTo();
    return false;
  }
}
