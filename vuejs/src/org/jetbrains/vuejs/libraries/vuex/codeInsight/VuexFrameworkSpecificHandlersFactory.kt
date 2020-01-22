// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.codeInsight

import com.intellij.lang.javascript.frameworks.JSFrameworkSpecificHandlersFactory
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.types.JSCompositeTypeFactory
import com.intellij.lang.javascript.psi.types.JSStringLiteralTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.containers.Stack
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.COMMIT
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.DISPATCH
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.GETTERS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MAP_ACTIONS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MAP_GETTERS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MAP_MUTATIONS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MAP_STATE
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexContainer
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexModelManager
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexNamedSymbol
import org.jetbrains.vuejs.libraries.vuex.model.store.VuexStore

class VuexFrameworkSpecificHandlersFactory : JSFrameworkSpecificHandlersFactory {

  companion object {
    val VUEX_FUNCTIONS_WITH_STRINGS = setOf(MAP_ACTIONS, MAP_GETTERS, MAP_MUTATIONS, MAP_STATE,
                                            DISPATCH, COMMIT, GETTERS)
  }

  override fun findExpectedType(parent: JSExpression, expectedTypeKind: JSExpectedTypeKind): JSType? {
    if (parent is JSLiteralExpression && parent.isStringLiteral) {
      val functionName = PsiTreeUtil.getContextOfType(parent, JSCallExpression::class.java, true,
                                                      JSFunctionItem::class.java, JSClass::class.java)
        ?.let { (it.stubSafeMethodExpression as? JSReferenceExpression)?.referenceName }
      if (VUEX_FUNCTIONS_WITH_STRINGS.contains(functionName)
          && isVueContext(parent)) {
        val stores = VuexModelManager.getAllVuexStores(parent.project)
        if (stores.isEmpty()) return null
        val result = mutableListOf<JSStringLiteralTypeImpl>()
        val typeSource = JSTypeSource(parent.containingFile, parent, JSTypeSource.SourceLanguage.JS, false)
        when (functionName) {
          MAP_ACTIONS, DISPATCH -> collectNames(stores, typeSource, result, VuexContainer::actions)
          MAP_MUTATIONS, COMMIT -> collectNames(stores, typeSource, result, VuexContainer::mutations)
          MAP_GETTERS, GETTERS -> collectNames(stores, typeSource, result, VuexContainer::getters)
          MAP_STATE -> collectNames(stores, typeSource, result, VuexContainer::state)
        }
        if (result.isEmpty()) return null
        return JSCompositeTypeFactory.createUnionType(typeSource, result)
      }
    }
    return null
  }

  private fun collectNames(stores: List<VuexStore>,
                           typeSource: JSTypeSource,
                           result: MutableList<JSStringLiteralTypeImpl>,
                           accessor: (VuexContainer) -> Map<String, VuexNamedSymbol>) {
    val containers = Stack<Pair<String, VuexContainer>>()
    stores.asSequence().mapTo(containers) { "" to it }
    while (!containers.empty()) {
      val (namespace, container) = containers.pop()
      container.modules.values.asSequence().mapTo(containers) {
        (if (it.isNamespaced) appendSegment(namespace, it.name) else namespace) to it
      }
      accessor(container).keys.asSequence()
        .map { appendSegment(namespace, it) }
        .mapTo(result) { JSStringLiteralTypeImpl(it, false, typeSource) }
    }
  }

  private fun appendSegment(namespace: String, segment: String): String {
    return (if (namespace.isBlank()) "" else "$namespace/") + segment
  }

}
