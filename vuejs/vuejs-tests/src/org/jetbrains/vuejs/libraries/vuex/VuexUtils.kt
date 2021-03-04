// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex

import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies

enum class VuexTestStore(val dirName: String) {
  CounterHot("counter-hot"),
  NuxtJs("nuxtjs"),
  ShoppingCart("shopping-cart"),
  Storefront("vue-storefront"),
  StarImport("star-import"),
  SimpleStore("simple-store"),
  StateViaLambda("state-via-lambda"),
  FunctionInit("function-init"),
}

fun CodeInsightTestFixture.configureStore(store: VuexTestStore) {
  configureVueDependencies(VueTestModule.VUEX_3_1_0, VueTestModule.VUE_2_6_10)
  copyDirectoryToProject("../stores/${store.dirName}", "store")
}
