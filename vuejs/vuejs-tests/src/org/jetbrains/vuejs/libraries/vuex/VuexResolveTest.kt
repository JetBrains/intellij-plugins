// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex

import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.openapi.application.PathManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.ComparisonFailure
import junit.framework.TestCase
import org.jetbrains.vuejs.lang.assertUnresolvedReference
import org.jetbrains.vuejs.lang.createPackageJsonWithVueDependency
import org.jetbrains.vuejs.lang.resolveReference

class VuexResolveTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = PathManager.getHomePath() + "/contrib/vuejs/vuejs-tests/testData/vuex/resolve"

  fun testStorefrontComponentStoreAccess() {
    doStorefrontTest(
      // Direct indexed getter
      "this.\$store.getters['cart/get<caret>Coupon']" to "cart/getters.ts:2139:JSProperty",
      "this.\$store.getters['ca<caret>rt/getCoupon']" to "store/index.ts:1265:JSLiteralExpression",
      "rootStore.getters['cart/get<caret>Coupon']" to "cart/getters.ts:2139:JSProperty",
      "rootStore.getters['ca<caret>rt/getCoupon']" to "store/index.ts:1265:JSLiteralExpression",

      // Direct referenced getter
      "this.\$store.getters.getCurrent<caret>StoreView" to "store/getters.ts:148:JSProperty",

      // Direct state
      "this.\$store.state.category.categories<caret>Map" to "category/index.ts:335:JSProperty",
      "this.\$store.state.cate<caret>gory.categoriesMap" to "store/index.ts:1180:JSProperty",

      // Direct dispatch
      "this.\$store.dispatch('cart/apply<caret>Coupon'" to "actions/couponActions.ts:354:TypeScriptFunctionProperty",
      "this.\$store.dispatch('ca<caret>rt/applyCoupon'" to "store/index.ts:1265:JSLiteralExpression",
      "rootStore.dispatch('cart/apply<caret>Coupon'" to "actions/couponActions.ts:354:TypeScriptFunctionProperty",
      "rootStore.dispatch('ca<caret>rt/applyCoupon'" to "store/index.ts:1265:JSLiteralExpression",

      // Direct commit
      "this.\$store.commit('cart/breadcrumbs/se<caret>t'" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty",
      "this.\$store.commit('cart/bread<caret>crumbs/set'" to "cart/index.ts:838:JSProperty",
      "rootStore.commit('cart/breadcrumbs/se<caret>t'" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty",
      "rootStore.commit('cart/bread<caret>crumbs/set'" to "cart/index.ts:838:JSProperty")
  }

  fun testStorefrontComponentMappedGetters() {
    doStorefrontTest(
      // Plain dictionary
      "isVirtualCart: 'cart/is<caret>VirtualCart'" to "cart/getters.ts:2359:JSProperty",
      "foo: 'cart/fo<caret>o'" to null,
      "foo: 'ca<caret>rt/foo'" to "store/index.ts:1265:JSLiteralExpression",

      // Namespaced dictionary
      "getCategories: 'get<caret>Categories'" to "category/getters.ts:1226:JSProperty",
      "routes: 'cart/breadcrumbs/get<caret>BreadcrumbsRoutes'" to null,
      "foo: 'fo<caret>o'" to null,

      // Plain array
      "(['cart/isCart<caret>Connected'" to "cart/getters.ts:1419:JSProperty",
      "'category/b<caret>ar'])" to null,
      "'cat<caret>egory/bar'])" to "store/index.ts:1180:JSProperty",

      // Namespaced array
      "['get<caret>CartItems'," to "cart/getters.ts:1278:JSProperty",
      "'foo<caret>bar'," to null,
      "mapGetters('c<caret>art'" to "store/index.ts:1265:JSLiteralExpression",
      "'bread<caret>crumbs/getBreadcrumbsRoutes'])" to "cart/index.ts:838:JSProperty",
      "'breadcrumbs/getBreadcrumbs<caret>Routes'])" to "breadcrumbs/index.ts:333:JSProperty")
  }

  fun testStorefrontComponentMappedState() {
    doStorefrontTest(
      // Plain string dictionary
      "micro: 'cart/isMicrocart<caret>Open'" to "cart/index.ts:305:JSProperty",
      "foo2: 'f<caret>oo'" to null,
      "ship: 'ship<caret>ping'" to "store/index.ts:529:JSProperty",

      // Namespaced string dictionary
      "micro2: 'isMicrocart<caret>Open'" to "cart/index.ts:305:JSProperty",
      "foo3: 'fo<caret>o'" to null,
      "ship2: 'ship<caret>ping'" to "cart/index.ts:544:JSProperty",

      // Plain array
      "(['cart/isMicrocart<caret>Open'" to "cart/index.ts:305:JSProperty",
      "['cart/isMicrocartOpen', 'ship<caret>ping']" to "store/index.ts:529:JSProperty",

      // Namespaced array
      "('cart', ['isMicrocart<caret>Open'" to "cart/index.ts:305:JSProperty",
      "['isMicrocartOpen', 'ship<caret>ping']" to "cart/index.ts:544:JSProperty",

      // Plain function dictionary
      "routes: state => state.ca<caret>rt.breadcrumbs.routes" to "store/index.ts:1265:JSLiteralExpression",
      "routes: state => state.cart.bread<caret>crumbs.routes" to "cart/index.ts:838:JSProperty",
      "routes: state => state.cart.breadcrumbs.rou<caret>tes" to "breadcrumbs/index.ts:69:JSProperty",
      "return state.ca<caret>rt.breadcrumbs.routes" to "store/index.ts:1265:JSLiteralExpression",
      "return state.cart.bread<caret>crumbs.routes" to "cart/index.ts:838:JSProperty",
      "return state.cart.breadcrumbs.rou<caret>tes" to "breadcrumbs/index.ts:69:JSProperty",
      "foo: state => state.ca<caret>rt.breadcrumbs.foo" to "store/index.ts:1265:JSLiteralExpression",
      "foo: state => state.cart.breadcrumbs.fo<caret>o" to null,
      "ship: state => state.ship<caret>ping" to "store/index.ts:529:JSProperty",

      // Namespaced function dictionary
      "routes2: state => state.bread<caret>crumbs.routes" to "cart/index.ts:838:JSProperty",
      "routes2: state => state.breadcrumbs.rou<caret>tes" to "breadcrumbs/index.ts:69:JSProperty",
      "foo2: state => state.bread<caret>crumbs.foo" to "cart/index.ts:838:JSProperty",
      "foo2: state => state.breadcrumbs.fo<caret>o" to null,
      "ship2: state => state.ship<caret>ping" to "cart/index.ts:544:JSProperty",
      "micro: state => state.isMicrocart<caret>Open" to "cart/index.ts:305:JSProperty",
      "shippingMethod: (state, getters) => getters.getShipping<caret>Method" to "cart/getters.ts:639:JSProperty",
      "return getters.getShipping<caret>Method" to "cart/getters.ts:639:JSProperty")
  }

  fun testStorefrontComponentMappedActions() {
    doStorefrontTest(
      // Plain array
      "...mapActions(['cart/configure<caret>Item'])" to "actions/itemActions.ts:446:TypeScriptFunctionProperty",

      // Namespaced array
      "...mapActions('cart/breadcrumbs', ['s<caret>et'])" to "breadcrumbs/index.ts:248:TypeScriptFunctionProperty",

      // Plain function dictionary
      "(dispatch) => dispatch('cart/configure<caret>Item')" to "actions/itemActions.ts:446:TypeScriptFunctionProperty",

      // Namespaced function dictionary
      "(dispatch) => dispatch('configure<caret>Item')" to "actions/itemActions.ts:446:TypeScriptFunctionProperty")
  }

  fun testStorefrontComponentMappedMutations() {
    doStorefrontTest(
      // Plain array
      "'cart/breadcrumbs/s<caret>et'\n    ])" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty",

      // Namespaced array
      "'breadcrumbs/s<caret>et'])" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty",

      // Plain function dictionary
      "commit('cart/breadcrumbs/s<caret>et') //1" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty",
      "commit('cart/breadcrumbs/s<caret>et') //2" to null,

      // Namespaced function dictionary
      "commit => commit('s<caret>et')" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty")
  }

  fun testStorefrontComponentNamespacedHelpers() {
    doStorefrontTest(
      "storefront-namespaced-component.ts",
      // Getters array
      "['get<caret>CartItems'," to "cart/getters.ts:1278:JSProperty",
      "'foo<caret>bar'," to null,
      "createNamespacedHelpers('c<caret>art'" to "store/index.ts:1265:JSLiteralExpression",
      "'bread<caret>crumbs/getBreadcrumbsRoutes'])" to "cart/index.ts:838:JSProperty",
      "'breadcrumbs/getBreadcrumbs<caret>Routes'])" to "breadcrumbs/index.ts:333:JSProperty",
      "'getLast<caret>TotalsSyncDate'" to null,
      "'cart/getLast<caret>TotalsSyncDate'" to null,
      "'getCategory<caret>Products'" to "category/getters.ts:1455:JSProperty",
      "'getCategoryProducts', 'f<caret>oo'" to null,

      // Getters dictionary
      "isVirtualCart: 'is<caret>VirtualCart'" to "cart/getters.ts:2359:JSProperty",
      "routes: 'bread<caret>crumbs/getBreadcrumbsRoutes'" to "cart/index.ts:838:JSProperty",
      "routes: 'breadcrumbs/get<caret>BreadcrumbsRoutes'" to "breadcrumbs/index.ts:333:JSProperty",
      "foo: 'ca<caret>rt/foo'" to null,

      // State string dictionary
      "micro2: 'isMicrocart<caret>Open'" to "cart/index.ts:305:JSProperty",
      "foo3: 'fo<caret>o'" to null,
      "ship2: 'ship<caret>ping'" to "cart/index.ts:544:JSProperty",

      // State array
      "['isMicrocart<caret>Open'" to "cart/index.ts:305:JSProperty",
      "['isMicrocartOpen', 'ship<caret>ping']" to "cart/index.ts:544:JSProperty",

      // State function dictionary
      "routes2: state => state.bread<caret>crumbs.routes" to "cart/index.ts:838:JSProperty",
      "routes2: state => state.breadcrumbs.rou<caret>tes" to "breadcrumbs/index.ts:69:JSProperty",
      "foo2: state => state.bread<caret>crumbs.foo" to "cart/index.ts:838:JSProperty",
      "foo2: state => state.breadcrumbs.fo<caret>o" to null,
      "ship2: state => state.ship<caret>ping" to "cart/index.ts:544:JSProperty",
      "micro: state => state.isMicrocart<caret>Open" to "cart/index.ts:305:JSProperty",
      "shippingMethod: (state, getters) => getters.getShipping<caret>Method" to "cart/getters.ts:639:JSProperty",
      "return getters.getShipping<caret>Method" to "cart/getters.ts:639:JSProperty",

      // Actions array
      "...mapActions(['breadcrumbs/s<caret>et', 'configureItem'])" to "breadcrumbs/index.ts:248:TypeScriptFunctionProperty",
      "...mapActions(['breadcrumbs/set', 'configure<caret>Item'])" to "actions/itemActions.ts:446:TypeScriptFunctionProperty",

      // Actions function dictionary
      "(dispatch) => dispatch('configure<caret>Item')" to "actions/itemActions.ts:446:TypeScriptFunctionProperty",

      // Mutations array
      "'breadcrumbs/s<caret>et'])" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty",

      // Mutations function dictionary
      "commit('breadcrumbs/s<caret>et') //1" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty",
      "commit('breadcrumbs/s<caret>et') //2" to null)
  }

  fun testStorefrontComponentDecorators() {
    doStorefrontTest(
      "storefront-decorated-component.ts",
      "@Getter('cart/isVirtual<caret>Cart')" to "cart/getters.ts:2359:JSProperty",
      "@Getter('isVirtual<caret>Cart')" to null,

      "@cartModule.Getter('get<caret>CartItems')" to "cart/getters.ts:1278:JSProperty",
      "@cartModule.Getter('foo<caret>bar')" to null,
      "@cartModule.Getter('breadcrumbs/getBreadcrumbs<caret>Routes')" to "breadcrumbs/index.ts:333:JSProperty",
      "@cartModule.Getter('bread<caret>crumbs/getBreadcrumbsRoutes')" to "cart/index.ts:838:JSProperty",

      "@State('cart/isMicrocart<caret>Open')" to "cart/index.ts:305:JSProperty",
      "@State('ship<caret>ping') shipping1" to "store/index.ts:529:JSProperty",
      "@State('foo<caret>bar') foobar2" to null,
      "@State(state => state.cart.breadcrumbs.rou<caret>tes)" to "breadcrumbs/index.ts:69:JSProperty",
      "@State(state => state.ca<caret>rt.breadcrumbs.routes)" to "store/index.ts:1265:JSLiteralExpression",

      "@cartModule.State('isMicrocart<caret>Open')" to "cart/index.ts:305:JSProperty",
      "@cartModule.State('ship<caret>ping')" to "cart/index.ts:544:JSProperty",
      "@cartModule.State(state => state.bread<caret>crumbs.routes)" to "cart/index.ts:838:JSProperty",
      "@cartModule.State(state => state.breadcrumbs.rou<caret>tes)" to "breadcrumbs/index.ts:69:JSProperty",

      "@Action('cart/configure<caret>Item')" to "actions/itemActions.ts:446:TypeScriptFunctionProperty",

      "@cartModule.Action('breadcrumbs/s<caret>et')" to "breadcrumbs/index.ts:248:TypeScriptFunctionProperty",

      "@Mutation('cart/breadcrumbs/s<caret>et')" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty",
      "@Mutation('cart/bread<caret>crumbs/set')" to "cart/index.ts:838:JSProperty",
      "@Mutation('ca<caret>rt/breadcrumbs/set')" to "store/index.ts:1265:JSLiteralExpression",

      "@cartModule.Mutation('breadcrumbs/s<caret>et')" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty",
      "@cartModule.Mutation('bread<caret>crumbs/set')" to "cart/index.ts:838:JSProperty")
  }

  fun testRootNamespacedAction() {
    createPackageJsonWithVueDependency(myFixture, "\"vuex\": \"^3.0.1\"")
    myFixture.configureByFiles("root-namespaced-module.ts")
    doTest(false,
           "'root<caret>Action'" to "src/root-namespaced-module.ts:164:JSProperty",
           "'inner<caret>RootAction'" to "src/root-namespaced-module.ts:439:JSProperty",
           "'namespaced<caret>Action'" to null,
           "'foo/namespaced<caret>Action'" to "src/root-namespaced-module.ts:124:TypeScriptFunctionProperty",
           "'foo/root<caret>Action'" to null,
           "'fo<caret>o/rootAction'" to "src/root-namespaced-module.ts:78:JSLiteralExpression",
           "'foo/inner<caret>RootAction'" to null,
           "'foo/inner/namespaced<caret>Action'" to "src/root-namespaced-module.ts:326:JSProperty",
           "'foo/in<caret>ner/namespacedAction'" to "src/root-namespaced-module.ts:266:JSProperty",
           "'fo<caret>o/inner/namespacedAction'" to "src/root-namespaced-module.ts:78:JSLiteralExpression",
           "'foo/inner/inner<caret>RootAction'" to null,
           "'foo/inner/root<caret>Action'" to null)
  }

  fun testRootNamespacedActionJS() {
    createPackageJsonWithVueDependency(myFixture, "\"vuex\": \"^3.0.1\"")
    myFixture.configureByFiles("root-namespaced-module.js")
    doTest(false,
           "'root<caret>Action'" to "src/root-namespaced-module.js:164:JSProperty",
           "'inner<caret>RootAction'" to "src/root-namespaced-module.js:439:JSProperty",
           "'namespaced<caret>Action'" to null,
           "'foo/namespaced<caret>Action'" to "src/root-namespaced-module.js:124:ES6FunctionProperty",
           "'foo/root<caret>Action'" to null,
           "'fo<caret>o/rootAction'" to "src/root-namespaced-module.js:78:JSLiteralExpression",
           "'foo/inner<caret>RootAction'" to null,
           "'foo/inner/namespaced<caret>Action'" to "src/root-namespaced-module.js:326:JSProperty",
           "'foo/in<caret>ner/namespacedAction'" to "src/root-namespaced-module.js:266:JSProperty",
           "'fo<caret>o/inner/namespacedAction'" to "src/root-namespaced-module.js:78:JSLiteralExpression",
           "'foo/inner/inner<caret>RootAction'" to null,
           "'foo/inner/root<caret>Action'" to null)
  }

  fun testStorefrontStoreActionsContext() {
    myFixture.configureStorefront()
    myFixture.configureFromTempProjectFile("store/category/actions.ts")
    doTest(false,
      // enclosing module getter
           "category || getters.getCategory<caret>From(route.path)" to "category/getters.ts:1553:JSProperty",
           "getters.get<caret>FiltersMap[searchCategory.id]" to "category/getters.ts:5084:JSProperty",
           "getters.getCurrent<caret>CartHash" to null,
           "context.getters.getFilters<caret>Map" to "category/getters.ts:5084:JSProperty",
           "context.getters.getCurrent<caret>CartHash" to null,
           "getters['getCurrentSearch<caret>Query']" to "category/getters.ts:5584:JSProperty",
           "getters['category/getCurrentSearch<caret>Query']" to null,

      // root getter
           "context.rootGetters['cart/isCart<caret>HashChanged']" to "cart/getters.ts:864:JSProperty",
           "context.rootGetters['isCart<caret>HashChanged']" to null,
           "context.rootGetters.isCart<caret>HashChanged" to null,
           "context.rootGetters.getCurrent<caret>StoreView" to "store/getters.ts:148:JSProperty",
           "rootGetters['getCurrentSearch<caret>Query']" to null,
           "rootGetters['category/getCurrentSearch<caret>Query']" to "category/getters.ts:5584:JSProperty",
           "rootGetters['getCurrent<caret>StoreView']" to "store/getters.ts:148:JSProperty",
           "rootGetters.isCart<caret>HashChanged" to null,
           "rootGetters.getCurrent<caret>StoreView" to "store/getters.ts:148:JSProperty",

      // enclosing module action
           "dispatch('load<caret>CategoryFilters'" to "category/actions.ts:9165:TypeScriptFunctionProperty",
           "dispatch('load<caret>CategoryFilter'" to null,
           "context.dispatch('change<caret>RouterFilterParameters', currentQuery" to "category/actions.ts:11702:TypeScriptFunctionProperty",
           "dispatch('change<caret>RouterFilterParameters', {foo:12}" to "category/actions.ts:11702:TypeScriptFunctionProperty",

      // action with root argument
           "await dispatch('cart/configure<caret>Item'" to "actions/itemActions.ts:446:TypeScriptFunctionProperty",
           "await dispatch('ca<caret>rt/configureItem'" to "store/index.ts:1265:JSLiteralExpression",
           "await context.dispatch('changeRouter<caret>FilterParameters', {}, {root: true})" to null,

      // state
           "rootState.sto<caret>ck.cache" to "store/index.ts:838:JSProperty",
           "context.state.searchProducts<caret>Stats" to "category/index.ts:425:JSProperty",
           "context.rootState.categories<caret>Map" to null,
           "context.rootState.category.categories<caret>Map" to "category/index.ts:335:JSProperty",

      // mutation commit
           "context.commit('cart/breadcrumbs/se<caret>t'" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty",
           "commit('cart/breadcrumbs/se<caret>t', {foo:12})" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty"

    )
  }

  fun testStorefrontStoreGettersContext() {
    myFixture.configureStorefront()
    myFixture.configureFromTempProjectFile("store/cart/getters.ts")
    doTest(false,
      // state
           "getShippingMethod: state => state.ship<caret>ping" to "cart/index.ts:544:JSProperty",
           "getShippingMethod2: (state, getters, rootState) => rootState.ship<caret>ping" to "store/index.ts:529:JSProperty",

      // getters
           "calculateTotals(getters.getFirst<caret>ShippingMethod" to "cart/getters.ts:1562:JSProperty",
           "rootGetters['cart/getCurrent<caret>CartHash']" to "cart/getters.ts:778:JSProperty"
    )
  }

  fun testStorefrontStoreMutationsContext() {
    myFixture.configureStorefront()
    myFixture.configureFromTempProjectFile("store/cart/breadcrumbs/index.ts")
    doTest(false,
           "state.rou<caret>tes = " to "breadcrumbs/index.ts:69:JSProperty"
    )
  }

  private fun doStorefrontTest(vararg args: Pair<String, String?>) {
    doStorefrontTest("storefront-component.ts", *args)
  }

  private fun doStorefrontTest(mainFile: String, vararg args: Pair<String, String?>) {
    myFixture.configureStorefront()
    myFixture.configureByFiles(mainFile)
    doTest(false, *args)
    // Check the same with JavaScript file
    myFixture.configureByText(mainFile.removeSuffix(".ts") + ".js", myFixture.file.text)
    doTest(true, *args)
  }

  private fun doTest(js: Boolean = false, vararg args: Pair<String, String?>) {
    for ((signature, output) in args) {
      try {
        if (output == null) {
          try {
            myFixture.assertUnresolvedReference(signature)
          }
          catch (e: AssertionError) {
            if (!js) {
              throw e
            }
          }
        }
        else {
          var result = myFixture.resolveReference(signature)
          if (result is JSImplicitElement) {
            result = result.context!!
          }
          TestCase.assertEquals(output, "${result.containingFile.parent!!.name}/${result.containingFile.name}:${result.textOffset}:$result")
        }
      }
      catch (e: ComparisonFailure) {
        throw ComparisonFailure(signature + ":" + e.message, e.expected, e.actual).initCause(e)
      }
      catch (e: AssertionError) {
        throw AssertionError(signature + ":" + e.message, e)
      }
    }
  }
}
