// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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
