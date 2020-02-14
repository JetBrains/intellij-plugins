// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.model.store

import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.psi.PsiElement
import com.intellij.util.containers.Stack
import org.jetbrains.vuejs.libraries.vuex.codeInsight.refs.VuexSymbolAccessor
import org.jetbrains.vuejs.libraries.vuex.types.VuexContainerStateType
import org.jetbrains.vuejs.model.VueImplicitElement

interface VuexStoreContext {

  val rootStores: List<VuexStore>
  val registeredModules: List<VuexModule>
  val element: PsiElement

  fun <T : VuexNamedSymbol> visitSymbols(symbolAccessor: (VuexContainer) -> Map<String, T>,
                                         consumer: (fullName: String, symbol: T) -> Unit) {
    val visited = mutableSetOf<String>()
    visit { namespace, container ->
      for (entry in symbolAccessor(container)) {
        val name = if ((entry.value as? VuexAction)?.isRoot == true) entry.key else appendSegment(namespace, entry.key)
        if (visited.add(name)) {
          consumer(name, entry.value)
        }
      }
      if (symbolAccessor == VuexContainer::state
          && namespace.isNotEmpty()
          && container is VuexNamedSymbol
          && visited.add(namespace)) {

        val moduleDefinition = container.source
        val resolveTarget = VueImplicitElement(namespace, VuexContainerStateType(moduleDefinition, VuexStaticNamespace(namespace)),
                                               moduleDefinition, JSImplicitElement.Type.Property)
        @Suppress("UNCHECKED_CAST")
        consumer(namespace, VuexStatePropertyImpl(container.name, moduleDefinition, resolveTarget) as T)
      }
    }
  }

  fun visit(symbolAccessor: VuexSymbolAccessor?,
            consumer: (namespace: String, symbol: Any) -> Unit) {
    if (symbolAccessor == null) {
      visit(consumer)
    }
    else {
      visitSymbols(symbolAccessor, consumer)
    }
  }

  fun visit(consumer: (namespace: String, container: VuexContainer) -> Unit) {
    val containers = Stack<Pair<String, VuexContainer>>()
    rootStores.asSequence().mapTo(containers) { "" to it }
    registeredModules.asSequence().mapTo(containers) { (if (it.isNamespaced) it.name else "") to it }

    while (!containers.empty()) {
      val (namespace, container) = containers.pop()
      container.modules.values.asSequence().mapTo(containers) {
        (if (it.isNamespaced) appendSegment(namespace, it.name) else namespace) to it
      }
      consumer(namespace, container)
    }
  }

  companion object {
    fun appendSegment(namespace: String, segment: String): String {
      return (if (namespace.isBlank()) "" else "$namespace/") + segment
    }
  }

}
