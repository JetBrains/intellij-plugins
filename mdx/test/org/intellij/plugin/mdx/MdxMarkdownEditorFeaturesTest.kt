package org.intellij.plugin.mdx

import com.intellij.codeInsight.editorActions.fillParagraph.LanguageFillParagraphExtension
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder
import com.intellij.lang.LanguageStructureViewBuilder
import com.intellij.lang.folding.LanguageFolding
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.testFramework.EditorTestUtil
import com.intellij.testFramework.TestDataPath
import org.intellij.plugin.mdx.markdown.MdxMarkdownActionPromoterExtension
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownHeader
import org.intellij.plugins.markdown.lang.supportsMarkdown
import org.junit.jupiter.api.Test

@TestDataPath($$"$PROJECT_ROOT/contrib/mdx/testData/markdownEditorFeatures")
class MdxMarkdownEditorFeaturesTest : MdxTestBase() {
  @Test
  fun testMdxSupportsMarkdownInContext() {
    val cases = mapOf(
      "plain prose" to "Some pro<caret>se text",
      "caret before expression" to "This is <caret>{variable}",
      "caret after expression" to "This is {variable}<caret>",
      "JSX body text" to "<Alert>This i<caret>s text</Alert>",
      "JSX body selection" to "<Alert>This <selection>text</selection> is selected</Alert>",
      "selection with inline JSX" to "<selection>All this <Alert>text</Alert> is selected</selection>",
      "selection with expression" to "<selection>All this {variable} is selected</selection>",
    )

    for ((name, text) in cases) {
      myFixture.configureByText("test.mdx", text)
      assertTrue("Expected $name to support Markdown features", mdxSupportsMarkdownAtCaret())
      assertTrue("Expected Markdown actions to be promoted in $name", mdxPromotesMarkdownActionsAtCaret())
    }
  }

  @Test
  fun testMdxPromotesMarkdownActionsWhenShortcutDispatchOmitsPsiFile() {
    myFixture.configureByFile("$testName.mdx")

    val dataContext = SimpleDataContext.builder()
      .add(CommonDataKeys.PROJECT, myFixture.project)
      .add(CommonDataKeys.EDITOR, myFixture.editor)
      .build()

    assertTrue(
      "Expected Markdown actions to be promoted without a PSI file in the shortcut data context",
      MdxMarkdownActionPromoterExtension().shouldPromoteMarkdownActions(dataContext)
    )
  }

  @Test
  fun testMdxDoesNotSupportMarkdownActionsInNonMarkdownContexts() {
    val cases = mapOf(
      "JSX opening tag" to "<Al<caret>ert>text</Alert>",
      "JSX closing tag" to "<Alert>text</Al<caret>ert>",
      "JSX self-closing tag" to "<Alert<caret> />",
      "ESM block" to "import value<caret> from './value'\n\nText",
      "MDX expression" to "Some {val<caret>ue} text",
      "MDX expression selection" to "Some <selection>{value}</selection> text",
      "JSX attribute" to "<Alert title=\"hel<caret>lo\" />",
      "code fence" to "```ts\nconst value<caret> = 1\n```",
      "code span" to "Some `co<caret>de` text",
      "front matter" to "---\ntit<caret>le: Hello\n---\n\nText",
    )

    for ((name, text) in cases) {
      myFixture.configureByText("test.mdx", text)
      assertTrue("Sanity check failed: $name must use an MDX file", myFixture.file.name.endsWith(".mdx"))
      assertTrue("Expected $name not to support Markdown actions", !mdxSupportsMarkdownAtCaret())
      assertTrue("Expected $name not to promote Markdown actions", !mdxPromotesMarkdownActionsAtCaret())
    }
  }

  @Test
  fun testFillParagraphAppliesInMdx() {
    myFixture.configureByFile("$testName.mdx")

    assertNotNull(LanguageFillParagraphExtension.INSTANCE.forLanguage(myFixture.file.language))
    performEditorAction(FILL_PARAGRAPH_ACTION_ID)

    myFixture.checkResultByFile("${testName}_after.mdx", true)
  }

  @Test
  fun testFillParagraphIsDisabledInMdxNonMarkdownContexts() {
    val cases = mapOf(
      "JSX opening tag" to "<Al<caret>ert>first\nsecond</Alert>",
      "ESM block" to "export const value = 'first<caret>'\nexport const other = 'second'\n\nText",
      "MDX expression" to "Some {first<caret> +\nsecond} text",
      "JSX attribute" to "<Alert title=\"first<caret>\nsecond\" />",
      "code span" to "Some `first<caret>`\nsecond text",
      "front matter" to "---\ntitle: first<caret>\ndescription: second\n---\n\nText",
    )

    for ((name, text) in cases) {
      myFixture.configureByText("source.mdx", text)
      val before = myFixture.editor.document.text

      performEditorAction(FILL_PARAGRAPH_ACTION_ID)

      assertEquals("Expected Fill Paragraph to leave $name unchanged", before, myFixture.editor.document.text)
    }
  }

