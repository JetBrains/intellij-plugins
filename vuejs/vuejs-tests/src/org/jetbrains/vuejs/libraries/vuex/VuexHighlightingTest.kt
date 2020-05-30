// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex

import com.intellij.openapi.application.PathManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.vuejs.lang.VueInspectionsProvider

class VuexHighlightingTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = PathManager.getHomePath() + "/contrib/vuejs/vuejs-tests/testData/libraries/vuex/highlighting"

  fun testStorefrontReferences() {
    myFixture.configureStore(VuexTestStore.Storefront)
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.configureByFile("storefrontReferences.vue")
    myFixture.checkHighlighting()
  }

}
