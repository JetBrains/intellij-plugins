// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.nuxt

import com.intellij.lang.javascript.buildTools.bundler.WebBundlerConfig
import com.intellij.lang.javascript.buildTools.bundler.WebBundlerResolve
import com.intellij.lang.javascript.buildTools.bundler.WebBundlerResolveAlias
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.webpack.WebpackConfigManager
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.getVueTestDataPath
import org.junit.jupiter.api.Test

@TestApplication
class NuxtCompletionTest {
  private val hybrid by hybridFixture(getVueTestDataPath() + "/libraries/nuxt/completion")
  private val myFixture get() = hybrid.fixture
  private val project get() = myFixture.project

  @Test
  fun testNuxtExtensionsBasic() {
    myFixture.configureVueDependencies(VueTestModule.NUXT_2_8_1, VueTestModule.VUE_2_6_10)
    myFixture.addFileToProject("index.ts", "import \"@nuxt/vue-app\"")
    myFixture.configureByText("test.vue", "<script>export default {<caret>}</script>")
    myFixture.completeBasic()
    assertThat(myFixture.lookupElementStrings!!).contains(
      "asyncData", "fetch", "head", "key", "layout", "loading", "middleware", "scrollToTop", "transition", "validate",
      "watchQuery"
    )
  }

  @Test
  fun testNuxtApp() {
    myFixture.configureVueDependencies(VueTestModule.NUXT_2_8_1, VueTestModule.VUE_2_6_10)
    myFixture.addFileToProject("index.ts", "import \"@nuxt/vue-app\"")
    myFixture.configureByText("test.vue", "<template>{{\$nuxt.<caret>}}</template>")
    myFixture.completeBasic()
    assertThat(myFixture.lookupElementStrings!!).contains(
      "\$options", "\$loading", "isOffline", "isOnline", "\$nuxt"
    )
  }

  @Test
  fun testNuxtExtensionsBasic2_13() {
    myFixture.configureVueDependencies(VueTestModule.NUXT_2_13_2, VueTestModule.VUE_2_6_10)
    myFixture.addFileToProject("index.ts", "import \"@nuxt/types/app\"")
    myFixture.configureByText("test.vue", "<script>export default {<caret>}</script>")
    myFixture.completeBasic()
    assertThat(myFixture.lookupElementStrings!!).contains(
      "asyncData", "fetch", "head", "key", "layout", "loading", "middleware", "scrollToTop", "transition", "validate",
      "watchQuery"
    )
  }

  @Test
  fun testNuxtApp2_13() {
    myFixture.configureVueDependencies(VueTestModule.NUXT_2_13_2, VueTestModule.VUE_2_6_10)
    myFixture.addFileToProject("index.ts", "import \"@nuxt/types/app\"")
    myFixture.configureByText("test.vue", "<template>{{\$nuxt.<caret>}}</template>")
    myFixture.completeBasic()
    assertThat(myFixture.lookupElementStrings!!).contains(
      "\$options", "\$loading", "isOffline", "isOnline", "\$nuxt", "\$fetch"
    )
  }

  @Test
  fun testNuxtComponents() {
    myFixture.configureVueDependencies(VueTestModule.NUXT_2_13_2, VueTestModule.VUE_2_6_10)
    WebpackConfigManager.getInstance(project).setConfig(
      WebBundlerConfig(WebBundlerResolve(WebBundlerResolveAlias.fromMap(mutableMapOf("@" to "/src", "~" to "/src")))))
    myFixture.copyDirectoryToProject(hybrid.getTestName(true), ".")
    myFixture.configureFromTempProjectFile("index.vue")
    myFixture.completeBasic()

    assertThat(myFixture.lookupElementStrings!!).contains(
      "Special", "LazySpecial", "Javascript", "LazyJavascript", "Regular", "LazyRegular",
      "Prefixed", "LazyPrefixed", "PrefixedAnother", "LazyPrefixedAnother",
      "special", "lazy-special", "javascript", "lazy-javascript", "regular", "lazy-regular",
      "prefixed", "lazy-prefixed", "prefixed-another", "lazy-prefixed-another"
    )
    assertThat(myFixture.lookupElementStrings!!).doesNotContain(
      "LazyPrefixedPrefixed", "lazy-prefixed-prefixed", "LazyAnother", "lazy-another", "LazyBad", "lazy-bad"
    )
  }

