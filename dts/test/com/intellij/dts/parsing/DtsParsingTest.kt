package com.intellij.dts.parsing

import com.intellij.testFramework.ParsingTestCase
import com.intellij.dts.lang.parser.DtsParserDefinition

abstract class DtsParsingTest(dataPath: String) : ParsingTestCase(dataPath, "dts", DtsParserDefinition()) {
    override fun getTestDataPath(): String = "testData/parser"

    class Accept : DtsParsingTest("accept") {
        private fun doTest() = doTest(true, true)

        fun testDeleteNode() = doTest()

        fun testLabel() = doTest()

        fun testOmitNode() = doTest()
    }

    class Recovery : DtsParsingTest("recovery") {
        private fun doTest() = doTest(true, false)

        fun testInvalidEntryWithHandle() = doTest()

        fun testMemreserve() = doTest()

        fun testCompilerDirectiveLineBreak() = doTest()
    }

    class Reject : DtsParsingTest("reject") {
        private fun doTest() = doTest(true, false)

        fun testInvalidEntry() = doTest()

        fun testMissingSlashInPath() = doTest()
    }
}