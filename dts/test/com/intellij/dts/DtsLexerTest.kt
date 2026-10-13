package com.intellij.dts

import com.intellij.dts.lang.lexer.DtsParserLexerAdapter
import com.intellij.openapi.util.text.StringUtil
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.lexerFixture
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.junit5.fixture.TestFixtures
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import kotlin.io.path.Path
import kotlin.io.path.readText

@TestFixtures
class DtsLexerTest {
  private val lexer by lexerFixture("$DTS_TEST_DATA_PATH/lexer", checkRestart = false) { DtsParserLexerAdapter() }

  private lateinit var testFilePath: String

  @BeforeEach
  fun setUp(testInfo: TestInfo) {
    testFilePath = "$DTS_TEST_DATA_PATH/lexer/${testInfo.testMethod.get().name.removePrefix("test")}"
  }

  @Test
  fun testCompilerDirectiveAfterWaitingValue(): Unit = doTest()

  private fun doTest() {
    val text = StringUtil.convertLineSeparators(Path("$testFilePath.dtsi").readText().trim { it <= ' ' })
    PlatformTestUtil.assertSameLinesWithFile("$testFilePath.txt", lexer.printTokens(text, 0))
  }
}