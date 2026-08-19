package com.intellij.mdx.frontend.markdown

import com.intellij.openapi.actionSystem.DataContext
import org.intellij.plugin.mdx.markdown.MdxMarkdownActionPromoterExtension
import org.intellij.plugins.markdown.ui.actions.MarkdownActionPromoterExtension

internal class MdxFrontendMarkdownActionPromoterExtension : MarkdownActionPromoterExtension {
  private val delegate = MdxMarkdownActionPromoterExtension()

  override fun shouldPromoteMarkdownActions(context: DataContext): Boolean = delegate.shouldPromoteMarkdownActions(context)
}
