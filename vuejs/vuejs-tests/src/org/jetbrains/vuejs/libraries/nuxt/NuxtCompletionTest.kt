// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.nuxt

import com.intellij.lang.javascript.buildTools.bundler.WebBundlerConfig
import com.intellij.lang.javascript.buildTools.bundler.WebBundlerResolve
import com.intellij.lang.javascript.buildTools.bundler.WebBundlerResolveAlias
import com.intellij.lang.javascript.buildTools.webpack.WebPackConfigManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.getVueTestDataPath

class NuxtCompletionTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = getVueTestDataPath() + "/libraries/nuxt/completion"

  fun testNuxtExtensionsBasic() {
    myFixture.configureVueDependencies(VueTestModule.NUXT_2_8_1, VueTestModule.VUE_2_6_10)
    myFixture.configureByText("test.vue", "<script>export default {<caret>}</script>")
    myFixture.completeBasic()
    assertContainsElements(
      myFixture.lookupElementStrings!!,
      "asyncData", "fetch", "head", "key", "layout", "loading", "middleware", "scrollToTop", "transition", "validate",
      "watchQuery"
    )
  }

  fun testNuxtApp() {
    myFixture.configureVueDependencies(VueTestModule.NUXT_2_8_1, VueTestModule.VUE_2_6_10)
    myFixture.configureByText("test.vue", "<template>{{\$nuxt.<caret>}}</template>")
    myFixture.completeBasic()
    assertContainsElements(
      myFixture.lookupElementStrings!!,
      "\$options", "\$loading", "isOffline", "isOnline", "\$nuxt"
    )
  }

  fun testNuxtExtensionsBasic2_13() {
    myFixture.configureVueDependencies(VueTestModule.NUXT_2_13_2, VueTestModule.VUE_2_6_10)
    myFixture.configureByText("test.vue", "<script>export default {<caret>}</script>")
    myFixture.completeBasic()
    assertContainsElements(
      myFixture.lookupElementStrings!!,
      "asyncData", "fetch", "head", "key", "layout", "loading", "middleware", "scrollToTop", "transition", "validate",
      "watchQuery"
    )
  }

  fun testNuxtApp2_13() {
    myFixture.configureVueDependencies(VueTestModule.NUXT_2_13_2, VueTestModule.VUE_2_6_10)
    myFixture.configureByText("test.vue", "<template>{{\$nuxt.<caret>}}</template>")
    myFixture.completeBasic()
    assertContainsElements(
      myFixture.lookupElementStrings!!,
      "\$options", "\$loading", "isOffline", "isOnline", "\$nuxt", "\$fetch"
    )
  }

  fun testNuxtComponents() {
    myFixture.configureVueDependencies(VueTestModule.NUXT_2_13_2, VueTestModule.VUE_2_6_10)
    WebPackConfigManager.getInstance(project).setConfig(
      WebBundlerConfig(WebBundlerResolve(WebBundlerResolveAlias.fromMap(mutableMapOf("@" to "/src", "~" to "/src")))))
    myFixture.copyDirectoryToProject(getTestName(true), ".")
    myFixture.configureFromTempProjectFile("index.vue")
    myFixture.completeBasic()

    assertContainsElements(
      myFixture.lookupElementStrings!!,
      "Special", "LazySpecial", "Javascript", "LazyJavascript", "Regular", "LazyRegular",
      "Prefixed", "LazyPrefixed", "PrefixedAnother", "LazyPrefixedAnother",
      "special", "lazy-special", "javascript", "lazy-javascript", "regular", "lazy-regular",
      "prefixed", "lazy-prefixed", "prefixed-another", "lazy-prefixed-another"
    )
    assertDoesntContain(
      myFixture.lookupElementStrings!!,
      "LazyPrefixedPrefixed", "lazy-prefixed-prefixed", "LazyAnother", "lazy-another", "LazyBad", "lazy-bad"
    )
  }

  fun testNuxtComponentsWithDirsProperty() {
    myFixture.configureVueDependencies(VueTestModule.NUXT_2_13_2, VueTestModule.VUE_2_6_10)
    WebPackConfigManager.getInstance(project).setConfig(
      WebBundlerConfig(WebBundlerResolve(WebBundlerResolveAlias.fromMap(mutableMapOf("@" to "/src", "~" to "/src")))))
    myFixture.copyDirectoryToProject(getTestName(true), ".")
    myFixture.configureFromTempProjectFile("index.vue")
    myFixture.completeBasic()

    assertContainsElements(
      myFixture.lookupElementStrings!!,
      "Special", "LazySpecial", "Javascript", "LazyJavascript", "Regular", "LazyRegular",
      "Prefixed", "LazyPrefixed", "PrefixedAnother", "LazyPrefixedAnother",
      "special", "lazy-special", "javascript", "lazy-javascript", "regular", "lazy-regular",
      "prefixed", "lazy-prefixed", "prefixed-another", "lazy-prefixed-another"
    )
    assertDoesntContain(
      myFixture.lookupElementStrings!!,
      "LazyPrefixedPrefixed", "lazy-prefixed-prefixed", "LazyAnother", "lazy-another", "LazyBad", "lazy-bad"
    )
  }

  fun testNuxtComponents2_15() {
    myFixture.configureVueDependencies(VueTestModule.NUXT_2_15_6, VueTestModule.VUE_2_6_10)
    WebPackConfigManager.getInstance(project).setConfig(
      WebBundlerConfig(WebBundlerResolve(WebBundlerResolveAlias.fromMap(mutableMapOf("@" to "/src", "~" to "/src")))))
    myFixture.copyDirectoryToProject(getTestName(true), ".")
    myFixture.configureFromTempProjectFile("index.vue")
    myFixture.completeBasic()

    assertContainsElements(
      myFixture.lookupElementStrings!!,
      "HooRegular", "HooComp", "HooSubFolderFoo12HooberComp", "HooSubFolderFoo12Special",
      "LazyHooRegular", "LazyHooComp", "LazyHooSubFolderFoo12HooberComp", "LazyHooSubFolderFoo12Special",
      "hoo-regular", "hoo-comp", "hoo-sub-folder-foo12-hoober-comp", "hoo-sub-folder-foo12-special",
      "lazy-hoo-regular", "lazy-hoo-comp", "lazy-hoo-sub-folder-foo12-hoober-comp", "lazy-hoo-sub-folder-foo12-special",

      "NestedTwiceFull", "NestedTwicePartial", "NestedTest", "NestedComp", "Unnested", "Always",
      "LazyNestedTwiceFull", "LazyNestedTwicePartial", "LazyNestedTest", "LazyNestedComp", "LazyUnnested", "LazyAlways",
      "nested-twice-full", "nested-twice-partial", "nested-test", "nested-comp", "unnested", "always",
      "lazy-nested-twice-full", "lazy-nested-twice-partial", "lazy-nested-test", "lazy-nested-comp", "lazy-unnested", "lazy-always",
      )
    assertDoesntContain(
      myFixture.lookupElementStrings!!,
      "HooSubFolderFoo12HooComp", "SubFolderFoo12HooComp", "NestedNestedComp", "NestedTwiceTwicePartial", "LazyLazyAlways"
    )
  }

}