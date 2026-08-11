package org.intellij.plugin.mdx.highlighting

import com.intellij.lexer.LexerBase
import com.intellij.psi.tree.IElementType
import org.intellij.plugin.mdx.lang.parse.MdxJsxScanner
import org.intellij.plugin.mdx.lang.parse.MdxTokenTypes
import org.intellij.plugins.markdown.lang.MarkdownElementType
import org.intellij.plugins.markdown.lang.lexer.MarkdownLexerAdapter

/**
 * Re-lexes inline content like [MarkdownLexerAdapter] does, but collapses every balanced `{expression}`
 * span into one [MdxTokenTypes.EMBEDDED_JS_CONTENT] token, so the editor highlighter's JS/JSX layer covers
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
        // Bound the scan window, or a densely-`{`-populated element re-scans to its end on every brace
        // (O(n^2) per keystroke); an expression longer than this is simply left uncollapsed.
        val scanLimit = minOf(endOffset, tokenStart + MAX_INLINE_EXPRESSION_SCAN)
        val expressionEnd = MdxJsxScanner.scanExpression(buffer, tokenStart, scanLimit)
        if (expressionEnd != -1) {
          addToken(JSX_BLOCK_CONTENT, tokenStart, expressionEnd)
          cursor = expressionEnd
          // Drop delegate tokens fully inside the collapsed span; a straddling token's tail is
          // re-emitted below via the emitStart clip.
          while (delegate.tokenType != null && delegate.tokenEnd <= expressionEnd) {
            delegate.advance()
          }
          continue
        }
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
    private val JSX_BLOCK_CONTENT: IElementType = MarkdownElementType.platformType(MdxTokenTypes.EMBEDDED_JS_CONTENT)

    /** Upper bound for a single inline `{expression}` scan; keeps the re-lexer linear on pathological input. */
    private const val MAX_INLINE_EXPRESSION_SCAN = 2000
  }
}
