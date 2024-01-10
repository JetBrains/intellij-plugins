// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.javascript.web.configure
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.controlflow.BaseJSControlFlowTest
import com.intellij.lang.javascript.psi.JSControlFlowScope
import com.intellij.lang.javascript.psi.controlflow.JSControlFlowBuilder
import org.angular2.Angular2TemplateInspectionsProvider
import org.angular2.Angular2TestModule
import org.angular2.Angular2TsConfigFile
import org.angular2.codeInsight.controlflow.Angular2ControlFlowBuilder
import org.angular2.Angular2TestUtil

class Angular2ControlFlowTest : BaseJSControlFlowTest() {
  override fun getTestDataPath(): String = Angular2TestUtil.getBaseTestDataPath() + "controlflow/"

  override fun setUp() {
    super.setUp()
    Angular2TestUtil.enableAstLoadingFilter(this)
  }

  fun testNonStrictMode() = doTest("<div <caret>*ngIf", skipTSConfig = true)

  fun testLogicalExpression() = doTest("{{<caret>isString")

  fun testConditionalExpression() = doTest("{{<caret>isString")

  fun testIfDiscriminatedUnion() = doTest("<div <caret>*customIf")

  fun testForInput() = doTest("<div <caret>*ngFor")

  fun testForInputNgTemplate() = doTest("<caret>[ngFor")

  fun testIfWithBinaryExpression() = doTest("<div <caret>*ngIf")

  fun testIfLoadedTypeGuard() = doTest("<p <caret>*appIfLoaded")

  fun testForLocalVariable() = doTest("<div <caret>*ngFor")

  fun testNullChecks() = doTest("<div <caret>*ngFor")

  fun testNullOrUndefinedComparison() = doTest("<div <caret>*ngIf")

  fun testIfBlock() = doTest("@if <caret>(",
                             Angular2TestModule.ANGULAR_CORE_17_1_0_NEXT_5, Angular2TestModule.ANGULAR_COMMON_17_1_0_NEXT_5)

  fun testSwitchBlock() = doTest("@switch <caret>(",
                                 Angular2TestModule.ANGULAR_CORE_17_1_0_NEXT_5, Angular2TestModule.ANGULAR_COMMON_17_1_0_NEXT_5)

  fun testIfAndSwitchBlocks() = doTest("@switch <caret>(",
                                       Angular2TestModule.ANGULAR_CORE_17_1_0_NEXT_5, Angular2TestModule.ANGULAR_COMMON_17_1_0_NEXT_5)

  override fun createJSControlFlowBuilder(): JSControlFlowBuilder {
    return Angular2ControlFlowBuilder()
  }

  private fun doTest(signature: String, vararg modules: Angular2TestModule) {
    doTest(signature, skipTSConfig = false, modules)
  }

  private fun doTest(signature: String, skipTSConfig: Boolean = false,
                     modules: Array<out Angular2TestModule> = arrayOf(Angular2TestModule.ANGULAR_CORE_15_1_5,
                                                                      Angular2TestModule.ANGULAR_COMMON_15_1_5)) {
    Angular2TestModule.configureCopy(myFixture, *modules)
    if (!skipTSConfig) {
      myFixture.configure(Angular2TsConfigFile())
    }
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())

    doCFTest(".ts") { file ->
      myFixture.checkHighlighting()
      val offset = Angular2TestUtil.findOffsetBySignature(signature, file)
      val element = InjectedLanguageManager.getInstance(project).findInjectedElementAt(file, offset)

      element!!.containingFile as JSControlFlowScope
    }
  }

}