  @Test
  fun testEnterContinuesBlockquoteInMdx() {
    myFixture.configureByFile("$testName.mdx")

    performEditorAction(EDITOR_ENTER_ACTION_ID)

    myFixture.checkResultByFile("${testName}_after.mdx")
  }

  @Test
  fun testEnterDoesNotContinueBlockquoteInMdxNonMarkdownContexts() {
    val cases = mapOf(
      "JSX opening tag" to "<Al<caret>ert>> Quote</Alert>",
      "ESM block" to "export const value = /* > Quote<caret> */ 1\n\nText",
      "MDX expression" to "Some {'> Quote<caret>'} text",
      "JSX attribute" to "<Alert title=\"> Quote<caret>\" />",
      "code fence" to "```\n> Quote<caret>\n```",
      "code span" to "`> Quote<caret>`",
      "front matter" to "---\ntitle: > Quote<caret>\n---\n\nText",
    )

    for ((name, text) in cases) {
      myFixture.configureByText("source.mdx", text)

      performEditorAction(EDITOR_ENTER_ACTION_ID)

      assertTrue("Expected Enter not to continue a blockquote in $name", !myFixture.editor.document.text.contains("> Quote\n> "))
    }
  }

  @Test
  fun testStructureViewShowsOnlyMdxMarkdownHeaders() {
    myFixture.configureByText("source.mdx", mixedContextHeadersText())

    val builder = LanguageStructureViewBuilder.getInstance().getStructureViewBuilder(myFixture.file)
    assertNotNull("Expected Markdown structure view to be registered for MDX", builder)
    val model = (builder as TreeBasedStructureViewBuilder).createStructureViewModel(myFixture.editor)
    val headers = model.root.children.mapNotNull { it.presentation.presentableText }

    assertTrue("Expected prose header in MDX structure view: $headers", headers.any { it.contains("Visible Header") })
    assertNoNonMarkdownHeaders(headers, "structure view")
  }

  @Test
  fun testFoldingBuildsOnlyMdxMarkdownHeaderRegions() {
    myFixture.configureByText("source.mdx", mixedContextHeadersText())

    val builder = LanguageFolding.INSTANCE.forLanguage(myFixture.file.language)
    assertNotNull("Expected Markdown folding builder to be registered for MDX", builder)
    val headerFoldStarts = LanguageFolding.buildFoldingDescriptors(builder, myFixture.file, myFixture.editor.document, false)
      .filter { it.element.psi is MarkdownHeader }
      .map { it.range.substring(myFixture.file.text) }
      .map { it.lineSequence().first() }

    assertTrue("Expected prose header fold in MDX: $headerFoldStarts", headerFoldStarts.any { it.contains("# Visible Header") })
    assertTrue("Expected JSX body header fold in MDX: $headerFoldStarts", headerFoldStarts.any { it.contains("# JSX Body Heading") })
    assertNoNonMarkdownHeaders(headerFoldStarts, "folding")
  }

  private fun assertNoNonMarkdownHeaders(actual: List<String>, featureName: String) {
    val forbidden = listOf(
      "Front Matter Heading",
      "Esm Heading",
      "Attribute Heading",
      "Expression Heading",
      "Fence Heading",
      "Code Span Heading",
    )
    for (header in forbidden) {
      assertTrue("Expected $featureName to ignore $header, actual: $actual", actual.none { it.contains(header) })
    }
  }

  private fun mixedContextHeadersText(): String {
    return """
      ---
      title: "# Front Matter Heading"
      ---

      import value from './module'
      export const hidden = "# Esm Heading"

      # Visible Header
      Visible content

      # Next Visible Header
      More visible content

      <Alert title="# Attribute Heading">
      # JSX Body Heading
      </Alert>

      Some {"# Expression Heading"}

      ```md
      # Fence Heading
      ```

      Some `# Code Span Heading`
      """.trimIndent()
  }

  private fun performEditorAction(actionId: String) {
    val action = ActionManager.getInstance().getAction(actionId)
    assertNotNull("Action $actionId must be registered", action)
    EditorTestUtil.executeAction(myFixture.editor, false, action)
  }

  private fun mdxSupportsMarkdownAtCaret(): Boolean {
    return myFixture.file.language.supportsMarkdown(dataContextAtCaret())
  }

  private fun mdxPromotesMarkdownActionsAtCaret(): Boolean {
    return MdxMarkdownActionPromoterExtension().shouldPromoteMarkdownActions(dataContextAtCaret())
  }

  private fun dataContextAtCaret(): DataContext {
    return SimpleDataContext.builder()
      .add(CommonDataKeys.PROJECT, myFixture.project)
      .add(CommonDataKeys.EDITOR, myFixture.editor)
      .add(CommonDataKeys.PSI_FILE, myFixture.file)
      .build()
  }

  companion object {
    private const val FILL_PARAGRAPH_ACTION_ID = "FillParagraph"
    private const val EDITOR_ENTER_ACTION_ID = "EditorEnter"
  }
}
