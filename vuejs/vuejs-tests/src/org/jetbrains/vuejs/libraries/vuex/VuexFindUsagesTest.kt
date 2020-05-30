// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex

import com.intellij.openapi.application.PathManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.vuejs.lang.checkUsages

class VuexFindUsagesTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = PathManager.getHomePath() + "/contrib/vuejs/vuejs-tests/testData/libraries/vuex/findUsages"

  fun testStorefront() {
    with(myFixture) {
      configureStore(VuexTestStore.Storefront)
      copyFileToProject("../resolve/storefront-component.ts", "storefront-component.ts")
      copyFileToProject("../resolve/storefront-decorated-component.ts", "storefront-decorated-component.ts")
      copyFileToProject("../resolve/storefront-namespaced-component.ts", "storefront-namespaced-component.ts")

      //// Breadcrumbs file
      checkUsages("store/cart/breadcrumbs/index.ts", "bc",
                  "getBreadcrumbs<caret>Routes", // Getters
                  "se<caret>t (stat", // Mutations
                  "se<caret>t ({ co", // Actions
                  "rou<caret>tes" // State
      )

      // TODO improve JS find usages and fix ambiguity of dynamic usages
      // State
      //checkUsages("store/index.ts", "state0",
      //            // TODO `<mutations.ts:(2812,2826):(6,14)>	state.shipping` should not show - WEB-42731
      //            // TODO `<index.ts:(544,556):(0,8)>	shipping: []` should not show
      //            "ship<caret>ping"
      //            //"cate<caret>gory: {",
      //            //"ca<caret>rt"
      //)
      //
      //checkUsages("store/cart/index.ts", "state1",
      //            "isMicro<caret>cartOpen",
      //            "ship<caret>ping",
      //            "bread<caret>crumbs")
      //
      //checkUsages("store/category/index.ts", "state2",
      //            "categories<caret>Map")
      //
      //checkUsages("store/category/CategoryState.ts", "state3",
      //            "categories<caret>Map")

      // Getters
      checkUsages("store/cart/getters.ts", "getters",
                  "is<caret>VirtualCart:",
                  "get<caret>CartItems:",
                  "get<caret>Coupon:",
                  "is<caret>CartConnected:",
                  "get<caret>LastTotalsSyncDate:",
                  "get<caret>ShippingMethod:")

      checkUsages("store/category/getters.ts", "getters2",
                  "get<caret>Categories:",
                  "get<caret>CategoryProducts:")

      // Mutations

      // Actions
      checkUsages("store/cart/actions/itemActions.ts", "actions",
                  "configure<caret>Item (")

      checkUsages("store/cart/actions/couponActions.ts", "actions2",
                  "async apply<caret>Coupon (")

    }
  }

  fun testShoppingCart() {
    with(myFixture) {
      configureStore(VuexTestStore.ShoppingCart)
      checkUsages("store/modules/cart.js", "cart",
                  "cart<caret>Products:",
                  "cart<caret>TotalPrice:",
                  "push<caret>ProductToCart (",
                  "increment<caret>ItemQuantity (",
                  "set<caret>CartItems (",
                  "set<caret>CheckoutStatus (")
      checkUsages("store/modules/products.js", "products",
                  "set<caret>Products (",
                  "decrement<caret>ProductInventory (")
    }
  }

  fun testSimpleStore() {
    with(myFixture) {
      myFixture.configureStore(VuexTestStore.SimpleStore)

      checkUsages("store/simpleStore.js", "simpleStore",
                  "action<caret>1:",
                  "state<caret>1:",
                  "ba<caret>r:",
                  "fo<caret>o:")
    }
  }

  private fun checkUsages(filePath: String, goldFileSuffix: String, vararg signatures: String) {
    val testName = getTestName(true)
    for ((i, signature) in signatures.withIndex()) {
      myFixture.configureFromTempProjectFile(filePath)
      myFixture.checkUsages(signature, "$testName.$goldFileSuffix${if (signatures.size > 1) ".$i" else ""}")
    }
  }

}
