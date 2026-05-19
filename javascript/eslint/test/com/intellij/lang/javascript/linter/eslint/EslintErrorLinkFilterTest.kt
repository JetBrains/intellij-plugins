package com.intellij.lang.javascript.linter.eslint

import com.intellij.execution.filters.FileHyperlinkRawData
import com.intellij.lang.javascript.linter.eslint.filter.EslintErrorFilter
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class EslintErrorLinkFilterTest : BasePlatformTestCase() {
  private lateinit var filter: EslintErrorFilter

  @Throws(Exception::class)
  public override fun setUp() {
    super.setUp()
    filter = EslintErrorFilter(project, null)
  }

  fun testMisc() {
    doNegativeTest("")
    doNegativeTest(" ")
  }

  fun testMultilineWithAbsolutePath() {
    doNegativeTest("> eslint .")
    doNegativeTest("")
    doNegativeTest("/home/user/link-examples/eslint/file.js")
    doNegativeTest(" ")
    doNegativeTest("  ")
    doNegativeTest("   1:1   ")
    doPositiveTest("   1:1   error    Definition for rule 'my-rule-no-capital' was not found         my-rule-no-capital",
                   "/home/user/link-examples/eslint/file.js", 1, 1, 3, 14)
    doPositiveTest("  11:25  error    'foo' is not defined                                           no-undef",
                   "/home/user/link-examples/eslint/file.js", 11, 25, 2, 14)
    doNegativeTest("a")
    //doNegativeTest("b");
    doNegativeTest("   1:1   error    Definition for rule 'my-rule-no-capital' was not found         my-rule-no-capital")
    doNegativeTest("/home/user/link-examples/eslint/file2.js")
    doPositiveTest("  32:5   warning  'f' is assigned a value but never used                         no-unused-vars",
                   "/home/user/link-examples/eslint/file2.js", 32, 5, 2, 16)
  }

  fun testMultilineWithRelativePath() {
    doNegativeTest("Compiled with warnings.")
    doNegativeTest("src/App.tsx")
    doNegativeTest("")
    doPositiveTest("  Line 5:7:  i is assigned a value but never used  @typescript-eslint/no-unused-vars", "src/App.tsx", 5, 7, 2, 10)
    doPositiveTest("  Line 15:1:  j is assigned a value but never used  @typescript-eslint/no-unused-vars", "src/App.tsx", 15, 1, 2, 11)
  }

  private fun doPositiveTest(line: String,
                             expectedPath: String,
                             expectedOneBasedLine: Int,
                             expectedOneBasedColumn: Int,
                             hyperlinkStartInd: Int,
                             hyperlinkEndInd: Int) {
    val expected = FileHyperlinkRawData(expectedPath,
                                        expectedOneBasedLine - 1,
                                        expectedOneBasedColumn - 1,
                                        hyperlinkStartInd,
                                        hyperlinkEndInd)
    var actual: List<FileHyperlinkRawData?> = filter.parse(line)
    assertEquals(listOf(expected), actual)
    if (!line.endsWith("\n")) {
      actual = filter.parse(line + "\n")
      assertEquals(listOf(expected), actual)
    }
  }

  private fun doNegativeTest(line: String) {
    assertEquals(emptyList<Any>(), filter.parse(line))
  }
}