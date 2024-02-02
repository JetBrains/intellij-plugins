// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.psi

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.SyntheticElement
import com.intellij.psi.impl.FakePsiElement

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
