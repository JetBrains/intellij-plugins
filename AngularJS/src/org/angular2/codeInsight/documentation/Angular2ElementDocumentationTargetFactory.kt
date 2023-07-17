// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.documentation

import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.PsiDocumentationTargetProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.parents
import com.intellij.psi.util.prevLeafs
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.lang.expr.psi.Angular2PipeExpression

class Angular2ElementDocumentationTargetFactory : PsiDocumentationTargetProvider {
  override fun documentationTarget(element: PsiElement, originalElement: PsiElement?): DocumentationTarget? =
    if (
      originalElement != null
      && originalElement.parents(true)
        // In code completion we get the next token as a context
        .plus(originalElement.prevLeafs.firstOrNull { it !is PsiWhiteSpace }?.parents(true) ?: emptySequence())
        .any { it is Angular2PipeExpression }
    ) {
      Angular2EntitiesProvider.getPipe(element)?.let { Angular2ElementDocumentationTarget.create(it.getName(), originalElement, it) }
    }
    else null
}