// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.TypeScriptTestUtil
import com.intellij.lang.javascript.controlflow.BaseJSControlFlowTest
import com.intellij.lang.javascript.psi.JSExecutionScope
import com.intellij.lang.javascript.psi.controlflow.JSControlFlowBuilder
import com.intellij.psi.xml.XmlFile
import org.jetbrains.vuejs.codeInsight.controlflow.VueControlFlowBuilder

class VueControlFlowTest : BaseJSControlFlowTest() {
  override fun getTestDataPath(): String = getVueTestDataPath() + "/controlflow/"

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(VueInspectionsProvider())
  }

  fun testConditionalExpression() = doTest()

  fun testIfElseIfElse() = doTest()

  fun testIfElseIfElseWithRef() = doTest()

  fun testIfElseIfElseWithCallExpressionGuard() = doTest()

  fun testFor() {
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    doTest()
  }

  fun testIfElseNullChecks() {
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    doTest()
  }

  fun testIfDiscriminator() = doTest()

  override fun createJSControlFlowBuilder(): JSControlFlowBuilder {
    return VueControlFlowBuilder()
  }

  fun doTest() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)

    doCFTest(".vue") { file ->
      myFixture.checkHighlighting()
      file as XmlFile

      file.document?.children?.find { it is JSExecutionScope } as JSExecutionScope?
    }
  }

}
