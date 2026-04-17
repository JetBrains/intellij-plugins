package com.intellij.dts.documentation

class DtsZephyrDocumentationTest : DtsDocumentationTest() {
  override fun getBasePath(): String = "documentation/zephyr"

  fun `test property binding`() = dtsTimeoutRunBlocking { doTest() }

  fun `test property binding with ref`() = dtsTimeoutRunBlocking { doTest() }

  fun `test property default binding`() = dtsTimeoutRunBlocking { doTest() }

  fun `test property inherited binding`() = dtsTimeoutRunBlocking { doTest() }

  fun `test property default`() = dtsTimeoutRunBlocking { doTest() }

  fun `test node binding`() = dtsTimeoutRunBlocking { doTest() }

  fun `test node inherited binding`() = dtsTimeoutRunBlocking { doTest() }

  fun `test node reference`() = dtsTimeoutRunBlocking { doTest() }

  fun `test node reference override`() = dtsTimeoutRunBlocking { doTest() }

  fun `test node default`() = dtsTimeoutRunBlocking { doTest() }

  fun `test dts code`() = dtsTimeoutRunBlocking { doTest() }

  fun `test default value`() = dtsTimeoutRunBlocking { doTest() }

  fun `test const value`() = dtsTimeoutRunBlocking { doTest() }

  fun `test on spi bus`() = dtsTimeoutRunBlocking { doTest() }

  fun `test on i2c bus`() = dtsTimeoutRunBlocking { doTest() }

  override suspend fun doTest() {
    addZephyr()
    super.doTest()
  }
}