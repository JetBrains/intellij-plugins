package com.intellij.dts.completion

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.openapi.editor.highlighter.HighlighterIterator
import com.intellij.psi.tree.TokenSet

private val stringTypes = TokenSet.create(
  DtsTypes.STRING_LITERAL,
  DtsTypes.CHAR_LITERAL,
  DtsTypes.INCLUDE_PATH,
)

class DtsQuoteHandler : SimpleTokenSetQuoteHandler(stringTypes) {
  override fun isClosingQuote(iterator: HighlighterIterator, offset: Int): Boolean {
    // fixes inconsistent completion behavior
    return super.isClosingQuote(iterator, offset) && iterator.end - iterator.start > 1
  }

  override fun isNonClosedLiteral(iterator: HighlighterIterator, chars: CharSequence): Boolean {
    // simple heuristic for non-closed literals, return true if there is a line
    // break between the opening quote and the potential closing quote
    val text = chars.slice(iterator.start until iterator.end)
    return super.isNonClosedLiteral(iterator, chars) || text.contains('\n')
  }
}