  @Test
  fun testNuxtComponentsWithDirsProperty() {
    myFixture.configureVueDependencies(VueTestModule.NUXT_2_13_2, VueTestModule.VUE_2_6_10)
    WebpackConfigManager.getInstance(project).setConfig(
      WebBundlerConfig(WebBundlerResolve(WebBundlerResolveAlias.fromMap(mutableMapOf("@" to "/src", "~" to "/src")))))
    myFixture.copyDirectoryToProject(hybrid.getTestName(true), ".")
    myFixture.configureFromTempProjectFile("index.vue")
    myFixture.completeBasic()

    assertThat(myFixture.lookupElementStrings!!).contains(
      "Special", "LazySpecial", "Javascript", "LazyJavascript", "Regular", "LazyRegular",
      "Prefixed", "LazyPrefixed", "PrefixedAnother", "LazyPrefixedAnother",
      "special", "lazy-special", "javascript", "lazy-javascript", "regular", "lazy-regular",
      "prefixed", "lazy-prefixed", "prefixed-another", "lazy-prefixed-another"
    )
    assertThat(myFixture.lookupElementStrings!!).doesNotContain(
      "LazyPrefixedPrefixed", "lazy-prefixed-prefixed", "LazyAnother", "lazy-another", "LazyBad", "lazy-bad"
    )
  }

  @Test
  fun testNuxtComponents2_15() {
    myFixture.configureVueDependencies(VueTestModule.NUXT_2_15_6, VueTestModule.VUE_2_6_10)
    WebpackConfigManager.getInstance(project).setConfig(
      WebBundlerConfig(WebBundlerResolve(WebBundlerResolveAlias.fromMap(mutableMapOf("@" to "/src", "~" to "/src")))))
    myFixture.copyDirectoryToProject(hybrid.getTestName(true), ".")
    myFixture.configureFromTempProjectFile("index.vue")
    myFixture.completeBasic()

    assertThat(myFixture.lookupElementStrings!!).contains(
      "HooRegular", "HooComp", "HooSubFolderFoo12HooberComp", "HooSubFolderFoo12Special",
      "LazyHooRegular", "LazyHooComp", "LazyHooSubFolderFoo12HooberComp", "LazyHooSubFolderFoo12Special",
      "hoo-regular", "hoo-comp", "hoo-sub-folder-foo12-hoober-comp", "hoo-sub-folder-foo12-special",
      "lazy-hoo-regular", "lazy-hoo-comp", "lazy-hoo-sub-folder-foo12-hoober-comp", "lazy-hoo-sub-folder-foo12-special",

      "NestedTwiceFull", "NestedTwicePartial", "NestedTest", "NestedComp", "Unnested", "Always",
      "LazyNestedTwiceFull", "LazyNestedTwicePartial", "LazyNestedTest", "LazyNestedComp", "LazyUnnested", "LazyAlways",
      "nested-twice-full", "nested-twice-partial", "nested-test", "nested-comp", "unnested", "always",
      "lazy-nested-twice-full", "lazy-nested-twice-partial", "lazy-nested-test", "lazy-nested-comp", "lazy-unnested", "lazy-always",
      )
    assertThat(myFixture.lookupElementStrings!!).doesNotContain(
      "HooSubFolderFoo12HooComp", "SubFolderFoo12HooComp", "NestedNestedComp", "NestedTwiceTwicePartial", "LazyLazyAlways"
    )
  }

}