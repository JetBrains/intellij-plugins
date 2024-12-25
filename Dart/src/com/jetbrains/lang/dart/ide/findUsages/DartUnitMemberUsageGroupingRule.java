// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.findUsages;

import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.PsiElement;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageGroup;
import com.intellij.usages.UsageTarget;
import com.intellij.usages.rules.PsiElementUsage;
import com.intellij.usages.rules.SingleParentUsageGroupingRule;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartUnitMemberUsageGroupingRule extends SingleParentUsageGroupingRule implements DumbAware {
  @Override
  protected @Nullable UsageGroup getParentGroupFor(@NotNull Usage usage, UsageTarget @NotNull [] targets) {
    PsiElement psiElement = usage instanceof PsiElementUsage ? ((PsiElementUsage)usage).getElement() : null;
    if (psiElement == null || psiElement.getLanguage() != DartLanguage.INSTANCE) return null;

    // todo Docs are not parsed perfectly and doc comment may be not a child of the corresponding function. Related to comment for DartDocUtil.getDocumentationText

    while (psiElement != null) {
      if (psiElement instanceof DartComponent componentElement && psiElement.getParent() instanceof DartFile) {
        return new DartComponentUsageGroup(componentElement);
      }
      psiElement = psiElement.getParent();
    }

    return null;
  }
}
