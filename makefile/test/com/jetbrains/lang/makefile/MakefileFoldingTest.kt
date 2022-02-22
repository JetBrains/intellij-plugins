package com.jetbrains.lang.makefile

import com.intellij.testFramework.JUnit38AssumeSupportRunner
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.annotations.NonNls
import org.junit.Test
import org.junit.runner.RunWith

/**
 * @see MakefileFoldingBuilder
 */
@RunWith(JUnit38AssumeSupportRunner::class)
class MakefileFoldingTest : BasePlatformTestCase() {
  @Test
  fun testRule(): Unit =
    doTest()

  @Test
  fun testVariable(): Unit =
    doTest()

  @Test
  fun testDefine(): Unit =
    doTest()

  /**
   * See [7.2 Syntax of Conditionals](https://www.gnu.org/software/make/manual/html_node/Conditional-Syntax.html).
   */
  @Test
  fun testIfEndif(): Unit =
    doTest()

  /**
   * See [7.2 Syntax of Conditionals](https://www.gnu.org/software/make/manual/html_node/Conditional-Syntax.html).
   */
  @Test
  fun testIfElseEndif(): Unit =
    doTest()

  /**
   * See [7.2 Syntax of Conditionals](https://www.gnu.org/software/make/manual/html_node/Conditional-Syntax.html).
   */
  @Test
  fun testIfElseifEndif(): Unit =
    doTest()

  /**
   * See [7.2 Syntax of Conditionals](https://www.gnu.org/software/make/manual/html_node/Conditional-Syntax.html).
   */
  @Test
  fun testIfElseifElseEndif(): Unit =
    doTest()

  private fun doTest(): Unit =
    try {
      myFixture.testFolding("$testDataPath/$basePath/${getTestName(true)}.mk")
    }
    catch (re: RuntimeException) {
      throw AssertionError(re.cause ?: re)
    }

  override fun getTestDataPath(): @NonNls String =
    BASE_TEST_DATA_PATH

  override fun getBasePath(): @NonNls String =
    "folding"
}
