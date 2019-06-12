// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluationHelper;
import com.intellij.lang.javascript.psi.types.JSUnionType;
import com.intellij.lang.javascript.psi.types.guard.TypeScriptTypeRelations;
import com.intellij.lang.typescript.resolve.TypeScriptTypeEvaluationHelper;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2TypeEvaluationHelper extends TypeScriptTypeEvaluationHelper {

  static final JSTypeEvaluationHelper INSTANCE = new Angular2TypeEvaluationHelper();

  @Nullable
  @Override
  public JSType getTypeFromTypeGuard(@NotNull PsiElement namedElement,
                                     @Nullable JSReferenceExpression expression,
                                     @Nullable JSType type,
                                     @Nullable PsiElement resolvedElement) {
    // Angular template syntax doesn't support type guards, so we need to remove strictness from union types
    type = TypeScriptTypeRelations.expandAndOptimizeTypeRecursive(type);
    if (type instanceof JSUnionType) {
      return type.copyWithStrict(false);
    }
    return type;
  }
}
