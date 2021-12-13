// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import org.jetbrains.vuejs.model.VueEntitiesContainer
import org.jetbrains.vuejs.model.VueFilter
import org.jetbrains.vuejs.model.VueGlobalImpl

class VueSourceFilter(override val defaultName: String,
                      private val originalSource: PsiElement) : VueFilter {

  override val parents: List<VueEntitiesContainer> get() = VueGlobalImpl.getParents(this)

  override val source: PsiElement get() {
    return (originalSource as? PsiReference)?.resolve() ?: originalSource
  }

}
