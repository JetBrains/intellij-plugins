package org.intellij.plugin.mdx.lang.parse

import org.intellij.markdown.flavours.gfm.GFMConstraints
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.constraints.CommonMarkdownConstraints

/** Keeps the indentation skipped by JSX when GFM recognizes a new Markdown modifier. */
internal class MdxMarkdownConstraints private constructor(
  private val indents: IntArray,
  types: CharArray,
  isExplicit: BooleanArray,
  charsEaten: Int,
  private val hasCheckbox: Boolean = false,
) : CommonMarkdownConstraints(indents, types, isExplicit, charsEaten) {
  override val base: CommonMarkdownConstraints
    get() = BASE

  override fun createNewConstraints(
    indents: IntArray,
    types: CharArray,
    isExplicit: BooleanArray,
    charsEaten: Int,
  ): CommonMarkdownConstraints = MdxMarkdownConstraints(indents, types, isExplicit, charsEaten)

  override fun addModifierIfNeeded(pos: LookaheadText.Position?): CommonMarkdownConstraints? {
    val modified = asGfmConstraints().addModifierIfNeeded(pos) as? GFMConstraints ?: return null
    val skippedIndent = if (pos == null) 0 else skippedIndent(pos)
    return MdxMarkdownConstraints(
      indents + (modified.indent + skippedIndent),
      modified.types,
      modified.isExplicit,
      modified.charsEaten,
      modified.hasCheckbox(),
    )
  }

  fun asGfmConstraints(): GFMConstraints = GFMConstraints(indents, types, isExplicit, charsEaten, hasCheckbox)

  fun applyToNextJsxLine(pos: LookaheadText.Position?): MdxMarkdownConstraints {
    if (pos == null) return BASE
    val line = pos.currentLine
    val nextIndents = IntArray(indents.size)
    val explicit = BooleanArray(indents.size)
    var offset = 0
    var column = 0
    var count = 0
    var consumed = 0
    for (index in types.indices) {
      if (types[index] == '>') {
        // JSX owns the whitespace before a quote, including indentation beyond three columns.
        while (offset < line.length && (line[offset] == ' ' || line[offset] == '\t')) {
          column += if (line[offset++] == '\t') 4 - column % 4 else 1
        }
        if (offset == line.length || line[offset] != '>') break
        offset++
        column++
        val hasSpace = offset == line.length || line[offset] == ' ' || line[offset] == '\t'
        nextIndents[index] = column + if (hasSpace) 1 else 0
        if (offset < line.length && hasSpace) {
          column += if (line[offset++] == '\t') 4 - column % 4 else 1
        }
        explicit[index] = true
      }
      else {
        val previousIndent = if (index == 0) 0 else indents[index - 1]
        val nextPreviousIndent = if (index == 0) 0 else nextIndents[index - 1]
        val requiredIndent = nextPreviousIndent + indents[index] - previousIndent
        while (column < requiredIndent && offset < line.length && (line[offset] == ' ' || line[offset] == '\t')) {
          column += if (line[offset++] == '\t') 4 - column % 4 else 1
        }
        if (column < requiredIndent && offset < line.length) break
        nextIndents[index] = requiredIndent
      }
      consumed = offset
      count++
    }
    return MdxMarkdownConstraints(nextIndents.copyOf(count), types.copyOf(count), explicit.copyOf(count), consumed)
  }

  private fun skippedIndent(pos: LookaheadText.Position): Int {
    val end = pos.offsetInCurrentLine
    if (end <= charsEaten) return 0
    val line = pos.currentLine
    var column = indent
    if (charsEaten > 0 && line[charsEaten - 1] == '\t') {
      column += (4 - column % 4) % 4
    }
    for (offset in charsEaten..<end) {
      column += when (line[offset]) {
        ' ' -> 1
        '\t' -> 4 - column % 4
        else -> return 0
      }
    }
    // GFM already accounts for the unused columns of the tab before its starting position.
    val tabRemainder = if (line[end - 1] == '\t') (4 - indent % 4) % 4 else 0
    return column - indent - tabRemainder
  }

  companion object {
    val BASE: MdxMarkdownConstraints = MdxMarkdownConstraints(IntArray(0), CharArray(0), BooleanArray(0), 0)
  }
}
