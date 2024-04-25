package com.intellij.dts.pp.test

import com.intellij.dts.pp.test.impl.TestParserLexerAdapter
import com.intellij.lexer.Lexer
import com.intellij.testFramework.LexerTestCase

class PpLexerTest : LexerTestCase() {
  override fun createLexer(): Lexer = TestParserLexerAdapter()

  override fun getDirPath(): String = "$PP_TEST_DATA_PATH/lexer"

  override fun getPathToTestDataFile(extension: String): String = "$dirPath/${getPpTestName()}$extension"

  fun `test header q name`() = doTest()

  fun `test header h name`() = doTest()

  fun `test integer literals`() = doTest()

  fun `test char literals`() = doTest()

  fun `test char escapes`() = doTest()

  fun `test float literals`() = doTest()

  fun `test string literals`() = doTest()

  fun `test operator punctuator`() = doTest()

  private fun doTest() = doFileTest("test")
}