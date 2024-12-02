// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.pasteAndWaitJSCopyPasteService
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VueCopyPasteTest : BasePlatformTestCase() {
  override fun getBasePath(): String = ""
  override fun getTestDataPath(): String = getVueTestDataPath() + "/copyPaste"

  private fun doTest() {
    myFixture.copyDirectoryToProject(getTestName(false), ".")
    myFixture.configureFromTempProjectFile("Source.vue")
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_COPY)
    myFixture.configureFromTempProjectFile("Destination.vue")
    myFixture.pasteAndWaitJSCopyPasteService()
    myFixture.checkResultByFile(getTestName(false) + "/Destination_after.vue")
  }

  fun testSimpleWithImports() {
    doTest()
  }

  fun testSimpleWithNoImports() {
    doTest()
  }

  fun testSimpleWithImportsBindingContext() {
    doTest()
  }

  fun testScriptSetupComponent() {
    doTest()
  }

  fun testScriptSetupToOptionsComponent() {
    doTest()
  }

  fun testES6ToTemplateExpression() {
    doTest()
  }

  fun testTemplateExpressionToES6() {
    doTest()
  }

  fun testES6ToTemplateExpressionInjected() {
    doTest()
  }

  fun testTemplateExpressionInjectedToES6() {
    doTest()
  }

  fun testOptionsToOptionsComponent() {
    doTest()
  }

  fun testOptionsToScriptSetupComponent() {
    doTest()
  }

  fun testEmptyScriptSetup() {
    doTest()
  }

}