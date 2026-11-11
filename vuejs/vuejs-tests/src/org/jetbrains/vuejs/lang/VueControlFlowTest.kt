// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.JSFixtureOptions
import com.intellij.lang.javascript.TypeScriptTestUtil
import com.intellij.lang.javascript.controlflow.JSControlFlowFixture.Companion.jsControlFlowFixture
import com.intellij.lang.javascript.jsProjectFixture
import com.intellij.lang.javascript.psi.JSExecutionScope
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.psi.xml.XmlFile
import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.vuejs.codeInsight.controlflow.VueControlFlowBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestApplication
class VueControlFlowTest {
  private val projectFixture = jsProjectFixture()
  private val controlFlow by jsControlFlowFixture(projectFixture, JSFixtureOptions(getVueTestDataPath() + "/controlflow/")) {
    VueControlFlowBuilder()
  }

  @BeforeEach
  fun setUp() {
    controlFlow.fixture.enableInspections(VueInspectionsProvider())
  }

  @Test
  fun testLogicalExpression(): Unit = doTest()

  @Test
  fun testConditionalExpression(): Unit = doTest()

  @Test
  fun testIfElseIfElse(): Unit = doTest()

  @Test
  fun testIfElseIfElseWithRef(): Unit = doTest()

  @Test
  fun testIfElseIfElseWithCallExpressionGuard(): Unit = doTest()

  @Test
  fun testFor() {
    TypeScriptTestUtil.setStrictNullChecks(controlFlow.project, controlFlow.testRootDisposable)
    doTest()
  }

  @Test
  fun testIfElseNullChecks() {
    TypeScriptTestUtil.setStrictNullChecks(controlFlow.project, controlFlow.testRootDisposable)
    doTest()
  }

  @Test
  fun testIfDiscriminator(): Unit = doTest()

  @Test
  fun testIfWithBinaryExpression(): Unit = doTest()

  fun doTest() {
    val myFixture = controlFlow.fixture
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)

    controlFlow.doCFTest(".vue") { file ->
      myFixture.checkHighlighting()
      file as XmlFile

      runReadActionBlocking { file.document?.children?.find { it is JSExecutionScope } as JSExecutionScope? }
    }
  }

}
