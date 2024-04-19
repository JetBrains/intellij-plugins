// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry

abstract class HCLValueWithReferencesMixin(node: ASTNode) : HCLElementImpl(node) {
  private val myRefLock: Any = Any()
  private var myModCount: Long = -1
  private var myRefs: Array<PsiReference> = emptyArray()


  override fun getReferences(): Array<out PsiReference> {
    val count = manager.modificationTracker.modificationCount
    if (count != myModCount) {
      synchronized(myRefLock) {
        myRefs = ReferenceProvidersRegistry.getReferencesFromProviders(this)
        myModCount = count
      }
    }
    return myRefs
  }

  override fun getReference(): PsiReference? {
    return references.firstOrNull()
  }
}