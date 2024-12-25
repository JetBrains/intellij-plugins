// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java;

import com.intellij.lang.Language;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public final class CucumberJavaInjector implements MultiHostInjector {
  public static final Language regexpLanguage = Language.findLanguageByID("RegExp");

  @Override
  public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement element) {
    if (regexpLanguage == null) {
      return;
    }
    if (element instanceof PsiLiteralExpression && element instanceof PsiLanguageInjectionHost && element.getTextLength() > 2) {
      final PsiElement firstChild = element.getFirstChild();
      if (firstChild != null && firstChild.getNode().getElementType() == JavaTokenType.STRING_LITERAL) {
        PsiAnnotation annotation = PsiTreeUtil.getParentOfType(element, PsiAnnotation.class);
        if (annotation != null &&
            (CucumberJavaUtil.isCucumberStepAnnotation(annotation) || CucumberJavaUtil.isCucumberHookAnnotation(annotation))) {
          final TextRange range = new TextRange(1, element.getTextLength() - 1);
          Module module = ModuleUtilCore.findModuleForPsiElement(element);
          if (module != null && !CucumberJavaVersionUtil.isCucumber3OrMore(module)) {
            registrar.startInjecting(regexpLanguage).addPlace(null, null, (PsiLanguageInjectionHost)element, range).doneInjecting();
          } 
        }
      }
    }
  }

  @Override
  public @NotNull List<Class<? extends PsiElement>> elementsToInjectIn() {
    return Collections.singletonList(PsiLiteralExpression.class);
  }
}
