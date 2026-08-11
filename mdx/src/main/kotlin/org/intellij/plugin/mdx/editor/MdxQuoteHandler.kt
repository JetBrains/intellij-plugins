package org.intellij.plugin.mdx.editor

import com.intellij.codeInsight.editorActions.QuoteHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.highlighter.HighlighterIterator
import org.intellij.plugins.markdown.braces.MarkdownQuoteHandler

internal class MdxQuoteHandler: QuoteHandler {
  private val delegate = MarkdownQuoteHandler()

  override fun isClosingQuote(iterator: HighlighterIterator, offset: Int): Boolean = delegate.isClosingQuote(iterator, offset)

  override fun isOpeningQuote(iterator: HighlighterIterator, offset: Int): Boolean = delegate.isOpeningQuote(iterator, offset)

  override fun hasNonClosedLiteral(
    editor: Editor,
    iterator: HighlighterIterator,
    offset: Int,
  ): Boolean = delegate.hasNonClosedLiteral(editor, iterator, offset)

  override fun isInsideLiteral(iterator: HighlighterIterator): Boolean = delegate.isInsideLiteral(iterator)

}
