package org.intellij.plugin.mdx

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.testFramework.TestDataPath
import org.junit.jupiter.api.Test

@TestDataPath($$"$PROJECT_ROOT/contrib/mdx/testData/format")
class MdxFormatterTest : MdxTestBase() {

    private fun doTest(before: String = testName, after: String = "${testName}_after") {
        myFixture.configureByFile("$before.mdx")
        reformat()
        myFixture.checkResultByFile("$after.mdx")
        reformat()
        myFixture.checkResultByFile("$after.mdx")
    }

    private fun reformat() {
        WriteCommandAction.runWriteCommandAction(myFixture.project) {
            val file = myFixture.file
            CodeStyleManager.getInstance(myFixture.project).reformatText(file, 0, file.textLength)
        }
    }

    private fun topLevelHost() = InjectedLanguageManager.getInstance(myFixture.project).getTopLevelFile(myFixture.file)

    private fun expectedText(): String {
        val virtualFile = myFixture.copyFileToProject("${testName}_after.mdx")
        val psiFile = myFixture.psiManager.findFile(virtualFile) ?: error("No PSI file for $virtualFile")
        return psiFile.text
    }

    @Test
    fun testFormatting() = doTest()

    /** Preservation floor: reformat keeps a YAML front matter block verbatim, incl. its TAB-indented multi-line value. WEB-64195. */
    @Test
    fun testFrontMatter() = doTest()

    /** Reformat aligns a GFM table inside a JSX component and keeps the JSX child indentation. WEB-59572. */
    @Test
    fun testGfmTable() = doTest()

    /** Reformat preserves reference-style link definitions. WEB-61406. */
    @Test
    fun testReferenceLinks() = doTest()

    /** Preservation floor: reformat keeps the two-space hard break and trims only insignificant trailing whitespace. IJPL-92308, IJPL-94690, IJPL-96478. */
    @Test
    fun testTrailingSpace() = doTest()

    /** Correct prose formatting: headings/blockquote/fenced code preserved, nested list indentation preserved. */
    @Test
    fun testMarkdownProse() = doTest()

    /** A fenced code block inside a JSX flow element (blank-line separated, as in the GfmTable fixture). */
    @Test
    fun testCodeFenceInsideJsxFlowElementIsIndented() = doTest()

    /** A fenced code block directly adjacent to the tags (no blank lines). */
    @Test
    fun testCodeFenceInsideJsxFlowElementTightIsIndented() = doTest()

    /**
     * A Markdown list nested in a JSX flow element: every item must align at the same indent.
     * Today the first paragraph/item picks up a partial indent from the foreign outer-element handling
     * while later items stay flush, so the list is misaligned.
     */
    @Test
    fun testListInsideJsxFlowElementIsIndentedConsistently() = doTest()

    /**
     * A new line inside a JSX flow element's body must indent to the body level instead of dropping to
     * column zero (e.g. Enter after a code fence). Exercised through adjustLineIndent, which the Enter
     * handler calls; getChildAttributes previously had a dead type check that always returned no indent. WEB-78468.
     */
    @Test
    fun testNewLineAfterCodeFenceInsideJsxIsIndented() {
        myFixture.configureByFile("$testName.mdx")
        WriteCommandAction.runWriteCommandAction(myFixture.project) {
            CodeStyleManager.getInstance(myFixture.project).adjustLineIndent(myFixture.file, myFixture.caretOffset)
        }
        myFixture.checkResultByFile("${testName}_after.mdx")
    }

    /**
     * Accepting a completion inside a JSX flow element must not strip or drift the indentation of a nested
     * code fence (the completion-triggered reformat used to corrupt it). WEB-78468.
     */
    @Test
    fun testCompletionInsideJsxFlowElementKeepsCodeFenceIndent() {
        myFixture.configureByFile("$testName.mdx")
        myFixture.completeBasic()
        myFixture.type('\n')
        myFixture.checkResultByFile("${testName}_after.mdx")
    }

