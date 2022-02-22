package com.jetbrains.lang.makefile

import com.intellij.codeInsight.daemon.impl.IdentifierHighlighterPassFactory
import com.intellij.openapi.util.NlsSafe
import com.intellij.testFramework.EditorTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Paths

/**
 * @see MakefileCodeBlockSupportHandler
 */
@RunWith(JUnit4::class)
class MakefileCodeBlockSupportHandlerTest : BasePlatformTestCase() {
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
      val testName = getTestName(true)
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
