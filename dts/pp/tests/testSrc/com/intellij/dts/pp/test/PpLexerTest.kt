package com.intellij.dts.pp.test

import com.intellij.dts.pp.test.impl.TestParserLexerAdapter
import com.intellij.openapi.util.text.StringUtil
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.lexerFixture
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.junit5.fixture.TestFixtures
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.readText

@TestFixtures
class PpLexerTest {
  private val lexer by lexerFixture(testDataPath.resolve("lexer").toString(), checkRestart = false) { TestParserLexerAdapter() }

  private lateinit var testFilePath: Path

  @BeforeEach
  fun setUp(testInfo: TestInfo) {
    testFilePath = testDataPath.resolve("lexer").resolve(getPpTestName(testInfo.testMethod.get().name))
  }

  @Test
  fun `test header q name`(): Unit = doTest()

  @Test
  fun `test header h name`(): Unit = doTest()

  @Test
  fun `test integer literals`(): Unit = doTest()

  @Test
  fun `test char literals`(): Unit = doTest()

  @Test
  fun `test char escapes`(): Unit = doTest()

  @Test
  fun `test float literals`(): Unit = doTest()

  @Test
  fun `test string literals`(): Unit = doTest()

  @Test
  fun `test operator punctuator`(): Unit = doTest()

  @Test
  fun `test restore state`(): Unit = doTest()

  private fun doTest() {
    val text = StringUtil.convertLineSeparators(Path("$testFilePath.test").readText().trim { it <= ' ' })
    PlatformTestUtil.assertSameLinesWithFile("$testFilePath.txt", lexer.printTokens(text, 0))
  }
}
