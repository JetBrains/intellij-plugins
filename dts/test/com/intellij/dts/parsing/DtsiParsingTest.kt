package com.intellij.dts.parsing

abstract class DtsiParsingTest(dataPath: String) : DtsParsingTestBase(dataPath, "dtsi") {
    class Dtsi : DtsiParsingTest("dtsi") {
        fun testDeleteNode() = doTest(true, true)

        fun testDtsLike() = doTest(true, true)

        fun testMixedDtsLike() = doTest(true, false)

        fun testMixedNodeLike() = doTest(true, false)

        fun testNodeLike() = doTest(true, true)
    }

    class Accept : DtsiParsingTest("accept") {
        private fun doTest() = doTest(true, true)

        fun testByteArray() = doTest()

        fun testByteArrayValues() = doTest()

        fun testCellArray() = doTest()

        fun testCellArrayValues() = doTest()

        fun testExpr() = doTest()

        fun testMacroAsBits() = doTest()

        fun testMacroAsValue() = doTest()

        fun testMacroInByteArray() = doTest()

        fun testMacroInCellArray() = doTest()

        fun testMacroInExpr() = doTest()

        fun testMacroInMacro() = doTest()

        fun testMacroList() = doTest()

        fun testMacroWithParen() = doTest()

        fun testPropertyValues() = doTest()
    }

    class Recovery : DtsiParsingTest("recovery") {
        private fun doTest() = doTest(true, false)

        fun testBits() = doTest()

        fun testByteArray() = doTest()

        fun testCellArray() = doTest()

        fun testPHandle() = doTest()

        fun testProperty() = doTest()

        fun testPropertyLineBreak() = doTest()

        fun testSubNodeLineBreak() = doTest()

        fun testLift() = doTest()
    }

    class Reject : DtsiParsingTest("reject") {
        private fun doTest() = doTest(true, false)

        fun testCharInByteArray() = doTest()

        fun testLabelAfterBits() = doTest()

        fun testInvalidMacroNames() = doTest()
    }
}