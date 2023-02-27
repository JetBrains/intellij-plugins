/*
 * Copyright 2000-2019 JetBrains s.r.o.
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
package org.intellij.terraform.hil.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.search.SearchScope
import com.intellij.util.IncorrectOperationException
import org.intellij.terraform.hcl.HCLElementTypes
import org.intellij.terraform.hcl.psi.impl.ElementChangeUtil
import org.intellij.terraform.config.model.getTerraformSearchScope
import org.intellij.terraform.hil.psi.ILProperty
import org.jetbrains.annotations.NonNls

abstract class ILPropertyMixin(node: ASTNode) : ILExpressionBase(node), ILProperty {

  abstract override fun getName(): String

  @Throws(IncorrectOperationException::class)
  override fun setName(@NonNls name: String): PsiElement {
    ElementChangeUtil.doNameReplacement(this, nameIdentifier, name, HCLElementTypes.IDENTIFIER)
    return this
  }

  override fun getNameIdentifier(): PsiElement {
    return nameElement
  }

  override fun getTextOffset(): Int {
    return nameIdentifier.textOffset
  }

  override fun getUseScope(): SearchScope {
    return this.getTerraformSearchScope()
  }

  override fun isEquivalentTo(another: PsiElement?): Boolean {
    return this === another || another === nameIdentifier
  }
}
