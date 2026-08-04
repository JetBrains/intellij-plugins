package org.intellij.plugin.mdx

import com.intellij.application.options.CodeStyle
import com.intellij.codeInsight.editorActions.CompletionAutoPopupHandler
import com.intellij.codeInsight.template.impl.TemplateManagerImpl
import com.intellij.openapi.application.impl.NonBlockingReadActionImpl
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
  fun testUsesMarkdownEditorIntegrations() {
    assertEquals(
      "com.intellij.markdown.frontend.editor.MarkdownLineWrapPositionStrategy",
      LanguageLineWrapPositionStrategy.INSTANCE.forLanguage(MdxLanguage).javaClass.name,
    )
    assertTrue(MdxLanguage.supportsMarkdown())
  }

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

}
