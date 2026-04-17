package com.intellij.dts.documentation

class DtsBindingDocumentationTest : DtsDocumentationTest() {
  override fun getBasePath(): String = "documentation/binding"

  fun `test compatible espressif,esp32-eth`() = dtsTimeoutRunBlocking { doTest() }

  fun `test compatible espressif,esp32-pinctrl`() = dtsTimeoutRunBlocking { doTest() }

  fun `test compatible espressif,esp32-ledc`() = dtsTimeoutRunBlocking { doTest() }

  fun `test reference override`() = dtsTimeoutRunBlocking { doTest() }

  fun `test direct child`() = dtsTimeoutRunBlocking { doTest() }

  fun `test nested direct child`() = dtsTimeoutRunBlocking { doTest() }

  fun `test ref child`() = dtsTimeoutRunBlocking { doTest() }

  fun `test nested ref child`() = dtsTimeoutRunBlocking { doTest() }

  override suspend fun doTest() {
    addZephyr()
    super.doTest()
  }
}