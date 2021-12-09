// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex.resolve;

import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.resolve.JSTypeGuardEvaluator;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ActionScriptTypeGuardEvaluator extends JSTypeGuardEvaluator {

  public static final ActionScriptTypeGuardEvaluator INSTANCE = new ActionScriptTypeGuardEvaluator();

  @Override
  @Nullable
  public JSType getTypeFromTypeGuard(@NotNull PsiElement namedElement,
                                     @Nullable PsiElement place,
                                     @Nullable JSType preprocessedType,
                                     @Nullable PsiElement resolvedElement) {
    return preprocessedType;
  }

}
