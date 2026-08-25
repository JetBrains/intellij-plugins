package org.intellij.plugin.mdx.lang.parse

import org.intellij.markdown.IElementType
import org.intellij.markdown.parser.sequentialparsers.RangesListBuilder
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.TokensCache

internal class MdxInlineElementParser : SequentialParser {
  override fun parse(tokens: TokensCache, rangesToGlue: List<IntRange>): SequentialParser.ParsingResult {
    val result = SequentialParser.ParsingResultBuilder()
    if (rangesToGlue.isEmpty()) return result.withFurtherProcessing(emptyList())

    val outerExcludedTokenIndexes = mutableSetOf<Int>()
    val innerParsingSpaces = mutableListOf<List<IntRange>>()
    val text = tokens.originalText
    val opaqueRanges = opaqueRanges(tokens, rangesToGlue)

    var offset = tokens.Iterator(rangesToGlue.first().first).start
    val limitIterator = tokens.Iterator(rangesToGlue.last().last)
    val limit = if (limitIterator.type == null) limitIterator.start else limitIterator.end
    while (offset < limit) {
      val opaqueEnd = opaqueRanges.endOffsetContaining(offset)
      if (opaqueEnd != null) {
        offset = opaqueEnd.coerceAtMost(limit)
        continue
      }
      when (text[offset]) {
        '<' -> {
          val element = MdxJsxScanner.scanJsxElement(text, offset, limit, opaqueRanges)
          if (element != null && element.termination != MdxJsxScanner.Termination.UNTERMINATED) {
            val rootRange = addNode(result, tokens, element.range, MdxMarkdownLibElementTypes.MDX_JSX_TEXT_ELEMENT)
            val innerExcludedTokenIndexes = mutableSetOf<Int>()
            for ((tagRange, tagKind, _, attributes) in element.tags) {
              val tagType = when (tagKind) {
                MdxJsxScanner.TagKind.OPENING -> MdxMarkdownLibElementTypes.MDX_JSX_OPENING_ELEMENT
                MdxJsxScanner.TagKind.CLOSING -> MdxMarkdownLibElementTypes.MDX_JSX_CLOSING_ELEMENT
                MdxJsxScanner.TagKind.SELF_CLOSING -> MdxMarkdownLibElementTypes.MDX_JSX_SELF_CLOSING_ELEMENT
              }
              val tagTokenRange = addNode(result, tokens, tagRange, tagType)
              for (attribute in attributes) {
                val attributeRange = tokens.toTokenRange(attribute)
                if (attributeRange != null && tagTokenRange != null && attributeRange.isStrictlyInside(tagTokenRange)) {
                  result.withNode(SequentialParser.Node(attributeRange, MdxMarkdownLibElementTypes.MDX_JSX_ATTRIBUTE))
                }
              }
              excludeTokens(tokens, rangesToGlue, tagRange, innerExcludedTokenIndexes)
            }
            for (expression in element.expressions) {
              val expressionRange = tokens.toTokenRange(expression)
              if (expressionRange != null && rootRange != null && expressionRange.isStrictlyInside(rootRange)) {
                result.withNode(SequentialParser.Node(expressionRange, MdxMarkdownLibElementTypes.MDX_EXPRESSION))
              }
              excludeTokens(tokens, rangesToGlue, expression, innerExcludedTokenIndexes)
            }
            excludeTokens(tokens, rangesToGlue, element.range, outerExcludedTokenIndexes)
            elementContentRange(element)?.let { contentRange ->
              parsingSpace(tokens, rangesToGlue, contentRange, innerExcludedTokenIndexes)
                .takeIf { it.isNotEmpty() }
                ?.let(innerParsingSpaces::add)
            }
            offset = element.range.last
            continue
          }
          val openingPrefix = MdxJsxScanner.incompleteOpeningTagRange(text, offset, limit)
          if (openingPrefix != null) {
            addNode(result, tokens, openingPrefix, MdxMarkdownLibElementTypes.MDX_JSX_TEXT_ELEMENT)
            addNode(result, tokens, openingPrefix, MdxMarkdownLibElementTypes.MDX_JSX_OPENING_ELEMENT)
            excludeTokens(tokens, rangesToGlue, openingPrefix, outerExcludedTokenIndexes)
            offset = openingPrefix.last
            continue
          }
        }
        '{' -> {
          val expressionEnd = MdxExpressionBoundaryScanner.findExpressionEnd(text, offset, limit)
          if (expressionEnd != -1) {
            addNode(result, tokens, offset..expressionEnd, MdxMarkdownLibElementTypes.MDX_EXPRESSION)
            excludeTokens(tokens, rangesToGlue, offset..expressionEnd, outerExcludedTokenIndexes)
            offset = expressionEnd
            continue
          }
        }
      }
      offset++
    }

    result.withFurtherProcessing(parsingSpace(tokens, rangesToGlue, excludedTokenIndexes = outerExcludedTokenIndexes))
    for (innerParsingSpace in innerParsingSpaces) {
      result.withFurtherProcessing(innerParsingSpace)
    }
    return result
  }

