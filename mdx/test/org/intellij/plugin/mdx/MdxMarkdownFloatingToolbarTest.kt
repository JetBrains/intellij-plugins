package org.intellij.plugin.mdx

import com.intellij.openapi.actionSystem.ActionManager
import org.intellij.plugins.markdown.lang.supportsMarkdown
import org.junit.jupiter.api.Test

class MdxMarkdownFloatingToolbarTest : MdxTestBase() {
  private val markdownFloatingToolbarGroupId = "Markdown.Toolbar.Floating"

  @Test
  fun testMarkdownFloatingToolbarActionGroupIsRegistered() {
    assertNotNull(
      "MDX floating toolbar must reuse the Markdown floating toolbar action group",
      ActionManager.getInstance().getAction(markdownFloatingToolbarGroupId)
    )
  }

  @Test
  fun testMdxFloatingToolbarAllowsMarkdown() {
    val cases = mapOf(
      "plain prose" to "Some pro<caret>se text",
      "JSX body text" to "<Alert>te<caret>xt</Alert>",
    )

    for ((name, text) in cases) {
      myFixture.configureByText("test.mdx", text)
      assertTrue("Expected $name to allow the Markdown floating toolbar", !isToolbarIgnoredAtCaret())
    }
  }

  @Test
  fun testMdxFloatingToolbarIgnoresNonMarkdownContexts() {
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
      assertTrue("Expected MDX floating toolbar to ignore $name", isToolbarIgnoredAtCaret())
    }
  }

  private fun isToolbarIgnoredAtCaret(): Boolean {
    val element = myFixture.file.findElementAt(myFixture.caretOffset)
    assertNotNull("Expected an element at caret", element)
    return !element!!.supportsMarkdown()
  }
}
