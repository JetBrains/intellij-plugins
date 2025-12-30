// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight

import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.types.JSUnionType
import com.intellij.lang.javascript.psi.types.guard.TypeScriptTypeRelations
import com.intellij.lang.typescript.resolve.TypeScriptTypeGuardEvaluator
import com.intellij.psi.PsiElement
import org.angular2.codeInsight.config.Angular2Compiler.isStrictTemplates

object Angular2TypeGuardEvaluator : TypeScriptTypeGuardEvaluator() {

  public override fun getTypeFromTypeGuard(namedElement: PsiElement,
                                           place: PsiElement?,
                                           type: JSType?,
                                           resolvedElement: PsiElement?): JSType? {
    // Actually, expressions like {{isString(qux) && qux.length}} in Angular language tools are type-narrowed even outside strictTemplates,
    // so ideally we'd need 2 separate type narrowing mechanisms
    if (isStrictTemplates(place)) {
      return super.getTypeFromTypeGuard(namedElement, place, type, resolvedElement)
    }

    // Old, non-strict mode
    // Angular template syntax doesn't support type guards, so we need to remove strictness from union types
    val optimized = TypeScriptTypeRelations.expandAndOptimizeTypeRecursive(type, place)
    return if (optimized is JSUnionType)
      optimized.copyWithStrict(false)
    else type
    // WEB-39538: Optimization changes type source making it impossible to evaluate generics in some cases
  }
}
