package com.jetbrains.lang.makefile

import com.intellij.codeInsight.daemon.impl.IdentifierHighlighterPassFactory
import com.intellij.openapi.util.NlsSafe
import com.intellij.testFramework.EditorTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.runners.MethodSorters.NAME_ASCENDING
import java.nio.file.Paths

/**
 * @see MakefileCodeBlockSupportHandler
 */
@RunWith(JUnit4::class)
@FixMethodOrder(NAME_ASCENDING)
class MakefileCodeBlockSupportHandlerTest : BasePlatformTestCase() {
  @get:Rule
  val testName: TestName = TestName()

  @Before
  fun before() {
    myFixture.setReadEditorMarkupModel(true)
  }

  override fun getTestDataPath(): @NlsSafe String =
    Paths.get(BASE_TEST_DATA_PATH).resolve("highlighting").resolve("codeBlocks").toString()

  @Test
  fun ifEndif(): Unit =
    doTest()

  @Test
  fun ifElseEndif(): Unit =
    doTest()

  @Test
  fun ifElseifEndif(): Unit =
    doTest()

  @Test
  fun ifElseifElseEndif(): Unit =
    doTest()

  private fun doTest() {
    IdentifierHighlighterPassFactory.doWithHighlightingEnabled(project, testRootDisposable) {
      val testName = testName.methodName
      myFixture.configureByFile("$testName.$DEFAULT_EXTENSION")
      EditorTestUtil.checkEditorHighlighting(myFixture,
                                             Paths.get(testDataPath).resolve("$testName.result.$DEFAULT_EXTENSION").toString(),
                                             setOf("MATCHED_BRACE_ATTRIBUTES"))
    }
  }

  private companion object {
    /**
     * @see MakefileFileType.getDefaultExtension
     */
    private const val DEFAULT_EXTENSION = "mk"
  }
}
