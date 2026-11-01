package com.jetbrains.lang.makefile

import com.intellij.codeInsight.daemon.impl.IdentifierHighlighterPassFactory
import com.intellij.openapi.application.EDT
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightProjectFixture
import com.intellij.testFramework.EditorTestUtil
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.TestMethodOrder
import java.nio.file.Paths

/**
 * @see MakefileCodeBlockSupportHandler
 */
@TestApplication
@TestMethodOrder(MethodOrderer.MethodName::class)
class MakefileCodeBlockSupportHandlerTest {
  private val projectFixture = codeInsightProjectFixture()
  private val myFixture by codeInsightFixture(projectFixture)

  private lateinit var testName: String

  @BeforeEach
  fun before(testInfo: TestInfo) {
    testName = testInfo.testMethod.get().name
    myFixture.testDataPath = Paths.get(BASE_TEST_DATA_PATH).resolve("highlighting").resolve("codeBlocks").toString()
    myFixture.setReadEditorMarkupModel(true)
  }

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

  private fun doTest(): Unit = timeoutRunBlocking {
    withContext(Dispatchers.EDT) {
      IdentifierHighlighterPassFactory.doWithIdentifierHighlightingEnabled(myFixture.project) {
        myFixture.configureByFile("$testName.$DEFAULT_EXTENSION")
        EditorTestUtil.checkEditorHighlighting(myFixture,
                                               Paths.get(myFixture.testDataPath).resolve("$testName.result.$DEFAULT_EXTENSION").toString(),
                                               setOf("MATCHED_BRACE_ATTRIBUTES"))
      }
    }
  }

  private companion object {
    /**
     * @see MakefileFileType.getDefaultExtension
     */
    private const val DEFAULT_EXTENSION = "mk"
  }
}
