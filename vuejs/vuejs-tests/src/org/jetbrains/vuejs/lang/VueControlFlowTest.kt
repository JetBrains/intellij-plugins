// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.codeInsight.controlflow.ControlFlowBuilder
import com.intellij.lang.javascript.controlflow.BaseJSControlFlowTest
import com.intellij.lang.javascript.psi.JSExecutionScope
import com.intellij.lang.javascript.psi.controlflow.JSControlFlowService
import com.intellij.psi.xml.XmlFile
import org.jetbrains.vuejs.codeInsight.controlflow.VueControlFlowBuilder

class VueControlFlowTest : BaseJSControlFlowTest() {
  override fun getTestDataPath(): String = getVueTestDataPath() + "/controlflow/"

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(VueInspectionsProvider())
  }

  fun testIfElseIfElse() = doTest()

  fun testIfElseIfElseWithRef() = doTest()

  fun testIfElseIfElseWithCallExpressionGuard() = doTest()

  fun testFor() = doTest()

  override fun getControlFlowBuilder(scope: JSExecutionScope): ControlFlowBuilder {
    return VueControlFlowBuilder(scope).getControlFlowBuilder()
  }

  fun doTest() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)

    doCFTest(".vue") { file ->
      myFixture.checkHighlighting()
      file as XmlFile

      file.document?.children?.find(JSControlFlowService::isControlFlowScope) as JSExecutionScope?
    }
  }

}
