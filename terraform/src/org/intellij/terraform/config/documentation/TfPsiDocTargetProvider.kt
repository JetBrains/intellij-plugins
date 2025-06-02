// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.documentation

import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.PsiDocumentationTargetProvider
import com.intellij.psi.PsiElement
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLStringLiteral
import org.intellij.terraform.hcl.psi.getNameElementUnquoted

internal class TfPsiDocTargetProvider : TfBaseDocumentationProvider(), PsiDocumentationTargetProvider {

  override fun documentationTarget(element: PsiElement, originalElement: PsiElement?): DocumentationTarget? {
    if (element is HCLBlock) {
      val name = element.getNameElementUnquoted(2)
      val blockName = originalElement?.parent as? HCLStringLiteral
      if (name != null && name == blockName?.unquotedText) {
        return null
      }
    }

    return computeDocumentationTarget(element)
  }

}