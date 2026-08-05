package org.intellij.plugin.mdx

import com.intellij.injected.editor.EditorWindow
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.testFramework.TestDataPath
import org.junit.jupiter.api.Test
import java.awt.datatransfer.StringSelection

@TestDataPath($$"$PROJECT_ROOT/contrib/mdx/testData/format")
class MdxFormatterTest : MdxTestBase() {

  /** Reformats `<before>.mdx` twice, checking both passes against `<after>.mdx` (proves idempotency). */
  private fun doTest(before: String = testName, after: String = "${testName}_after") {
    myFixture.configureByFile("$before.mdx")
    reformat()
    myFixture.checkResultByFile("$after.mdx")
    reformat()
    myFixture.checkResultByFile("$after.mdx")
  }

  private fun doTestEnterAutoFormatting() {
    myFixture.configureByFile("$testName.mdx")
    myFixture.type("\n")
    assertEquals(expectedText(), topLevelHost().text)
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

  /**
   * Compares the host *document*, not PSI: the caret may land in an injected fragment, so `myFixture.file` is not
   * the .mdx file, and the platform's own Enter path leaves PSI uncommitted.
   */
  private fun doActionTest(actionId: String) {
    myFixture.configureByFile("$testName.mdx")
    myFixture.performEditorAction(actionId)
    assertEquals(expectedText(), hostDocumentText())
  }

  private fun hostDocumentText(): String =
    ((myFixture.editor as? EditorWindow)?.delegate ?: myFixture.editor).document.text

  /** Pastes [clipboard] at the fixture's caret. Its body is deliberately indented by two, not the project's four. */
  private fun doPasteTest(clipboard: String = "const a = 1\nif (a) {\n  console.log(a)\n}") {
    myFixture.configureByFile("$testName.mdx")
    CopyPasteManager.getInstance().setContents(StringSelection(clipboard))
    myFixture.performEditorAction("EditorPaste")
    assertEquals(expectedText(), topLevelHost().text)
  }

  @Test
  fun testFormatting() = doTest()

  @Test
  fun testFormattingAfterIsIdempotent() = doTest("Formatting_after", "Formatting_after")

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

  /** Formatting an already-correctly-formatted document must be a no-op (idempotence). */
  @Test
  fun testIdempotent() = doTest("Idempotent_after", "Idempotent_after")

  /**
   * Enter right after a code fence nested in a JSX flow element indents the new line to the flow body
   * level, not one level deeper: [org.intellij.plugin.mdx.editor.MdxEnterHandler] inserts it at
   * the fence column, since the platform's default Enter routes through the XML formatter and over-indents.
   */
  @Test
  fun testEnterAfterCodeFenceInJsxFlowElementIndentsToBodyLevel() {
    myFixture.configureByFile("$testName.mdx")
    myFixture.performEditorAction("EditorEnter")
    myFixture.checkResultByFile("${testName}_after.mdx")
  }

  /** A fenced code block inside a JSX flow element (blank-line separated, as in the GfmTable fixture). */
  @Test
  fun testCodeFenceInsideJsxFlowElementIsIndented() = doTest()

  /** A fenced code block directly adjacent to the tags (no blank lines). */
  @Test
  fun testCodeFenceInsideJsxFlowElementTightIsIndented() = doTest()

  /** A Markdown list nested in a JSX flow element: every item aligns at the same indent. */
  @Test
  fun testListInsideJsxFlowElementIsIndentedConsistently() = doTest()

  /** An empty code fence must not gain a spurious blank body line. */
  @Test
  fun testEmptyCodeFenceInsideJsxFlowElementHasNoBlankLine() = doTest()

  /** A `<div>` wrapping a code fence is still indented one level as a real XmlTag, fence kept OUTER. */
  @Test
  fun testCodeFenceThenListInsideJsxFlowElementIndentsBody() = doTest()

  /** The already-indented form of the case above must reformat to itself (idempotent). */
  @Test
  fun testCodeFenceAndListInsideJsxFlowElementReformatIsIdempotent() = doTest(testName, testName)

  /** Reformatting a code fence nested in a JSX flow element must be idempotent (an atomic leaf block). */
  @Test
  fun testCodeFenceInsideJsxFlowElementReformatIsIdempotent() = doTest()

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
   * step deeper than the opening line, then the closer on its own line (MdxEnterHandler). It must
   * also not corrupt the incremental lexer: the fence body is indented to +4 under `<div>`, and the pre-fix
   * MdxJsxScanner.skipCodeFence (≤3-space rule) failed to skip it on the re-lex triggered by the edit,
   * scanning `{`/`}` as JSX and throwing "Intersecting parsed nodes". After the scanner fix the host stays
   * well-formed. The caret lands in the injected TS fragment, so the host is read via the top-level file. WEB-78468.
   */
  @Test
  fun testEnterBetweenBracesInCodeFenceInsideJsxDoesNotCorruptLexer() {
    doTestEnterAutoFormatting()
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
   * MdxEnterHandler edits the fence body on the host document and stops, so that adjuster never runs. WEB-78468.
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

  /**
   * Enter between a JSX element's tags inside an indented fence expands it like braces: a body line one step
   * deeper, the closing tag on its own line at the fence base (MdxEnterHandler runs before a
   * competing JS/XML delegate that would mis-indent it on the injected 0-based fragment).
   */
  @Test
  fun testEnterBetweenJsxTagsInCodeFenceInsideJsxIsIndented() {
    doTestEnterAutoFormatting()
  }

  /** As above for a component tag (`<Foo></Foo>`). */
  @Test
  fun testEnterBetweenComponentTagsInCodeFenceInsideJsxIsIndented() {
    doTestEnterAutoFormatting()
  }

  /** Enter right after a lone opening tag (no matching closer) keeps the new line at the tag's own indent. */
  @Test
  fun testEnterAfterOpeningJsxTagInCodeFenceInsideJsxIsIndented() {
    doTestEnterAutoFormatting()
  }

  /**
   * Enter between an empty JSX tag pair nested inside an ESM statement's function/expression body (e.g.
   * `export function f() { return <div></div> }`) indents the body line one step and keeps the closing tag
   * at the statement's own indent: MdxFormattingModelBuilder deliberately treats the whole MDX_ESM_BLOCK as
   * one opaque leaf (to leave import/export syntax untouched), so it has no structural indent info here and
   * the platform's default Enter handling would otherwise drop the closing tag to column 0. WEB-78468.
   */
  @Test
  fun testEnterBetweenJsxTagsInEsmBlockIsIndented() {
    doTestEnterAutoFormatting()
  }

  /**
   * Enter below a whitespace-only line of an indented fence must leave that line alone. The sandbox round trip
   * used to dedent it to nothing and re-indent it back as an *empty* line, so writing the body back changed text
   * above the caret; that host change spans whole CODE_FENCE_CONTENT lines and invalidates the shreds of the
   * injected DocumentWindow the caret lives in, collapsing its length below the offset `EnterHandler` snapshotted
   * before calling the delegate ("Wrong caret offset change by MdxEnterHandler"). Both fixtures hinge on
   * lines that are *only* whitespace — keep them intact. WEB-78468.
   */
  @Test
  fun testEnterBelowIndentedBlankLineInInjectedFenceKeepsInjectionValid() {
    doTestEnterAutoFormatting()
  }

  /**
   * As above, but the caret sits *on* a whitespace-only line indented less than the fence base. Re-indenting that
   * line to the base on write-back replaced it whole, which desynced the injected document from its PSI
   * ("After patch: doc: ... ---PSI: ...") — the same corruption, caught one step earlier. The line keeps its own
   * whitespace; only the line Enter opens gets the fence base. WEB-78468.
   */
  @Test
  fun testEnterOnUnderIndentedBlankLineInInjectedFenceKeepsInjectionValid() {
    doTestEnterAutoFormatting()
  }

  /**
   * Enter and Tab in a fence whose body has no code yet take the sandbox base from the fence's opening line, so
   * the line they open lands at that base (Enter) or one indent step past it (Tab), not at column zero. Since
   * every line of such a body is blank, its whitespace is the fence indentation rather than content and is
   * dedented for the sandbox; otherwise it would feed the sandbox indenter on top of the base. WEB-78468.
   */
  @Test
  fun testEnterInEmptyIndentedCodeFenceIndentsToFenceBase() = doActionTest("EditorEnter")

  @Test
  fun testTabInEmptyIndentedCodeFenceIndentsOneStepFromFenceBase() = doActionTest("EditorTab")

  /**
   * Enter inside a fence the sandbox cannot replay — no language in the info string, or a language with no
   * injection — must still carry the line's indentation over. Markdown's list indent provider answers with an
   * empty indent for any offset inside a code fence and is chosen for MDX by language alone, so the new line
   * used to land in column zero. WEB-78468.
   */
  @Test
  fun testEnterInFenceWithoutLanguageKeepsIndent() = doActionTest("EditorEnter")

  /** As above for a fence whose info string names a language nothing injects into. */
  @Test
  fun testEnterInFenceWithUninjectedLanguageKeepsIndent() = doActionTest("EditorEnter")

  /**
   * Enter on the fence's own opening line opens the first body line, which must land at the fence's indent. The
   * caret is not in the body, so the sandbox declines it and the same empty Markdown indent applied. WEB-78468.
   */
  @Test
  fun testEnterOnFenceOpeningLineIndentsFirstBodyLine() = doActionTest("EditorEnter")

  /** As above for an opening line with no info string at all. */
  @Test
  fun testEnterOnFenceOpeningLineWithoutInfoStringIndentsFirstBodyLine() = doActionTest("EditorEnter")

  /**
   * ```<caret>``` on one line is a single unterminated fence of six backticks; Enter splits it, and the
   * backticks carried onto the new line must keep the fence's indent instead of dropping to column zero.
   */
  @Test
  fun testEnterBetweenAdjacentFenceBackticksIndentsSplitLine() = doActionTest("EditorEnter")

  /** Tab inside a code fence indents one level from the fence base instead of to column zero. */
  @Test
  fun testTabInsideCodeFenceInsideJsxKeepsFenceIndent() {
    myFixture.configureByFile("$testName.mdx")
    myFixture.performEditorAction("EditorTab")
    assertEquals(expectedText(), topLevelHost().text)
  }

  /**
   * Pasting a multi-line block into an *indented* fence must land it at the fence base, formatted the way the
   * fence language would format it. The platform's paste indents each pasted line through `adjustLineIndent`,
   * which is a no-op inside a fence (MdxFormattingModelBuilder reports the fence as a leaf), so the clipboard
   * text used to be dropped in verbatim: every line after the first sat at its own absolute column, losing the
   * fence indent entirely. MdxCodeFencePasteHandler replays the paste in a sandbox instead. WEB-78468.
   */
  @Test
  fun testPasteIntoIndentedCodeFenceIsIndentedToFenceBase() = doPasteTest()

  /**
   * Same for a fence at column zero, where nothing is lost but the block is still not normalized: the pasted
   * body keeps the clipboard's own two-space indent instead of the project's indent size. WEB-78468.
   */
  @Test
  fun testPasteIntoTopLevelCodeFenceIsFormatted() = doPasteTest()

  /**
   * A fence whose body has no code yet has no indentation to infer the sandbox base from, so it must come from
   * the fence's own opening line — otherwise the replayed action's output is written back at column zero.
   */
  @Test
  fun testPasteIntoEmptyIndentedCodeFenceIsIndentedToFenceBase() = doPasteTest("const a = 2;")

  /**
   * A code fence whose body contains JSX (including a line-starting closing tag like `</Foo>`) must parse as
   * one fence and reformat without crashing, not have its `</Foo>` line mistaken for the flow element's own
   * closing tag.
   */
  @Test
  fun testReformatCodeFenceWithJsxContentInsideJsx() = doTest()

  /** A fence body indented less than its backticks must still be a code fence and reformat without crashing. */
  @Test
  fun testReformatLessIndentedCodeFenceBodyInsideJsx() = doTest()

  /** A less-indented body with mixed indentation is normalized to the fence base. */
  @Test
  fun testReformatMixedIndentCodeFenceBodyInsideJsx() = doTest()

  @Test
  fun testReformatCodeFenceWithBracesInsideJsxIsIdempotent() = doTest()

  // An inline `{expression}` in prose must stay inline, not be exploded onto its own lines by the JS formatter.
  @Test
  fun testReformatKeepsInlineExpressionInline() = doTest(testName, testName)

  // Nested JSX is indented one level per nesting and reformatting is idempotent (no drift to the right).
  @Test
  fun testReformatNestedJsxIsIdempotent() = doTest()

  /** A non-inline tag whose children are all inline (e.g. `<span>`) stays on one line, matching real TSX. */
  @Test
  fun testNonInlineTagWithOnlyInlineChildrenStaysCompact() = doTest(testName, testName)

  /** A non-inline sibling forces itself and the parent's closing tag onto their own lines; an inline sibling before it stays put. Matches real TSX (`JavaScriptFormatterTest`). */
  @Test
  fun testMixedInlineAndBlockChildrenWrapsClosingTag() = doTest()

  /**
   * JSX inside an ESM (import/export) statement's function body is fully formatter-aware, matching a real
   * .tsx file: MDX_ESM_BLOCK is no longer an opaque leaf, so a mis-indented `return` line inside
   * `export function` is reindented like any other JS/JSX content, while the import declaration itself is
   * left as-is (matching real TS/TSX's own import formatting).
   */
  @Test
  fun testEsmBlockJsxBodyIsReformatted() = doTest()

  /**
   * A plain ESM block with no JSX in it (including a pathological case of two statements glued together
   * with no separating whitespace) stays opaque and untouched, avoiding a formatter block-covering
   * assertion crash that reformatting it could trip (see MdxIntegrationTest#testOptimizeImports).
   */
  @Test
  fun testGluedEsmStatementsWithoutJsxStayOpaque() = doTest(testName, testName)


  /** Reformat inserts a blank line after front matter when the following content is flush against it. */
  @Test
  fun testFrontMatterInsertsBlankLineBeforeImport() = doTest()

  /** Reformat collapses multiple blank lines after front matter down to exactly one. */
  @Test
  fun testFrontMatterCollapsesExcessBlankLinesToOne() = doTest()

  /** An excessive gap after front matter must stabilize to one blank line and stay stable on reformat. */
  @Test
  fun testFrontMatterFollowedByBlankLinesAndJsxReformatIsIdempotent() = doTest()
}
