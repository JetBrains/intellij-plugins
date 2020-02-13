// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex

import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import org.jetbrains.vuejs.lang.createPackageJsonWithVueDependency


fun CodeInsightTestFixture.configureCounterHotStore() {
  configureStore("counter-hot")
}

fun CodeInsightTestFixture.configureShoppingCartStore() {
  configureStore("shopping-cart")
}

fun CodeInsightTestFixture.configureStorefront() {
  configureStore("vue-storefront")
}

private fun CodeInsightTestFixture.configureStore(name: String) {
  createPackageJsonWithVueDependency(this, "\"vuex\": \"^3.0.1\"")
  copyDirectoryToProject("../../libs/vuex/node_modules", "node_modules")
  copyDirectoryToProject("../../types/vue-2.6.10", "node_modules/vue")
  copyDirectoryToProject("../stores/${name}", "store")
}
