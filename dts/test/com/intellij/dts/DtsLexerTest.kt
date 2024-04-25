package com.intellij.dts

import com.intellij.dts.lang.lexer.DtsParserLexerAdapter
import com.intellij.lexer.Lexer
import com.intellij.testFramework.LexerTestCase

class DtsLexerTest : LexerTestCase() {
  override fun createLexer(): Lexer = DtsParserLexerAdapter()

  override fun getDirPath(): String = "$DTS_TEST_DATA_PATH/lexer"

  override fun getPathToTestDataFile(extension: String): String {
    return "$dirPath/${getTestName(false)}$extension"
  }

  fun testCompilerDirectiveAfterWaitingValue() = doTest()

  private fun doTest() = doFileTest("dtsi")
}