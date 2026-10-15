package com.intellij.dts.parsing

import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class DtsAcceptParsingTest : DtsParsingTestBase("accept") {
  private fun doTest(extension: String) = doTest(extension, ensureNoErrorElements = true)

  @Test
  fun testDeleteNode() = doTest("dts")

  @Test
  fun testLabel() = doTest("dts")

  @Test
  fun testOmitNode() = doTest("dts")

  @Test
  fun testInclude() = doTest("dts")

  @Test
  fun testMultipleIncludes() = doTest("dts")

  @Test
  fun testByteArray() = doTest("dtsi")

  @Test
  fun testByteArrayValues() = doTest("dtsi")

  @Test
  fun testCellArray() = doTest("dtsi")

  @Test
  fun testCellArrayValues() = doTest("dtsi")

  @Test
  fun testExpr() = doTest("dtsi")

  @Test
  fun testMacroAsBits() = doTest("dtsi")

  @Test
  fun testMacroAsValue() = doTest("dtsi")

  @Test
  fun testMacroInByteArray() = doTest("dtsi")

  @Test
  fun testMacroInCellArray() = doTest("dtsi")

  @Test
  fun testMacroInExpr() = doTest("dtsi")

  @Test
  fun testMacroInMacro() = doTest("dtsi")

  @Test
  fun testMacroList() = doTest("dtsi")

  @Test
  fun testMacroWithParen() = doTest("dtsi")

  @Test
  fun testPropertyValues() = doTest("dtsi")
}

@TestApplication
class DtsRecoveryParsingTest : DtsParsingTestBase("recovery") {
  private fun doTest(extension: String) = doTest(extension, ensureNoErrorElements = false)

  @Test
  fun testInvalidEntryWithHandle() = doTest("dts")

  @Test
  fun testMemreserve() = doTest("dts")

  @Test
  fun testCompilerDirectiveLineBreak() = doTest("dts")

  @Test
  fun testInclude() = doTest("dts")

  @Test
  fun testInvalidEntryFollowedByInclude() = doTest("dts")

  @Test
  fun testInvalidEntryFollowedByComment() = doTest("dts")

  @Test
  fun testBits() = doTest("dtsi")

  @Test
  fun testByteArray() = doTest("dtsi")

  @Test
  fun testCellArray() = doTest("dtsi")

  @Test
  fun testPHandle() = doTest("dtsi")

  @Test
  fun testProperty() = doTest("dtsi")

  @Test
  fun testPropertyLineBreak() = doTest("dtsi")

  @Test
  fun testSubNodeLineBreak() = doTest("dtsi")

  @Test
  fun testLift() = doTest("dtsi")

  @Test
  fun testLiftWithContent() = doTest("dtsi")
}

@TestApplication
class DtsRejectParsingTest : DtsParsingTestBase("reject") {
  private fun doTest(extension: String) = doTest(extension, ensureNoErrorElements = false)

  @Test
  fun testInvalidEntry() = doTest("dts")

  @Test
  fun testMissingSlashInPath() = doTest("dts")

  @Test
  fun testCharInByteArray() = doTest("dtsi")

  @Test
  fun testLabelAfterBits() = doTest("dtsi")

  @Test
  fun testInvalidMacroNames() = doTest("dtsi")
}

@TestApplication
class DtsIncludeParsingTest : DtsParsingTestBase("dtsi") {
  @Test
  fun testDeleteNode() = doTest("dtsi", ensureNoErrorElements = true)

  @Test
  fun testDtsLike() = doTest("dtsi", ensureNoErrorElements = true)

  @Test
  fun testMixedDtsLike() = doTest("dtsi", ensureNoErrorElements = false)

  @Test
  fun testMixedNodeLike() = doTest("dtsi", ensureNoErrorElements = false)

  @Test
  fun testNodeLike() = doTest("dtsi", ensureNoErrorElements = true)
}
