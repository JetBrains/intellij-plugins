package com.jetbrains.lang.dart.ide.editor;

import com.intellij.codeInsight.editorActions.wordSelection.AbstractWordSelectioner;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.DartLanguage;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class DartWordSelectionHandler extends AbstractWordSelectioner {

  @Override
  public boolean canSelect(@NotNull final PsiElement e) {
    return e.getLanguage() == DartLanguage.INSTANCE;
  }

  @Override
  public List<TextRange> select(@NotNull final PsiElement psiElement,
                                @NotNull final CharSequence editorText,
                                final int cursorOffset,
                                @NotNull final Editor editor) {
    final List<TextRange> ranges = super.select(psiElement, editorText, cursorOffset, editor);

    final PsiElement semicolon = DartSelectionFilter.getSiblingSemicolonIfExpression(psiElement);
    if (semicolon != null) {
      includeSemicolonInRanges(ranges, psiElement.getTextRange(), semicolon.getTextRange().getEndOffset());
    }

    return ranges;
  }

  private static void includeSemicolonInRanges(@NotNull final List<TextRange> ranges,
                                               @NotNull final TextRange elementWithoutSemicolonRange,
                                               final int semicolonEndOffset) {
    for (int i = 0; i < ranges.size(); i++) {
      final TextRange range = ranges.get(i);
      if (range.getStartOffset() <= elementWithoutSemicolonRange.getStartOffset() &&
          range.getEndOffset() == elementWithoutSemicolonRange.getEndOffset()) {
        ranges.set(i, TextRange.create(range.getStartOffset(), semicolonEndOffset));
      }
    }
  }
}
