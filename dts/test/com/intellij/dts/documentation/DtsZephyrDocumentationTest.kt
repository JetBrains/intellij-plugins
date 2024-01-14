package com.intellij.dts.documentation

class DtsZephyrDocumentationTest : DtsDocumentationTest() {
  override fun getBasePath(): String = "documentation/zephyr"

  override fun setUp() {
    super.setUp()
    addZephyr()
  }

  fun `test property binding`() = doTest()

  fun `test property binding with ref`() = doTest()

  fun `test property default binding`() = doTest()

  fun `test property inherited binding`() = doTest()

  fun `test property default`() = doTest()

  fun `test node binding`() = doTest()

  fun `test node inherited binding`() = doTest()

  fun `test node reference`() = doTest()

  fun `test node reference override`() = doTest()

  fun `test node default`() = doTest()

  fun `test dts code`() = doTest()

  fun `test default value`() = doTest()

  fun `test const value`() = doTest()

  fun `test on spi bus`() = doTest()

  fun `test on i2c bus`() = doTest()
}