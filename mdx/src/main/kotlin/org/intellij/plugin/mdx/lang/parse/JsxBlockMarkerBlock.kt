package org.intellij.plugin.mdx.lang.parse

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.*
import org.intellij.markdown.parser.markerblocks.MarkdownParserUtil
import org.intellij.markdown.parser.markerblocks.MarkerBlock
import org.intellij.markdown.parser.markerblocks.MarkerBlockImpl
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import java.lang.Integer.min
import java.util.*

class JsxBlockMarkerBlock(myConstraints: MarkdownConstraints,
                          private val productionHolder: ProductionHolder,
                          private val endCheckingRegex: Regex?,
                          private val isExportImport: Boolean,
                          private var tagOrBracketStack: Stack<CharSequence>?
) : MarkerBlockImpl(myConstraints, productionHolder.mark()) {

    private var realInterestingOffset = -1

    private var hadEmptyString = false

    override fun allowsSubBlocks(): Boolean = false

    override fun isInterestingOffset(pos: LookaheadText.Position): Boolean = true

    override fun getDefaultAction(): MarkerBlock.ClosingAction {
        return MarkerBlock.ClosingAction.DONE
    }

    override fun doProcessToken(pos: LookaheadText.Position, currentConstraints: MarkdownConstraints): MarkerBlock.ProcessingResult {
        if (!isExportImport) {
            if (pos.offsetInCurrentLine != -1) {
                return MarkerBlock.ProcessingResult.CANCEL
            }

            val prevLine = pos.prevLine ?: return MarkerBlock.ProcessingResult.DEFAULT
            if (!constraints.applyToNextLine(pos).extendsPrev(constraints)) {
                return MarkerBlock.ProcessingResult.DEFAULT
            }

            if (endCheckingRegex == null && MarkdownParserUtil.calcNumberOfConsequentEols(pos, constraints) >= 2 && tagOrBracketStack.isNullOrEmpty()) {
                return MarkerBlock.ProcessingResult.DEFAULT
            } else if (endCheckingRegex != null && endCheckingRegex.find(prevLine) != null) {
                return MarkerBlock.ProcessingResult.DEFAULT
            }

            if (pos.currentLine.isNotEmpty()) {
                productionHolder.addProduction(listOf(SequentialParser.Node(
                        pos.offset + constraints.getCharsEaten(pos.currentLine)..pos.offset + 1 + constraints.getCharsEaten(pos.currentLine),
                        MdxTokenTypes.JSX_BLOCK_CONTENT)))
                JsxBlockUtil.parseParenthesis(pos, tagOrBracketStack!!, productionHolder, constraints, true)
            }
        } else {

            if (pos.offset < realInterestingOffset) {
                return MarkerBlock.ProcessingResult.CANCEL
            }
            // Eat everything if we're on the first line of import statement
            if (pos.char != '\n') {
                return MarkerBlock.ProcessingResult.CANCEL
            }

            assert(pos.char == '\n')

            val nextLineConstraints = constraints.applyToNextLineAndAddModifiers(pos)
            if (!nextLineConstraints.extendsPrev(constraints)) {
                return MarkerBlock.ProcessingResult.DEFAULT
            }

            val nextLineOffset = pos.nextLineOrEofOffset
            realInterestingOffset = nextLineOffset
            var currentLine: CharSequence
            if (nextLineConstraints.indent > pos.currentLine.length) {
                currentLine = pos.currentLine.subSequence(0, pos.currentLine.length)
            } else {
                currentLine = pos.currentLine.subSequence(nextLineConstraints.indent, pos.currentLine.length)
            }

            if (currentLine.isEmpty()) {
                productionHolder.addProduction(listOf(SequentialParser.Node(
                        nextLineOffset - 1..nextLineOffset, MdxTokenTypes.JSX_BLOCK_CONTENT)))
                hadEmptyString = true
                return MarkerBlock.ProcessingResult.CANCEL
            } else if (hadEmptyString) {
                productionHolder.addProduction(listOf(SequentialParser.Node(
                        pos.offset..pos.offset + 1, MdxTokenTypes.JSX_BLOCK_CONTENT)))
                if (tagOrBracketStack?.empty()!!) {
                    return MarkerBlock.ProcessingResult.DEFAULT
                } else {
                    hadEmptyString = false
                    var endOfRange = nextLineOffset
                    if (!pos.nextLine.isNullOrEmpty()) {
                        endOfRange++
                    }
                    val contentRange = (pos.offset + 1 + constraints.indent).coerceAtMost(nextLineOffset)..endOfRange
                    if (contentRange.first < contentRange.last) {
                        JsxBlockUtil.parseExportParenthesis(pos, tagOrBracketStack!!)
                        productionHolder.addProduction(listOf(SequentialParser.Node(
                                contentRange, MdxTokenTypes.JSX_BLOCK_CONTENT)))
                    }
                }
            } else {
                var endOfRange = nextLineOffset
                if (!pos.nextLine.isNullOrEmpty()) {
                    endOfRange++
                }
                val contentRange = (pos.offset + 1 + constraints.indent).coerceAtMost(nextLineOffset)..endOfRange
                if (contentRange.first < contentRange.last) {
                    JsxBlockUtil.parseExportParenthesis(pos, tagOrBracketStack!!)
                    productionHolder.addProduction(listOf(SequentialParser.Node(
                            contentRange, MdxTokenTypes.JSX_BLOCK_CONTENT)))
                }
            }

        }
        return MarkerBlock.ProcessingResult.CANCEL
    }

    override fun calcNextInterestingOffset(pos: LookaheadText.Position): Int {
        return pos.offset + 1
    }

    override fun getDefaultNodeType(): IElementType {
        return MdxElementTypes.JSX_BLOCK
    }
}