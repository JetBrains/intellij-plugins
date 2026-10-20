package com.intellij.dts.documentation

import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/contrib/dts/tests/testData/documentation/zephyr")
class DtsZephyrDocumentationTest : DtsDocumentationTest(needsZephyr = true) {
  @Test
  fun `test property binding`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test property binding with ref`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test property default binding`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test property inherited binding`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test property default`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test node binding`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test node inherited binding`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test node reference`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test node reference override`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test node default`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test dts code`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test default value`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test const value`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test on spi bus`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test on i2c bus`() = dtsTimeoutRunBlocking { doTest() }
}