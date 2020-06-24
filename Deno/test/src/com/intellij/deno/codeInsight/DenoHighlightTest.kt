package com.intellij.deno.codeInsight

import com.intellij.deno.DenoTestBase
import com.intellij.lang.javascript.JSDaemonAnalyzerLightTestCase

class DenoHighlightTest : DenoTestBase() {

  override fun setUp() {
    super.setUp()
    val tools = JSDaemonAnalyzerLightTestCase.configureDefaultLocalInspectionTools()
    myFixture.enableInspections(*tools.toTypedArray())
  }
  
  fun testDenoGlobal() {
    myFixture.configureByText("foo.ts", "console.log(Deno)\n" +
                                        "console.log(<error>Deno1</error>)")
    myFixture.testHighlighting()
  }
}