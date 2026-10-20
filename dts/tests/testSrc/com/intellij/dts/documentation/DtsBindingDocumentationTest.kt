package com.intellij.dts.documentation

import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/contrib/dts/tests/testData/documentation/binding")
class DtsBindingDocumentationTest : DtsDocumentationTest(needsZephyr = true) {
  @Test
  fun `test compatible espressif,esp32-eth`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test compatible espressif,esp32-pinctrl`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test compatible espressif,esp32-ledc`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test reference override`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test direct child`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test nested direct child`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test ref child`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test nested ref child`() = dtsTimeoutRunBlocking { doTest() }
}