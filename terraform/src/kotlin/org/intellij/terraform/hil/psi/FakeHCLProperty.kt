/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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