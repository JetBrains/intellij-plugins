package com.intellij.deno.codeInsight

import com.intellij.deno.DenoTestBase
import com.intellij.lang.javascript.JSCompletionFixture
import com.intellij.lang.javascript.JSDaemonAnalyzerFixture

class DenoHighlightCompletionTest : DenoTestBase() {

  override fun setUp() {
    super.setUp()
    val tools = JSDaemonAnalyzerFixture.configureDefaultLocalInspectionTools()
    myFixture.enableInspections(*tools.toTypedArray())
  }
  
  fun testDenoGlobal() {
    myFixture.configureByText("foo.ts", "console.log(Deno)\n" +
                                        "console.log(<error>Deno1</error>)")
    myFixture.testHighlighting()
  }
  
  fun testDenoGlobalCompletion() {
    myFixture.configureByText("foo.ts","De<caret>")
    val elements = myFixture.completeBasic()
    JSCompletionFixture.checkWeHaveInCompletion(elements, "Deno")
  }
}