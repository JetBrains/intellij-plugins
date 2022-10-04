// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.resolve.JSTypeGuardEvaluator;
import com.intellij.lang.javascript.psi.types.JSUnionType;
import com.intellij.lang.javascript.psi.types.guard.TypeScriptTypeRelations;
import com.intellij.lang.typescript.resolve.TypeScriptTypeGuardEvaluator;
import com.intellij.lang.typescript.tsconfig.TypeScriptConfig;
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigUtil;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2TypeGuardEvaluator extends TypeScriptTypeGuardEvaluator {

  static final JSTypeGuardEvaluator INSTANCE = new Angular2TypeGuardEvaluator();

  @Override
  public @Nullable JSType getTypeFromTypeGuard(@NotNull PsiElement namedElement,
                                               @Nullable PsiElement place,
                                               @Nullable JSType type,
                                               @Nullable PsiElement resolvedElement) {
    TypeScriptConfig config = TypeScriptConfigUtil.getConfigForPsiFile(namedElement.getContainingFile());
    var angularCompilerOptions = Angular2TypeScriptConfigCustomizer.getAngularCompilerOptions(config);

    if (angularCompilerOptions != null && angularCompilerOptions.getStrictTemplates()) {
      return super.getTypeFromTypeGuard(namedElement, place, type, resolvedElement);
    }

    // Old, non-strict mode
    // Angular template syntax doesn't support type guards, so we need to remove strictness from union types
    JSType optimized = TypeScriptTypeRelations.expandAndOptimizeTypeRecursive(type);
    if (optimized instanceof JSUnionType) {
      return optimized.copyWithStrict(false);
    }
    // WEB-39538: Optimization changes type source making it impossible to evaluate generics in some cases
    return type;
  }
}
