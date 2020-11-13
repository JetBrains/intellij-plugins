// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.vuejs.lang.VueInspectionsProvider
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.getVueTestDataPath

class VuexHighlightingTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = getVueTestDataPath() + "/libraries/vuex/highlighting"

  fun testStorefrontReferences() {
    myFixture.configureStore(VuexTestStore.Storefront)
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.configureByFile("storefrontReferences.vue")
    myFixture.checkHighlighting()
  }

  fun testTypedParameter() {
    myFixture.configureVueDependencies(VueTestModule.VUEX_3_1_0, VueTestModule.VUE_2_6_10)
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.configureByFile("typedParameter.ts")
    myFixture.checkHighlighting()
  }

  fun testStateViaLambda() {
    myFixture.configureVueDependencies(VueTestModule.VUEX_3_1_0, VueTestModule.VUE_2_6_10)
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.configureByFile("stateViaLambda.ts")
    myFixture.checkHighlighting()
  }

  fun testStateViaLambda2() {
    myFixture.configureStore(VuexTestStore.StateViaLambda)
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.configureByFile("stateViaLambda.vue")
    myFixture.checkHighlighting()
  }

}
