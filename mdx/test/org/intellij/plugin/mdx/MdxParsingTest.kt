package org.intellij.plugin.mdx

import com.intellij.openapi.util.text.StringUtil
import com.intellij.testFramework.ParsingTestCase
import com.intellij.testFramework.TestDataPath
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestDataPath($$"$PROJECT_ROOT/contrib/mdx/testData/parsing")
class MdxParsingTest : MdxTestBase() {

  @BeforeEach fun enableParsingTestInspections() = enableJsxInspections()

  /**
   * Parses `parsing/<testName>.mdx` with the REAL MDX language on the shared test application (like
   * every other MDX test), then compares every view-provider root against the checked-in
   * `<testName>.MDX.txt` / `<testName>.MdxJS.txt` snapshots. The snapshot format (whitespace + ranges,
   * one file per PSI root) is produced by reusing [ParsingTestCase.doCheckResult]. Running on the real
   * application — instead of the old mock-application [ParsingTestCase] — is what lets this class share
   * a JVM with the heavy code-insight tests without the parser tree changing under it.
   */
  private fun doTest() {
    val virtualFile = myFixture.copyFileToProject("$testName.mdx", "$testName-raw.mdx")
    val psiFile = myFixture.psiManager.findFile(virtualFile) ?: error("No PSI file for $virtualFile")
    val text = StringUtil.convertLineSeparators(psiFile.text).trim()
    myFixture.configureByText("$testName.mdx", text)
    ParsingTestCase.doCheckResult(testDataPath, myFixture.file, true, testName, false, true)
  }

  @Test
  fun testParsingTestData() {
    doTest()
  }

  @Test
  fun testParsingList() {
    doTest()
  }

  @Test
  fun testParsingWithNewLines() {
    doTest()
  }

  /**
   * Broad scanner scenario (restored from WEB-78496): ESM `import`/`export`, an inline
   * MDX_JSX_TEXT_ELEMENT carrying inline Markdown (`**new**`), an inline `{expression}` with nested
   * JSX, a fragment `<>...</>` with a heading and a nested `<Panel>` flow element whose attributes
   * (`title`, `data`) and Markdown body are structured, and an unterminated `<Broken attr={...`.
   */
  @Test
  fun testParsingJsxScenarios() {
    doTest()
  }

  /**
   * Markdown children of a flow JSX element (restored from WEB-78496): inline Markdown on the same
   * line as the opening `<Panel>` tag, the same wrapping onto a continuation line, and Markdown
   * indented under the opening tag — all nest as Markdown inside the MDX_JSX_FLOW_ELEMENT.
   */
  @Test
  fun testParsingFlowJsxMarkdown() {
    doTest()
  }

  /**
   * Snapshot of a JSX `<Alert>...</Alert>` flow component (NOT the GFM `> [!NOTE]` admonition — see
   * testParsingGfmAlert for that). Captures current behavior.
   */
  @Test
  fun testParsingAlert() {
    doTest()
  }

  @Test
  fun testParsingEmbedded() {
    doTest()
  }

  @Test
  fun testParsingPrisma() {
    doTest()
  }

  @Test
  fun testParsingLongText() {
    doTest()
  }

  // --- WEB-78468 (MDX redesign) coverage: problematic examples from the linked issues ---

  /**
   * Trailing whitespace after inline flow content (`<div>dqwd` then a continuation line with trailing
   * spaces) used to crash the block parser with "Intersecting parsed nodes".
   */
  @Test
  fun testParsingFlowContentTrailingSpace() {
    doTest()
  }

  /**
   * A half-typed closing tag (`</div`, no `>` yet) followed by a code fence used to crash the block
   * parser with "Intersecting parsed nodes" when the opening-line paragraph spanned into the fence.
   */
  @Test
  fun testParsingIncompleteClosingBeforeFence() {
    doTest()
  }

  /**
   * WEB-59041 regression anchor: fenced code inside a JSX element. The scanner keeps the fenced code
   * opaque inside a structured `<div>` flow element (no stray XmlTag for the `<...>` in the code).
   */
  @Test
  fun testParsingCodeBlockInJsx() {
    doTest()
  }

  /**
   * WEB-56756 regression net: a top-level fenced ```tsx``` block with generics/`{}`. The fence is
   * standalone Markdown (the JSX scanner never touches a top-level fence), so the generics and `{}`
   * stay opaque inside CODE_FENCE_CONTENT. Spec-correct.
   */
  @Test
  fun testParsingTsxCodeBlock() {
    doTest()
  }

  /**
   * WEB-47755 regression anchor: generic type params (`Bar<Baz>`) in a fenced code block nested in
   * JSX. The `Bar<Baz>` stays inside the opaque code node (no separate XmlTag) and the ESM imports
   * plus nested JSX flow elements are structured. See testGenericsInCodeBlockHaveNoErrors below.
   */
  @Test
  fun testParsingGenericsInCodeBlock() {
    doTest()
  }

