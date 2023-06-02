package com.intellij.dts

import com.intellij.dts.lang.lexer.DtsLexerAdapter
import com.intellij.lexer.Lexer
import com.intellij.testFramework.LexerTestCase

class DtsLexerTest : LexerTestCase() {
    override fun createLexer(): Lexer = DtsLexerAdapter()

    override fun getDirPath(): String = "testData/lexer"

    override fun getPathToTestDataFile(extension: String?): String {
        return "$dirPath/${getTestName(false)}$extension"
    }

    fun testCompilerDirectiveAfterWaitingValue() = doTest()

    private fun doTest() = doFileTest("dtsi")
}