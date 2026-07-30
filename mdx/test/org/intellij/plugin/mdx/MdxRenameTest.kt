package org.intellij.plugin.mdx

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.testFramework.TestDataPath
import org.junit.jupiter.api.Test

/**
 * Rename of a symbol inside an injected code fence must preserve the fence's indentation and must not corrupt
 * the host file.
 *
 * Regression for WEB-78468: renaming inside a fence nested in a JSX `<div>` triggered a postponed reformat
 * that dedented the fence to column 0 (the fence leaf returned `NoneIndent` and `MdxCodeFencePostFormatProcessor`,
 * which would re-indent it, does not run on postponed formatting). The dedent write-back also invalidated the
 * host `VirtualFile`, so an async JS import-graph pass threw `InvalidVirtualFileAccessException`. The fix gives
 * the fence its real nesting indent in `MdxFormattingModelBuilder.MdxBlock.getIndent`.
 */
@TestDataPath($$"$PROJECT_ROOT/contrib/mdx/testData/rename")
class MdxRenameTest : MdxTestBase() {

  private fun doTest(newName: String = "renamed") {
    myFixture.configureByFile("$testName.mdx")
    myFixture.renameElementAtCaret(newName)
    assertEquals(expectedText(), topLevelHostText())
  }

  private fun topLevelHostText(): String {
    return InjectedLanguageManager.getInstance(myFixture.project).getTopLevelFile(myFixture.file).text
  }

  private fun expectedText(): String {
    val virtualFile = myFixture.copyFileToProject("${testName}_after.mdx")
    val psiFile = myFixture.psiManager.findFile(virtualFile) ?: error("No PSI file for $virtualFile")
    return psiFile.text
  }

  @Test
  fun testRenameKeepsFenceIndentInSingleDiv() = doTest()

  @Test
  fun testRenameKeepsFenceIndentInNestedDivs() = doTest()
}
