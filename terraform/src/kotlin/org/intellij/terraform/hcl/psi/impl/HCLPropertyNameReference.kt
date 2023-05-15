// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.psi.impl

import com.intellij.openapi.util.TextRange
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.util.ArrayUtil
import com.intellij.util.IncorrectOperationException
import org.intellij.terraform.hcl.psi.HCLProperty

/**
 * @author Mikhail Golubev
 */
class HCLPropertyNameReference(private val myProperty: HCLProperty) : PsiReference {

  override fun getElement(): PsiElement = myProperty

  override fun getRangeInElement(): TextRange {
    val nameElement = myProperty.nameElement
    // Either value of string with quotes stripped or element's text as is
    return ElementManipulators.getValueTextRange(nameElement)
  }

  override fun resolve(): PsiElement = myProperty

  override fun getCanonicalText(): String = myProperty.name

  @Throws(IncorrectOperationException::class)
  override fun handleElementRename(newElementName: String): PsiElement {
    return myProperty.setName(newElementName)
  }

  @Throws(IncorrectOperationException::class)
  override fun bindToElement(element: PsiElement): PsiElement? {
    return null
  }

  override fun isReferenceTo(element: PsiElement): Boolean {
    if (element !is HCLProperty) {
      return false
    }
    // May reference to the property with the same name for compatibility with JavaScript JSON support
    val selfResolve = resolve()
    return element.name == canonicalText && selfResolve != element
  }

  override fun getVariants(): Array<Any> = ArrayUtil.EMPTY_OBJECT_ARRAY

  override fun isSoft(): Boolean = true
}
