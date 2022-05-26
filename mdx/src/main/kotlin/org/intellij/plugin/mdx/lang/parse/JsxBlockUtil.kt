package org.intellij.plugin.mdx.lang.parse

import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.parser.LookaheadText
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.constraints.MarkdownConstraints
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.plugin.mdx.lang.parse.JsxBlockProvider.Companion.ATTRIBUTES_REGEX
import org.intellij.plugin.mdx.lang.parse.JsxBlockProvider.Companion.TAG_REGEX
import java.util.*

object JsxBlockUtil {
    fun parseParenthesis(pos: LookaheadText.Position,
                         tagStack: Stack<CharSequence>,
                         productionHolder: ProductionHolder,
                         constraints: MarkdownConstraints,
                         hasEnter: Boolean) {
        val groups: MutableList<Pair<String, IntRange>> = mutableListOf()
        val text = if (pos.offsetInCurrentLine >= 0) {pos.currentLineFromPosition} else {pos.currentLine}
        
        //only if it isn't an attribute
        if (!ATTRIBUTES_REGEX.matches(text)) {
            TAG_REGEX.findAll(text).iterator().forEach {
                groups.add(Pair(it.groupValues[0], it.range))
            }
        }
        val delta = if (hasEnter) {
            1 + pos.offset
        } else {
            pos.offset
        }
        var myPos = 0
        var nextGroupInd = 0
        while (myPos < text.length) {
            if (nextGroupInd < groups.size && groups[nextGroupInd].second.first == myPos) {
                val tokenSequence = groups[nextGroupInd].first
                if (tokenSequence.matches(JsxBlockProvider.OPEN_TAG_REGEX) && !tokenSequence.matches(Regex(JsxBlockProvider.EMPTY_TAG))) {
                    tagStack.push(tokenSequence)
                } else if (tokenSequence.matches(JsxBlockProvider.CLOSE_TAG_REGEX)) {
                    if (!tagStack.empty() && tagStack.peek().matches(JsxBlockProvider.OPEN_TAG_REGEX)) {
                        //                        assert(tagStack.peek() == tokenSequence)
                        tagStack.pop()
                    } else {
                        tagStack.push(tokenSequence)
                    }
                }
                productionHolder.addProduction(listOf(SequentialParser.Node(
                        groups[nextGroupInd].second.first + delta..groups[nextGroupInd].second.last + 1 + delta, MdxTokenTypes.JSX_BLOCK_CONTENT)))
                myPos = groups[nextGroupInd].second.last + 1
                nextGroupInd++
            } else {
                val rangeEnd = if (nextGroupInd < groups.size) {
                    groups[nextGroupInd].second.first
                } else {
                    text.length
                }
                val myRange = myPos + delta..rangeEnd + delta
                if (tagStack.size == 0 || tagStack.peek().matches(JsxBlockProvider.CLOSE_TAG_REGEX)) {
                    productionHolder.addProduction(listOf(SequentialParser.Node(
                            myRange, MarkdownTokenTypes.TEXT)))
                } else {
                    productionHolder.addProduction(listOf(SequentialParser.Node(
                            myRange, MdxTokenTypes.JSX_BLOCK_CONTENT)))
                }
                myPos = rangeEnd
            }
        }
    }
    fun parseExportParenthesis(pos: LookaheadText.Position,
                               bracketStack: Stack<CharSequence>){
        val groups: MutableList<String> = mutableListOf()
        val text = if (pos.offsetInCurrentLine >= 0) {pos.currentLineFromPosition} else {pos.currentLine}
        Regex("\\{|\\}|\\(|\\)").findAll(text).iterator().forEach {
            groups.add(it.groupValues[0])
        }
        for (group in groups) {
            if (group == "{" || group == "(") {
                bracketStack.push(group)
            } else {
                if (!bracketStack.empty()) {bracketStack.pop()}
            }
        }
    }
}
