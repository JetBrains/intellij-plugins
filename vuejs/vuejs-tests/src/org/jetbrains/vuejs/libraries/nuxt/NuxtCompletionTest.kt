// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.nuxt

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureDependencies

class NuxtCompletionTest : BasePlatformTestCase() {

  fun testNuxtExtensionsBasic() {
    myFixture.configureDependencies(VueTestModule.NUXT_2_8_1, VueTestModule.VUE_2_6_10)
    myFixture.configureByText("test.vue", "<script>export default {<caret>}</script>")
    myFixture.completeBasic()
    assertContainsElements(
      myFixture.lookupElementStrings!!,
      "asyncData", "fetch", "head", "key", "layout", "loading", "middleware", "scrollToTop", "transition", "validate",
      "watchQuery"
    )
  }

  fun testNuxtApp() {
    myFixture.configureDependencies(VueTestModule.NUXT_2_8_1, VueTestModule.VUE_2_6_10)
    myFixture.configureByText("test.vue", "<template>{{\$nuxt.<caret>}}</template>")
    myFixture.completeBasic()
    assertContainsElements(
      myFixture.lookupElementStrings!!,
      "\$options", "\$loading", "isOffline", "isOnline", "\$nuxt"
    )
  }

  fun testNuxtExtensionsBasic2_13() {
    myFixture.configureDependencies(VueTestModule.NUXT_2_13_2, VueTestModule.VUE_2_6_10)
    myFixture.configureByText("test.vue", "<script>export default {<caret>}</script>")
    myFixture.completeBasic()
    assertContainsElements(
      myFixture.lookupElementStrings!!,
      "asyncData", "fetch", "head", "key", "layout", "loading", "middleware", "scrollToTop", "transition", "validate",
      "watchQuery"
    )
  }

  fun testNuxtApp2_13() {
    myFixture.configureDependencies(VueTestModule.NUXT_2_13_2, VueTestModule.VUE_2_6_10)
    myFixture.configureByText("test.vue", "<template>{{\$nuxt.<caret>}}</template>")
    myFixture.completeBasic()
    assertContainsElements(
      myFixture.lookupElementStrings!!,
      "\$options", "\$loading", "isOffline", "isOnline", "\$nuxt", "\$fetch"
    )
  }

}