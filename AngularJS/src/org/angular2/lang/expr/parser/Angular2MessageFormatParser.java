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
import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.parsing.ExpressionParser;
import com.intellij.psi.tree.IElementType;
import org.angular2.codeInsight.Angular2PluralCategories;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Irina.Chernushina on 11/30/2015.
 */
public class Angular2MessageFormatParser extends ExpressionParser<Angular2Parser> {
  @NonNls public static final String OFFSET_OPTION = "offset";
  private boolean myInsideSelectExpression = false;

  public Angular2MessageFormatParser(@NotNull Angular2Parser parser) {
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
      //if (!myJavaScriptParser.getExpressionParser().parseUnaryExpression()) {
      //  return rollback(expr);
      //}
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

    collapseTokenElement(Angular2ElementTypes.MESSAGE_FORMAT_EXPRESSION_NAME);
    if (builder.getTokenType() != JSTokenTypes.COMMA) {
      return rollback(expr);
    }
    builder.advanceLexer();
    if (ExtensionType.select.name().equals(extensionText)) {
      parseOptionsTail();
    } else {
      parsePluralTail();
    }
    expr.done(Angular2ElementTypes.MESSAGE_FORMAT_EXPRESSION);
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
            if (! expectDoubleRBrace(true)) {
              mark.drop();
              return false;
            }
          }
          else {
            builder.error("expected {{");
            mark.drop();
            return false;
          }
        }
        else if (JSTokenTypes.RBRACE == type) {
          mark.done(Angular2ElementTypes.MESSAGE_FORMAT_MESSAGE);
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

  private boolean expectDoubleRBrace(boolean advance) {
    if (!isRBraceOrNull(builder.getTokenType()) || !isRBraceOrNull(builder.lookAhead(1))) {
      builder.error("expected }}");
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
        builder.error("expected colon");
        return false;
      }
      final IElementType value = builder.lookAhead(2);
      if (!JSTokenTypes.LITERALS.contains(value) && JSTokenTypes.IDENTIFIER != value) {
        builder.advanceLexer();
        builder.advanceLexer();
        builder.error("expected offset option value");
        return false;
      }
      final PsiBuilder.Marker mark = builder.mark();
      builder.advanceLexer();// offset
      builder.advanceLexer();// colon
      builder.advanceLexer();// value
      mark.done(Angular2ElementTypes.MESSAGE_FORMAT_OPTION);
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
        } else if (JSTokenTypes.LBRACE == type) {
          builder.error("expected selection keyword");
          return;
        } else {
          final PsiBuilder.Marker mark = builder.mark();
          // = can be only in the beginning, like =0
          while (!JSTokenTypes.PARSER_WHITE_SPACE_TOKENS.contains(builder.rawLookup(0)) && builder.rawLookup(0) != null) {
            builder.advanceLexer();
          }
          mark.collapse(Angular2ElementTypes.MESSAGE_FORMAT_SELECTION_KEYWORD);
          key = false;
        }
      } else {
        if (JSTokenTypes.LBRACE == type) {
          builder.advanceLexer();
          if (!parseInnerMessage()) return;  //+-
          key = true;
        } else {
          builder.error("expected message in {} delimiters");
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
    plural(Angular2PluralCategories.other.name()), select("other");

    private final Set<String> myRequiredSelectionKeywords;

    ExtensionType(String... keywords) {
      if (keywords.length == 0) {
        myRequiredSelectionKeywords = null;
      } else {
        myRequiredSelectionKeywords = new HashSet<>();
        Collections.addAll(myRequiredSelectionKeywords, keywords);
      }
    }

    @NotNull
    public Set<String> getRequiredSelectionKeywords() {
      return myRequiredSelectionKeywords == null ? Collections.emptySet() : myRequiredSelectionKeywords;
    }
  }
}
