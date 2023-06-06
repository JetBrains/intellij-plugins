// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.inspections

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.javascript.ecmascript6.TypeScriptAnalysisHandlersFactory
import com.intellij.lang.javascript.ecmascript6.TypeScriptAnnotatingVisitor
import com.intellij.psi.PsiElement

class AstroFrontmatterAnalysisHandlersFactory : TypeScriptAnalysisHandlersFactory() {
  override fun createAnnotatingVisitor(psiElement: PsiElement, holder: AnnotationHolder): TypeScriptAnnotatingVisitor =
    AstroFrontmatterAnnotatingVisitor(psiElement, holder)
}
