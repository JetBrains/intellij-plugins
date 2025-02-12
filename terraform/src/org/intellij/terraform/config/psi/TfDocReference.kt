// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

internal class TfDocReference(element: PsiElement) : PsiReferenceBase<PsiElement>(element) {

  private val displayText: String

  init {
    val range = if (element.textLength < 2) {
      TextRange(1, 1)
    }
    else {
      TextRange(1, element.textLength - 1)
    }
    displayText = range.substring(element.text)
  }

  override fun resolve(): PsiElement = TfDocumentPsi(element, displayText)
}
