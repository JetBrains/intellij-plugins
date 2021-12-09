// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.linters.tslint

import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.lang.javascript.linter.eslint.EslintInspection
import com.intellij.lang.javascript.linter.eslint.EslintServiceTestBase
import org.jetbrains.vuejs.lang.vueRelativeTestDataPath

class VueESLintHighlightingTest : EslintServiceTestBase() {

  override fun getBasePath(): String = vueRelativeTestDataPath() + "/linters/eslint/"

  override fun getInspection(): InspectionProfileEntry = EslintInspection()

  override fun getPackageName(): String = "eslint"

  override fun getGlobalPackageVersionsToInstall(): Map<String, String> = mapOf(
    "eslint" to "latest",
    "eslint-plugin-vue" to "latest",
    "@babel/core" to "latest",
    "@babel/eslint-parser" to "latest",
    "@vue/cli-plugin-babel" to "latest",
  )

  fun testVueSpecificRules() {
    doEditorHighlightingTest("test.vue")
  }

  fun testHtml() {
    doEditorHighlightingTest("test.html")
  }
}
