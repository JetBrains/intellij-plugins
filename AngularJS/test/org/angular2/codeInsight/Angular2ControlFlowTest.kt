// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.controlflow.BaseJSControlFlowTest
import com.intellij.lang.javascript.psi.JSControlFlowScope
import com.intellij.lang.javascript.psi.controlflow.JSControlFlowBuilder
import org.angular2.codeInsight.controlflow.Angular2ControlFlowBuilder
import org.angular2.inspections.Angular2TemplateInspectionsProvider
import org.angular2.modules.Angular2TestModule
import org.angularjs.AngularTestUtil

class Angular2ControlFlowTest : BaseJSControlFlowTest() {
  override fun getTestDataPath(): String = AngularTestUtil.getBaseTestDataPath(javaClass) + "controlflow/"

  override fun setUp() {
    super.setUp()
    AngularTestUtil.enableAstLoadingFilter(this)
  }

  fun testNonStrictMode() = doTest("<div <caret>*customIf", skipTSConfig = true)

  fun testIfDiscriminatedUnion() = doTest("<div <caret>*customIf")

  fun testIfLoadedTypeGuard() = doTest("<p <caret>*appIfLoaded")

  override fun createJSControlFlowBuilder(): JSControlFlowBuilder {
    return Angular2ControlFlowBuilder()
  }

  fun doTest(signature: String, skipTSConfig: Boolean = false) {
    Angular2TestModule.configureLink(myFixture, Angular2TestModule.ANGULAR_CORE_13_3_5, Angular2TestModule.ANGULAR_COMMON_13_3_5)
    if (!skipTSConfig) {
      myFixture.configureByFile("tsconfig.json")
    }
    myFixture.enableInspections(Angular2TemplateInspectionsProvider())

    doCFTest(".ts") { file ->
      myFixture.checkHighlighting()
      val offset = AngularTestUtil.findOffsetBySignature(signature, file)
      val element = InjectedLanguageManager.getInstance(project).findInjectedElementAt(file, offset)

      element!!.containingFile as JSControlFlowScope
    }
  }

}
