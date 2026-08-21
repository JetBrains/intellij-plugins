package org.intellij.plugin.mdx

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.editorActions.CompletionAutoPopupHandler
import com.intellij.codeInsight.template.impl.TemplateManagerImpl
import com.intellij.openapi.application.impl.NonBlockingReadActionImpl
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.LanguageLineWrapPositionStrategy
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.TestModeFlags
import com.intellij.testFramework.assertNoErrorLogged
import org.intellij.plugin.mdx.completion.MdxXmlAutoPopupEnabler
import org.intellij.plugin.mdx.lang.MdxLanguage
import org.intellij.plugins.markdown.lang.supportsMarkdown
import org.junit.jupiter.api.Test

/**
 * LIVE editor feature tests for MDX:
 *  - JSX tag-name auto-popup on typing `<` (and extending a `<My` prefix);
 *  - auto-closing JSX/HTML tags (and the `<>` fragment) on typing `>`.
 *
 * Both features bridge the typed-handler dispatch (which sees the base Markdown/MDX file) to the JSX
 * PSI living in the projected MdxJS layer. WEB-78468.
 */
@TestDataPath($$"$PROJECT_ROOT/contrib/mdx/testData/liveEditing")
class MdxLiveEditingTest : MdxTestBase() {

  // -------------------------------------------------------------------------
  // FEATURE 1: JSX tag-name auto-popup on `<`
  //
  // The autopopup is scheduled by the platform XmlAutoPopupHandler only when an XmlAutoPopupEnabler
  // approves the context. The two layers are tested separately: MdxXmlAutoPopupEnabler gating
  // directly (deterministic), plus end-to-end lookup contents through the async autopopup and the
  // synchronous completion.
  // -------------------------------------------------------------------------

  private fun typeAndPumpAutoPopup(ch: Char) {
    TestModeFlags.set(CompletionAutoPopupHandler.ourTestingAutopopup, true, testRootDisposable)
    myFixture.type(ch)
    // The autopopup is scheduled on the keystroke and runs asynchronously (CommittingDocuments ->
    // non-blocking read action -> show-lookup on the EDT). Cycle event-pumping and NBRA-draining
    // until the lookup appears.
    repeat(10) {
      PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
      NonBlockingReadActionImpl.waitForAsyncTaskCompletion()
      PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
      if (myFixture.lookup != null) return
    }
  }

  /** Mirror the platform contract: the enabler is consulted on committed PSI. */
  private fun enablerApproves(): Boolean {
    PsiDocumentManager.getInstance(myFixture.project).commitAllDocuments()
    return MdxXmlAutoPopupEnabler().shouldShowPopup(myFixture.file, myFixture.caretOffset)
  }

  private fun assertEnablerApproves(typeKeys: String) {
    myFixture.configureByText("test.mdx", "<caret>")
    myFixture.type(typeKeys)
    assertTrue(enablerApproves())
  }

  private fun assertEnablerRejects(typeKeys: String = "") {
    myFixture.configureByFile("$testName.mdx")
    if (typeKeys.isNotEmpty()) myFixture.type(typeKeys)
    assertFalse(enablerApproves())
  }

  private fun checkTyping(completionChar: Char) {
    myFixture.configureByFile("$testName.mdx")
    myFixture.type(completionChar.toString())
    myFixture.checkResultByFile("${testName}_after.mdx")
  }

  private fun checkAutoWrapOnTyping(text: String) {
    CodeStyle.doWithTemporarySettings(myFixture.project, CodeStyle.getSettings(myFixture.project)) { settings ->
      settings.WRAP_WHEN_TYPING_REACHES_RIGHT_MARGIN = true
      settings.getCommonSettings(MdxLanguage).RIGHT_MARGIN = 80
      myFixture.configureByFile("$testName.mdx")
      myFixture.type(text)
      myFixture.checkResultByFile("${testName}_after.mdx")
    }
  }

  @Test
  fun testEnablerApprovesLessThan() {
    assertEnablerApproves("<")
  }

  @Test
  fun testEnablerApprovesTagNamePrefix() {
    assertEnablerApproves("<My")
  }

  @Test
  fun testEnablerRejectsInsideFencedCodeBlock() {
    assertEnablerRejects("<")
  }

  @Test
  fun testEnablerRejectsInsideFrontMatter() {
    assertEnablerRejects("<")
  }

  @Test
  fun testEnablerRejectsNonTagContext() {
    // No '<' before the caret: not a tag-start context.
    assertEnablerRejects()
  }

