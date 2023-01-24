// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.codeInsight.refs

import com.intellij.lang.javascript.ecmascript6.TypeScriptReferenceExpressionResolver
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.ResolveResult
import org.jetbrains.astro.codeInsight.ASTRO_GLOBAL_INTERFACE
import org.jetbrains.astro.codeInsight.ASTRO_IMPLICIT_OBJECT
import org.jetbrains.astro.codeInsight.AstroImplicitElement
import org.jetbrains.astro.codeInsight.astroContentRoot
import org.jetbrains.astro.types.AstroGlobalType

class AstroReferenceExpressionResolver(expression: JSReferenceExpressionImpl, ignorePerformanceLimits: Boolean)
  : TypeScriptReferenceExpressionResolver(expression, ignorePerformanceLimits) {

  override fun resolve(expression: JSReferenceExpressionImpl, incompleteCode: Boolean): Array<ResolveResult> {
    if (myRef.qualifier == null && myReferencedName == ASTRO_IMPLICIT_OBJECT) {
      return PsiElementResolveResult.createResults(
        AstroImplicitElement(ASTRO_GLOBAL_INTERFACE, AstroGlobalType(expression.containingFile.astroContentRoot()!!),
                             expression, JSImplicitElement.Type.Interface))
    }
    return super.resolve(expression, incompleteCode)
  }

}