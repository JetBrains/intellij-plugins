package com.intellij.dts.pp.test

import com.intellij.dts.pp.test.impl.TestParserDefinition
import com.intellij.testFramework.ParsingTestCase

class PpParserTest : ParsingTestCase("parser", "test", TestParserDefinition()) {
  override fun getTestDataPath(): String = PP_TEST_DATA_PATH

  override fun getTestName(lowercaseFirstLetter: Boolean): String = getPpTestName()

  fun `test define`() = doTest()

  fun `test ifdef active`() = doTest()

  fun `test ifdef inactive`() = doTest()

  fun `test incomplete ifdef`() = doTest()

  fun `test else active`() = doTest()

  fun `test else inactive`() = doTest()

  fun `test elifdef chain`() = doTest()

  fun `test elifdef active`() = doTest()

  fun doTest() = super.doTest(true, true)
}