// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.psi

import com.intellij.openapi.paths.WebReference
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference

/**
 * Represents a reference to a Terraform documentation URL. It is a delegating wrapper over [com.intellij.openapi.paths.WebReference]
 * but it contains specific logic related to PSI text range. Also, it returns specific PsiReference different from what we have in WebReference.
 * Hence, it is not worth extending [com.intellij.psi.impl.source.resolve.reference.impl.PsiDelegateReference] for this specific case.
 *
 * @property element The underlying element of the reference.
 * @property webReference The web reference associated with the Terraform documentation URL.
 * @property displayText The display text for the reference.
 */
internal class TerraformDocReference(element: PsiElement, url: String?) : PsiReference {

  private val webReference: WebReference
  private val displayText: String

  init {
    val range = if (element.textLength < 2) {
      TextRange(1, 1)
    }
    else {
      TextRange(1, element.textLength - 1)
    }
    webReference = WebReference(element, range, url)
    displayText = range.substring(element.text)
  }

  override fun getElement(): PsiElement = webReference.element

  override fun getRangeInElement(): TextRange = webReference.rangeInElement

  override fun resolve(): PsiElement = TerraformDocumentPsi(webReference.element, webReference.rangeInElement, displayText,
                                                            webReference.url)

  override fun getCanonicalText(): String = webReference.canonicalText

  override fun handleElementRename(newElementName: String): PsiElement = webReference.handleElementRename(newElementName)

  override fun bindToElement(element: PsiElement): PsiElement = webReference.bindToElement(element)

  override fun isReferenceTo(element: PsiElement): Boolean = webReference.isReferenceTo(element)

  override fun isSoft(): Boolean = true

  override fun getAbsoluteRange(): TextRange = webReference.absoluteRange

  override fun getVariants(): Array<Any> = webReference.variants
}
