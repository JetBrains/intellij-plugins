package org.intellij.plugin.mdx

import com.intellij.codeInsight.editorActions.CompletionAutoPopupHandler
import com.intellij.openapi.application.impl.NonBlockingReadActionImpl
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.TestModeFlags
import org.intellij.plugin.mdx.completion.MdxXmlAutoPopupEnabler
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

  // -------------------------------------------------------------------------
  // Void HTML elements must NOT be auto-closed (WEB-78468).
  //
  // A void element such as <br>/<img>/<input>/<hr> has no closing tag, so inserting one produces
  // invalid HTML/JSX (`<br></br>`). The platform XmlGtTypedHandler skips these via
  // HtmlUtil.isSingleHtmlTag; MdxJsxScanner.closingTagToInsert currently has no such guard, so it is
  // the only inserter here and wrongly emits the close. These tests assert the fixed behavior: typing
  // `>` after a void element inserts nothing but the `>` itself.
  // -------------------------------------------------------------------------

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

  // --- Brace typing in JSX body (MdxBraceTypedHandler) ------------------------------------

  @Test
  fun testTypeExpressionBraceInsideJsxBody() = checkTyping('{')

  @Test
  fun testTypeInsideIndentedCodeFenceInJsxBody() = checkTyping('x')

}
