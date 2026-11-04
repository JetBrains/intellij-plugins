package com.jetbrains.lang.makefile

import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightProjectFixture
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.annotations.NonNls
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo

/**
 * @see MakefileFoldingBuilder
 */
@TestApplication
@TestDataPath($$"$PROJECT_ROOT/contrib/makefile/tests/testData")
class MakefileFoldingTest {
  private val projectFixture = codeInsightProjectFixture()
  private val myFixture by codeInsightFixture(projectFixture)

  private lateinit var testName: String

  @BeforeEach
  fun setUp(testInfo: TestInfo) {
    testName = PlatformTestUtil.getTestName(testInfo.testMethod.get().name, true)
  }

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
      myFixture.testFolding("${myFixture.testDataPath}/$basePath/$testName.mk")
    }
    catch (re: RuntimeException) {
      throw AssertionError(re.cause ?: re)
    }

  private val basePath: @NonNls String =
    "folding"
}