  /**
   * WEB-74507 regression anchor: `<` inside a template-literal expression attribute (`code={`...`}`).
   * The scanner keeps it as a structured MDX_JSX_ATTRIBUTE / expression (the `<` is not split into a
   * stray XmlTag/error). See testExpressionAttributeHasNoErrors below.
   */
  @Test
  fun testParsingJsxExpressionAttribute() {
    doTest()
  }

  /** Markdown blocks (heading, list) nested inside a JSX flow element. */
  @Test
  fun testParsingMarkdownInJsx() {
    doTest()
  }

  /** Inline JSX text element with inline Markdown on the same line. */
  @Test
  fun testParsingInlineJsx() {
    doTest()
  }

  /** Flow, inline and comment `{expressions}`. */
  @Test
  fun testParsingExpressions() {
    doTest()
  }

  /** ESM `import`/`export` interleaved with JSX content. */
  @Test
  fun testParsingEsm() {
    doTest()
  }

  /** ESM is only valid at document root, not in Markdown list or blockquote content. */
  @Test
  fun testEsmInMarkdownContainersIsText() {
    doTest()
  }

  /** Nested JSX flow components. */
  @Test
  fun testParsingNestedComponents() {
    doTest()
  }

  /** Inline expression comment `{/* ... */}` and inline expression `{1 + 1}` inside a paragraph. */
  @Test
  fun testParsingInlineExpressionComment() {
    doTest()
  }

  /** Inline JSX component `<Component />` mixed inside a text paragraph. */
  @Test
  fun testParsingJsxInMarkdownInline() {
    doTest()
  }

  /** Multi-line ESM `import` and multi-line `export` object literal. */
  @Test
  fun testParsingMultilineEsm() {
    doTest()
  }

  /**
   * Four-space indented lines. MDX disables indented code blocks (WEB-78496 fixup), so the parser no
   * longer produces a Markdown CODE_BLOCK — the lines parse as a paragraph. Spec-correct.
   */
  @Test
  fun testParsingIndentedCode() {
    doTest()
  }

  /**
   * `<https://example.com>` autolink. MDX disables autolinks (oracle = parseError), but the IntelliJ
   * parser does not flag it as an error — this snapshot captures that current non-spec behavior. The
   * strict invalid-MDX target is testParsingHtmlComment / testHtmlCommentIsInvalidMdx below.
   */
  @Test
  fun testParsingAutolink() {
    doTest()
  }

  /** Empty fence and unknown-language fence. */
  @Test
  fun testParsingEmptyAndUnknownFence() {
    doTest()
  }

  // --- WEB-78468 coverage-gap group D: current-behavior regression anchors for the redesign ---

  /**
   * WEB-78468 group D regression anchor: inline JSX (`<Icon />`, `<Logo />`) embedded inside
   * Markdown inline emphasis and inside a link label. Snapshot captures CURRENT parser behavior.
   */
  @Test
  fun testParsingInlineJsxInEmphasis() {
    doTest()
  }

  /**
   * WEB-78468 group D regression anchor: a JSX flow element (`<Note>...</Note>`) inside a Markdown
   * blockquote, followed by plain text. Snapshot captures CURRENT parser behavior.
   */
  @Test
  fun testParsingJsxInBlockquote() {
    doTest()
  }

  /**
   * WEB-78468 group D regression anchor: JSX flow elements as Markdown list items, including a
   * nested sub-list item. Snapshot captures CURRENT parser behavior.
   */
  @Test
  fun testParsingJsxInListItem() {
    doTest()
  }

  /**
   * WEB-78468 group D regression anchor: Markdown children (heading, list) indented under a JSX
   * flow component — a common MDX formatting style. Snapshot captures CURRENT parser behavior.
   */
  @Test
  fun testParsingMarkdownInJsxIndented() {
    doTest()
  }

  /**
   * WEB-78468 group D regression anchor: a JSX fragment (`<>...</>`) with a text child.
   * Snapshot captures CURRENT parser behavior.
   */
  @Test
  fun testParsingFragments() {
    doTest()
  }

  /**
   * WEB-78468 group D regression anchor: backslash escapes `\<` and `\{` that must NOT start a tag
   * or expression. Snapshot captures CURRENT parser behavior.
   */
  @Test
  fun testParsingEscapes() {
    doTest()
  }

  /**
   * WEB-78468 group D regression anchor: an HTML comment `<!-- ... -->`. Invalid MDX (oracle =
   * parseError), but the IntelliJ parser does not flag it as an error — this snapshot captures that
   * current non-spec behavior. See testHtmlCommentIsInvalidMdx in the oracle section below.
   */
  @Test
  fun testParsingHtmlComment() {
    doTest()
  }

  /**
   * WEB-78468 group D regression anchor: operators inside JSX expression attributes — `{1 < 2}`
   * and a generic call `{useMemo<Foo>(() => x)}`. Snapshot captures CURRENT parser behavior.
   */
  @Test
  fun testParsingOperatorInAttribute() {
    doTest()
  }

  /**
   * WEB-78468 group D regression anchor: nested JSX elements inside an attribute array value
   * (`tabs={[<FileWithIcon ... />, ...]}`), the real ParsingPrisma pattern. Snapshot captures
   * CURRENT parser behavior.
   */
  @Test
  fun testParsingJsxInAttributeArray() {
    doTest()
  }

  @Test
  fun testParsingFrontMatter() {
    doTest()
  }

  // --- WEB-78468 coverage-gap group E: oracle-cross-checked fixtures (correctness in
  //     MdxRedesignTargetTest). Snapshots below capture CURRENT behavior as a regression net. ---

  /** TOML `+++`...`+++` front matter. Spec-correct (FRONT_MATTER_HEADER, oracle = `toml` node). */
  @Test
  fun testParsingTomlFrontMatter() {
    doTest()
  }

  /** GFM table with alignment. Spec-correct (oracle = `table` align[left,center,right]). */
  @Test
  fun testParsingGfmTable() {
    doTest()
  }

  /** GFM strikethrough `~~...~~`. Spec-correct (oracle = `delete` node). */
  @Test
  fun testParsingStrikethrough() {
    doTest()
  }

  /**
   * GFM task list (unchecked / checked checkboxes). Snapshot = CURRENT behavior (the checkbox markers
   * are read as text / reference link); oracle = two `listItem`s with `checked` — the redesign target.
   */
  @Test
  fun testParsingTaskList() {
    doTest()
  }

  /**
   * GitHub `> [!NOTE]` alert. Snapshot = CURRENT behavior (`[!NOTE]` as reference link); oracle = a
   * plain blockquote with literal `[!NOTE]` text — the redesign target.
   */
  @Test
  fun testParsingGfmAlert() {
    doTest()
  }

  /**
   * WEB-57359 regression anchor: a line that STARTS with `import`/`export` (oracle = parseError; only
   * mid-prose use stays text). Snapshot captures the CURRENT mis-parse into ESM with errors.
   */
  @Test
  fun testParsingImportInProse() {
    doTest()
  }

  /** Empty `{}` expression in prose. Snapshot = CURRENT (`{}` text); oracle = empty `mdxTextExpression`. */
  @Test
  fun testParsingEmptyExpression() {
    doTest()
  }

  /**
   * Flow expression with JSX `{a < b ? <Yes/> : <No/>}`. The scanner keeps the whole `{...}` as a
   * single flow expression node (the `<` is not split into a stray TEXT/HTML_TAG), matching the
   * oracle `mdxFlowExpression`. See testFlowExpressionWithJsx below.
   */
  @Test
  fun testParsingFlowExpressionWithJsx() {
    doTest()
  }

  /** Nested fence (````md` wrapping ```js`). Spec-correct (oracle = one opaque `code` node). */
  @Test
  fun testParsingNestedFence() {
    doTest()
  }

  /**
   * CRLF line endings. The trailing `<Note>flow</Note>` line is assembled into one
   * MDX_JSX_FLOW_ELEMENT (opening tag + `flow` paragraph + closing tag), matching the oracle's
   * `paragraph -> mdxJsxTextElement name=Note` (one element whose child is the text `flow`); the
   * heading and paragraph parse the same under CRLF as LF.
   */
  @Test
  fun testParsingCrlf() {
    doTest()
  }

  /** Whitespace-only file. Spec-correct (oracle = empty root). */
  @Test
  fun testParsingWhitespaceOnly() {
    doTest()
  }

  /** Member-expression component `<Foo.Bar/>`. Spec-correct (oracle = `mdxJsxFlowElement` name Foo.Bar). */
  @Test
  fun testParsingMemberExpressionComponent() {
    doTest()
  }

  /** `export default` / `export { X as default }`. Spec-correct (oracle = two `mdxjsEsm` + heading). */
  @Test
  fun testParsingExportDefault() {
    doTest()
  }

  /**
   * WEB-49952 regression anchor: JSX `<img/>` 3-space indented under an ordered-list item. Snapshot =
   * CURRENT (opaque JSX_BLOCK); oracle = the `img` flow element nested in the first `listItem`.
   */
  @Test
  fun testParsingJsxInOrderedList() {
    doTest()
  }

  /**
   * WEB-75114 regression anchor: a multi-line JSX attribute (`options={[1, 2]}` across lines). The
   * `<Answer>` parses into one MDX_JSX_FLOW_ELEMENT with a structured MDX_JSX_ATTRIBUTE (`options`)
   * and the body "text" as a Markdown paragraph child, matching the oracle. See testMultilineJsxAttributeChildIsMarkdown below.
   */
  @Test
  fun testParsingMultilineJsxAttribute() {
    doTest()
  }

}
