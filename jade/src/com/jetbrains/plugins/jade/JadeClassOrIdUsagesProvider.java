// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.css.CssSelectorSuffix;
import com.intellij.psi.css.usages.CssClassOrIdUsagesProvider;
import com.jetbrains.plugins.jade.js.JavaScriptInJadeLanguageDialect;
import com.jetbrains.plugins.jade.psi.impl.JadeClassImpl;
import org.jetbrains.annotations.NotNull;

final class JadeClassOrIdUsagesProvider implements CssClassOrIdUsagesProvider {
  @Override
  public boolean isUsage(@NotNull CssSelectorSuffix selectorSuffix, @NotNull PsiElement candidate, int offsetInCandidate) {
    // element from a base language PSI tree comes here
    if (!JavaScriptInJadeLanguageDialect.INSTANCE.is(candidate.getContainingFile().getLanguage())) {
      return false;
    }
    final PsiFile jadeFile = candidate.getContainingFile().getViewProvider().getPsi(JadeLanguage.INSTANCE);
    int offsetInFile = candidate.getTextOffset() + offsetInCandidate;
    final PsiElement element = jadeFile.findElementAt(offsetInFile);
    if (element != null && element.getParent() instanceof JadeClassImpl) {
      final PsiReference reference = element.getParent().getReference();
      return reference != null && reference.isReferenceTo(selectorSuffix);
    }
    return false;
  }
}
