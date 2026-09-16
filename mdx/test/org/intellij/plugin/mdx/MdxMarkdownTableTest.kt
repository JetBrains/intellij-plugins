package org.intellij.plugin.mdx

import com.intellij.codeInsight.hints.BlockConstraints
import com.intellij.codeInsight.hints.BlockInlayRenderer
import com.intellij.codeInsight.hints.HorizontalConstraints
import com.intellij.codeInsight.hints.InlayHintsProviderFactory
import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.InlineInlayRenderer
import com.intellij.codeInsight.hints.NoSettings
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.RootInlayPresentation
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.EditorTestUtil
import com.intellij.testFramework.TestDataPath
import org.intellij.plugin.mdx.lang.MdxLanguage
import org.intellij.plugins.markdown.editor.tables.ui.MarkdownTableInlayProvider
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownTable
import org.junit.jupiter.api.Test

@Suppress("MarkdownIncorrectTableFormatting")
@TestDataPath($$"$PROJECT_ROOT/contrib/mdx/testData/markdownTable")
class MdxMarkdownTableTest : MdxTestBase() {
  @Test
  fun testMarkdownTableInlayProviderSupportsMdxTables() {
    myFixture.configureByFile("$testName.mdx")

    val provider = MarkdownTableInlayProvider()
    assertNotNull(
      "Expected Markdown table inlays to be collectable for MDX files",
      provider.getCollectorFor(myFixture.file, myFixture.editor, NoSettings(), EmptyInlayHintsSink)
    )
  }

  @Test
  fun testMarkdownTableInlayProviderIsRegisteredOnceForMdx() {
    val providers = InlayHintsProviderFactory.EP.extensionList
      .flatMap { it.getProvidersInfoForLanguage(MdxLanguage) }
      .filterIsInstance<MarkdownTableInlayProvider>()

    assertEquals(
      "MarkdownLanguage is the base language of MdxLanguage, so the platform applies the Markdown " +
        "registration of MarkdownTableInlayProvider to MDX. A second MDX registration makes the platform " +
        "collect the table bars twice. Providers found: ${providers.map { it.javaClass.name }}",
      1,
      providers.size
    )
  }

  @Test
  fun testMarkdownTableBarsAreCollectedOnceInMdx() {
    myFixture.configureByFile("$testName.mdx")
    val table = PsiTreeUtil.findChildOfType(myFixture.file, MarkdownTable::class.java)
                ?: error("Test setup: the MDX file must hold a Markdown table")
    val tableRange = table.textRange

    myFixture.doHighlighting()

    val inlayModel = myFixture.editor.inlayModel
    val columnBars = inlayModel.getBlockElementsInRange(
      tableRange.startOffset,
      tableRange.startOffset,
      BlockInlayRenderer::class.java
    )
    assertEquals("The table must show one column bar", 1, columnBars.size)
    assertEquals(
      "The column bar must hold one presentation. Two presentations mean two providers collected it.",
      1,
      columnBars.single().renderer.getConstrainedPresentations().size
    )

    val rowBars = inlayModel.getInlineElementsInRange(
      tableRange.startOffset,
      tableRange.endOffset,
      InlineInlayRenderer::class.java
    )
    assertEquals("Each table row must show one row bar", table.getRows(true).size + 1, rowBars.size)
    for (rowBar in rowBars) {
      assertEquals(
        "A row bar must hold one presentation. Two presentations mean two providers collected it.",
        1,
        rowBar.renderer.getConstrainedPresentations().size
      )
    }
  }

  @Test
  fun testMarkdownTableActionsAreAvailableInMdx() {
    myFixture.configureByFile("$testName.mdx")

    for (actionId in MARKDOWN_TABLE_ACTION_IDS) {
      assertActionEnabled(actionId)
    }
  }

  @Test
  fun testMarkdownTableInsertRowActionAppliesInMdx() {
    myFixture.configureByFile("$testName.mdx")

    performInsertRowBelowAction()

    myFixture.checkResultByFile("${testName}_after.mdx")
  }

  @Test
  fun testMarkdownTableOnTypeReformattingAppliesInMdx() {
    myFixture.configureByFile("$testName.mdx")

    myFixture.type("some")

    myFixture.checkResultByFile("${testName}_after.mdx")
  }

  @Test
  fun testMarkdownTableActionsAreDisabledInMdxNonMarkdownContexts() {
    val cases = mapOf(
      "JSX opening tag" to "<Al<caret>ert>text</Alert>",
      "ESM block" to "import value<caret> from './value'\n\nText",
      "MDX expression" to "Some {val<caret>ue} text",
      "JSX attribute" to "<Alert title=\"hel<caret>lo\" />",
      "code fence" to "```ts\nconst value<caret> = 1\n```",
      "code span" to "Some `co<caret>de` text",
      "front matter" to "---\ntit<caret>le: Hello\n---\n\nText",
    )

    for ((name, text) in cases) {
      myFixture.configureByText("test.mdx", text)
      for (actionId in MARKDOWN_TABLE_ACTION_IDS) {
        assertTrue("Expected $actionId to be disabled in $name", !isActionEnabled(actionId))
      }
    }
  }

  private fun performInsertRowBelowAction() {
    EditorTestUtil.executeAction(myFixture.editor, true, action(INSERT_ROW_BELOW_ACTION_ID))
  }

  private fun assertActionEnabled(actionId: String) {
    assertTrue("Expected $actionId to be enabled", isActionEnabled(actionId))
  }

  private fun isActionEnabled(actionId: String): Boolean {
    return EditorTestUtil.checkActionIsEnabled(myFixture.editor, action(actionId))
  }

  private fun action(actionId: String): AnAction {
    val action = ActionManager.getInstance().getAction(actionId)
    assertNotNull("Action $actionId must be registered", action)
    return action
  }

  private object EmptyInlayHintsSink : InlayHintsSink {
    override fun addInlineElement(
      offset: Int,
      relatesToPrecedingText: Boolean,
      presentation: InlayPresentation,
      placeAtTheEndOfLine: Boolean
    ) = unexpectedCall()

    override fun addBlockElement(
      offset: Int,
      relatesToPrecedingText: Boolean,
      showAbove: Boolean,
      priority: Int,
      presentation: InlayPresentation
    ) = unexpectedCall()

    override fun addInlineElement(
      offset: Int,
      presentation: RootInlayPresentation<*>,
      constraints: HorizontalConstraints?
    ) = unexpectedCall()

    override fun addBlockElement(
      logicalLine: Int,
      showAbove: Boolean,
      presentation: RootInlayPresentation<*>,
      constraints: BlockConstraints?
    ) = unexpectedCall()

    private fun unexpectedCall(): Nothing {
      error("getCollectorFor must not add inlays")
    }
  }

  companion object {
    private const val INSERT_ROW_BELOW_ACTION_ID = "Markdown.Table.InsertRow.InsertBelow"

    private val MARKDOWN_TABLE_ACTION_IDS = listOf(
      "Markdown.Table.InsertRow.InsertAbove",
      INSERT_ROW_BELOW_ACTION_ID,
      "Markdown.Table.RemoveCurrentRow",
      "Markdown.Table.InsertTableColumn.InsertAfter",
      "Markdown.Table.RemoveCurrentColumn",
      "Markdown.Table.SetColumnAlignment.Left",
      "Markdown.Table.SwapRows.SwapWithBelow",
      "Markdown.Table.SwapColumns.SwapWithRightColumn",
    )
  }
}
