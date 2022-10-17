// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.highlight;

import com.intellij.codeInsight.CustomExceptionHandler;
import com.intellij.plugins.drools.lang.psi.DroolsFunctionStatement;
import com.intellij.plugins.drools.lang.psi.DroolsJavaRhsStatement;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DroolsExceptionHandler extends CustomExceptionHandler {
  @Override
  public boolean isHandled(@Nullable PsiElement element, @NotNull PsiClassType exceptionType, PsiElement topElement) {
    return PsiTreeUtil.getParentOfType(element, DroolsJavaRhsStatement.class) != null  || PsiTreeUtil.getParentOfType(element, DroolsFunctionStatement.class) != null;
  }
}
