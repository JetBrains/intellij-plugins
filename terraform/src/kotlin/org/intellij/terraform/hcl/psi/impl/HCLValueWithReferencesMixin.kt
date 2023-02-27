/*
 * Copyright 2000-2017 JetBrains s.r.o.
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