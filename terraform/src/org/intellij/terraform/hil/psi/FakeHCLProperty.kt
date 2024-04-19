// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.psi

import com.intellij.psi.PsiElement
import org.intellij.terraform.config.model.Type
import org.intellij.terraform.hcl.psi.HCLExpression
import org.intellij.terraform.hcl.psi.HCLProperty

open class FakeHCLProperty(private val _name: String, _parent: PsiElement, val dynamic: Boolean = false) : RenameableFakePsiElement(
  _parent), HCLProperty {
  override fun getName(): String {
    return _name
  }

  override fun getNameElement(): HCLExpression {
    throw UnsupportedOperationException("FakeProperty doesn't have an name element")
  }

  override fun getValue(): HCLExpression? {
    return null
  }

  override fun getNameIdentifier(): PsiElement? {
    return null
  }

  // It's impossible to navigate to fake non-existing elements
  override fun canNavigate(): Boolean {
    return false
  }

  override fun canNavigateToSource(): Boolean {
    return false
  }

  override fun navigate(requestFocus: Boolean) {
  }

  override fun isWritable(): Boolean {
    return false
  }
}

/**
 * Wrapper for the FakeHCLProperty for type declaration, it is a workaround until the resolve to type declaration directly will be implemented
 */
class FakeTypeProperty(name: String, parent: PsiElement, val type: Type?, dynamic: Boolean = false) :
  FakeHCLProperty(name, parent, dynamic) {
  override fun isEquivalentTo(another: PsiElement?): Boolean {
    if (another !is FakeTypeProperty) return super.isEquivalentTo(another)

    return name == another.name && type == another.type && another.manager.areElementsEquivalent(this.parent, another.parent)
  }
}