  private fun addNode(result: SequentialParser.ParsingResultBuilder,
                      tokens: TokensCache,
                      charRange: IntRange,
                      type: IElementType): IntRange? {
    val tokenRange = tokens.toTokenRange(charRange) ?: return null
    result.withNode(SequentialParser.Node(tokenRange, type))
    return tokenRange
  }

  private fun excludeTokens(tokens: TokensCache,
                            parsingRanges: List<IntRange>,
                            charRange: IntRange,
                            excludedTokenIndexes: MutableSet<Int>) {
    var iterator: TokensCache.Iterator = tokens.RangesListIterator(parsingRanges)
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

  private fun parsingSpace(
    tokens: TokensCache,
    parsingRanges: List<IntRange>,
    charRange: IntRange? = null,
    excludedTokenIndexes: Set<Int>,
  ): List<IntRange> {
    val result = RangesListBuilder()
    var iterator: TokensCache.Iterator = tokens.RangesListIterator(parsingRanges)
    while (iterator.type != null) {
      val isInsideRange = charRange == null || iterator.end > charRange.first && iterator.start < charRange.last
      if (isInsideRange && iterator.index !in excludedTokenIndexes) {
        result.put(iterator.index)
      }
      if (charRange != null && iterator.start >= charRange.last) {
        break
      }
      iterator = iterator.advance()
    }
    return result.get()
  }

  private fun elementContentRange(element: MdxJsxScanner.Element): IntRange? {
    val opening = element.tags.firstOrNull() ?: return null
    val closing = element.tags.lastOrNull() ?: return null
    if (opening.kind != MdxJsxScanner.TagKind.OPENING || closing.kind != MdxJsxScanner.TagKind.CLOSING) {
      return null
    }
    return (opening.range.last..closing.range.first).takeIf { it.first < it.last }
  }

  private fun opaqueRanges(tokens: TokensCache, parsingRanges: List<IntRange>): MdxOpaqueRanges {
    val allowed = BooleanArray(tokens.filteredTokens.size)
    for (range in parsingRanges) {
      for (index in range) {
        if (index in allowed.indices) allowed[index] = true
      }
    }

    val ranges = mutableListOf<IntRange>()
    var index = 0
    while (index < allowed.size) {
      if (allowed[index]) {
        index++
        continue
      }
      val start = tokens.Iterator(index).start
      var end = tokens.Iterator(index).end
      index++
      while (index < allowed.size && !allowed[index]) {
        end = tokens.Iterator(index).end
        index++
      }
      ranges.add(start..end)
    }
    return MdxOpaqueRanges.of(ranges)
  }

  private fun IntRange.isStrictlyInside(parent: IntRange): Boolean {
    return parent.first <= first && last <= parent.last && (parent.first != first || parent.last != last)
  }
}

internal fun TokensCache.toTokenRange(charRange: IntRange): IntRange? {
  var iterator: TokensCache.Iterator = Iterator(0)
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
