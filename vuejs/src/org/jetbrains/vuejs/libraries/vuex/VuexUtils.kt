// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex

import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.util.JSDestructuringUtil
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.castSafelyTo
import org.jetbrains.vuejs.codeInsight.getStubSafeCallArguments
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral

object VuexUtils {

  const val VUEX_NAMESPACE = "Vuex"
  const val STORE = "Store"
  const val REGISTER_MODULE = "registerModule"

  const val MAP_STATE = "mapState"
  const val MAP_GETTERS = "mapGetters"
  const val MAP_MUTATIONS = "mapMutations"
  const val MAP_ACTIONS = "mapActions"
  const val CREATE_NAMESPACED_HELPERS = "createNamespacedHelpers"

  const val GETTER_DEC = "Getter"
  const val STATE_DEC = "State"
  const val ACTION_DEC = "Action"
  const val MUTATION_DEC = "Mutation"
  private const val CREATE_NAMESPACED_DECS = "namespace"

  const val DISPATCH = "dispatch"
  const val COMMIT = "commit"
  const val GETTERS = "getters"
  const val STATE = "state"
  const val ROOT_GETTERS = "rootGetters"
  const val ROOT_STATE = "rootState"

  val VUEX_MAPPERS = setOf(MAP_STATE, MAP_GETTERS, MAP_MUTATIONS, MAP_ACTIONS)

  fun getNamespaceFromMapper(element: PsiElement, decorator: Boolean): String {
    val call = PsiTreeUtil.getContextOfType(element, JSCallExpression::class.java)
    val functionRef = call?.methodExpression?.castSafelyTo<JSReferenceExpression>()
                      ?: return ""
    return (if (functionRef.qualifier !== null)
      functionRef.qualifier.castSafelyTo<JSReferenceExpression>()
        ?.resolve()
        ?.castSafelyTo<JSVariable>()
        ?.let { getNamespaceFromHelpersVar(it, decorator) }
    else {
      val functionName = functionRef.referenceName ?: return ""
      val location = JSStubBasedPsiTreeUtil.resolveLocally(functionName, functionRef)
      if (location is JSVariable)
        getNamespaceFromHelpersVar(location, decorator)
      else
        getStubSafeCallArguments(call)
          .getOrNull(0)
          ?.castSafelyTo<JSLiteralExpression>()
          ?.let { getTextIfLiteral(it) }
    }) ?: ""
  }

  private fun getNamespaceFromHelpersVar(variable: JSVariable, decorator: Boolean): String? {
    return (variable.initializer
            ?: JSDestructuringUtil.getNearestDestructuringInitializer(variable))
      ?.castSafelyTo<JSCallExpression>()
      ?.takeIf {
        it.methodExpression?.castSafelyTo<JSReferenceExpression>()
          ?.referenceName == if (decorator) CREATE_NAMESPACED_DECS else CREATE_NAMESPACED_HELPERS
      }
      ?.arguments
      ?.getOrNull(0)
      ?.let { getTextIfLiteral(it) }
  }
}
