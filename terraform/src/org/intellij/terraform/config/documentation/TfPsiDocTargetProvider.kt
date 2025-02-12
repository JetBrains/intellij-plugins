// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.documentation

import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.PsiDocumentationTargetProvider
import com.intellij.psi.PsiElement

internal class TfPsiDocTargetProvider: BaseTfDocumentationProvider(), PsiDocumentationTargetProvider {

  override fun documentationTarget(element: PsiElement, originalElement: PsiElement?): DocumentationTarget? {
    return computeDocumentationTarget(element)
  }

}