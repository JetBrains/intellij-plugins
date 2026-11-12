// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.linters.tslint

import com.intellij.lang.javascript.linter.eslint.EslintInspection
import com.intellij.lang.javascript.linter.eslint.EslintServiceFixture.Companion.eslintServiceFixture
import com.intellij.lang.javascript.modules.JSTempDirFixtureOptions
import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.vuejs.lang.vueRelativeTestDataPath
import org.junit.jupiter.api.Test

@TestApplication
class VueESLintHighlightingTest {
  private val eslint by eslintServiceFixture(
    JSTempDirFixtureOptions.forBasePath(vueRelativeTestDataPath() + "/linters/eslint/")
      .withMainPackage("eslint")
      .withGlobalPackages(linkedMapOf(
        "eslint" to "8.57.0",
        "eslint-plugin-vue" to "latest",
        "@babel/core" to "latest",
        "@babel/eslint-parser" to "latest",
        "@vue/cli-plugin-babel" to "latest",
      )),
    { EslintInspection() })

  @Test
  fun testVue2SpecificRules() {
    eslint.doEditorHighlightingTest("test.vue")
  }

  @Test
  fun testVue3SpecificRules() {
    eslint.doEditorHighlightingTest("test.vue")
  }

  @Test
  fun testHtml() {
    eslint.doEditorHighlightingTest("test.html")
  }
}
