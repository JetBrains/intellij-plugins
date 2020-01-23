// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex

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

  val VUEX_MAPPERS = setOf(MAP_STATE, MAP_GETTERS, MAP_MUTATIONS, MAP_ACTIONS)
}
