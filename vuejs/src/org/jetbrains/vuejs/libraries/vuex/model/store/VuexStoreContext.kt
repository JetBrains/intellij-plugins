// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.model.store

import com.intellij.psi.PsiElement
import com.intellij.util.containers.Stack
import org.jetbrains.vuejs.libraries.vuex.codeInsight.refs.VuexSymbolAccessor

interface VuexStoreContext {

  val rootStores: List<VuexStore>
  val registeredModules: List<VuexModule>
  val element: PsiElement

  fun <T : VuexNamedSymbol> visitSymbols(symbolAccessor: (VuexContainer) -> Map<String, T>,
                                         consumer: (qualifiedName: String, symbol: T) -> Unit) {
    val visited = mutableSetOf<String>()
    visit { qualifiedName, container ->
      for (entry in symbolAccessor(container)) {
        val symbolName = if ((entry.value as? VuexAction)?.isRoot == true) entry.key else appendSegment(qualifiedName, entry.key)
        if (visited.add(symbolName)) {
          consumer(symbolName, entry.value)
        }
      }
      if (symbolAccessor == VuexContainer::state
          && qualifiedName.isNotEmpty()
          && container is VuexNamedSymbol
          && visited.add(qualifiedName)) {
        @Suppress("UNCHECKED_CAST")
        consumer(qualifiedName, VuexStatePropertyImpl(container.name, container.source) as T)
      }
    }
  }

  fun visit(symbolAccessor: VuexSymbolAccessor?,
            consumer: (qualifiedName: String, symbol: Any) -> Unit) {
    if (symbolAccessor == null) {
      visit(consumer)
    }
    else {
      visitSymbols(symbolAccessor, consumer)
    }
  }

  fun visit(consumer: (qualifiedName: String, container: VuexContainer) -> Unit) {
    val containers = Stack<Triple<String, VuexContainer, Set<PsiElement>>>()
    rootStores.mapTo(containers) { Triple("", it, setOf(it.source)) }
    registeredModules.mapTo(containers) { Triple((if (it.isNamespaced) it.name else ""), it, setOf(it.source)) }

    while (!containers.empty()) {
      val (qualifiedName, container, sources) = containers.pop()
      container.modules.values.asSequence()
        .filter { !sources.contains(it.source) }
        .mapTo(containers) {
          Triple((if (it.isNamespaced) appendSegment(qualifiedName, it.name) else qualifiedName), it, sources + it.source)
        }
      consumer(qualifiedName, container)
    }
  }

  companion object {
    fun appendSegment(qualifiedName: String, segment: String): String {
      return (if (qualifiedName.isBlank()) "" else "$qualifiedName/") + segment
    }
  }

}
