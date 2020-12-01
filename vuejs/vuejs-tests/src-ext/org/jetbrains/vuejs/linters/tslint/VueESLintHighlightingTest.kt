// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.linters.tslint

import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.lang.javascript.linter.eslint.EslintInspection
import com.intellij.lang.javascript.linter.eslint.EslintServiceTestBase
import org.jetbrains.vuejs.lang.vueRelativeTestDataPath

class VueESLintHighlightingTest: EslintServiceTestBase() {

  override fun getBasePath(): String = vueRelativeTestDataPath() + "/linters/eslint/"

  override fun getInspection(): InspectionProfileEntry = EslintInspection()

  override fun getPackageName(): String = "eslint"

  fun testVueSpecificRules() {
    doEditorHighlightingTest("test.vue")
  }

  fun testHtml() {
    doEditorHighlightingTest("test.html")
  }


}
