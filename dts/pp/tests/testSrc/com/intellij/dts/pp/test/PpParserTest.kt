package com.intellij.dts.pp.test

import com.intellij.dts.pp.test.impl.TestParserDefinition
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.parsingFixture
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo

@TestApplication
class PpParserTest {
  companion object {
    private val projectFixture = projectFixture()
  }

  private val parsing by parsingFixture(projectFixture, "$PP_TEST_DATA_PATH/parser", "test", definitions = arrayOf(TestParserDefinition()))

  @BeforeEach
  fun setUp(testInfo: TestInfo) {
    parsing.testName = getPpTestName(testInfo.testMethod.get().name)
  }

  @Test
  fun `test define`(): Unit = doTest()

  @Test
  fun `test ifdef active`(): Unit = doTest()

  @Test
  fun `test ifdef inactive`(): Unit = doTest()

  @Test
  fun `test incomplete ifdef`(): Unit = doTest()

  @Test
  fun `test else active`(): Unit = doTest()

  @Test
  fun `test else inactive`(): Unit = doTest()

  @Test
  fun `test elifdef chain`(): Unit = doTest()

  @Test
  fun `test elifdef active`(): Unit = doTest()

  @Test
  fun `test if nested`(): Unit = doTest()

  private fun doTest() = parsing.doTest(true, true)
}