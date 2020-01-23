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
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.COMMIT
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.DISPATCH
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.GETTERS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MAP_ACTIONS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MAP_GETTERS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MAP_MUTATIONS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.MAP_STATE
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.VUEX_MAPPERS
import org.jetbrains.vuejs.libraries.vuex.model.store.*

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
        val modules = VuexModelManager.getRegisteredModules(parent.project)
        if (stores.isEmpty() && modules.isEmpty()) return null
        val result = mutableSetOf<String>()

        if (!VUEX_MAPPERS.contains(functionName) || parent.parent is JSArrayLiteralExpression) {
          // TODO: Take into account mapper binding to namespace
          val namespace = if (VUEX_MAPPERS.contains(functionName))
            PsiTreeUtil.getParentOfType(parent, JSArgumentList::class.java)
              ?.arguments?.getOrNull(0)?.let { getTextIfLiteral(it) }
            ?: ""
          else ""

          val accessor = when (functionName) {
            MAP_ACTIONS, DISPATCH -> VuexContainer::actions
            MAP_MUTATIONS, COMMIT -> VuexContainer::mutations
            MAP_GETTERS, GETTERS -> VuexContainer::getters
            MAP_STATE -> VuexContainer::state
            else -> return null
          }

          collectNames(namespace, stores, modules, result, accessor)
        }
        else if (parent.parent is JSArgumentList) {
          collectNamespaces(stores, modules, result)
        }
        if (result.isEmpty()) return null
        val typeSource = JSTypeSource(parent.containingFile, parent, JSTypeSource.SourceLanguage.JS, false)
        return JSCompositeTypeFactory.createUnionType(typeSource, result.map {
          JSStringLiteralTypeImpl(it, false, typeSource)
        })
      }
    }
    return null
  }

  private fun collectNamespaces(stores: List<VuexStore>,
                                modules: List<VuexModule>,
                                result: MutableSet<String>) {
    visit(stores, modules) { namespace, _ ->
      if (namespace.isNotBlank()) {
        result.add(namespace)
      }
    }
  }

  private fun collectNames(sourceNamespace: String,
                           stores: List<VuexStore>,
                           modules: List<VuexModule>,
                           result: MutableSet<String>,
                           accessor: (VuexContainer) -> Map<String, VuexNamedSymbol>) {
    val namespacePrefix = if (sourceNamespace.isEmpty()) "" else "$sourceNamespace/"
    visit(stores, modules) { namespace, container ->
      accessor(container).keys.asSequence()
        .map { appendSegment(namespace, it) }
        .filter { it.startsWith(namespacePrefix) }
        .mapTo(result) { it.substring(namespacePrefix.length) }
    }
  }

  private fun visit(stores: List<VuexStore>,
                    modules: List<VuexModule>,
                    visitor: (String, VuexContainer) -> Unit) {
    val containers = Stack<Pair<String, VuexContainer>>()
    stores.asSequence().mapTo(containers) { "" to it }
    modules.asSequence().mapTo(containers) { (if (it.isNamespaced) it.name else "") to it }

    while (!containers.empty()) {
      val (namespace, container) = containers.pop()
      container.modules.values.asSequence().mapTo(containers) {
        (if (it.isNamespaced) appendSegment(namespace, it.name) else namespace) to it
      }
      visitor(namespace, container)
    }
  }

  private fun appendSegment(namespace: String, segment: String): String {
    return (if (namespace.isBlank()) "" else "$namespace/") + segment
  }

}
