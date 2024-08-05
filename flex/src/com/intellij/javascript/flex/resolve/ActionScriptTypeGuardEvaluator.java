// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.resolve;

import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.resolve.JSTypeGuardEvaluator;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ActionScriptTypeGuardEvaluator extends JSTypeGuardEvaluator {

  public static final ActionScriptTypeGuardEvaluator INSTANCE = new ActionScriptTypeGuardEvaluator();

  @Override
  public @Nullable JSType getTypeFromTypeGuard(@NotNull PsiElement namedElement,
                                               @Nullable PsiElement place,
                                               @Nullable JSType preprocessedType,
                                               @Nullable PsiElement resolvedElement) {
    return preprocessedType;
  }

}
