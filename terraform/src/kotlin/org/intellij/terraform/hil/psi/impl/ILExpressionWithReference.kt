// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry


open class ILExpressionWithReference(node: ASTNode) : ILExpressionBase(node) {

  override fun getReferences(): Array<PsiReference> {
    return ReferenceProvidersRegistry.getReferencesFromProviders(this)
  }

  override fun getReference(): PsiReference? {
    return references.firstOrNull()
  }

}
