// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex

import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies

enum class VuexTestStore(val dirName: String, val isComposition: Boolean = false) {
  CounterHot("counter-hot"),
  NuxtJs("nuxtjs"),
  ShoppingCart("shopping-cart"),
  Storefront("vue-storefront"),
  StarImport("star-import"),
  SimpleStore("simple-store"),
  StateViaLambda("state-via-lambda"),
  FunctionInit("function-init"),
  Comics("comics"),
  CompositionCounter("composition-counter", true),
  CompositionShoppingCart("composition-shopping-cart", true),
}

fun CodeInsightTestFixture.configureStore(store: VuexTestStore) {
  if (store.isComposition)
    configureVueDependencies(VueTestModule.VUEX_4_0_0, VueTestModule.VUE_3_0_0)
  else
    configureVueDependencies(VueTestModule.VUEX_3_1_0, VueTestModule.VUE_2_6_10)
  copyDirectoryToProject("../stores/${store.dirName}", "store")
}
