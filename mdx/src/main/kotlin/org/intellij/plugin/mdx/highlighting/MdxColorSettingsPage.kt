package org.intellij.plugin.mdx.highlighting

import com.intellij.openapi.fileTypes.SyntaxHighlighter
import org.intellij.plugin.mdx.MdxBundle
import org.intellij.plugins.markdown.highlighting.MarkdownColorSettingsPage

class MdxColorSettingsPage : MarkdownColorSettingsPage() {
  override fun getHighlighter(): SyntaxHighlighter = MdxSyntaxHighlighter()
  override fun getDisplayName(): String = MdxBundle.message("mdx.name")
  override fun getDemoText(): String = "import React from \'react\';\n\n" +
                                       "#hello\n\n" +
                                       "<Button>Press the button</Button>"
}