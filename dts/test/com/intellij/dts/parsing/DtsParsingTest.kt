package com.intellij.dts.parsing

abstract class DtsParsingTest(dataPath: String) : DtsParsingTestBase(dataPath, "dts") {
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