    /**
     * Pressing Enter between braces inside an indented code fence expands the block: a body line one indent
     * step deeper than the opening line, then the closer on its own line (MdxCodeFenceEnterHandler). It must
     * also not corrupt the incremental lexer: the fence body is indented to +4 under `<div>`, and the pre-fix
     * MdxJsxScanner.skipCodeFence (≤3-space rule) failed to skip it on the re-lex triggered by the edit,
     * scanning `{`/`}` as JSX and throwing "Intersecting parsed nodes". After the scanner fix the host stays
     * well-formed. The caret lands in the injected TS fragment, so the host is read via the top-level file. WEB-78468.
     */
    @Test
    fun testEnterBetweenBracesInCodeFenceInsideJsxDoesNotCorruptLexer() {
        myFixture.configureByFile("$testName.mdx")
        myFixture.type("\n")
        assertEquals(expectedText(), topLevelHost().text)
    }

    /**
     * A tsx code fence nested in a JSX flow element is language-injected (the JS plugin's fence provider
     * resolves `tsx`), so JS/TS completion works inside it — here the `console` global. WEB-78468.
     */
    @Test
    fun testConsoleCompletesInsideTsxCodeFenceInsideJsxFlowElement() {
        myFixture.configureByFile("$testName.mdx")
        val items = myFixture.completeBasic()?.map { it.lookupString } ?: emptyList()
        assertTrue("`console` should complete inside a tsx fence nested in <div>, got $items", items.contains("console"))
    }

    /**
     * Accepting a JS/TS completion (`console`) inside an *indented* tsx fence nested in <div> must insert the
     * completion and leave the fence indentation intact. The fence body is injected with its leading indent, so
     * the platform's LookupUtil takes the whole `    conso` as the prefix and would replace it (indent and all)
     * with `console`, dropping the indent; MdxCodeFenceCompletionIndentRestorer re-indents the caret line back to
     * the fence base afterwards. WEB-78468.
     */
    @Test
    fun testConsoleCompletionInsideIndentedTsxCodeFenceKeepsIndent() {
      myFixture.configureByFile("$testName.mdx")
      selectCompletionItem("console")
      myFixture.checkResultByFile("${testName}_after.mdx")
    }
    /**
     * Enter inside an *indented* injected code fence must not corrupt the injected document. Without a
     * dedicated handler the platform's FormatterBasedIndentAdjuster runs on the injected fence fragment and
     * creates a RangeMarker in its DocumentWindow whose indent range has no valid host mapping, logging
     * "RangeMarkerWindow(invalid,..) is invalid immediately after creation" (which fails this test).
     * MdxCodeFenceEnterHandler edits the fence body on the host document and stops, so that adjuster never runs. WEB-78468.
     */
    @Test
    fun testEnterInsideIndentedInjectedCodeFenceKeepsInjectionValid() {
        myFixture.configureByFile("$testName.mdx")
        myFixture.type("\n")
        assertEquals(expectedText(), topLevelHost().viewProvider.document!!.text)
    }

    /**
     * As [testConsoleCompletionInsideIndentedTsxCodeFenceKeepsIndent] but for a keyword completion
     * (`function`), which reaches the same indent-stripping LookupUtil path. WEB-78468.
     */
    @Test
    fun testFunctionKeywordCompletionInsideIndentedTsxCodeFenceKeepsIndent() {
      myFixture.configureByFile("$testName.mdx")
      selectCompletionItem("function")
      assertEquals(expectedText(), topLevelHost().viewProvider.document!!.text)
    }

    @Test
    fun testReformatCodeFenceWithBracesInsideJsxIsIdempotent() = doTest()

    // An inline `{expression}` in prose must stay inline, not be exploded onto its own lines by the JS formatter.
    @Test
    fun testReformatKeepsInlineExpressionInline() {
        doTest(testName, testName)
    }

    // Nested JSX is indented one level per nesting and reformatting is idempotent (no drift to the right).
    @Test
    fun testReformatNestedJsxIsIdempotent() = doTest()
}
