package org.angularjs.lang.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.parsing.ExpressionParser;
import com.intellij.psi.tree.IElementType;
import org.angularjs.codeInsight.AngularJSPluralCategories;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.angularjs.AngularJSBundle.message;

/**
 * @author Irina.Chernushina on 11/30/2015.
 */
public class AngularJSMessageFormatParser extends ExpressionParser<AngularJSParser> {
  @NonNls public static final String OFFSET_OPTION = "offset";
  private boolean myInsideSelectExpression = false;

  public AngularJSMessageFormatParser(@NotNull AngularJSParser parser) {
    super(parser);
  }

  public boolean parseMessage() {
    if (myInsideSelectExpression) return false;

    final PsiBuilder.Marker expr = builder.mark();
    if (!isIdentifierToken(builder.getTokenType())) {
      return rollback(expr);
    }
    final PsiBuilder.Marker refMark = builder.mark();
    //if (!myJavaScriptParser.getExpressionParser().parseQualifiedTypeName()) {
    myInsideSelectExpression = true;
    try {
      if (!myJavaScriptParser.getExpressionParser().parseUnaryExpression()) {
        return rollback(expr);
      }
    }
    finally {
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
    if (ExtensionType.select.name().equals(extensionText)) {
      parseOptionsTail();
    }
    else {
      parsePluralTail();
    }
    expr.done(AngularJSElementTypes.MESSAGE_FORMAT_EXPRESSION);
    return true;
  }

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
            if (!expectDoubleRBrace(true)) {
              mark.drop();
              return false;
            }
          }
          else {
            builder.error(message("angularjs.parser.message.expected.double.lbrace"));
            mark.drop();
            return false;
          }
        }
        else {
          mark.done(AngularJSElementTypes.MESSAGE_FORMAT_MESSAGE);
          builder.advanceLexer();
          return true;
        }
      }
      else {
        if (stringLiteralMark == null) stringLiteralMark = builder.mark();
        builder.advanceLexer();
      }
    }
    if (stringLiteralMark != null) stringLiteralMark.drop();
    mark.drop();
    return false;
  }

  private boolean expectDoubleRBrace(boolean advance) {
    if (!isRBraceOrNull(builder.getTokenType()) || !isRBraceOrNull(builder.lookAhead(1))) {
      builder.error(message("angularjs.parser.message.expected.double.rbrace"));
      return false;
    }
    if (advance) {
      builder.advanceLexer();
      builder.advanceLexer();
    }
    return true;
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
      if (builder.lookAhead(1) != JSTokenTypes.COLON) {
        builder.advanceLexer();
        builder.error(JavaScriptBundle.message("javascript.parser.message.expected.colon"));
        return false;
      }
      final IElementType value = builder.lookAhead(2);
      if (!JSTokenTypes.LITERALS.contains(value) && JSTokenTypes.IDENTIFIER != value) {
        builder.advanceLexer();
        builder.advanceLexer();
        builder.error(message("angularjs.parser.message.expected.offset.option"));
        return false;
      }
      final PsiBuilder.Marker mark = builder.mark();
      builder.advanceLexer();// offset
      builder.advanceLexer();// colon
      builder.advanceLexer();// value
      mark.done(AngularJSElementTypes.MESSAGE_FORMAT_OPTION);
    }
    return true;
  }

  private void parseOptionsTail() {
    boolean key = true;
    while (!builder.eof()) {
      final IElementType type = builder.getTokenType();
      if (key) {
        if (JSTokenTypes.RBRACE == type) {
          expectDoubleRBrace(false);
          return;
        }
        else if (JSTokenTypes.LBRACE == type) {
          builder.error(message("angularjs.parser.message.expected.selection.keyword"));
          return;
        }
        else {
          final PsiBuilder.Marker mark = builder.mark();
          // = can be only in the beginning, like =0
          while (!JSTokenTypes.PARSER_WHITE_SPACE_TOKENS.contains(builder.rawLookup(0)) && builder.rawLookup(0) != null) {
            builder.advanceLexer();
          }
          mark.collapse(AngularJSElementTypes.MESSAGE_FORMAT_SELECTION_KEYWORD);
          key = false;
        }
      }
      else {
        if (JSTokenTypes.LBRACE == type) {
          builder.advanceLexer();
          if (!parseInnerMessage()) return;  //+-
          key = true;
        }
        else {
          builder.error(message("angularjs.parser.message.expected.message.in.brace.delimiters"));
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
    return ExtensionType.select.name().equals(text) || ExtensionType.plural.name().equals(text);
  }

  private static boolean rollback(PsiBuilder.Marker expr) {
    expr.rollbackTo();
    return false;
  }

  public enum ExtensionType {
    plural(AngularJSPluralCategories.other.name()), select("other");

    private final Set<String> myRequiredSelectionKeywords;

    ExtensionType(String... keywords) {
      if (keywords.length == 0) {
        myRequiredSelectionKeywords = null;
      }
      else {
        myRequiredSelectionKeywords = new HashSet<>();
        Collections.addAll(myRequiredSelectionKeywords, keywords);
      }
    }

    public @NotNull Set<String> getRequiredSelectionKeywords() {
      return myRequiredSelectionKeywords == null ? Collections.emptySet() : myRequiredSelectionKeywords;
    }
  }
}
