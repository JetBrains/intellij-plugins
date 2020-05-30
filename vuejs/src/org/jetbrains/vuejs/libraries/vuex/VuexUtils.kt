// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex

import com.intellij.lang.javascript.psi.JSFunctionItem
import com.intellij.lang.javascript.psi.JSParameter
import com.intellij.psi.PsiElement
import com.intellij.psi.util.contextOfType

object VuexUtils {

  const val VUEX_PACKAGE = "vuex"
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
  const val CREATE_NAMESPACED_DECS = "namespace"

  const val PROP_NAMESPACED = "namespaced"
  const val PROP_ROOT = "root"
  const val PROP_TYPE = "type"

  const val DISPATCH = "dispatch"
  const val COMMIT = "commit"
  const val GETTERS = "getters"
  const val STATE = "state"
  const val ACTIONS = "actions"
  const val MUTATIONS = "mutations"
  const val MODULES = "modules"
  const val ROOT_GETTERS = "rootGetters"
  const val ROOT_STATE = "rootState"
  const val CONTEXT = "context"

  val VUEX_MAPPERS = setOf(MAP_STATE, MAP_GETTERS, MAP_MUTATIONS, MAP_ACTIONS)
  val VUEX_DEC_MAPPERS = setOf(GETTER_DEC, STATE_DEC, ACTION_DEC, MUTATION_DEC)

  fun isActionContextParameter(parameter: PsiElement?): Boolean {
    return (parameter as? JSParameter)?.name == CONTEXT
           && parameter.contextOfType(JSFunctionItem::class)?.parameters?.getOrNull(0) == parameter
  }

  fun isNamespaceChild(namespace: String, qualifiedName: String, onlyDirectChildren: Boolean): Boolean {
    return qualifiedName.startsWith(namespace)
           && qualifiedName.length > namespace.length
           && (!onlyDirectChildren || qualifiedName.indexOf('/', namespace.length) < 0)
  }

}
