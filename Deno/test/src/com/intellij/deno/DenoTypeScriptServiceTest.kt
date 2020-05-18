package com.intellij.deno

import com.intellij.lang.javascript.typescript.service.TypeScriptServiceTestBase
import com.intellij.lang.javascript.typescript.service.TypeScriptServiceTestRunner
import org.junit.runner.RunWith

@RunWith(TypeScriptServiceTestRunner::class)
class DenoTypeScriptServiceTest : TypeScriptServiceTestBase() {
  var before = false

  override fun setUp() {
    super.setUp()
    val project = myFixture.project
    before = DenoSettings.getService(project).isUseDeno()
    DenoSettings.getService(project).setUseDeno(true)
  }

  override fun tearDown() {
    DenoSettings.getService(project).setUseDeno(before)
    super.tearDown()
  }

  fun testSimpleDeno() {
    myFixture.configureByText("foo.ts", "console.log(Deno)\n" +
                                        "console.log(<error>Deno1</error>)")
    checkHighlightingByOptions(false)
  }
}