// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.ecmal4.JSImportStatement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageTarget;
import com.intellij.usages.rules.PsiElementUsage;
import org.jetbrains.annotations.NotNull;

public class ImportFilteringRule extends com.intellij.usages.rules.ImportFilteringRule {
  @Override
  public boolean isVisible(@NotNull Usage usage, @NotNull UsageTarget @NotNull [] targets) {
    if (usage instanceof PsiElementUsage) {
      final PsiElement psiElement = ((PsiElementUsage)usage).getElement();
      final PsiFile containingFile = psiElement.getContainingFile();

      if (containingFile != null && containingFile.getLanguage().is(JavaScriptSupportLoader.ECMA_SCRIPT_L4)) {
        return PsiTreeUtil.getParentOfType(psiElement, JSImportStatement.class, true) == null;
      }
    }
    return true;
  }
}
