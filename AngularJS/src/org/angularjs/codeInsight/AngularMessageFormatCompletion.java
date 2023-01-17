// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.codeInsight;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.LookupElementWeigher;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.JSCommaExpression;
import com.intellij.lang.javascript.psi.JSExpressionStatement;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.angularjs.lang.parser.AngularJSElementTypes;
import org.angularjs.lang.parser.AngularJSMessageFormatParser;
import org.angularjs.lang.psi.AngularJSMessageFormatExpression;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.angularjs.AngularJSBundle.message;

public final class AngularMessageFormatCompletion {
  public static final Comparator<AngularJSPluralCategories> PLURAL_CATEGORIES_COMPARATOR =
    Comparator.comparingInt(AngularJSPluralCategories::getCompletionOrder);
  public static final InsertHandler<LookupElement> MESSAGE_FORMAT_KEYWORD_INSERT_HANDLER = new InsertHandler<>() {
    @Override
    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
      context.setAddCompletionChar(false);
      final String string = item.getLookupString();

      final int offset = context.getEditor().getCaretModel().getCurrentCaret().getOffset();
      final String text =
        context.getDocument().getText(TextRange.create(offset, Math.min(offset + 100, context.getDocument().getTextLength()))).trim();
      // we need this code to improve the situation with the case when selection keyword starts with =
      // default implementation does not take it as completion pattern part, since = is not a part of java identifier
      final int idOffset = context.getStartOffset();
      if (idOffset > 0 && (idOffset + 1) < context.getDocument().getTextLength() &&
          "==".equals(context.getDocument().getText(TextRange.create(idOffset - 1, idOffset + 1)))) {
        context.getDocument().deleteString(idOffset, idOffset + 1);
      }

      if (!text.startsWith("{")) {
        final int diff = context.getSelectionEndOffset() - idOffset;
        EditorModificationUtil.insertStringAtCaret(context.getEditor(), " {}", false, string.length() - diff + 2);
      }
      context.commitDocument();
    }
  };
  public static final LookupElementWeigher MESSAGE_FORMAT_KEYWORD_WEIGHER = new LookupElementWeigher("angular.message.format") {

    @Override
    public Comparable weigh(@NotNull LookupElement element) {
      if (element.getObject() instanceof AngularJSPluralCategories) {
        return ((AngularJSPluralCategories)element.getObject()).getCompletionOrder();
      }
      if (element.getObject() instanceof String && ((String)element.getObject()).startsWith("=")) {
        try {
          return 10 + Integer.parseInt(element.getObject().toString().substring(1));
        }
        catch (NumberFormatException e) {
          //
        }
      }
      return Integer.MAX_VALUE;
    }
  };

  static boolean messageFormatCompletion(CompletionParameters parameters, CompletionResultSet result) {
    final PsiElement originalPosition = parameters.getOriginalPosition();
    if (originalPosition == null) return false;

    final PsiElement parent = originalPosition.getParent();
    if (parent instanceof JSReferenceExpression && parent.getParent() instanceof JSCommaExpression) {
      final PsiElement[] children = parent.getParent().getChildren();
      if (children.length >= 2 && children[1] == parent) {
        messageFormatExtensions(result);
        return true;
      }
    }
    if (parent instanceof AngularJSMessageFormatExpression) {
      final AngularJSMessageFormatExpression amfe = (AngularJSMessageFormatExpression)parent;
      if (originalPosition == amfe.getExtensionTypeElement()) {
        messageFormatExtensions(result);
        return true;
      }
      if (originalPosition.getNode().getElementType() == AngularJSElementTypes.MESSAGE_FORMAT_SELECTION_KEYWORD) {
        messageFormatSelectionKeywords(((AngularJSMessageFormatExpression)parent).getExtensionType(), result);
        return true;
      }
      if (originalPosition.getNode().getElementType() == JSTokenTypes.WHITE_SPACE) {
        if (originalPosition.getNextSibling() != null &&
            originalPosition.getNextSibling().getNode().getElementType() == AngularJSElementTypes.MESSAGE_FORMAT_SELECTION_KEYWORD ||
            originalPosition.getPrevSibling() != null &&
            originalPosition.getPrevSibling().getNode().getElementType() == JSTokenTypes.RBRACE) {
          messageFormatSelectionKeywords(((AngularJSMessageFormatExpression)parent).getExtensionType(), result);
          return true;
        }
      }
    }
    final PsiElement sibling = originalPosition.getPrevSibling();
    if (sibling instanceof AngularJSMessageFormatExpression) {
      messageFormatSelectionKeywords(((AngularJSMessageFormatExpression)sibling).getExtensionType(), result);
    }
    else if (sibling instanceof JSExpressionStatement && sibling.getFirstChild() instanceof AngularJSMessageFormatExpression) {
      messageFormatSelectionKeywords(((AngularJSMessageFormatExpression)sibling.getFirstChild()).getExtensionType(), result);
    }

    return false;
  }

  private static void messageFormatSelectionKeywords(AngularJSMessageFormatParser.ExtensionType type, CompletionResultSet result) {
    final CompletionResultSet set =
      result.withRelevanceSorter(CompletionSorter.emptySorter().weigh(MESSAGE_FORMAT_KEYWORD_WEIGHER));
    if (AngularJSMessageFormatParser.ExtensionType.plural.equals(type)) {
      final List<AngularJSPluralCategories> values = new ArrayList<>(Arrays.asList(AngularJSPluralCategories.values()));
      values.sort(PLURAL_CATEGORIES_COMPARATOR);
      for (AngularJSPluralCategories category : values) {
        LookupElementBuilder element = LookupElementBuilder.create(category).withInsertHandler(MESSAGE_FORMAT_KEYWORD_INSERT_HANDLER);
        if (AngularJSPluralCategories.other.equals(category)) {
          element = element.withTypeText(message("angularjs.completion.type.default.selection.keyword"), true);
        }
        else {
          element = element.withTypeText(message("angularjs.completion.type.plural.category"), true);
        }
        set.addElement(element);
      }
      for (int i = 0; i < 4; i++) {
        final LookupElementBuilder element = LookupElementBuilder.create("=" + i).withInsertHandler(MESSAGE_FORMAT_KEYWORD_INSERT_HANDLER);
        set.addElement(element);
      }
    }
    else {
      set.addElement(LookupElementBuilder.create("other")
                       .withTypeText(message("angularjs.completion.type.default.selection.keyword"), true)
                       .withInsertHandler(MESSAGE_FORMAT_KEYWORD_INSERT_HANDLER));
    }
    set.stopHere();
  }

  private static void messageFormatExtensions(CompletionResultSet result) {
    for (AngularJSMessageFormatParser.ExtensionType type : AngularJSMessageFormatParser.ExtensionType.values()) {
      final LookupElementBuilder elementBuilder = LookupElementBuilder.create(type.name())
        .withTypeText(message("angularjs.completion.type.message.format.extension"), true);
      result.consume(elementBuilder);
    }
    result.stopHere();
  }
}
