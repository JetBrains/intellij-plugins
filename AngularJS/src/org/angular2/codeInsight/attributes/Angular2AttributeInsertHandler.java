// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.completion.XmlAttributeInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.text.CharArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.BooleanSupplier;

public class Angular2AttributeInsertHandler implements InsertHandler<LookupElement> {

  private static final Set<Character> HTML_ATTR_NOT_ALLOWED_CHARS = ContainerUtil.newHashSet('=', '\'', '\"', '<', '>', '/', '\0');

  private final boolean myShouldRemoveLeftover;
  private final BooleanSupplier myShouldCompleteValue;
  private final String myAddSuffix;

  public Angular2AttributeInsertHandler(boolean shouldRemoveLeftover,
                                        @NotNull BooleanSupplier shouldCompleteValue,
                                        @Nullable String addSuffix) {
    myShouldRemoveLeftover = shouldRemoveLeftover;
    myShouldCompleteValue = shouldCompleteValue;
    myAddSuffix = addSuffix;
  }

  @Override
  public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
    final Editor editor = context.getEditor();
    final Document document = editor.getDocument();
    final int caretOffset = editor.getCaretModel().getOffset();

    if (myShouldRemoveLeftover) {
      int deleteOffset = caretOffset;
      boolean isHtml = context.getFile().getLanguage().isKindOf(HTMLLanguage.INSTANCE);
      final CharSequence text = document.getCharsSequence();
      while (deleteOffset < text.length()) {
        char ch = text.charAt(deleteOffset);
        if (Character.isWhitespace(ch)
            || HTML_ATTR_NOT_ALLOWED_CHARS.contains(ch)
            || (!isHtml && !Character.isLetterOrDigit(ch))) {
          break;
        }
        deleteOffset++;
      }
      if (deleteOffset > caretOffset) {
        document.deleteString(caretOffset, deleteOffset);
      }
    }
    if (myShouldCompleteValue.getAsBoolean()) {
      XmlAttributeInsertHandler.INSTANCE.handleInsert(context, item);
    }
    if (myAddSuffix != null) {
      final CharSequence text = document.getCharsSequence();
      if (!CharArrayUtil.regionMatches(text, caretOffset, myAddSuffix)) {
        document.insertString(caretOffset, myAddSuffix);
      }
    }
  }
}
