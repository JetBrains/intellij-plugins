package org.intellij.plugin.mdx.lang.parse

import org.intellij.markdown.IElementType
import org.intellij.markdown.parser.sequentialparsers.RangesListBuilder
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.TokensCache

internal class MdxInlineJsxParser : SequentialParser {
  override fun parse(tokens: TokensCache, rangesToGlue: List<IntRange>): SequentialParser.ParsingResult {
    val result = SequentialParser.ParsingResultBuilder()
    val excludedTokenIndexes = mutableSetOf<Int>()
    val text = tokens.originalText

    for (range in rangesToGlue) {
      var offset = tokens.Iterator(range.first).start
      val limitIterator = tokens.Iterator(range.last)
      val limit = if (limitIterator.type == null) limitIterator.start else limitIterator.end
      while (offset < limit) {
        when (text[offset]) {
          '<' -> {
            val element = MdxJsxScanner.scanJsxElement(text, offset, limit)
            if (element != null && element.balanced) {
              val rootRange = addNode(result, tokens, range, element.range, MdxMarkdownLibElementTypes.MDX_JSX_TEXT_ELEMENT)
              for (tag in element.tags) {
                val tagType = when (tag.kind) {
                  MdxJsxScanner.TagKind.OPENING -> MdxMarkdownLibElementTypes.MDX_JSX_OPENING_ELEMENT
                  MdxJsxScanner.TagKind.CLOSING -> MdxMarkdownLibElementTypes.MDX_JSX_CLOSING_ELEMENT
                  MdxJsxScanner.TagKind.SELF_CLOSING -> MdxMarkdownLibElementTypes.MDX_JSX_SELF_CLOSING_ELEMENT
                }
                val tagRange = addNode(result, tokens, range, tag.range, tagType)
                for (attribute in tag.attributes) {
                  val attributeRange = toTokenRange(tokens, range, attribute)
                  if (attributeRange != null && tagRange != null && attributeRange.isStrictlyInside(tagRange)) {
                    result.withNode(SequentialParser.Node(attributeRange, MdxMarkdownLibElementTypes.MDX_JSX_ATTRIBUTE))
                  }
                }
                excludeTokens(tokens, range, tag.range, excludedTokenIndexes)
              }
              for (expression in element.expressions) {
                val expressionRange = toTokenRange(tokens, range, expression)
                if (expressionRange != null && rootRange != null && expressionRange.isStrictlyInside(rootRange)) {
                  result.withNode(SequentialParser.Node(expressionRange, MdxMarkdownLibElementTypes.MDX_EXPRESSION))
                }
                excludeTokens(tokens, range, expression, excludedTokenIndexes)
              }
              offset = element.range.last
              continue
            }
          }
          '{' -> {
            val expressionEnd = MdxJsxScanner.scanExpression(text, offset, limit)
            if (expressionEnd != -1) {
              addNode(result, tokens, range, offset..expressionEnd, MdxMarkdownLibElementTypes.MDX_EXPRESSION)
              excludeTokens(tokens, range, offset..expressionEnd, excludedTokenIndexes)
              offset = expressionEnd
              continue
            }
          }
        }
        offset++
      }
    }

    val delegate = RangesListBuilder()
    var iterator: TokensCache.Iterator = tokens.RangesListIterator(rangesToGlue)
    while (iterator.type != null) {
      if (iterator.index !in excludedTokenIndexes) {
        delegate.put(iterator.index)
      }
      iterator = iterator.advance()
    }
    return result.withFurtherProcessing(delegate.get())
  }

  private fun addNode(result: SequentialParser.ParsingResultBuilder,
                      tokens: TokensCache,
                      parsingRange: IntRange,
                      charRange: IntRange,
                      type: IElementType): IntRange? {
    val tokenRange = toTokenRange(tokens, parsingRange, charRange) ?: return null
    result.withNode(SequentialParser.Node(tokenRange, type))
    return tokenRange
  }

  private fun excludeTokens(tokens: TokensCache, parsingRange: IntRange, charRange: IntRange, excludedTokenIndexes: MutableSet<Int>) {
    var iterator: TokensCache.Iterator = tokens.RangesListIterator(listOf(parsingRange))
    while (iterator.type != null) {
      if (iterator.end > charRange.first && iterator.start < charRange.last) {
        excludedTokenIndexes.add(iterator.index)
      }
      if (iterator.start >= charRange.last) {
        return
      }
      iterator = iterator.advance()
    }
  }

  private fun toTokenRange(tokens: TokensCache, parsingRange: IntRange, charRange: IntRange): IntRange? {
    var iterator: TokensCache.Iterator = tokens.RangesListIterator(listOf(parsingRange))
    var startIndex = -1
    var endIndex = -1
    while (iterator.type != null) {
      if (iterator.end > charRange.first && iterator.start < charRange.last) {
        if (startIndex == -1) {
          startIndex = iterator.index
        }
        endIndex = iterator.index + 1
      }
      if (iterator.start >= charRange.last) {
        break
      }
      iterator = iterator.advance()
    }
    return if (startIndex == -1) null else startIndex..endIndex
  }

  private fun IntRange.isStrictlyInside(parent: IntRange): Boolean {
    return parent.first <= first && last <= parent.last && (parent.first != first || parent.last != last)
  }
}
