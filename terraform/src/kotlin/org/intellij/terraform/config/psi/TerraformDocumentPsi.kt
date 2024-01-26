// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.psi

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.SyntheticElement
import com.intellij.psi.impl.FakePsiElement

/**
 * Synthetic PSI element whick represents a Terraform documentation reference.
 * We need this to cover issue with HCL parser which does not handle terraform module type properly. In contrast to
 * [com.intellij.openapi.paths.WebReference.MyFakePsiElement] returns meaningful text instead of simple URL, so
 * it looks better in documentation popups.
 *
 * @property element The underlying PSI element associated with the Terraform document.
 * @property rangeInElement The text range within the PSI element occupied by the Terraform document.
 * @property text The text content of the Terraform document.
 * @property url The URL of the Terraform document.
 */
internal class TerraformDocumentPsi(val element: PsiElement,
                                    private val rangeInElement: TextRange,
                                    private val text: String,
                                    val url: String) : FakePsiElement(), SyntheticElement {
  override fun getParent(): PsiElement {
    return element
  }

  override fun navigate(requestFocus: Boolean) {
    BrowserUtil.browse(url)
  }

  override fun getPresentableText(): String {
    return text
  }

  override fun getName(): String {
    return text
  }

  override fun getTextRange(): TextRange {
    val rangeInElement: TextRange = rangeInElement
    val elementRange: TextRange = element.getTextRange()
    return rangeInElement.shiftRight(elementRange.startOffset)
  }
}
