// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.nuxt

import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@TestApplication
class NuxtConfigTest {
  private val hybrid by hybridFixture("")
  private val myFixture get() = hybrid.fixture

  @Test
  fun testBasic() {
    myFixture.configureVueDependencies(VueTestModule.NUXT_2_8_1)
    myFixture.configureByText("nuxt.config.js", "export default {<caret>}")
    myFixture.completeBasic()
    assertEquals(
      listOf("\"vue.config\"", "build", "buildDir", "css", "dev", "env", "fetch", "generate", "globalName", "globals", "head", "hooks",
             "ignore", "ignorePrefix", "layoutTransition", "loading", "loadingIndicator", "mode", "modern", "modules", "modulesDir",
             "plugins", "render", "rootDir", "router", "server", "serverMiddleware", "srcDir", "transition", "watch", "watchers"),
      myFixture.lookupElementStrings)
  }

  @Test
  fun testHeadMeta() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_6_10, VueTestModule.NUXT_2_8_1)
    myFixture.configureByText("nuxt.config.js", "export default { head:{ meta:[{<caret>}] } }")
    myFixture.completeBasic()
    assertEquals(
      listOf("\"http-equiv\"", "charset", "content", "name", "vmid"),
      myFixture.lookupElementStrings)
  }

  @Test
  fun testBasic2_9() {
    myFixture.configureVueDependencies(VueTestModule.NUXT_2_9_2)
    myFixture.configureByText("nuxt.config.js", "export default {<caret>}")
    myFixture.completeBasic()
    assertEquals(
      listOf("\"vue.config\"", "build", "buildDir", "buildModules", "css", "dev", "dir", "env", "extensions", "fetch", "generate",
             "globalName", "globals", "head", "hooks", "ignore", "ignorePrefix", "layoutTransition", "loading", "loadingIndicator", "mode",
             "modern", "modules", "modulesDir", "plugins", "render", "rootDir", "router", "server", "serverMiddleware", "srcDir",
             "transition", "watch", "watchers"),
      myFixture.lookupElementStrings)
  }

  @Test
  fun testHeadMeta2_9() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_6_10, VueTestModule.NUXT_2_9_2)
    myFixture.configureByText("nuxt.config.js", "export default { head:{ meta:[{<caret>}] } }")
    myFixture.completeBasic()
    assertEquals(
      listOf("body", "charset", "content", "httpEquiv", "itemprop", "name", "once", "pbody", "property", "skip", "template", "vmid"),
      myFixture.lookupElementStrings)
  }

  @Test
  fun testBasic2_13() {
    myFixture.configureVueDependencies(VueTestModule.NUXT_2_13_2)
    myFixture.configureByText("nuxt.config.js", "export default {<caret>}")
    myFixture.completeBasic()
    assertEquals(
      listOf("\"vue.config\"", "build", "buildDir", "buildModules", "cli", "css", "dev", "dir", "env", "export", "extensions", "features",
             "fetch", "generate", "globalName", "globals", "head", "hooks", "ignore", "ignorePrefix", "layoutTransition", "loading",
             "loadingIndicator", "mode", "modern", "modules", "modulesDir", "plugins", "privateRuntimeConfig", "publicRuntimeConfig",
             "render", "rootDir", "router", "server", "serverMiddleware", "srcDir", "target", "transition", "watch", "watchers"),
      myFixture.lookupElementStrings)
  }

  @Test
  fun testHeadMeta2_13() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_6_10, VueTestModule.NUXT_2_13_2)
    myFixture.configureByText("nuxt.config.js", "export default { head:{ meta:[{<caret>}] } }")
    myFixture.completeBasic()
    assertEquals(
      listOf("body", "charset", "content", "httpEquiv", "itemprop", "name", "once", "pbody", "property", "skip", "template", "vmid"),
      myFixture.lookupElementStrings)
  }

}
