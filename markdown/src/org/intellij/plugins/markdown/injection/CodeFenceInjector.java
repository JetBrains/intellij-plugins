package org.intellij.plugins.markdown.injection;

import com.intellij.lang.Language;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.plugins.intelliLang.inject.InjectedLanguage;
import org.intellij.plugins.intelliLang.inject.TemporaryPlacesRegistry;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeFenceContentImpl;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeFenceImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CodeFenceInjector implements MultiHostInjector {
  @Override
  public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
    if (!(context instanceof MarkdownCodeFenceImpl)) {
      return;
    }
    if (PsiTreeUtil.findChildOfType(context, MarkdownCodeFenceContentImpl.class) == null) {
      return;
    }

    final Language language = findLangForInjection(((MarkdownCodeFenceImpl)context));
    if (language == null) {
      return;
    }

    registrar.startInjecting(language);
    boolean isFirst = true;
    for (MarkdownCodeFenceContentImpl content : PsiTreeUtil.findChildrenOfType(context, MarkdownCodeFenceContentImpl.class)) {
      registrar.addPlace(isFirst ? null : "\n",
                         null,
                         ((MarkdownCodeFenceImpl)context),
                         TextRange.create(content.getStartOffsetInParent(), content.getStartOffsetInParent() + content.getTextLength()));
      isFirst = false;
    }
    registrar.doneInjecting();
  }

  @NotNull
  @Override
  public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return Collections.singletonList(MarkdownCodeFenceImpl.class);
  }

  @Nullable
  private static Language findLangForInjection(@NotNull MarkdownCodeFenceImpl element) {
    final TemporaryPlacesRegistry registry = TemporaryPlacesRegistry.getInstance(element.getProject());
    final InjectedLanguage language = registry.getLanguageFor(element, element.getContainingFile());
    if (language != null) {
      return language.getLanguage();
    }

    final PsiElement fenceLangElement = element.findPsiChildByType(MarkdownTokenTypes.FENCE_LANG);
    if (fenceLangElement == null) {
      return null;
    }
    return guessLanguageByFenceLang(fenceLangElement.getText());
  }

  @Nullable
  private static Language guessLanguageByFenceLang(@NotNull String langName) {
    return LanguageGuesser.INSTANCE.guessLanguage(langName);
  }
}
