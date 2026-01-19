// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.createSmartPointer
import org.jetbrains.vuejs.model.VueEntitiesContainer
import org.jetbrains.vuejs.model.VueFilter
import org.jetbrains.vuejs.model.VueGlobalImpl
import org.jetbrains.vuejs.web.VUE_FILTERS
import org.jetbrains.vuejs.web.symbols.VueScopeElementSymbol

class VueSourceFilter(
  override val name: String,
  private val originalSource: PsiElement,
) : VueFilter {

  override val kind: PolySymbolKind
    get() = VUE_FILTERS

  override val parents: List<VueEntitiesContainer>
    get() = VueGlobalImpl.getParents(this)

  override val source: PsiElement
    get() = (originalSource as? PsiReference)?.resolve()
            ?: originalSource

  override fun equals(other: Any?): Boolean =
    other === this
    || other is VueSourceFilter
    && other.name == name
    && other.originalSource == originalSource

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + originalSource.hashCode()
    return result
  }

  override fun createPointer(): Pointer<out VueScopeElementSymbol> {
    val name = name
    val originalSourcePtr = originalSource.createSmartPointer()
    return Pointer {
      VueSourceFilter(name, originalSourcePtr.dereference() ?: return@Pointer null)
    }
  }

}
