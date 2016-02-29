package org.intellij.plugins.markdown.injection;

import com.intellij.lang.Language;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
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
    final List<MarkdownCodeFenceContentImpl> list = PsiTreeUtil.getChildrenOfTypeAsList(context, MarkdownCodeFenceContentImpl.class);
    for (int i = 0; i < list.size(); i++) {
      final MarkdownCodeFenceContentImpl content = list.get(i);
      final boolean includeEol = (i + 1 < list.size());
      final TextRange rangeInHost = TextRange.from(content.getStartOffsetInParent(),
                                                   content.getTextLength() + (includeEol ? 1 : 0));
      registrar.addPlace(null, null, ((MarkdownCodeFenceImpl)context), rangeInHost);
    }
    registrar.doneInjecting();
  }

  @NotNull
  @Override
  public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return Collections.singletonList(MarkdownCodeFenceImpl.class);
  }

  @Nullable
  protected Language findLangForInjection(@NotNull MarkdownCodeFenceImpl element) {
    final String fenceLanguage = element.getFenceLanguage();
    if (fenceLanguage == null) {
      return null;
    }
    return guessLanguageByFenceLang(fenceLanguage);
  }

  @Nullable
  private static Language guessLanguageByFenceLang(@NotNull String langName) {
    return LanguageGuesser.INSTANCE.guessLanguage(langName);
  }
}