  @Test
  fun testTagNameAutoPopupFiresInJsxContext() {
    // End-to-end: typing a JSX tag-name character in a `<…` context makes the enabler-gated
    // XmlAutoPopupHandler schedule the completion auto-popup (a lookup appears). The *contents* of
    // that lookup are asserted deterministically by testTagNameCompletionAtUnbalancedPrefix, because
    // driving the async completion to completion from this @RunInEdt fixture is not reliable.
    myFixture.configureByFile("MyComponent.mdx")
    myFixture.configureByFile("$testName.mdx")
    typeAndPumpAutoPopup('y')
    assertNotNull("Expected an auto-popup lookup after typing a JSX tag-name character", myFixture.lookup)
  }

  // -------------------------------------------------------------------------
  // FEATURE 2: auto-close JSX/HTML tags on `>`
  // -------------------------------------------------------------------------

  @Test
  fun testAutoCloseHtmlTag() = checkTyping('>')

  @Test
  fun testAutoCloseCustomComponentTag() = checkTyping('>')

  @Test
  fun testAutoCloseFragment() = checkTyping('>')

  @Test
  fun testAutoCloseFragmentNestedInTag() = checkTyping('>')

  @Test
  fun testAutoCloseFragmentNestedInBraces() = checkTyping('>')

  @Test
  fun testAutoCloseDoesNotAbsorbAdjacentTrailingText() {
    // The tag-name scan must stop at the caret, not absorb adjacent trailing text with no separator.
    checkTyping('>')
  }

  /**
   * Completing the innermost of several same-named nested tags (`<a>` six levels deep under a
   * differently-named `<data>`) must not crash: the auto-close feature's own `</a>` insertion, landing
   * mid-keystroke before the typed `>` completes the tag, used to let
   * [org.intellij.plugin.mdx.lang.parse.MdxJsxScanner.scanJsxElement]'s tag-matching stack reach past
   * the still-open `<data>` and pop it along with the outer `<a>`, producing two overlapping
   * MDX_JSX_FLOW_ELEMENT ranges and throwing "Intersecting parsed nodes detected" from the incremental
   * lexer. WEB-78468.
   */
  @Test
  fun testAutoCloseInDeeplyNestedSameNameTags() = checkTyping('>')

  @Test
  fun testRenamingMultilineOpeningTagKeepsMdxJsPsi() {
    myFixture.configureByText("test.mdx", "<div>\n  Hello\n</div>")
    WriteCommandAction.runWriteCommandAction(myFixture.project) {
      myFixture.editor.document.replaceString(1, 4, "dix")
    }
    PsiDocumentManager.getInstance(myFixture.project).commitAllDocuments()

    val tagNames = nodesOfTypeAllRoots(XML_TAG_NAME).map { it.text }
    assertTrue("Transiently mismatched tags must remain in MdxJS PSI: $tagNames", tagNames.containsAll(listOf("dix", "div")))
  }

  @Test
  fun testNoAutoCloseInsideArrowFunctionExpression() {
    // Regression (WEB-78468): typing `>` to complete an `=>` arrow inside a `{…}` attribute expression
    // must NOT be treated as the tag terminator, so no spurious closing tag is inserted -- even when a
    // real closing tag already follows.
    checkTyping('>')
  }

  @Test
  fun testNoAutoCloseBuildingArrowInUnterminatedExpression() {
    // The attribute expression is still open (no `}` yet): a `>` continuing the arrow stays inside it.
    checkTyping('>')
  }

  @Test
  fun testNoAutoCloseForComparisonInsideExpression() {
    // A `>` comparison operator inside an attribute expression is likewise not a tag terminator.
    checkTyping('>')
  }

  @Test
  fun testNoAutoCloseInsideQuotedAttributeValue() {
    // A `>` inside a quoted attribute value is string text, not the tag terminator.
    checkTyping('>')
  }

  @Test
  fun testAutoCloseAfterCompletedAttributeExpression() {
    // Sanity: once the attribute expression is balanced and the caret is back at the tag's top level,
    // typing `>` still auto-closes the tag.
    checkTyping('>')
  }

  @Test
  fun testAutoCloseAfterMultilineAttributeExpression() {
    // The backward search for the opening `<` must skip a whole multi-line `{…}` expression, not abort
    // on its newlines or its `=>` arrow's `>`.
    checkTyping('>')
  }

  @Test
  fun testAutoCloseAfterMultilineAttributeExpressionWithTrailingSpace() {
    // Same as above but with trailing whitespace after `}}` before the caret: still one `</div>`.
    checkTyping('>')
  }

