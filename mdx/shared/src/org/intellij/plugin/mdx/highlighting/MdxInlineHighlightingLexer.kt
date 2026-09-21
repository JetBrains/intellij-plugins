package org.intellij.plugin.mdx.highlighting

import com.intellij.lexer.LexerBase
import com.intellij.psi.tree.IElementType
import org.intellij.plugin.mdx.lang.parse.MdxExpressionBoundaryScanner
import org.intellij.plugin.mdx.lang.parse.MdxTokenTypes
import org.intellij.plugin.mdx.lang.parse.MdxMarkdownLibTokenTypes
import org.intellij.plugins.markdown.lang.lexer.MarkdownLexerAdapter

/**
 * Re-lexes inline content like [MarkdownLexerAdapter] does, but collapses every balanced `{expression}`
 * span into one [MdxMarkdownLibTokenTypes.EMBEDDED_JS_CONTENT] token, so the editor highlighter's JS/JSX layer covers
 * it instead of flattening it into plain `MARKDOWN_TEXT`.
 */
internal class MdxInlineHighlightingLexer : LexerBase() {
  private val delegate = MarkdownLexerAdapter()

  private lateinit var buffer: CharSequence
  private var bufferEnd = 0

  private val tokenTypes = ArrayList<IElementType>()
  private val tokenStarts = ArrayList<Int>()
  private val tokenEnds = ArrayList<Int>()
  private var index = 0

  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
    this.buffer = buffer
    this.bufferEnd = endOffset
    tokenTypes.clear()
    tokenStarts.clear()
    tokenEnds.clear()
    index = 0

    delegate.start(buffer, startOffset, endOffset, initialState)
    // Emitted tokens must tile [startOffset, endOffset) with no gaps/overlaps or LexerEditorHighlighter
    // rejects the lexer; [cursor] tracks the end of the last emitted token.
    var cursor = startOffset
    while (delegate.tokenType != null) {
      val tokenType = delegate.tokenType!!
      val tokenStart = delegate.tokenStart
      val tokenEnd = delegate.tokenEnd
      if (tokenStart == cursor && buffer[tokenStart] == '{') {

        val scanLimit = minOf(endOffset, tokenStart + MAX_INLINE_EXPRESSION_SCAN)
        val expressionEnd = MdxExpressionBoundaryScanner.findExpressionEnd(buffer, tokenStart, scanLimit)
        val collapsedEnd = if (expressionEnd != -1) expressionEnd else findLineEnd(buffer, tokenStart, scanLimit)
        if (collapsedEnd > tokenStart) {
          addToken(JSX_BLOCK_CONTENT, tokenStart, collapsedEnd)
          cursor = collapsedEnd
          // re-emitted below via the emitStart clip.
          while (delegate.tokenType != null && delegate.tokenEnd <= collapsedEnd) {
            delegate.advance()
          }
          continue
        }
      }
      else if (tokenStart == cursor && buffer[tokenStart] == '}') {
        addToken(JSX_BLOCK_CONTENT, tokenStart, tokenStart + 1)
        cursor = tokenStart + 1
        continue
      }
      // Clip a token whose head was consumed by a preceding collapsed expression.
      val emitStart = maxOf(tokenStart, cursor)
      if (emitStart < tokenEnd) {
        addToken(tokenType, emitStart, tokenEnd)
        cursor = tokenEnd
      }
      delegate.advance()
    }
  }

  private fun findLineEnd(buffer: CharSequence, start: Int, limit: Int): Int {
    var end = start
    while (end < limit && buffer[end] != '\n') end++
    return if (end < limit) end + 1 else end
  }

  private fun addToken(type: IElementType, start: Int, end: Int) {
    tokenTypes.add(type)
    tokenStarts.add(start)
    tokenEnds.add(end)
  }

  override fun getState(): Int = 0

  override fun getTokenType(): IElementType? = tokenTypes.getOrNull(index)

  override fun getTokenStart(): Int = if (index < tokenStarts.size) tokenStarts[index] else bufferEnd

  override fun getTokenEnd(): Int = if (index < tokenEnds.size) tokenEnds[index] else bufferEnd

  override fun advance() {
    index++
  }

  override fun getBufferSequence(): CharSequence = buffer

  override fun getBufferEnd(): Int = bufferEnd

  companion object {
    private val JSX_BLOCK_CONTENT: IElementType = MdxTokenTypes.EMBEDDED_JS_CONTENT

    /** Upper bound for a single inline `{expression}` scan; keeps the re-lexer linear on pathological input. */
    private const val MAX_INLINE_EXPRESSION_SCAN = 2000
  }
}
