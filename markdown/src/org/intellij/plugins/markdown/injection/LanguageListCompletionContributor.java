package org.intellij.plugins.markdown.injection;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionInitializationContext;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.ui.DeferredIconImpl;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class LanguageListCompletionContributor extends CompletionContributor {

  @Override
  public void beforeCompletion(@NotNull CompletionInitializationContext context) {
    if (context.getFile() instanceof MarkdownFile) {
      context.setDummyIdentifier(CompletionInitializationContext.DUMMY_IDENTIFIER + "\n");
    }
  }

  @Override
  public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
    return typeChar == '`' && position.getNode().getElementType() == MarkdownTokenTypes.CODE_FENCE_START;
  }

  @Override
  public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
    final PsiElement completionElement = parameters.getPosition();
    if (PsiUtilCore.getElementType(completionElement) == MarkdownTokenTypes.FENCE_LANG) {
      doFillVariants(parameters, result);
    }
  }

  private static void doFillVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
    for (Map.Entry<String, Language> entry : LanguageGuesser.INSTANCE.getLangToLanguageMap().entrySet()) {
      final Language language = entry.getValue();


      final LookupElementBuilder lookupElementBuilder =
        LookupElementBuilder.create(entry.getKey()).withIcon(new DeferredIconImpl<>(null, language, true, language1 -> {
          final LanguageFileType fileType = language1.getAssociatedFileType();
          return fileType != null ? fileType.getIcon() : null;
        }))
          .withTypeText(language.getDisplayName(), true)
          .withInsertHandler((context, item) -> {
            if (isInMiddleOfUncollapsedFence(parameters.getOriginalPosition(), context.getStartOffset())) {
              context.getDocument().insertString(context.getTailOffset(), "\n\n");
              context.getEditor().getCaretModel().moveCaretRelatively(1, 0, false, false, false);
            }
          });

      result.addElement(lookupElementBuilder);
    }
  }

  public static boolean isInMiddleOfUncollapsedFence(@Nullable PsiElement element, int offset) {
    if (element == null) {
      return false;
    }
    if (PsiUtilCore.getElementType(element) == MarkdownTokenTypes.CODE_FENCE_START) {
      final TextRange range = element.getTextRange();
      return range.getStartOffset() + range.getEndOffset() == offset * 2;
    }
    if (PsiUtilCore.getElementType(element) == MarkdownTokenTypes.TEXT
        && PsiUtilCore.getElementType(element.getParent()) == MarkdownElementTypes.CODE_SPAN) {
      final TextRange range = element.getTextRange();
      final TextRange parentRange = element.getParent().getTextRange();

      return range.getStartOffset() - parentRange.getStartOffset() == parentRange.getEndOffset() - range.getEndOffset();
    }

    return false;
  }
}
