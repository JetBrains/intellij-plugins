// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.pasteAndWaitJSCopyPasteService
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.application.EDT
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes

@TestApplication
class VueCopyPasteTest {
  private val hybrid by hybridFixture(getVueTestDataPath() + "/copyPaste")
  private val myFixture get() = hybrid.fixture

  private fun doTest() {
    myFixture.copyDirectoryToProject(hybrid.getTestName(false), ".")
    myFixture.configureFromTempProjectFile("Source.vue")
    myFixture.performEditorAction(IdeActions.ACTION_EDITOR_COPY)
    myFixture.configureFromTempProjectFile("Destination.vue")
    myFixture.pasteAndWaitJSCopyPasteService()
    myFixture.checkResultByFile(hybrid.getTestName(false) + "/Destination_after.vue")
  }

  @Test
  fun testSimpleWithImports(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doTest()
  }

  @Test
  fun testSimpleWithNoImports(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doTest()
  }

  @Test
  fun testSimpleWithImportsBindingContext(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doTest()
  }

  @Test
  fun testScriptSetupComponent(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doTest()
  }

  @Test
  fun testScriptSetupToOptionsComponent(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doTest()
  }

  @Test
  fun testES6ToTemplateExpression(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doTest()
  }

  @Test
  fun testTemplateExpressionToES6(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doTest()
  }

  @Test
  fun testES6ToTemplateExpressionInjected(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doTest()
  }

  @Test
  fun testTemplateExpressionInjectedToES6(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doTest()
  }

  @Test
  fun testOptionsToOptionsComponent(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doTest()
  }

  @Test
  fun testOptionsToScriptSetupComponent(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doTest()
  }

  @Test
  fun testEmptyScriptSetup(): Unit = timeoutRunBlocking(timeout = 2.minutes, context = Dispatchers.EDT) {
    doTest()
  }

}