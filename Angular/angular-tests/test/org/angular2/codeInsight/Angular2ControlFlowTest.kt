// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.JSFixtureOptions
import com.intellij.lang.javascript.controlflow.JSControlFlowFixture.Companion.jsControlFlowFixture
import com.intellij.lang.javascript.jsProjectFixture
import com.intellij.lang.javascript.psi.JSControlFlowScope
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.polySymbols.testFramework.configure
import com.intellij.testFramework.junit5.RegistryKey
import com.intellij.testFramework.junit5.TestApplication
import org.angular2.Angular2TemplateInspectionsProvider
import org.angular2.Angular2TestModule
import org.angular2.Angular2TestModule.Companion.configureDependencies
import org.angular2.Angular2TestUtil
import org.angular2.Angular2TsConfigFile
import org.angular2.codeInsight.controlflow.Angular2ControlFlowBuilder
import org.junit.jupiter.api.Test

@TestApplication
@RegistryKey(key = "ast.loading.filter", value = "true")
class Angular2ControlFlowTest {
  private val projectFixture = jsProjectFixture()
  private val controlFlow by jsControlFlowFixture(
    projectFixture,
    JSFixtureOptions(Angular2TestUtil.getBaseTestDataPath() + "controlflow/"),
  ) { Angular2ControlFlowBuilder() }

  @Test
  fun testNonStrictMode(): Unit = doTest("<div <caret>*ngIf", skipTSConfig = true)

  @Test
  fun testLogicalExpression(): Unit = doTest("{{<caret>isString")

  @Test
  fun testConditionalExpression(): Unit = doTest("{{<caret>isString")

  @Test
  fun testIfDiscriminatedUnion(): Unit = doTest("<div <caret>*customIf")

  @Test
  fun testForInput(): Unit = doTest("<div <caret>*ngFor")

  @Test
  fun testForInputNgTemplate(): Unit = doTest("<caret>[ngFor")

  @Test
  fun testIfWithBinaryExpression(): Unit = doTest("<div <caret>*ngIf")

  @Test
  fun testIfLoadedTypeGuard(): Unit = doTest("<p <caret>*appIfLoaded")

  @Test
  fun testForLocalVariable(): Unit = doTest("<div <caret>*ngFor")

  @Test
  fun testNullChecks(): Unit = doTest("<div <caret>*ngFor")

  @Test
  fun testNullOrUndefinedComparison(): Unit = doTest("<div <caret>*ngIf")

  @Test
  fun testIfBlock(): Unit = doTest("@if <caret>(",
                                   Angular2TestModule.ANGULAR_CORE_17_3_0, Angular2TestModule.ANGULAR_COMMON_17_3_0)

  @Test
  fun testSwitchBlock(): Unit = doTest("@switch <caret>(",
                                       Angular2TestModule.ANGULAR_CORE_17_3_0, Angular2TestModule.ANGULAR_COMMON_17_3_0)

  @Test
  fun testIfAndSwitchBlocks(): Unit = doTest("@switch <caret>(",
                                             Angular2TestModule.ANGULAR_CORE_17_3_0, Angular2TestModule.ANGULAR_COMMON_17_3_0)

  private fun doTest(signature: String, vararg modules: Angular2TestModule) {
    doTest(signature, skipTSConfig = false, modules)
  }

  private fun doTest(
    signature: String, skipTSConfig: Boolean = false,
    modules: Array<out Angular2TestModule> = arrayOf(Angular2TestModule.ANGULAR_CORE_15_1_5,
                                                     Angular2TestModule.ANGULAR_COMMON_15_1_5),
  ) {
    val myFixture = controlFlow.fixture
    myFixture.configureDependencies(*modules)
    if (!skipTSConfig) {
      myFixture.configure(Angular2TsConfigFile())
    }
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())

    controlFlow.doCFTest(".ts") { file ->
      myFixture.checkHighlighting()
      runReadActionBlocking {
        val offset = Angular2TestUtil.findOffsetBySignature(signature, file)
        val element = InjectedLanguageManager.getInstance(controlFlow.project).findInjectedElementAt(file, offset)

        element!!.containingFile as JSControlFlowScope
      }
    }
  }

}
