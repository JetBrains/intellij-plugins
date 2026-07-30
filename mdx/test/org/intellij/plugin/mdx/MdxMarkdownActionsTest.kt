package org.intellij.plugin.mdx

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.EditorTestUtil
import org.junit.jupiter.api.Test
import java.awt.datatransfer.StringSelection

@TestDataPath($$"$PROJECT_ROOT/contrib/mdx/testData/markdownActions")
class MdxMarkdownActionsTest : MdxTestBase() {
  @Test
  fun testMarkdownStylingActionsApplyInMdx() {
    doActionTest(
      actionId = BOLD_ACTION_ID,
      before = "MarkdownStylingActionsApplyInMdx_1",
      after = "MarkdownStylingActionsApplyInMdx_1_after"
    )
    doActionTest(
      actionId = ITALIC_ACTION_ID,
      before = "MarkdownStylingActionsApplyInMdx_2",
      after = "MarkdownStylingActionsApplyInMdx_2_after"
    )
    doActionTest(
      actionId = STRIKETHROUGH_ACTION_ID,
      before = "MarkdownStylingActionsApplyInMdx_3",
      after = "MarkdownStylingActionsApplyInMdx_3_after"
    )
    doActionTest(
      actionId = CODE_SPAN_ACTION_ID,
      before = "MarkdownStylingActionsApplyInMdx_4",
      after = "MarkdownStylingActionsApplyInMdx_4_after"
    )
  }

  @Test
  fun testMarkdownStylingActionsApplyInMdxJsxBody() {
    doActionTest(
      actionId = BOLD_ACTION_ID,
      before = testName,
      after = "${testName}_after"
    )
  }

  @Test
  fun testMarkdownStylingActionsApplyToMixedMdxSelections() {
    doActionTest(
      actionId = BOLD_ACTION_ID,
      before = "MarkdownStylingActionsApplyToMixedMdxSelections_1",
      after = "MarkdownStylingActionsApplyToMixedMdxSelections_1_after"
    )
    doActionTest(
      actionId = ITALIC_ACTION_ID,
      before = "MarkdownStylingActionsApplyToMixedMdxSelections_2",
      after = "MarkdownStylingActionsApplyToMixedMdxSelections_2_after"
    )
  }

  @Test
  fun testMarkdownCreateLinkActionAppliesInMdx() {
    CopyPasteManager.getInstance().setContents(StringSelection(""))
    doActionTest(
      actionId = CREATE_LINK_ACTION_ID,
      before = testName,
      after = "${testName}_after"
    )
  }

  @Test
  fun testMarkdownHeaderActionAppliesInMdx() {
    doActionTest(
      actionId = HEADER_DOWN_ACTION_ID,
      before = testName,
      after = "${testName}_after"
    )
  }

  @Test
  fun testMarkdownListActionsAreAvailableInMdx() {
    myFixture.configureByFile("$testName.mdx")

    assertActionEnabled(CREATE_OR_CHANGE_LIST_ACTION_ID)
    assertActionEnabled(SET_HEADER_LEVEL_ACTION_ID)
  }

  private fun doActionTest(actionId: String, before: String = testName, after: String = "${testName}_after") {
    myFixture.configureByFile("$before.mdx")
    assertActionEnabled(actionId)
    performAction(actionId)
    myFixture.checkResultByFile("$after.mdx")
  }

  private fun performAction(actionId: String) {
    EditorTestUtil.executeAction(myFixture.editor, true, action(actionId))
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

  companion object {
    private const val BOLD_ACTION_ID = "org.intellij.plugins.markdown.ui.actions.styling.ToggleBoldAction"
    private const val ITALIC_ACTION_ID = "org.intellij.plugins.markdown.ui.actions.styling.ToggleItalicAction"
    private const val STRIKETHROUGH_ACTION_ID = "org.intellij.plugins.markdown.ui.actions.styling.ToggleStrikethroughAction"
    private const val CODE_SPAN_ACTION_ID = "org.intellij.plugins.markdown.ui.actions.styling.ToggleCodeSpanAction"
    private const val CREATE_LINK_ACTION_ID = "Markdown.Styling.CreateLink"
    private const val HEADER_DOWN_ACTION_ID = "org.intellij.plugins.markdown.ui.actions.styling.HeaderDownAction"
    private const val SET_HEADER_LEVEL_ACTION_ID = "Markdown.Styling.SetHeaderLevel"
    private const val CREATE_OR_CHANGE_LIST_ACTION_ID = "Markdown.Styling.CreateOrChangeList"
  }
}