  // Void HTML elements (<br>/<img>/<input>/<hr>, ...) have no closing tag, so typing `>` after one
  // must insert nothing but the `>` itself.

  @Test
  fun testNoAutoCloseVoidBrTag() = checkTyping('>')

  @Test
  fun testNoAutoCloseVoidInputTag() = checkTyping('>')

  @Test
  fun testAutoCloseCapitalizedComponentNamedLikeVoidTag() {
    // Guard for the fix: the void-element check must be case-sensitive to lowercase names, so a
    // capitalized JSX component (never a void element) such as <Input> still auto-closes.
    checkTyping('>')
  }

  // --- Markdown hard wrapping ---------------------------------------------------------------

  @Test
  fun testAutoWrapLongMarkdownLine() = checkAutoWrapOnTyping("synchronization")

  @Test
  fun testAutoWrapKeepsMultiParagraphBlockQuote() = checkAutoWrapOnTyping("synchronization")

  @Test
  fun testAutoWrapDoesNotBreakInlineLink() = checkAutoWrapOnTyping("X")

  // --- Brace typing in JSX body (MdxBraceTypedHandler) ------------------------------------

  @Test
  fun testTypeExpressionBraceInsideJsxBody() = checkTyping('{')

  @Test
  fun testTypeInsideIndentedCodeFenceInJsxBody() = checkTyping('x')

  // --- Formatter must not crash on transient/incomplete PSI (MdxFormattingModelBuilder) ----------

  @Test
  fun testBackspaceInIncompleteArrowFunctionDoesNotCrashFormatter() {
    myFixture.configureByFile("$testName.mdx")
    // The same platform call SmartIndentingBackspaceHandler makes via CodeStyle.getLineIndent.
    CodeStyleManager.getInstance(myFixture.project).getLineIndent(myFixture.file, myFixture.caretOffset)
  }

  @Test
  fun testLineIndentAfterUnclosedFenceFollowingEmptyFenceDoesNotCrashFormatter() {
    myFixture.configureByFile("$testName.mdx")
    CodeStyleManager.getInstance(myFixture.project).getLineIndent(myFixture.file, myFixture.caretOffset)
  }

  @Test
  fun testDoKeywordCompletionInsideFenceDoesNotCrashFormatter() {
    myFixture.configureByFile("$testName.mdx")
    TemplateManagerImpl.setTemplateTesting(testRootDisposable)
    assertNoErrorLogged { selectCompletionItem("do") }
    assertTrue(
      "Expected the fence content to survive the live-template reformat",
      myFixture.editor.document.text.contains("npx")
    )
  }


  // --- Backtick typing: auto-close and the fence language popup ------------------------------

  @Test
  fun testAutoCloseBacktick() = checkTyping('`')

  @Test
  fun testNoAutoCloseBacktickAfterWord() = checkTyping('`')

  /**
   * MarkdownQuoteHandler treats a lone backtick as an opener whenever it's not flanked by word
   * characters (IJPL-232322), which includes JSX bracket punctuation like `<`/`>` - so a backtick typed
   * between JSX tags auto-closes the same as it would between other punctuation or whitespace.
   */
  @Test
  fun testAutoCloseBacktickInsideJsxText() = checkTyping('`')

  /**
   * Typing the third backtick of a fence must offer the language list, the way it does in plain Markdown.
   * `MarkdownTypedHandler.checkAutoPopup` is what schedules that popup, and it used to bail on anything that is not
   * a `MarkdownFile` — so in .mdx no handler reacted to the backtick at all and no lookup ever appeared. The lookup
   * *contents* are asserted by MdxCompletionTest.testCodeFenceLanguageCompletionBetweenAdjacentDelimiters.
   */
  @Test
  fun testFenceLanguageAutoPopupFiresOnThirdBacktick() {
    // Typed from scratch, so each backtick auto-closes and the third leaves the caret amid six of them.
    myFixture.configureByText("test.mdx", "<caret>")
    myFixture.type("``")
    typeAndPumpAutoPopup('`')
    assertEquals("``````", myFixture.editor.document.text)
    assertNotNull("Expected an auto-popup lookup after typing the third backtick of a code fence", myFixture.lookup)
  }

  // --- Enter auto-indent inside JSX flow elements and code fences (MdxEnterHandler) -------------

