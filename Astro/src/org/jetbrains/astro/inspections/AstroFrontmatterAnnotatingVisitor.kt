// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.inspections

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.javascript.ecmascript6.TypeScriptAnnotatingVisitor
import com.intellij.lang.javascript.psi.JSReturnStatement
import com.intellij.psi.PsiElement

class AstroFrontmatterAnnotatingVisitor(psiElement: PsiElement, holder: AnnotationHolder) : TypeScriptAnnotatingVisitor(psiElement, holder) {
  override fun visitJSReturnStatement(node: JSReturnStatement) {
    // Do nothing - Astro allows root level returns in the frontmatter.
  }
}
