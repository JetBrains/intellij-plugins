package org.intellij.plugin.mdx.lang.parse

import com.intellij.openapi.util.TextRange
import org.intellij.markdown.IElementType
import org.intellij.markdown.parser.sequentialparsers.RangesListBuilder
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.TokensCache

internal class MdxInlineElementParser : SequentialParser {
  override fun parse(tokens: TokensCache, rangesToGlue: List<IntRange>): SequentialParser.ParsingResult {
    val result = SequentialParser.ParsingResultBuilder()
    if (rangesToGlue.isEmpty()) return result.withFurtherProcessing(emptyList())

    val tokenPartition = TokenPartition(tokens, rangesToGlue)
    val outerExcludedRanges = mutableListOf<TextRange>()
    val innerParsingSpaces = mutableListOf<List<IntRange>>()
    val text = tokens.originalText
    val opaqueRanges = tokenPartition.opaqueRanges()

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
            val rootRange = addNode(result, tokenPartition, element.range, MdxMarkdownLibElementTypes.MDX_JSX_TEXT_ELEMENT)
            val owners = arrayOfNulls<MdxJsxScanner.ElementRecord>(element.elements.size)
            for (owner in element.elements) {
              owners[owner.index] = owner
              if (owner.parentIndex != null) {
                addNode(result, tokenPartition, owner.range, MdxMarkdownLibElementTypes.MDX_JSX_TEXT_ELEMENT)
              }
            }
            val innerExcludedRanges = mutableListOf<TextRange>()
            for ((tagRange, tagKind, _, attributes) in element.tags) {
              val tagType = when (tagKind) {
                MdxJsxScanner.TagKind.OPENING -> MdxMarkdownLibElementTypes.MDX_JSX_OPENING_ELEMENT
                MdxJsxScanner.TagKind.CLOSING -> MdxMarkdownLibElementTypes.MDX_JSX_CLOSING_ELEMENT
                MdxJsxScanner.TagKind.SELF_CLOSING -> MdxMarkdownLibElementTypes.MDX_JSX_SELF_CLOSING_ELEMENT
              }
              val tagTokenRange = addNode(result, tokenPartition, tagRange, tagType)
              for (attribute in attributes) {
                val attributeRange = tokenPartition.toTokenRange(attribute)
                if (attributeRange != null && tagTokenRange != null && attributeRange.isStrictlyInside(tagTokenRange)) {
                  result.withNode(SequentialParser.Node(attributeRange, MdxMarkdownLibElementTypes.MDX_JSX_ATTRIBUTE))
                }
              }
              innerExcludedRanges.add(tagRange)
            }
            for (expression in element.expressions) {
              val expressionRange = tokenPartition.toTokenRange(expression)
              if (expressionRange != null && rootRange != null && expressionRange.isStrictlyInside(rootRange)) {
                result.withNode(SequentialParser.Node(expressionRange, MdxMarkdownLibElementTypes.MDX_EXPRESSION))
              }
              innerExcludedRanges.add(expression)
            }
            outerExcludedRanges.add(element.range)
            innerParsingSpaces.addAll(tokenPartition.bodyParsingSpaces(owners.map { checkNotNull(it) }, innerExcludedRanges))
            offset = element.range.endOffset
            continue
          }
          val openingPrefix = MdxJsxScanner.incompleteOpeningTagRange(text, offset, limit)
          if (openingPrefix != null) {
            addNode(result, tokenPartition, openingPrefix, MdxMarkdownLibElementTypes.MDX_JSX_TEXT_ELEMENT)
            addNode(result, tokenPartition, openingPrefix, MdxMarkdownLibElementTypes.MDX_JSX_OPENING_ELEMENT)
            outerExcludedRanges.add(openingPrefix)
            offset = openingPrefix.endOffset
            continue
          }
        }
        '{' -> {
          val expressionEnd = MdxExpressionBoundaryScanner.findExpressionEnd(text, offset, limit)
          if (expressionEnd != -1) {
            val expressionRange = TextRange(offset, expressionEnd)
            addNode(result, tokenPartition, expressionRange, MdxMarkdownLibElementTypes.MDX_EXPRESSION)
            outerExcludedRanges.add(expressionRange)
            offset = expressionEnd
            continue
          }
        }
      }
      offset++
    }

    result.withFurtherProcessing(tokenPartition.parsingSpace(excludedRanges = outerExcludedRanges))
    for (innerParsingSpace in innerParsingSpaces) {
      result.withFurtherProcessing(innerParsingSpace)
    }
    return result
  }

  private fun addNode(result: SequentialParser.ParsingResultBuilder,
                      tokenPartition: TokenPartition,
                      charRange: TextRange,
                      type: IElementType): IntRange? {
    val tokenRange = tokenPartition.toTokenRange(charRange) ?: return null
    result.withNode(SequentialParser.Node(tokenRange, type))
    return tokenRange
  }

  private fun IntRange.isStrictlyInside(parent: IntRange): Boolean {
    return parent.first <= first && last <= parent.last && (parent.first != first || parent.last != last)
  }

  private class TokenPartition(tokens: TokensCache, parsingRanges: List<IntRange>) {
    private val allTokens = collectTokens(tokens.Iterator(0))
    private val parsingTokenFlags = BooleanArray(tokens.filteredTokens.size).apply {
      for (range in parsingRanges) {
        val first = range.first.coerceAtLeast(0)
        val last = range.last.coerceAtMost(lastIndex)
        for (index in first..last) {
          this[index] = true
        }
      }
    }
    private val parsingTokens = allTokens.filter(::isParsingToken)

    fun opaqueRanges(): MdxTextRangeSet {
      val ranges = buildList {
        var position = 0
        while (position < allTokens.size) {
          if (isParsingToken(allTokens[position])) {
            position++
            continue
          }
          val start = allTokens[position].start
          var end = allTokens[position].end
          position++
          while (position < allTokens.size && !isParsingToken(allTokens[position])) {
            end = allTokens[position].end
            position++
          }
          if (start < end) add(TextRange(start, end))
        }
      }
      return MdxTextRangeSet.of(ranges)
    }

    fun toTokenRange(charRange: TextRange): IntRange? {
      val first = firstEndingAfter(allTokens, charRange.startOffset)
      val afterLast = firstStartingAtOrAfter(allTokens, charRange.endOffset)
      if (first >= afterLast) return null
      return allTokens[first].index..(allTokens[afterLast - 1].index + 1)
    }

    fun parsingSpace(excludedRanges: Collection<TextRange>): List<IntRange> {
      val exclusions = MdxTextRangeSet.of(excludedRanges)
      var exclusionIndex = 0
      val result = RangesListBuilder()
      for ((index, start, end) in parsingTokens) {
        while (exclusionIndex < exclusions.size && exclusions[exclusionIndex].endOffset <= start) {
          exclusionIndex++
        }
        val excluded = exclusionIndex < exclusions.size &&
                       exclusions[exclusionIndex].startOffset < end &&
                       start < exclusions[exclusionIndex].endOffset
        if (!excluded) {
          result.put(index)
        }
      }
      return result.get()
    }

    fun bodyParsingSpaces(
      owners: List<MdxJsxScanner.ElementRecord>,
      excludedRanges: Collection<TextRange>,
    ): List<List<IntRange>> {
      if (owners.isEmpty()) return emptyList()
      val root = owners.first()
      val builders = List(owners.size) { RangesListBuilder() }
      val exclusions = MdxTextRangeSet.of(excludedRanges)
      var exclusionIndex = 0
      var nextOwner = 0
      var owner: MdxJsxScanner.ElementRecord? = null
      val first = firstEndingAfter(parsingTokens, root.range.startOffset)
      val afterLast = firstStartingAtOrAfter(parsingTokens, root.range.endOffset)
      for (position in first..<afterLast) {
        val token = parsingTokens[position]
        while (nextOwner < owners.size && owners[nextOwner].range.startOffset < token.end) {
          owner = owners[nextOwner++]
        }
        while (owner != null && owner.range.endOffset <= token.start) {
          owner = owner.parentIndex?.let(owners::get)
        }
        while (exclusionIndex < exclusions.size && exclusions[exclusionIndex].endOffset <= token.start) {
          exclusionIndex++
        }
        if (exclusionIndex < exclusions.size && exclusions[exclusionIndex].startOffset < token.end) continue
        val body = owner?.bodyRange ?: continue
        if (body.startOffset <= token.start && token.end <= body.endOffset) builders[owner.index].put(token.index)
      }
      return builders.map { it.get() }.filter { it.isNotEmpty() }
    }

    private fun collectTokens(initialIterator: TokensCache.Iterator): List<IndexedToken> {
      return buildList {
        var iterator = initialIterator
        while (iterator.type != null) {
          add(IndexedToken(iterator.index, iterator.start, iterator.end))
          iterator = iterator.advance()
        }
      }
    }

    private fun isParsingToken(token: IndexedToken): Boolean {
      return token.index in parsingTokenFlags.indices && parsingTokenFlags[token.index]
    }

    private fun firstEndingAfter(tokens: List<IndexedToken>, offset: Int): Int {
      var low = 0
      var high = tokens.size
      while (low < high) {
        val middle = (low + high) ushr 1
        if (tokens[middle].end <= offset) low = middle + 1
        else high = middle
      }
      return low
    }

    private fun firstStartingAtOrAfter(tokens: List<IndexedToken>, offset: Int): Int {
      var low = 0
      var high = tokens.size
      while (low < high) {
        val middle = (low + high) ushr 1
        if (tokens[middle].start < offset) low = middle + 1
        else high = middle
      }
      return low
    }

    private data class IndexedToken(val index: Int, val start: Int, val end: Int)
  }
}
