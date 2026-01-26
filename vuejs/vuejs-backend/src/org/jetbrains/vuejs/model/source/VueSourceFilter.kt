// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.model.Pointer
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.createSmartPointer
import org.jetbrains.vuejs.model.VueEntitiesContainer
import org.jetbrains.vuejs.model.VueFilter
import org.jetbrains.vuejs.model.VueGlobalImpl
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.model.VueScopeElementSymbol

data class VueSourceFilter(
  override val name: String,
  private val originalSource: PsiElement,
  override val vueProximity: VueModelVisitor.Proximity? = null,
) : VueFilter {

  override val parents: List<VueEntitiesContainer>
    get() = VueGlobalImpl.getParents(this)

  override val source: PsiElement
    get() = (originalSource as? PsiReference)?.resolve()
            ?: originalSource

  override fun withVueProximity(proximity: VueModelVisitor.Proximity): VueScopeElementSymbol =
    VueSourceFilter(name, originalSource, proximity)

  override fun createPointer(): Pointer<VueSourceFilter> {
    val name = name
    val originalSourcePtr = originalSource.createSmartPointer()
    return Pointer {
      VueSourceFilter(name, originalSourcePtr.dereference() ?: return@Pointer null)
    }
  }

}
