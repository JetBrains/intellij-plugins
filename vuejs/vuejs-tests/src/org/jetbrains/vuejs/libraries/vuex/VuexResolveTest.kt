// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex

import com.intellij.webSymbols.assertUnresolvedReference
import com.intellij.webSymbols.resolveReference
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.ComparisonFailure
import junit.framework.TestCase
import org.jetbrains.vuejs.lang.createPackageJsonWithVueDependency
import org.jetbrains.vuejs.lang.getVueTestDataPath

class VuexResolveTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = getVueTestDataPath() + "/libraries/vuex/resolve"

  fun testStorefrontComponentStoreAccess() {
    doStorefrontTest(
      // Direct indexed getter
      "this.\$store.getters['cart/get<caret>Coupon']" to "cart/getters.ts:2139:JSProperty",
      "this.\$store.getters['ca<caret>rt/getCoupon']" to "store/index.ts:1240:JSLiteralExpression",
      "rootStore.getters['cart/get<caret>Coupon']" to "cart/getters.ts:2139:JSProperty",
      "rootStore.getters['ca<caret>rt/getCoupon']" to "store/index.ts:1240:JSLiteralExpression",

      // Direct referenced getter
      "this.\$store.getters.getCurrent<caret>StoreView" to "store/getters.ts:148:JSProperty",

      // Direct state
      "this.\$store.state.category.categories<caret>Map" to "category/index.ts:335:JSProperty",
      "this.\$store.state.cate<caret>gory.categoriesMap" to "store/index.ts:1155:JSProperty",

      // Direct dispatch
      "this.\$store.dispatch('cart/apply<caret>Coupon'" to "actions/couponActions.ts:354:TypeScriptFunctionProperty",
      "this.\$store.dispatch('ca<caret>rt/applyCoupon'" to "store/index.ts:1240:JSLiteralExpression",
      "rootStore.dispatch('cart/apply<caret>Coupon'" to "actions/couponActions.ts:354:TypeScriptFunctionProperty",
      "rootStore.dispatch('ca<caret>rt/applyCoupon'" to "store/index.ts:1240:JSLiteralExpression",
      "rootStore.dispatch({type: 'cart/apply<caret>Coupon'" to "actions/couponActions.ts:354:TypeScriptFunctionProperty",
      "rootStore.dispatch({type: 'ca<caret>rt/applyCoupon'" to "store/index.ts:1240:JSLiteralExpression",

      // Direct commit
      "this.\$store.commit('cart/breadcrumbs/se<caret>t'" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty",
      "this.\$store.commit('cart/bread<caret>crumbs/set'" to "cart/index.ts:831:JSProperty",
      "rootStore.commit('cart/breadcrumbs/se<caret>t'" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty",
      "rootStore.commit('cart/bread<caret>crumbs/set'" to "cart/index.ts:831:JSProperty",
      "rootStore.commit({type: 'cart/breadcrumbs/se<caret>t'" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty",
      "rootStore.commit({type: 'cart/bread<caret>crumbs/set'" to "cart/index.ts:831:JSProperty")
  }

  fun testStorefrontComponentMappedGetters() {
    doStorefrontTest(
      // Plain dictionary
      "isVirtualCart: 'cart/is<caret>VirtualCart'" to "cart/getters.ts:2359:JSProperty",
      "foo: 'cart/fo<caret>o'" to null,
      "foo: 'ca<caret>rt/foo'" to "store/index.ts:1240:JSLiteralExpression",

      // Namespaced dictionary
      "getCategories: 'get<caret>Categories'" to "category/getters.ts:1226:JSProperty",
      "routes: 'cart/breadcrumbs/get<caret>BreadcrumbsRoutes'" to null,
      "foo: 'fo<caret>o'" to null,

      // Plain array
      "(['cart/isCart<caret>Connected'" to "cart/getters.ts:1419:JSProperty",
      "'category/b<caret>ar'])" to null,
      "'cat<caret>egory/bar'])" to "store/index.ts:1155:JSProperty",

      // Namespaced array
      "['get<caret>CartItems'," to "cart/getters.ts:1278:JSProperty",
      "'foo<caret>bar'," to null,
      "mapGetters('c<caret>art'" to "store/index.ts:1240:JSLiteralExpression",
      "'bread<caret>crumbs/getBreadcrumbsRoutes'])" to "cart/index.ts:831:JSProperty",
      "'breadcrumbs/getBreadcrumbs<caret>Routes'])" to "breadcrumbs/index.ts:333:JSProperty")
  }

  fun testStorefrontComponentMappedState() {
    doStorefrontTest(
      // Plain string dictionary
      "micro: 'cart/isMicrocart<caret>Open'" to "cart/index.ts:298:JSProperty",
      "foo2: 'f<caret>oo'" to null,
      "ship: 'ship<caret>ping'" to "store/index.ts:504:JSProperty",

      // Namespaced string dictionary
      "micro2: 'isMicrocart<caret>Open'" to "cart/index.ts:298:JSProperty",
      "foo3: 'fo<caret>o'" to null,
      "ship2: 'ship<caret>ping'" to "cart/index.ts:537:JSProperty",

      // Plain array
      "(['cart/isMicrocart<caret>Open'" to "cart/index.ts:298:JSProperty",
      "['cart/isMicrocartOpen', 'ship<caret>ping']" to "store/index.ts:504:JSProperty",

      // Namespaced array
      "('cart', ['isMicrocart<caret>Open'" to "cart/index.ts:298:JSProperty",
      "['isMicrocartOpen', 'ship<caret>ping']" to "cart/index.ts:537:JSProperty",

      // Plain function dictionary
      "routes: state => state.ca<caret>rt.breadcrumbs.routes" to "store/index.ts:1240:JSLiteralExpression",
      "routes: state => state.cart.bread<caret>crumbs.routes" to "cart/index.ts:831:JSProperty",
      "routes: state => state.cart.breadcrumbs.rou<caret>tes" to "breadcrumbs/index.ts:69:JSProperty",
      "return state.ca<caret>rt.breadcrumbs.routes" to "store/index.ts:1240:JSLiteralExpression",
      "return state.cart.bread<caret>crumbs.routes" to "cart/index.ts:831:JSProperty",
      "return state.cart.breadcrumbs.rou<caret>tes" to "breadcrumbs/index.ts:69:JSProperty",
      "foo: state => state.ca<caret>rt.breadcrumbs.foo" to "store/index.ts:1240:JSLiteralExpression",
      "foo: state => state.cart.breadcrumbs.fo<caret>o" to null,
      "ship: state => state.ship<caret>ping" to "store/index.ts:504:JSProperty",

      // Namespaced function dictionary
      "routes2: state => state.bread<caret>crumbs.routes" to "cart/index.ts:831:JSProperty",
      "routes2: state => state.breadcrumbs.rou<caret>tes" to "breadcrumbs/index.ts:69:JSProperty",
      "foo2: state => state.bread<caret>crumbs.foo" to "cart/index.ts:831:JSProperty",
      "foo2: state => state.breadcrumbs.fo<caret>o" to null,
      "ship2: state => state.ship<caret>ping" to "cart/index.ts:537:JSProperty",
      "micro: state => state.isMicrocart<caret>Open" to "cart/index.ts:298:JSProperty",
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
      "(dispatch) => dispatch({type: 'cart/configure<caret>Item'" to "actions/itemActions.ts:446:TypeScriptFunctionProperty",

      // Namespaced function dictionary
      "(dispatch) => dispatch('configure<caret>Item')" to "actions/itemActions.ts:446:TypeScriptFunctionProperty",
      "(dispatch) => dispatch({type: 'configure<caret>Item'" to "actions/itemActions.ts:446:TypeScriptFunctionProperty")
  }

  fun testStorefrontComponentMappedMutations() {
    doStorefrontTest(
      // Plain array
      "'cart/breadcrumbs/s<caret>et'\n    ])" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty",

      // Namespaced array
      "'breadcrumbs/s<caret>et'])" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty",

      // Plain function dictionary
      "commit('cart/breadcrumbs/s<caret>et') //1" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty",
      "commit({type: 'cart/breadcrumbs/s<caret>et" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty",
      "commit('cart/breadcrumbs/s<caret>et') //2" to null,

      // Namespaced function dictionary
      "commit => commit('s<caret>et')" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty",
      "commit => commit({type: 's<caret>et'" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty")
  }

  fun testStorefrontComponentNamespacedHelpers() {
    doStorefrontTest(
      "storefront-namespaced-component.ts",
      // Getters array
      "['get<caret>CartItems'," to "cart/getters.ts:1278:JSProperty",
      "'foo<caret>bar'," to null,
      "createNamespacedHelpers('c<caret>art'" to "store/index.ts:1240:JSLiteralExpression",
      "'bread<caret>crumbs/getBreadcrumbsRoutes'])" to "cart/index.ts:831:JSProperty",
      "'breadcrumbs/getBreadcrumbs<caret>Routes'])" to "breadcrumbs/index.ts:333:JSProperty",
      "'getLast<caret>TotalsSyncDate'" to null,
      "'cart/getLast<caret>TotalsSyncDate'" to null,
      "'getCategory<caret>Products'" to "category/getters.ts:1455:JSProperty",
      "'getCategoryProducts', 'f<caret>oo'" to null,

      // Getters dictionary
      "isVirtualCart: 'is<caret>VirtualCart'" to "cart/getters.ts:2359:JSProperty",
      "routes: 'bread<caret>crumbs/getBreadcrumbsRoutes'" to "cart/index.ts:831:JSProperty",
      "routes: 'breadcrumbs/get<caret>BreadcrumbsRoutes'" to "breadcrumbs/index.ts:333:JSProperty",
      "foo: 'ca<caret>rt/foo'" to null,

      // State string dictionary
      "micro2: 'isMicrocart<caret>Open'" to "cart/index.ts:298:JSProperty",
      "foo3: 'fo<caret>o'" to null,
      "ship2: 'ship<caret>ping'" to "cart/index.ts:537:JSProperty",

      // State array
      "['isMicrocart<caret>Open'" to "cart/index.ts:298:JSProperty",
      "['isMicrocartOpen', 'ship<caret>ping']" to "cart/index.ts:537:JSProperty",

      // State function dictionary
      "routes2: state => state.bread<caret>crumbs.routes" to "cart/index.ts:831:JSProperty",
      "routes2: state => state.breadcrumbs.rou<caret>tes" to "breadcrumbs/index.ts:69:JSProperty",
      "foo2: state => state.bread<caret>crumbs.foo" to "cart/index.ts:831:JSProperty",
      "foo2: state => state.breadcrumbs.fo<caret>o" to null,
      "ship2: state => state.ship<caret>ping" to "cart/index.ts:537:JSProperty",
      "micro: state => state.isMicrocart<caret>Open" to "cart/index.ts:298:JSProperty",
      "shippingMethod: (state, getters) => getters.getShipping<caret>Method" to "cart/getters.ts:639:JSProperty",
      "return getters.getShipping<caret>Method" to "cart/getters.ts:639:JSProperty",

      // Actions array
      "...mapActions(['breadcrumbs/s<caret>et', 'configureItem'])" to "breadcrumbs/index.ts:248:TypeScriptFunctionProperty",
      "...mapActions(['breadcrumbs/set', 'configure<caret>Item'])" to "actions/itemActions.ts:446:TypeScriptFunctionProperty",

      // Actions function dictionary
      "(dispatch) => dispatch('configure<caret>Item')" to "actions/itemActions.ts:446:TypeScriptFunctionProperty",
      "(dispatch) => dispatch({type: 'configure<caret>Item'" to "actions/itemActions.ts:446:TypeScriptFunctionProperty",

      // Mutations array
      "'breadcrumbs/s<caret>et'])" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty",

      // Mutations function dictionary
      "commit('breadcrumbs/s<caret>et') //1" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty",
      "commit({type: 'breadcrumbs/s<caret>et'" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty",
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
      "@cartModule.Getter('bread<caret>crumbs/getBreadcrumbsRoutes')" to "cart/index.ts:831:JSProperty",

      "@State('cart/isMicrocart<caret>Open')" to "cart/index.ts:298:JSProperty",
      "@State('ship<caret>ping') shipping1" to "store/index.ts:504:JSProperty",
      "@State('foo<caret>bar') foobar2" to null,
      "@State('cart/bread<caret>crumbs')" to "cart/index.ts:831:JSProperty",
      "@State(state => state.cart.breadcrumbs.rou<caret>tes)" to "breadcrumbs/index.ts:69:JSProperty",
      "@State(state => state.ca<caret>rt.breadcrumbs.routes)" to "store/index.ts:1240:JSLiteralExpression",

      "@cartModule.State('isMicrocart<caret>Open')" to "cart/index.ts:298:JSProperty",
      "@cartModule.State('ship<caret>ping')" to "cart/index.ts:537:JSProperty",
      "@cartModule.State(state => state.bread<caret>crumbs.routes)" to "cart/index.ts:831:JSProperty",
      "@cartModule.State(state => state.breadcrumbs.rou<caret>tes)" to "breadcrumbs/index.ts:69:JSProperty",

      "@Action('cart/configure<caret>Item')" to "actions/itemActions.ts:446:TypeScriptFunctionProperty",

      "@cartModule.Action('breadcrumbs/s<caret>et')" to "breadcrumbs/index.ts:248:TypeScriptFunctionProperty",

      "@Mutation('cart/breadcrumbs/s<caret>et')" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty",
      "@Mutation('cart/bread<caret>crumbs/set')" to "cart/index.ts:831:JSProperty",
      "@Mutation('ca<caret>rt/breadcrumbs/set')" to "store/index.ts:1240:JSLiteralExpression",

      "@cartModule.Mutation('breadcrumbs/s<caret>et')" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty",
      "@cartModule.Mutation('bread<caret>crumbs/set')" to "cart/index.ts:831:JSProperty")
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
           "'foo/namespaced<caret>Action'" to "src/root-namespaced-module.js:124:JSFunctionProperty",
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
    myFixture.configureStore(VuexTestStore.Storefront)
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
           "context.dispatch('change<caret>RouterFilterParameters', currentQuery" to "category/actions.ts:11716:TypeScriptFunctionProperty",
           "dispatch('change<caret>RouterFilterParameters', {foo:12}" to "category/actions.ts:11716:TypeScriptFunctionProperty",

      // action with root argument
           "await dispatch('cart/configure<caret>Item'" to "actions/itemActions.ts:446:TypeScriptFunctionProperty",
           "await dispatch('ca<caret>rt/configureItem'" to "store/index.ts:1240:JSLiteralExpression",
           "await context.dispatch('changeRouter<caret>FilterParameters', {}, {root: true})" to null,

      // state
           "rootState.sto<caret>ck.cache" to "store/index.ts:813:JSProperty",
           "context.state.searchProducts<caret>Stats" to "category/index.ts:425:JSProperty",
           "context.rootState.categories<caret>Map" to null,
           "context.rootState.category.categories<caret>Map" to "category/index.ts:335:JSProperty",

      // mutation commit
           "context.commit('cart/breadcrumbs/se<caret>t'" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty",
           "commit('cart/breadcrumbs/se<caret>t', {foo:12})" to null

    )
  }

  fun testStorefrontStoreGettersContext() {
    myFixture.configureStore(VuexTestStore.Storefront)
    myFixture.configureFromTempProjectFile("store/cart/getters.ts")
    doTest(false,
      // state
           "getShippingMethod: state => state.ship<caret>ping" to "cart/index.ts:537:JSProperty",
           "getShippingMethod2: (state, getters, rootState) => rootState.ship<caret>ping" to "store/index.ts:504:JSProperty",

      // getters
           "calculateTotals(getters.getFirst<caret>ShippingMethod" to "cart/getters.ts:1562:JSProperty",
           "rootGetters['cart/getCurrent<caret>CartHash']" to "cart/getters.ts:778:JSProperty"
    )
  }

  fun testStorefrontStoreMutationsContext() {
    myFixture.configureStore(VuexTestStore.Storefront)
    myFixture.configureFromTempProjectFile("store/cart/breadcrumbs/index.ts")
    doTest(false,
           "state.rou<caret>tes = " to "breadcrumbs/index.ts:69:JSProperty"
    )
  }

  fun testShoppingCartResolution() {
    myFixture.configureStore(VuexTestStore.ShoppingCart)
    myFixture.configureFromTempProjectFile("store/modules/cart.js")
    doTest(false,
           "return state.ite<caret>ms.m" to "modules/cart.js:99:JSProperty",
           "rootState.products.a<caret>ll.f" to "modules/products.js:70:JSProperty",
           "return getters.cart<caret>Products" to "modules/cart.js:167:JSProperty",
           "[...state.it<caret>ems]" to "modules/cart.js:99:JSProperty",
           "commit('set<caret>CheckoutStatus', null)" to "modules/cart.js:1956:JSFunctionProperty",
           "commit('set<caret>CartItems', { items: [] })" to "modules/cart.js:1890:JSFunctionProperty",
           "state.it<caret>ems.push({" to "modules/cart.js:99:JSProperty"
    )
    myFixture.configureFromTempProjectFile("store/modules/products.js")
    doTest(false,
           "commit('set<caret>Products'" to "modules/products.js:295:JSFunctionProperty",
           "state.al<caret>l" to "modules/products.js:70:JSProperty")
  }

  fun testCounterHotResolution() {
    myFixture.configureStore(VuexTestStore.CounterHot)
    myFixture.configureFromTempProjectFile("store/actions.js")
    doTest(false,
           "commit('inc<caret>rement')\n  }, 1000)" to "store/mutations.js:13:JSVariable",
           "(state.co<caret>unt + 1)" to "store/index.js:195:JSProperty",
           "commit('dec<caret>rement')" to "store/mutations.js:102:JSVariable")
    myFixture.configureFromTempProjectFile("store/getters.js")
    doTest(false,
           "state.co<caret>unt" to "store/index.js:195:JSProperty",
           "state.his<caret>tory" to "store/index.js:207:JSProperty")
    myFixture.configureFromTempProjectFile("store/mutations.js")
    doTest(false,
           "state.co<caret>unt" to "store/index.js:195:JSProperty",
           "state.his<caret>tory" to "store/index.js:207:JSProperty")
    myFixture.configureByText("test.vue", """
      <script>
        import { mapGetters, mapActions } from 'vuex'
        export default {
          computed: mapGetters(['recentHistory']),
          methods: mapActions(['increment'])
        }
      </script>
    """.trimIndent())
    doTest(false,
           "'recent<caret>History'" to "store/getters.js:73:JSVariable",
           "'inc<caret>rement'" to "store/actions.js:13:JSVariable")
  }

  fun testNuxtJsResolution() {
    myFixture.configureStore(VuexTestStore.NuxtJs)
    myFixture.configureFromTempProjectFile("store/pages/index.vue")
    doTest(false,
           "'change<caret>Data'" to "store/actions.js:19:JSFunctionProperty",
           "'foo/change<caret>Data'" to "foo/actions.js:19:JSFunctionProperty",
           "'bar/update<caret>Data'" to "store/bar.js:27:JSFunctionProperty",
           "'bar/change<caret>Data'" to null,
           "'ba<caret>r/changeData'" to "store/bar.js:0:JSFile:bar.js",)
    myFixture.configureFromTempProjectFile("store/store/foo/actions.js")
    doTest(false,
           "UPDATE_TEST_<caret>DATA" to "foo/index.js:75:JSFunctionProperty")
    myFixture.configureByText("foo.vue", """
      <script>
      export default {
        mounted() {
          this.${"store"}.dispatch('changeData', 'new data is nice');
          this.${"store"}.dispatch('foo/changeData', 'foo data is nice');
        }
      }
      </script>
    """)
    doTest(false,
           "'change<caret>Data'" to null,
           "'foo/change<caret>Data'" to null)
  }

  fun testCompositionApiResolution() {
    myFixture.configureStore(VuexTestStore.CompositionCounter)
    myFixture.configureByFile("composition-counter.vue")
    doTest(
      false,
      "{{ co<caret>unt }}" to "src/composition-counter.vue:466:JSProperty",
      "store.state.cou<caret>nt" to "store/store.js:127:JSProperty",
      "store.getters.even<caret>OrOdd" to "store/store.js:1072:JSProperty",
      "store.dispatch('inc<caret>rement')" to "store/store.js:635:JSProperty",
      "@click=\"inc<caret>rement\"" to "src/composition-counter.vue:572:JSProperty",
    )
  }

  fun testStoreModuleCaching() {
    val constFragment = "const counterModule"

    val storeModuleFile = myFixture.configureByFile("storeModuleCaching/store/counter/index.js")
    myFixture.configureByFile("storeModuleCaching/store/index.js")
    val appFile = myFixture.configureByFile("storeModuleCaching/App.vue")

    TestCase.assertNotNull(appFile.findReferenceAt(myFixture.caretOffset)?.resolve()?.parent)

    WriteCommandAction.runWriteCommandAction(project) {
      PsiDocumentManager.getInstance(project).getDocument(storeModuleFile)?.let { document ->
        document.replaceString(0, constFragment.length, "") // remove const
        PsiDocumentManager.getInstance(project).commitDocument(document)
      }
    }

    TestCase.assertNull(appFile.findReferenceAt(myFixture.caretOffset)?.resolve()?.parent)

    WriteCommandAction.runWriteCommandAction(project) {
      PsiDocumentManager.getInstance(project).getDocument(storeModuleFile)?.let { document ->
        document.replaceString(0, 0, constFragment) // restore const
        PsiDocumentManager.getInstance(project).commitDocument(document)
      }
    }

    TestCase.assertNotNull(appFile.findReferenceAt(myFixture.caretOffset)?.resolve()?.parent)
  }

  private fun doStorefrontTest(vararg args: Pair<String, String?>) {
    doStorefrontTest("storefront-component.ts", *args)
  }

  private fun doStorefrontTest(mainFile: String, vararg args: Pair<String, String?>) {
    myFixture.configureStore(VuexTestStore.Storefront)
    myFixture.configureByFiles(mainFile)
    doTest(false, *args)
    // Check the same with JavaScript file
    myFixture.configureByText(mainFile.removeSuffix(".ts") + ".js", myFixture.file.text)
    doTest(true, *args)
  }

  private fun doTest(permissive: Boolean = false, vararg args: Pair<String, String?>) {
    for ((signature, output) in args) {
      try {
        if (output == null) {
          try {
            myFixture.assertUnresolvedReference(signature)
          }
          catch (e: AssertionError) {
            if (!permissive) {
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
