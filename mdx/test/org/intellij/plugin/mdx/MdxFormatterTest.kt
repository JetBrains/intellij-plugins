package org.intellij.plugin.mdx

import com.intellij.application.options.CodeStyle
import com.intellij.injected.editor.EditorWindow
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.xml.HtmlCodeStyleSettings
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.TestDataPath
import org.intellij.plugin.mdx.js.MdxJSLanguage
import org.intellij.plugin.mdx.lang.MdxLanguage
import org.intellij.plugin.mdx.lang.parse.MdxElementTypes
import org.intellij.plugins.markdown.lang.MarkdownElementTypes
import org.intellij.plugins.markdown.lang.MarkdownLanguage
import org.intellij.plugins.markdown.lang.formatter.settings.MarkdownCustomCodeStyleSettings
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownBlockQuote
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownList
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownListItem
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

  private fun reformat() {
    WriteCommandAction.runWriteCommandAction(myFixture.project) {
      val file = myFixture.file
      CodeStyleManager.getInstance(myFixture.project).reformatText(file, 0, file.textLength)
    }
  }

  private fun withMarkdownReflowSettings(wrapTextIfLong: Boolean = true, action: () -> Unit) {
    CodeStyle.doWithTemporarySettings(myFixture.project, CodeStyle.getSettings(myFixture.project)) { settings ->
      configureMarkdownReflow(settings, wrapTextIfLong)
      action()
    }
  }

  private fun configureMarkdownReflow(settings: CodeStyleSettings, wrapTextIfLong: Boolean) {
    settings.getCommonSettings(MarkdownLanguage.INSTANCE).RIGHT_MARGIN = 20
    settings.getCommonSettings(MdxLanguage).RIGHT_MARGIN = 20
    settings.getCustomSettings(MarkdownCustomCodeStyleSettings::class.java).apply {
      WRAP_TEXT_IF_LONG = wrapTextIfLong
      KEEP_LINE_BREAKS_INSIDE_TEXT_BLOCKS = false
    }
  }

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
    assertEquals(expectedText(), hostDocumentText())
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

  @Test
  fun testMarkdownReflowMatchesMarkdown() = withMarkdownReflowSettings {
    val source = "One two three four five six seven eight nine ten eleven twelve thirteen."

    myFixture.configureByText("sample.md", source)
    reformat()
    val markdownResult = myFixture.file.text

    myFixture.configureByText("sample.mdx", source)
    reformat()
    assertEquals(markdownResult, myFixture.file.text)
    reformat()
    assertEquals(markdownResult, myFixture.file.text)
  }

  @Test
  fun testMarkdownReflowRespectsWrapTextSetting() = withMarkdownReflowSettings(wrapTextIfLong = false) {
    val source = "One two three four five six seven eight nine ten eleven twelve thirteen."
    myFixture.configureByText("sample.mdx", source)

    reformat()

    assertEquals(source, myFixture.file.text)
  }

  /** Separate paragraphs in a blockquote must not be folded into one line. */
  @Test
  fun testBlockquoteWithMultipleParagraphsIsNotFolded() = doTest()

  @Test
  fun testBlockquoteInsideJsxKeepsIndent() {
    checkBlockquoteFormatting("<div>\n    > first\n</div>\n")
  }

  @Test
  fun testBlockquoteInsideJsxMovesWithBodyIndent() {
    for (indent in listOf("", "  ", "    ", "        ", "\t")) {
      checkBlockquoteFormatting(
        "<div>\n$indent> first\n$indent> second\n</div>\n",
        "<div>\n    > first\n    > second\n</div>\n",
      )
    }
  }

  @Test
  fun testBlockquoteInsideJsxKeepsNestedMarkdown() {
    checkBlockquoteFormatting(
      """
        <div>
          > first **bold** {value} and `code`
          >
          > > nested
          > >
          > > - outer
          > >   - inner
          >
          > last <Badge />
        </div>
      """.trimIndent(),
      """
        <div>
            > first **bold** {value} and `code`
            >
            > > nested
            > >
            > > - outer
            > >   - inner
            >
            > last <Badge />
        </div>
      """.trimIndent(),
    )
  }

  @Test
  fun testBlockquoteInsideNestedJsxUsesBodyIndent() {
    checkBlockquoteFormatting(
      "<div>\n<section>\n> first\n> second\n</section>\n</div>\n",
      "<div>\n    <section>\n        > first\n        > second\n    </section>\n</div>\n",
    )
  }

  @Test
  fun testBlockquoteInsideFragmentUsesBodyIndent() {
    checkBlockquoteFormatting("<>\n> first\n> second\n</>\n", "<>\n    > first\n    > second\n</>\n")
  }

  @Test
  fun testBlockquoteInsideListKeepsRelativeIndent() {
    checkBlockquoteFormatting(
      "<div>\n- outer\n\n  > first\n  > second\n- sibling\n</div>\n",
      "<div>\n    - outer\n\n      > first\n      > second\n    - sibling\n</div>\n",
    )
  }

  @Test
  fun testReformatSelectedBlockquoteInsideJsxUsesBodyIndent() {
    checkBlockquoteFormatting(
      "<div>\n<selection>  > first\n  > second</selection>\n</div>\n",
      "<div>\n    > first\n    > second\n</div>\n",
    )
  }

  @Test
  fun testReformatSelectedBlockquoteWithoutLeadingWhitespaceUsesBodyIndent() {
    checkBlockquoteFormatting(
      "<div>\n  <selection>> first\n  > second</selection>\n</div>\n",
      "<div>\n    > first\n    > second\n</div>\n",
    )
  }

  @Test
  fun testReformatSelectedBlockquoteKeepsUnselectedListIndent() {
    for (indent in listOf("", "  ", "      ", "\t")) {
      val quoteIndent = " ".repeat(if (indent == "\t") 6 else indent.length + 2)
      checkBlockquoteFormatting(
        "<div>\n$indent- outer\n\n<selection>$indent  > first\n$indent  > second</selection>\n$indent- sibling\n</div>\n",
        "<div>\n$indent- outer\n\n$quoteIndent> first\n$quoteIndent> second\n$indent- sibling\n</div>\n",
      )
    }
  }

  @Test
  fun testBlockquoteInsideJsxUsesJavaScriptIndent() {
    for (indentSize in listOf(2, 4)) {
      CodeStyle.doWithTemporarySettings(myFixture.project, CodeStyle.getSettings(myFixture.project)) { settings ->
        settings.getCommonSettings(MdxJSLanguage.INSTANCE).indentOptions!!.apply {
          INDENT_SIZE = indentSize
          USE_TAB_CHARACTER = false
        }
        val indent = " ".repeat(indentSize)
        checkBlockquoteFormatting(
          "<div>\n        > first\n        > second\n</div>\n",
          "<div>\n$indent> first\n$indent> second\n</div>\n",
        )
      }
    }
  }

  @Test
  fun testBlockquoteInsideJsxRespectsTagsWithoutChildIndent() {
    CodeStyle.doWithTemporarySettings(myFixture.project, CodeStyle.getSettings(myFixture.project)) { settings ->
      settings.getCustomSettings(HtmlCodeStyleSettings::class.java).HTML_DO_NOT_INDENT_CHILDREN_OF = "div"
      checkBlockquoteFormatting("<div>\n    > first\n    > second\n</div>\n", "<div>\n> first\n> second\n</div>\n")
    }
  }

  @Test
  fun testBlockquoteOutsideJsxKeepsContent() {
    checkBlockquoteFormatting("> first\n>\n> > nested\n> > - outer\n> >   - inner\n>\n> last\n")
    checkBlockquoteFormatting("- outer\n\n  > first\n  > second\n- sibling\n")
  }

  private fun checkBlockquoteFormatting(source: String, expected: String? = null) {
    myFixture.configureByText("quote.mdx", source)
    val expectedText = expected ?: myFixture.file.text
    val selected = myFixture.editor.selectionModel.hasSelection()
    val structure = blockquoteStructure()
    assertTrue(structure.any { it.first() === MarkdownElementTypes.BLOCK_QUOTE })
    repeat(2) {
      if (selected) {
        assertTrue(myFixture.editor.selectionModel.hasSelection())
        myFixture.performEditorAction("ReformatCode")
      }
      else {
        reformat()
      }
      assertEquals("Markdown ancestry changed after formatting:\n$source", structure, blockquoteStructure())
      assertEquals(expectedText, myFixture.file.text)
    }
  }

  private fun blockquoteStructure(): List<List<IElementType>> {
    val containers = PsiTreeUtil.collectElements(myFixture.file) { it is MarkdownBlockQuote || it is MarkdownListItem }
    return containers.map { element ->
      generateSequence(element) { it.parent }
        .filter {
          it is MarkdownBlockQuote || it is MarkdownList || it is MarkdownListItem ||
          it.node?.elementType === MdxElementTypes.MDX_JSX_FLOW_ELEMENT
        }
        .map { it.node.elementType }
        .toList()
    }
  }

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

  @Test
  fun testNestedListInsideJsxKeepsNesting() {
    checkListFormatting("<div>\n    - outer\n        - inner\n</div>\n")
  }

  @Test
  fun testReformatSelectedListInsideJsxKeepsIndent() {
    checkListSelectionFormatting("<div>\n<selection>    - outer\n        - inner</selection>\n</div>\n")
  }

  @Test
  fun testReformatSelectedNestedListInsideJsxKeepsIndent() {
    checkListSelectionFormatting("<div>\n    - outer\n<selection>        - inner</selection>\n</div>\n")
  }

  @Test
  fun testReformatSelectedNestedListWithoutLeadingWhitespaceKeepsIndent() {
    checkListSelectionFormatting("<div>\n    - outer\n        <selection>- inner</selection>\n</div>\n")
  }

  @Test
  fun testReformatSelectedListInsideNestedJsxUsesBodyIndent() {
    checkListSelectionFormatting(
      "<div>\n    <section>\n<selection>  - outer\n      - inner</selection>\n    </section>\n</div>\n",
      "<div>\n    <section>\n        - outer\n            - inner\n    </section>\n</div>\n",
    )
  }

  @Test
  fun testReformatSelectedNestedListKeepsUnselectedParentIndent() {
    for (indentSize in listOf(2, 4)) {
      CodeStyle.doWithTemporarySettings(myFixture.project, CodeStyle.getSettings(myFixture.project)) { settings ->
        settings.getCommonSettings(MdxJSLanguage.INSTANCE).indentOptions!!.INDENT_SIZE = indentSize
        for (indent in listOf("", "  ", "      ")) {
          checkListSelectionFormatting("<div>\n$indent- outer\n<selection>$indent    - inner</selection>\n</div>\n")
        }
      }
    }
  }

  @Test
  fun testReformatSelectedNestedItemsKeepsEachRelativeIndent() {
    checkListSelectionFormatting("""
      <div>
          - outer
              - before
              <selection>- selected
                  10. child
              - after</selection>
              - outside
          - sibling
      </div>
    """.trimIndent())
  }

  @Test
  fun testReformatSelectedJsxInsideListKeepsNesting() {
    checkListSelectionFormatting("""
      <div>
          - outer

            <selection><section>
                - inner
                  - child
            </section></selection>
          - sibling
      </div>
    """.trimIndent())
  }

  @Test
  fun testReformatSelectedNestedListKeepsUnselectedTabs() {
    checkListSelectionFormatting(
      "<div>\n\t- outer\n<selection>\t\t- inner</selection>\n</div>\n",
      "<div>\n\t- outer\n        - inner\n</div>\n",
    )
  }

  @Test
  fun testReformatSelectedNestedListFormatsInlineMdx() {
    checkListSelectionFormatting(
      "<div>\n    - outer {value}\n<selection>        - inner <Badge /> and `code`</selection>\n    - sibling <Badge />\n</div>\n",
      "<div>\n    - outer {value}\n        - inner <Badge/> and `code`\n    - sibling <Badge />\n</div>\n",
    )
  }

  @Test
  fun testReformatSelectedNestedListKeepsContinuationIndent() {
    checkListSelectionFormatting("""
      <div>
          - outer
              <selection>- inner
                continuation

                paragraph</selection>
          - sibling
      </div>
    """.trimIndent())
  }

  @Test
  fun testNestedListsInsideJsxMoveWithBodyIndent() {
    for (marker in listOf("-", "10.", "- [ ]")) {
      val nestedIndent = if (marker == "10.") "    " else "  "
      for (indent in listOf("", "  ", "    ", "      ", "\t")) {
        checkListFormatting(
          "<div>\n$indent$marker one\n$indent$nestedIndent- nested\n$indent$marker two\n</div>\n",
          "<div>\n    $marker one\n    $nestedIndent- nested\n    $marker two\n</div>\n",
        )
      }
    }
  }

  @Test
  fun testNestedListsInsideJsxKeepEachRelativeIndent() {
    checkListFormatting(
      "<div>\n- one\n  - two\n      - three\n  - after two\n- after one\n</div>\n",
      "<div>\n    - one\n      - two\n          - three\n      - after two\n    - after one\n</div>\n",
    )
  }

  @Test
  fun testNestedListsInsideJsxRespectTabStops() {
    checkListFormatting(
      "<div>\n  - one\n  \t- nested\n  - two\n</div>\n",
      "<div>\n    - one\n      - nested\n    - two\n</div>\n",
    )
    checkListFormatting(
      "<div>\n\t- one\n\t\t- nested\n\t- two\n</div>\n",
      "<div>\n    - one\n        - nested\n    - two\n</div>\n",
    )
  }

  @Test
  fun testNestedListsInsideNestedJsxKeepNesting() {
    checkListFormatting(
      "<div>\n<section>\n- outer\n    - inner\n</section>\n</div>\n",
      "<div>\n    <section>\n        - outer\n            - inner\n    </section>\n</div>\n",
    )
  }

  @Test
  fun testJsxInsideListStartsNewListIndentContext() {
    checkListFormatting(
      "<div>\n- outer\n\n  <section>\n  - inner\n    - child\n  </section>\n- sibling\n</div>\n",
      "<div>\n    - outer\n\n      <section>\n          - inner\n            - child\n      </section>\n    - sibling\n</div>\n",
    )
  }

  @Test
  fun testNestedListInsideJsxUsesJavaScriptIndent() {
    for (indentSize in listOf(2, 4)) {
      CodeStyle.doWithTemporarySettings(myFixture.project, CodeStyle.getSettings(myFixture.project)) { settings ->
        settings.getCommonSettings(MdxJSLanguage.INSTANCE).indentOptions!!.apply {
          INDENT_SIZE = indentSize
          USE_TAB_CHARACTER = false
        }
        val indent = " ".repeat(indentSize)
        checkListFormatting(
          "<div>\n    - outer\n        - inner\n</div>\n",
          "<div>\n$indent- outer\n$indent    - inner\n</div>\n",
        )
      }
    }
  }

  @Test
  fun testNestedListWithInlineMdxKeepsContent() {
    checkListFormatting(
      "<div>\n- outer **bold** {value}\n    - inner <Badge /> and `code`\n- sibling\n</div>\n",
      "<div>\n    - outer **bold** {value}\n        - inner <Badge/> and `code`\n    - sibling\n</div>\n",
    )
  }

  private fun checkListFormatting(source: String, expected: String = source) {
    myFixture.configureByText("list.mdx", source)
    val structure = listStructure()
    assertTrue(structure.isNotEmpty())
    repeat(2) {
      reformat()
      assertEquals("List ancestry changed after formatting:\n$source", structure, listStructure())
      assertEquals(expected, myFixture.file.text)
    }
  }

  private fun checkListSelectionFormatting(source: String, expected: String? = null) {
    myFixture.configureByText("list.mdx", source)
    val expectedText = expected ?: myFixture.file.text
    val structure = listStructure()
    assertTrue(structure.isNotEmpty())
    repeat(2) {
      assertTrue(myFixture.editor.selectionModel.hasSelection())
      myFixture.performEditorAction("ReformatCode")
      assertEquals("List ancestry changed after formatting:\n$source", structure, listStructure())
      assertEquals(expectedText, myFixture.file.text)
    }
  }

  private fun listStructure(): List<List<IElementType>> {
    return PsiTreeUtil.collectElementsOfType(myFixture.file, MarkdownListItem::class.java).map { item ->
      generateSequence(item.parent) { it.parent }
        .filterIsInstance<MarkdownList>()
        .map { it.node.elementType }
        .toList()
    }
  }

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
   * the platform's LookupUtil used to take the whole `    conso` as the prefix and replace it, indent and all,
   * with `console` — MDX carried its own listener to put the indent back. The platform no longer drops it, so
   * this only guards against the regression coming back. WEB-78468.
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
    assertEquals(expectedText(), hostDocumentText())
  }

  /**
   * As [testConsoleCompletionInsideIndentedTsxCodeFenceKeepsIndent] but for a keyword completion
   * (`function`), which reaches the same indent-stripping LookupUtil path. WEB-78468.
   */
  @Test
  fun testFunctionKeywordCompletionInsideIndentedTsxCodeFenceKeepsIndent() {
    myFixture.configureByFile("$testName.mdx")
    selectCompletionItem("function")
    assertEquals(expectedText(), hostDocumentText())
  }

  /**
   * Enter and Tab in a fence whose body has no code yet take the sandbox prefix from the fence's opening line, so
   * the line they open lands at that prefix (Enter) or one indent step past it (Tab), not at column zero. Since no
   * line of such a body carries code, their prefixes are the fence's own rather than content: they are stripped for
   * the sandbox — otherwise they would feed its indenter on top of the prefix — and restored on the way back, which
   * is what keeps a blockquoted fence's markers on the lines Enter leaves behind. WEB-78468.
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
   * Enter on a blank line that already carries the fence's indentation as literal whitespace, inside a fence
   * without a language, must not double it. Running the language-less sandbox directly on that indentation
   * (rather than a dedented copy that needs it re-added afterward) leaves nothing to double. WEB-78468.
   */
  @Test
  fun testEnterOnPreIndentedBlankLineInFenceWithoutLanguageKeepsIndent() = doActionTest("EditorEnter")

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
  fun testTabInsideCodeFenceInsideJsxKeepsFenceIndent() = doActionTest("EditorTab")

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
   * Pasting into a fence with no language (nothing to inject, so no language formatter to reflow it) must still
   * add the fence base to every pasted line, via the sandbox's plain-text fallback. Before that fallback
   * existed, `adjustLineIndent` no-op'd for this fence body the same way it does for a real one, so only the
   * first pasted line landed at the fence base and every line after it kept its own absolute column.
   */
  @Test
  fun testPasteIntoOpaqueCodeFenceIsIndentedToFenceBase() = doPasteTest()


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


  /**
   * What a code fence line carries in front of its code is not necessarily spaces: a fence inside a blockquote
   * carries a `>` marker on every one of its lines, and indentation may be tabs. The sandbox round trip has to
   * strip that whole prefix on the way in and put it back on the way out, including on the line the replayed
   * action opens — otherwise the new line falls out of the quote, or the tabs turn into spaces. WEB-78468.
   */
  @Test
  fun testEnterInsideBlockquotedCodeFenceKeepsQuoteMarker() = doActionTest("EditorEnter")

  /** A body with no code in it yet takes the prefix from the fence's opening line, markers included. */

  /** Reformat keeps the markers of a blockquoted fence and formats the code between them. */
  @Test
  fun testReformatBlockquotedCodeFenceKeepsQuoteMarker() = doTest()

  /** Indentation may be tabs, and a tab is not four spaces to be swapped in on the way back. */
  @Test
  fun testEnterInsideTabIndentedCodeFenceKeepsTabs() = doActionTest("EditorEnter")

  /** As above for an ordered item, whose marker is wider. */

  /**
   * The bullet's stand-in has to be exactly as wide as the bullet: a paste puts every line back at the fence
   * indent, so a stand-in one character short drops each pasted line out of the list item.
   */
  @Test
  fun testPasteIntoCodeFenceInListItemIsIndentedToItemBody() = doPasteTest()

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
