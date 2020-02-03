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

  fun testStorefrontDirectIndexedGetter() {
    doStorefrontTest("this.\$store.getters['cart/get<caret>Coupon']" to "cart/getters.ts:2139:JSProperty",
                     "this.\$store.getters['ca<caret>rt/getCoupon']" to "store/index.ts:1265:JSLiteralExpression",
                     "rootStore.getters['cart/get<caret>Coupon']" to "cart/getters.ts:2139:JSProperty",
                     "rootStore.getters['ca<caret>rt/getCoupon']" to "store/index.ts:1265:JSLiteralExpression")
  }

  fun testStorefrontDirectReferencedGetter() {
    doStorefrontTest("this.\$store.getters.getCurrent<caret>StoreView" to "store/getters.ts:148:JSProperty")
  }

  fun testStorefrontDirectState() {
    doStorefrontTest("this.\$store.state.category.categories<caret>Map" to "category/index.ts:335:JSProperty",
                     "this.\$store.state.cate<caret>gory.categoriesMap" to "store/index.ts:1180:JSProperty")
  }

  fun testStorefrontDirectReferencedGetterJS() {
    doStorefrontTest("this.\$store.getters.getCurrent<caret>StoreView" to "store/getters.ts:148:JSProperty")
  }

  fun testStorefrontDirectStateJS() {
    doStorefrontTest("this.\$store.state.category.categories<caret>Map" to "category/index.ts:335:JSProperty",
                     "this.\$store.state.cate<caret>gory.categoriesMap" to "store/index.ts:1180:JSProperty")
  }

  fun testStorefrontDirectDispatch() {
    doStorefrontTest("this.\$store.dispatch('cart/apply<caret>Coupon'" to "actions/couponActions.ts:354:TypeScriptFunctionProperty",
                     "this.\$store.dispatch('ca<caret>rt/applyCoupon'" to "store/index.ts:1265:JSLiteralExpression",
                     "rootStore.dispatch('cart/apply<caret>Coupon'" to "actions/couponActions.ts:354:TypeScriptFunctionProperty",
                     "rootStore.dispatch('ca<caret>rt/applyCoupon'" to "store/index.ts:1265:JSLiteralExpression")
  }

  fun testStorefrontDirectCommit() {
    doStorefrontTest("this.\$store.commit('cart/breadcrumbs/se<caret>t'" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty",
                     "this.\$store.commit('cart/bread<caret>crumbs/set'" to "cart/index.ts:838:JSProperty",
                     "rootStore.commit('cart/breadcrumbs/se<caret>t'" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty",
                     "rootStore.commit('cart/bread<caret>crumbs/set'" to "cart/index.ts:838:JSProperty")
  }

  fun testStorefrontMappedGettersPlainDictionary() {
    doStorefrontTest("isVirtualCart: 'cart/is<caret>VirtualCart'" to "cart/getters.ts:2359:JSProperty",
                     "foo: 'cart/fo<caret>o'" to null,
                     "foo: 'ca<caret>rt/foo'" to "store/index.ts:1265:JSLiteralExpression")
  }

  fun testStorefrontMappedGettersNamespacedDictionary() {
    doStorefrontTest("getCategories: 'get<caret>Categories'" to "category/getters.ts:1226:JSProperty",
                     "routes: 'cart/breadcrumbs/get<caret>BreadcrumbsRoutes'" to null,
                     "foo: 'fo<caret>o'" to null)
  }

  fun testStorefrontMappedGettersPlainArray() {
    doStorefrontTest("(['cart/isCart<caret>Connected'" to "cart/getters.ts:1419:JSProperty",
                     "'category/b<caret>ar'])" to null,
                     "'cat<caret>egory/bar'])" to "store/index.ts:1180:JSProperty")
  }

  fun testStorefrontMappedGettersNamespacedArray() {
    doStorefrontTest("['get<caret>CartItems'," to "cart/getters.ts:1278:JSProperty",
                     "'foo<caret>bar'," to null,
                     "mapGetters('c<caret>art'" to "store/index.ts:1265:JSLiteralExpression",
                     "'bread<caret>crumbs/getBreadcrumbsRoutes'])" to "cart/index.ts:838:JSProperty",
                     "'breadcrumbs/getBreadcrumbs<caret>Routes'])" to "breadcrumbs/index.ts:333:JSProperty")
  }

  fun testStorefrontMappedStatePlainStringDictionary() {
    doStorefrontTest("micro: 'cart/isMicrocart<caret>Open'" to "cart/index.ts:305:JSProperty",
                     "foo2: 'f<caret>oo'" to null,
                     "ship: 'ship<caret>ping'" to "store/index.ts:529:JSProperty")
  }

  fun testStorefrontMappedStateNamespacedStringDictionary() {
    doStorefrontTest("micro2: 'isMicrocart<caret>Open'" to "cart/index.ts:305:JSProperty",
                     "foo3: 'fo<caret>o'" to null,
                     "ship2: 'ship<caret>ping'" to "cart/index.ts:544:JSProperty")
  }

  fun testStorefrontMappedStatePlainArray() {
    doStorefrontTest("(['cart/isMicrocart<caret>Open'" to "cart/index.ts:305:JSProperty",
                     "['cart/isMicrocartOpen', 'ship<caret>ping']" to "store/index.ts:529:JSProperty")
  }

  fun testStorefrontMappedStateNamespacedArray() {
    doStorefrontTest("('cart', ['isMicrocart<caret>Open'" to "cart/index.ts:305:JSProperty",
                     "['isMicrocartOpen', 'ship<caret>ping']" to "cart/index.ts:544:JSProperty")
  }

  fun testStorefrontMappedStatePlainFunctionDictionary() {
    doStorefrontTest("routes: state => state.ca<caret>rt.breadcrumbs.routes" to "store/index.ts:1265:JSLiteralExpression",
                     "routes: state => state.cart.bread<caret>crumbs.routes" to "cart/index.ts:838:JSProperty",
                     "routes: state => state.cart.breadcrumbs.rou<caret>tes" to "breadcrumbs/index.ts:69:JSProperty",
                     "return state.ca<caret>rt.breadcrumbs.routes" to "store/index.ts:1265:JSLiteralExpression",
                     "return state.cart.bread<caret>crumbs.routes" to "cart/index.ts:838:JSProperty",
                     "return state.cart.breadcrumbs.rou<caret>tes" to "breadcrumbs/index.ts:69:JSProperty",
                     "foo: state => state.ca<caret>rt.breadcrumbs.foo" to "store/index.ts:1265:JSLiteralExpression",
                     "foo: state => state.cart.breadcrumbs.fo<caret>o" to null,
                     "ship: state => state.ship<caret>ping" to "store/index.ts:529:JSProperty")
  }

  fun testStorefrontMappedStateNamespacedFunctionDictionary() {
    doStorefrontTest("routes2: state => state.bread<caret>crumbs.routes" to "cart/index.ts:838:JSProperty",
                     "routes2: state => state.breadcrumbs.rou<caret>tes" to "breadcrumbs/index.ts:69:JSProperty",
                     "foo2: state => state.bread<caret>crumbs.foo" to "cart/index.ts:838:JSProperty",
                     "foo2: state => state.breadcrumbs.fo<caret>o" to null,
                     "ship2: state => state.ship<caret>ping" to "cart/index.ts:544:JSProperty",
                     "micro: state => state.isMicrocart<caret>Open" to "cart/index.ts:305:JSProperty",
                     "shippingMethod: (state, getters) => getters.getShipping<caret>Method" to "cart/getters.ts:639:JSProperty",
                     "return getters.getShipping<caret>Method" to "cart/getters.ts:639:JSProperty")
  }

  fun testStorefrontMappedActionsPlainArray() {
    doStorefrontTest("...mapActions(['cart/configure<caret>Item'])" to "actions/itemActions.ts:446:TypeScriptFunctionProperty")
  }

  fun testStorefrontMappedActionsNamespacedArray() {
    doStorefrontTest("...mapActions('cart/breadcrumbs', ['s<caret>et'])" to "breadcrumbs/index.ts:248:TypeScriptFunctionProperty")
  }

  fun testStorefrontMappedActionsPlainFunctionDictionary() {
    doStorefrontTest("(dispatch) => dispatch('cart/configure<caret>Item')" to "actions/itemActions.ts:446:TypeScriptFunctionProperty")
  }

  fun testStorefrontMappedActionsNamespacedFunctionDictionary() {
    doStorefrontTest("(dispatch) => dispatch('configure<caret>Item')" to "actions/itemActions.ts:446:TypeScriptFunctionProperty")
  }

  fun testStorefrontMappedMutationsPlainArray() {
    doStorefrontTest("'cart/breadcrumbs/s<caret>et'\n    ])" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty")
  }

  fun testStorefrontMappedMutationsNamespacedArray() {
    doStorefrontTest("'breadcrumbs/s<caret>et'])" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty")
  }

  fun testStorefrontMappedMutationsPlainFunctionDictionary() {
    doStorefrontTest("commit('cart/breadcrumbs/s<caret>et') //1" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty",
                     "commit('cart/breadcrumbs/s<caret>et') //2" to null)
  }

  fun testStorefrontMappedMutationsNamespacedFunctionDictionary() {
    doStorefrontTest("commit => commit('s<caret>et')" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty")
  }

  fun testStorefrontNamespacedMappedGettersArray() {
    doStorefrontNamespacedTest("['get<caret>CartItems'," to "cart/getters.ts:1278:JSProperty",
                               "'foo<caret>bar'," to null,
                               "createNamespacedHelpers('c<caret>art'" to "store/index.ts:1265:JSLiteralExpression",
                               "'bread<caret>crumbs/getBreadcrumbsRoutes'])" to "cart/index.ts:838:JSProperty",
                               "'breadcrumbs/getBreadcrumbs<caret>Routes'])" to "breadcrumbs/index.ts:333:JSProperty",
                               "'getLast<caret>TotalsSyncDate'" to null,
                               "'cart/getLast<caret>TotalsSyncDate'" to null,
                               "'getCategory<caret>Products'" to "category/getters.ts:1455:JSProperty",
                               "'getCategoryProducts', 'f<caret>oo'" to null)
  }

  fun testStorefrontNamespacedMappedGettersDictionary() {
    doStorefrontNamespacedTest("isVirtualCart: 'is<caret>VirtualCart'" to "cart/getters.ts:2359:JSProperty",
                               "routes: 'bread<caret>crumbs/getBreadcrumbsRoutes'" to "cart/index.ts:838:JSProperty",
                               "routes: 'breadcrumbs/get<caret>BreadcrumbsRoutes'" to "breadcrumbs/index.ts:333:JSProperty",
                               "foo: 'ca<caret>rt/foo'" to null)
  }

  fun testStorefrontNamespacedMappedStateStringDictionary() {
    doStorefrontNamespacedTest("micro2: 'isMicrocart<caret>Open'" to "cart/index.ts:305:JSProperty",
                               "foo3: 'fo<caret>o'" to null,
                               "ship2: 'ship<caret>ping'" to "cart/index.ts:544:JSProperty")
  }

  fun testStorefrontNamespacedMappedStateArray() {
    doStorefrontNamespacedTest("['isMicrocart<caret>Open'" to "cart/index.ts:305:JSProperty",
                               "['isMicrocartOpen', 'ship<caret>ping']" to "cart/index.ts:544:JSProperty")
  }

  fun testStorefrontNamespacedMappedStateFunctionDictionary() {
    doStorefrontTest("routes2: state => state.bread<caret>crumbs.routes" to "cart/index.ts:838:JSProperty",
                     "routes2: state => state.breadcrumbs.rou<caret>tes" to "breadcrumbs/index.ts:69:JSProperty",
                     "foo2: state => state.bread<caret>crumbs.foo" to "cart/index.ts:838:JSProperty",
                     "foo2: state => state.breadcrumbs.fo<caret>o" to null,
                     "ship2: state => state.ship<caret>ping" to "cart/index.ts:544:JSProperty",
                     "micro: state => state.isMicrocart<caret>Open" to "cart/index.ts:305:JSProperty",
                     "shippingMethod: (state, getters) => getters.getShipping<caret>Method" to "cart/getters.ts:639:JSProperty",
                     "return getters.getShipping<caret>Method" to "cart/getters.ts:639:JSProperty")
  }

  fun testStorefrontNamespacedMappedActionsArray() {
    doStorefrontNamespacedTest(
      "...mapActions(['breadcrumbs/s<caret>et', 'configureItem'])" to "breadcrumbs/index.ts:248:TypeScriptFunctionProperty",
      "...mapActions(['breadcrumbs/set', 'configure<caret>Item'])" to "actions/itemActions.ts:446:TypeScriptFunctionProperty")
  }

  fun testStorefrontNamespacedMappedActionsFunctionDictionary() {
    doStorefrontNamespacedTest("(dispatch) => dispatch('configure<caret>Item')" to "actions/itemActions.ts:446:TypeScriptFunctionProperty")
  }

  fun testStorefrontNamespacedMappedMutationsArray() {
    doStorefrontNamespacedTest("'breadcrumbs/s<caret>et'])" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty")
  }

  fun testStorefrontNamespacedMappedMutationsFunctionDictionary() {
    doStorefrontNamespacedTest("commit('breadcrumbs/s<caret>et') //1" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty",
                               "commit('breadcrumbs/s<caret>et') //2" to null)
  }

  fun testStorefrontDecoratedGetterMapper() {
    doStorefrontDecoratedTest("@Getter('cart/isVirtual<caret>Cart')" to "cart/getters.ts:2359:JSProperty",
                              "@Getter('isVirtual<caret>Cart')" to null)
  }

  fun testStorefrontDecoratedModuleGetterMapper() {
    doStorefrontDecoratedTest("@cartModule.Getter('get<caret>CartItems')" to "cart/getters.ts:1278:JSProperty",
                              "@cartModule.Getter('foo<caret>bar')" to null,
                              "@cartModule.Getter('breadcrumbs/getBreadcrumbs<caret>Routes')" to "breadcrumbs/index.ts:333:JSProperty",
                              "@cartModule.Getter('bread<caret>crumbs/getBreadcrumbsRoutes')" to "cart/index.ts:838:JSProperty")
  }

  fun testStorefrontDecoratedStateMapper() {
    doStorefrontDecoratedTest("@State('cart/isMicrocart<caret>Open')" to "cart/index.ts:305:JSProperty",
                              "@State('ship<caret>ping') shipping1" to "store/index.ts:529:JSProperty",
                              "@State('foo<caret>bar') foobar2" to null,
                              "@State(state => state.cart.breadcrumbs.rou<caret>tes)" to "breadcrumbs/index.ts:69:JSProperty",
                              "@State(state => state.ca<caret>rt.breadcrumbs.routes)" to "store/index.ts:1265:JSLiteralExpression"
    )
  }

  fun testStorefrontDecoratedModuleStateMapper() {
    doStorefrontDecoratedTest("@cartModule.State('isMicrocart<caret>Open')" to "cart/index.ts:305:JSProperty",
                              "@cartModule.State('ship<caret>ping')" to "cart/index.ts:544:JSProperty",
                              "@cartModule.State(state => state.bread<caret>crumbs.routes)" to "cart/index.ts:838:JSProperty",
                              "@cartModule.State(state => state.breadcrumbs.rou<caret>tes)" to "breadcrumbs/index.ts:69:JSProperty"
    )
  }

  fun testStorefrontDecoratedActionMapper() {
    doStorefrontDecoratedTest("@Action('cart/configure<caret>Item')" to "actions/itemActions.ts:446:TypeScriptFunctionProperty")
  }

  fun testStorefrontDecoratedModuleActionMapper() {
    doStorefrontDecoratedTest("@cartModule.Action('breadcrumbs/s<caret>et')" to "breadcrumbs/index.ts:248:TypeScriptFunctionProperty")
  }

  fun testStorefrontDecoratedMutationMapper() {
    doStorefrontDecoratedTest("@Mutation('cart/breadcrumbs/s<caret>et')" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty",
                              "@Mutation('cart/bread<caret>crumbs/set')" to "cart/index.ts:838:JSProperty",
                              "@Mutation('ca<caret>rt/breadcrumbs/set')" to "store/index.ts:1265:JSLiteralExpression")
  }

  fun testStorefrontDecoratedModuleMutationMapper() {
    doStorefrontDecoratedTest("@cartModule.Mutation('breadcrumbs/s<caret>et')" to "breadcrumbs/index.ts:123:TypeScriptFunctionProperty",
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

  private fun doStorefrontNamespacedTest(vararg args: Pair<String, String?>) {
    doStorefrontTest("storefront-namespaced-component.ts", *args)
  }

  private fun doStorefrontDecoratedTest(vararg args: Pair<String, String?>) {
    doStorefrontTest("storefront-decorated-component.ts", *args)
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
          } catch (e: AssertionError) {
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
