// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluationSupport;
import com.intellij.lang.javascript.psi.types.JSCompositeTypeImpl;
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory;
import com.intellij.lang.javascript.psi.types.JSUnionOrIntersectionType;
import com.intellij.lang.javascript.psi.types.guard.TypeScriptTypeRelations;
import com.intellij.lang.typescript.resolve.TypeScriptTypeEvaluationSupport;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2TypeEvaluationSupport extends TypeScriptTypeEvaluationSupport {

  static final JSTypeEvaluationSupport INSTANCE = new Angular2TypeEvaluationSupport();

  @Nullable
  @Override
  public JSType getTypeFromTypeGuard(@NotNull PsiElement namedElement,
                                     @Nullable JSReferenceExpression expression,
                                     @Nullable JSType type,
                                     @Nullable PsiElement resolvedElement) {
    // Angular template syntax doesn't support type guards, so we need to remove strictness from union types
    type = TypeScriptTypeRelations.expandAndOptimizeTypeRecursive(type);
    if (type instanceof JSCompositeTypeImpl) {
      return new JSCompositeTypeImpl(JSTypeSourceFactory.copyTypeSource(type.getSource(), false),
                                     ((JSUnionOrIntersectionType)type).getTypes());
    }
    return type;
  }
}