  /**
   * Pressing Enter between braces inside an indented code fence expands the block: a body line one indent
   * step deeper than the opening line, then the closer on its own line (MdxEnterHandler). It must
   * also not corrupt the incremental lexer: the fence body is indented to +4 under `<div>`, and the pre-fix
   * MdxJsxScanner.skipCodeFence (≤3-space rule) failed to skip it on the re-lex triggered by the edit,
   * scanning `{`/`}` as JSX and throwing "Intersecting parsed nodes". After the scanner fix the host stays
   * well-formed. The caret lands in the injected TS fragment, so the host is read via the top-level file. WEB-78468.
   */
  @Test
  fun testEnterBetweenBracesInCodeFenceInsideJsxDoesNotCorruptLexer() = checkTyping('\n')

  /**
   * Enter between a JSX element's tags inside an indented fence expands it like braces: a body line one step
   * deeper, the closing tag on its own line at the fence base (MdxEnterHandler runs before a
   * competing JS/XML delegate that would mis-indent it on the injected 0-based fragment).
   */
  @Test
  fun testEnterBetweenJsxTagsInCodeFenceInsideJsxIsIndented() = checkTyping('\n')

  /** As above for a component tag (`<Foo></Foo>`). */
  @Test
  fun testEnterBetweenComponentTagsInCodeFenceInsideJsxIsIndented() = checkTyping('\n')

  /** Enter right after a lone opening tag (no matching closer) keeps the new line at the tag's own indent. */
  @Test
  fun testEnterAfterOpeningJsxTagInCodeFenceInsideJsxIsIndented() = checkTyping('\n')

  /**
   * Enter between an empty JSX tag pair nested inside an ESM statement's function/expression body (e.g.
   * `export function f() { return <div></div> }`) indents the body line one step and keeps the closing tag
   * at the statement's own indent: MdxFormattingModelBuilder deliberately treats the whole MDX_ESM_BLOCK as
   * one opaque leaf (to leave import/export syntax untouched), so it has no structural indent info here and
   * the platform's default Enter handling would otherwise drop the closing tag to column 0. WEB-78468.
   */
  @Test
  fun testEnterBetweenJsxTagsInEsmBlockIsIndented() = checkTyping('\n')

  @Test
  fun testEnterBetweenJsxTagsAfterLeadingBlankLineIsIndented() = checkTyping('\n')

  /**
   * Enter below a whitespace-only line of an indented fence must leave that line alone. The sandbox round trip
   * used to dedent it to nothing and re-indent it back as an *empty* line, so writing the body back changed text
   * above the caret; that host change spans whole CODE_FENCE_CONTENT lines and invalidates the shreds of the
   * injected DocumentWindow the caret lives in, collapsing its length below the offset `EnterHandler` snapshotted
   * before calling the delegate ("Wrong caret offset change by MdxEnterHandler"). Both fixtures hinge on
   * lines that are *only* whitespace — keep them intact. WEB-78468.
   */
  @Test
  fun testEnterBelowIndentedBlankLineInInjectedFenceKeepsInjectionValid() = checkTyping('\n')

  /**
   * As above, but the caret sits *on* a whitespace-only line indented less than the fence base. Re-indenting that
   * line to the base on write-back replaced it whole, which desynced the injected document from its PSI
   * ("After patch: doc: ... ---PSI: ...") — the same corruption, caught one step earlier. The line keeps its own
   * whitespace; only the line Enter opens gets the fence base. WEB-78468.
   */
  @Test
  fun testEnterOnUnderIndentedBlankLineInInjectedFenceKeepsInjectionValid() = checkTyping('\n')

  /**
   * Splitting an existing text line inside a JSX flow element's body with Enter must keep the moved half
   * at the body's indent level, aligned with the sibling text line above it — not indent it one level
   * deeper: the platform's default Enter routes the split through the XML/JS formatter, which over-indents
   * once any other reformat has already run earlier in the session (MdxEnterHandler inserts the
   * line itself to avoid that path entirely). WEB-78468.
   */
  @Test
  fun testEnterSplittingTextInJsxFlowElementBodyKeepsSiblingIndent() = checkTyping('\n')

  /**
   * As [testEnterSplittingTextInJsxFlowElementBodyKeepsSiblingIndent], but the caret sits right after a space
   * rather than directly between two non-space characters: the JS/JSX parser splits JSX text into separate
   * `XmlText` siblings around embedded whitespace, so a caret right against that whitespace isn't strictly
   * inside either sibling — a case the original guard missed entirely. WEB-78468.
   */
  @Test
  fun testEnterAfterSpaceInJsxFlowElementBodyKeepsSiblingIndent() = checkTyping('\n')

}
