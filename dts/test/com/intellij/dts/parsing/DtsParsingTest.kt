package com.intellij.dts.parsing

import com.intellij.dts.completion.DtsBraceMatcher
import com.intellij.dts.lang.parser.DtsParserDefinition
import com.intellij.lang.LanguageBraceMatching
import com.intellij.testFramework.ParsingTestCase

abstract class DtsParsingTest(dataPath: String) : ParsingTestCase(dataPath, "dts", DtsParserDefinition()) {
    override fun getTestDataPath(): String = "testData/parser"

    override fun setUp() {
        super.setUp()

        // fixes issue when parser tests run before typing tests
        addExplicitExtension(LanguageBraceMatching.INSTANCE, myLanguage, DtsBraceMatcher())
    }

    class Accept : DtsParsingTest("accept") {
        private fun doTest() = doTest(true, true)

        fun testDeleteNode() = doTest()

        fun testLabel() = doTest()

        fun testOmitNode() = doTest()

        fun testInclude() = doTest()

        fun testMultipleIncludes() = doTest()
    }

    class Recovery : DtsParsingTest("recovery") {
        private fun doTest() = doTest(true, false)

        fun testInvalidEntryWithHandle() = doTest()

        fun testMemreserve() = doTest()

        fun testCompilerDirectiveLineBreak() = doTest()

        fun testInclude() = doTest()
    }

    class Reject : DtsParsingTest("reject") {
        private fun doTest() = doTest(true, false)

        fun testInvalidEntry() = doTest()

        fun testMissingSlashInPath() = doTest()
    }
}