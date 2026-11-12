package com.intellij.deno.codeInsight

import com.intellij.deno.denoCodeInsightFixture
import com.intellij.lang.javascript.JSCompletionFixture
import com.intellij.lang.javascript.JSDaemonAnalyzerFixture
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.sharedCodeInsightProjectFixture
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestApplication
class DenoHighlightCompletionTest {
  companion object {
    private val projectFixture = sharedCodeInsightProjectFixture()
  }

  private val myFixture by denoCodeInsightFixture(projectFixture)

  @BeforeEach
  fun setUp() {
    val tools = JSDaemonAnalyzerFixture.configureDefaultLocalInspectionTools()
    myFixture.enableInspections(*tools.toTypedArray())
  }

  @Test
  fun testDenoGlobal() {
    myFixture.configureByText("foo.ts", "console.log(Deno)\n" +
                                        "console.log(<error>Deno1</error>)")
    myFixture.testHighlighting()
  }

  @Test
  fun testDenoGlobalCompletion() {
    myFixture.configureByText("foo.ts","De<caret>")
    val elements = myFixture.completeBasic()
    JSCompletionFixture.checkWeHaveInCompletion(elements, "Deno")
  }
}
