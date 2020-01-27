// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex

import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
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

  const val DISPATCH = "dispatch"
  const val COMMIT = "commit"
  const val GETTERS = "getters"
  const val STATE = "state"
  const val ROOT_GETTERS = "rootGetters"
  const val ROOT_STATE = "rootState"

  val VUEX_MAPPERS = setOf(MAP_STATE, MAP_GETTERS, MAP_MUTATIONS, MAP_ACTIONS)

  fun getNamespaceFromMapper(element: PsiElement): String {
    // TODO take into account local namespace mapping
    return PsiTreeUtil.getContextOfType(element, JSCallExpression::class.java)
             ?.let { getStubSafeCallArguments(it) }
             ?.getOrNull(0)
             ?.castSafelyTo<JSLiteralExpression>()
             ?.let { getTextIfLiteral(it) }
           ?: ""
  }
}
