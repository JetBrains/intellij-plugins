// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.support;

import com.intellij.plugins.drools.DroolsFileType;
import com.intellij.plugins.drools.lang.psi.DroolsRhs;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.UseScopeEnlarger;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public class DroolsRhsScopeEnlarger extends UseScopeEnlarger {

  @Override
  public SearchScope getAdditionalUseScope(@NotNull PsiElement element) {
    final PsiFile file = element.getContainingFile();
    if (file == null || file.getFileType() != DroolsFileType.DROOLS_FILE_TYPE) return null;

    final DroolsRhs droolsRhs = PsiTreeUtil.getParentOfType(element, DroolsRhs.class);
    if (droolsRhs != null) {
      return new LocalSearchScope(droolsRhs);
    }
    return null;
  }
}
