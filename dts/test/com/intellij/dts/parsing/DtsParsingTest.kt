package com.intellij.dts.parsing

class DtsAcceptParsingTest : DtsParsingTestBase("accept") {
  override fun doTest(extension: String) = super.doTest(extension, ensureNoErrorElements = true)

  fun testDeleteNode() = doTest("dts")

  fun testLabel() = doTest("dts")

  fun testOmitNode() = doTest("dts")

  fun testInclude() = doTest("dts")

  fun testMultipleIncludes() = doTest("dts")

  fun testByteArray() = doTest("dtsi")

  fun testByteArrayValues() = doTest("dtsi")

  fun testCellArray() = doTest("dtsi")

  fun testCellArrayValues() = doTest("dtsi")

  fun testExpr() = doTest("dtsi")

  fun testMacroAsBits() = doTest("dtsi")

  fun testMacroAsValue() = doTest("dtsi")

  fun testMacroInByteArray() = doTest("dtsi")

  fun testMacroInCellArray() = doTest("dtsi")

  fun testMacroInExpr() = doTest("dtsi")

  fun testMacroInMacro() = doTest("dtsi")

  fun testMacroList() = doTest("dtsi")

  fun testMacroWithParen() = doTest("dtsi")

  fun testPropertyValues() = doTest("dtsi")
}

class DtsRecoveryParsingTest : DtsParsingTestBase("recovery") {
  override fun doTest(extension: String) = super.doTest(extension, ensureNoErrorElements = false)

  fun testInvalidEntryWithHandle() = doTest("dts")

  fun testMemreserve() = doTest("dts")

  fun testCompilerDirectiveLineBreak() = doTest("dts")

  fun testInclude() = doTest("dts")

  fun testInvalidEntryFollowedByInclude() = doTest("dts")

  fun testInvalidEntryFollowedByComment() = doTest("dts")

  fun testBits() = doTest("dtsi")

  fun testByteArray() = doTest("dtsi")

  fun testCellArray() = doTest("dtsi")

  fun testPHandle() = doTest("dtsi")

  fun testProperty() = doTest("dtsi")

  fun testPropertyLineBreak() = doTest("dtsi")

  fun testSubNodeLineBreak() = doTest("dtsi")

  fun testLift() = doTest("dtsi")

  fun testLiftWithContent() = doTest("dtsi")
}

class DtsRejectParsingTest : DtsParsingTestBase("reject") {
  override fun doTest(extension: String) = super.doTest(extension, ensureNoErrorElements = false)

  fun testInvalidEntry() = doTest("dts")

  fun testMissingSlashInPath() = doTest("dts")

  fun testCharInByteArray() = doTest("dtsi")

  fun testLabelAfterBits() = doTest("dtsi")

  fun testInvalidMacroNames() = doTest("dtsi")
}

class DtsIncludeParsingTest : DtsParsingTestBase("dtsi") {
  fun testDeleteNode() = doTest("dtsi", ensureNoErrorElements = true)

  fun testDtsLike() = doTest("dtsi", ensureNoErrorElements = true)

  fun testMixedDtsLike() = doTest("dtsi", ensureNoErrorElements = false)

  fun testMixedNodeLike() = doTest("dtsi", ensureNoErrorElements = false)

  fun testNodeLike() = doTest("dtsi", ensureNoErrorElements = true)
}
