package org.intellij.plugin.mdx

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.EditorTestUtil
import com.intellij.testFramework.TestDataPath
import org.intellij.plugins.markdown.lang.supportsMarkdown
import org.intellij.plugins.markdown.settings.MarkdownCodeInsightSettings
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestDataPath($$"$PROJECT_ROOT/contrib/mdx/testData/markdownListEditing")
class MdxMarkdownListEditingTest : MdxTestBase() {
  @BeforeEach
  fun enableMarkdownListSmartKeys() {
    val settings = MarkdownCodeInsightSettings.getInstance()
    settings.reset()
    settings.state.renumberListsOnType = true
    Disposer.register(testRootDisposable) { settings.reset() }
  }

  @Test
  fun testEnterContinuesOrderedListInMdx() {
    doListEditTest(actionId = EDITOR_ENTER_ACTION_ID)
  }

  @Test
  fun testTypingSpaceRenumbersNewOrderedListItemInMdx() {
    doListEditTest(typedChar = ' ')
  }

  @Test
  fun testTabCreatesNestedListItemInMdx() {
    doListEditTest(actionId = EDITOR_TAB_ACTION_ID)
  }

  @Test
  fun testBackspaceRemovesListMarkerInMdx() {
    doListEditTest(typedChar = '\b')
  }

  @Test
  fun testEnterContinuesBlockquoteListInMdx() {
    doListEditTest(actionId = EDITOR_ENTER_ACTION_ID)
  }

  @Test
  fun testMarkdownListSmartKeysAreDisabledInMdxNonMarkdownContexts() {
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
      assertTrue("Expected Markdown list smart keys to be disabled in $name", !mdxSupportsMarkdownAtCaret())
    }
  }

  private fun doListEditTest(actionId: String? = null, typedChar: Char? = null) {
    myFixture.configureByFile("$testName.mdx")
    if (actionId != null) performEditorAction(actionId)
    if (typedChar != null) myFixture.type(typedChar)
    myFixture.checkResultByFile("${testName}_after.mdx")
  }

  private fun dataContextAtCaret(): DataContext {
    return SimpleDataContext.builder()
      .add(CommonDataKeys.PROJECT, myFixture.project)
      .add(CommonDataKeys.EDITOR, myFixture.editor)
      .add(CommonDataKeys.PSI_FILE, myFixture.file)
      .build()
  }

  private fun mdxSupportsMarkdownAtCaret(): Boolean {
    return myFixture.file.language.supportsMarkdown(dataContextAtCaret())
  }

  private fun performEditorAction(actionId: String) {
    val action = ActionManager.getInstance().getAction(actionId)
    assertNotNull("Action $actionId must be registered", action)
    EditorTestUtil.executeAction(myFixture.editor, true, action)
  }

  companion object {
    private const val EDITOR_ENTER_ACTION_ID = "EditorEnter"
    private const val EDITOR_TAB_ACTION_ID = "EditorTab"
  }
}
