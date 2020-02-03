// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.model.store

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.containers.Stack
import org.jetbrains.vuejs.libraries.vuex.codeInsight.refs.VuexSymbolAccessor

interface VuexStoreContext {

  val rootStores: List<VuexStore>
  val registeredModules: List<VuexModule>
  val element: PsiElement

  fun <T : VuexNamedSymbol> visitSymbols(symbolAccessor: (VuexContainer) -> Map<String, T>,
                                         consumer: (fullName: String, symbol: T) -> Unit) {
    visit { namespace, container ->
      for (entry in symbolAccessor(container)) {
        if ((entry.value as? VuexAction)?.isRoot == true) {
          consumer(entry.key, entry.value)
        }
        else {
          consumer(appendSegment(namespace, entry.key), entry.value)
        }
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
      // TODO properly resolve local context of the element
      if (PsiTreeUtil.isAncestor(container.initializer, element, false)) {
        consumer("", container)
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
