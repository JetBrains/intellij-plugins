package org.intellij.plugins.markdown.injection;

import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.plugins.intelliLang.inject.InjectedLanguage;
import org.intellij.plugins.intelliLang.inject.TemporaryPlacesRegistry;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeFenceContentImpl;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeFenceImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class CodeFenceInjector implements MultiHostInjector {
  @Override
  public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
    if (!(context instanceof MarkdownCodeFenceImpl)) {
      return;
    }

    final TemporaryPlacesRegistry registry = TemporaryPlacesRegistry.getInstance(context.getProject());
    final InjectedLanguage language = registry.getLanguageFor(((MarkdownCodeFenceImpl)context), context.getContainingFile());
    if (language == null || language.getLanguage() == null) {
      return;
    }

    registrar.startInjecting(language.getLanguage());
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
}
