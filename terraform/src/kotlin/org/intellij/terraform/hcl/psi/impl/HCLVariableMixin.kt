/*
 * Copyright 2000-2018 JetBrains s.r.o.
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
package org.intellij.terraform.hcl.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.util.IncorrectOperationException
import org.intellij.terraform.config.model.getTerraformSearchScope
import org.intellij.terraform.hcl.psi.HCLVariable

abstract class HCLVariableMixin(node: ASTNode) : HCLValueWithReferencesMixin(node), HCLVariable {

  override fun getName(): String {
    return this.text
  }

  @Throws(IncorrectOperationException::class)
  override fun setName(name: String): HCLVariable {
    val node = firstChild.node
    assert(node is LeafElement)
    (node as LeafElement).replaceWithText(name)
    return this
  }

  override fun getUseScope(): SearchScope {
    return this.getTerraformSearchScope()
  }

  override fun getResolveScope(): GlobalSearchScope {
    return this.getTerraformSearchScope()
  }
}
