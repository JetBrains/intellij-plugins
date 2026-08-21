package org.intellij.plugin.mdx.lang.template

import com.intellij.lang.javascript.types.JSEmbeddedBlockElementType
import com.intellij.lexer.Lexer
import com.intellij.openapi.util.TextRange
import com.intellij.psi.templateLanguages.TemplateDataElementType
import com.intellij.psi.templateLanguages.TemplateDataModifications
import com.intellij.psi.tree.TokenSet
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.accept
import org.intellij.markdown.ast.visitors.RecursiveVisitor
import org.intellij.markdown.parser.CancellationToken
import org.intellij.markdown.parser.MarkdownParser
import org.intellij.plugin.mdx.lang.MdxLanguage
import org.intellij.plugin.mdx.lang.parse.MdxFlavourDescriptor
import org.intellij.plugin.mdx.lang.parse.MdxExpressionBoundaryScanner
import org.intellij.plugin.mdx.lang.parse.MdxMarkdownLibElementTypes
import org.intellij.plugin.mdx.lang.parse.MdxTokenTypes
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes

object MdxTemplateDataElementType : MdxTemplateDataElementTypeBase(),
                                    JSEmbeddedBlockElementType {

  override fun isModule(): Boolean = true
}


open class MdxTemplateDataElementTypeBase : TemplateDataElementType("MDX_TEMPLATE_JSX",
                                                                    MdxLanguage,
                                                                    MdxTokenTypes.EMBEDDED_JS_CONTENT,
                                                                    MdxTemplateElementTypes.OUTER_MARKDOWN_CONTENT) {
  override fun collectTemplateModifications(sourceCode: CharSequence, baseLexer: Lexer): TemplateDataModifications {
    val structure = collectTemplateStructure(sourceCode)
    val comments = collectHtmlComments(sourceCode, baseLexer, structure.opaqueRanges)
    val invalidComments = comments.invalid
    val embeddedRanges = (structure.embeddedRanges.flatMap { it.subtract(comments.opaque) } + invalidComments)
      .sortedWith(compareBy<TextRange> { it.startOffset }.thenBy { it.endOffset })
      .mergeTouchingRanges()

    val operations = mutableListOf<Modification>()
    var offset = 0
    for (range in embeddedRanges) {
      if (offset < range.startOffset) {
        operations.add(Modification.Outer(TextRange.create(offset, range.startOffset)))
      }
      offset = maxOf(offset, range.endOffset)
    }
    if (offset < sourceCode.length) {
      operations.add(Modification.Outer(TextRange.create(offset, sourceCode.length)))
    }

    // JavaScript accepts `<!--` as a legacy line-comment opener. MDX explicitly rejects HTML
    // comments, so hide one dash and let the existing JSX parser report malformed markup.
    for (range in invalidComments) {
      operations.add(Modification.Outer(TextRange.create(range.startOffset + 3, range.startOffset + 4)))
    }

    for ((rootRange, rootKind) in structure.roots) {
      if (!rootKind.requiresStatementSeparator) continue
      for (range in structure.embeddedRanges.filter { rootRange.contains(it) }) {
        val continuesAtBoundary = structure.embeddedRanges.any {
          it !== range && it.startOffset == range.endOffset && it.endOffset > range.endOffset
        }
        if (!continuesAtBoundary && !endsWithSemicolon(sourceCode, range)) {
          operations.add(Modification.Remove(range.endOffset, ";"))
        }
      }
    }
    for (comment in invalidComments) {
      if (!endsWithSemicolon(sourceCode, comment)) {
        operations.add(Modification.Remove(comment.endOffset, ";"))
      }
    }

    val modifications = TemplateDataModifications()
    for (operation in operations.sortedWith(compareBy<Modification> { it.offset }.thenBy { it.priority })) {
      when (operation) {
        is Modification.Outer -> modifications.addOuterRange(operation.range, true)
        is Modification.Remove -> modifications.addRangeToRemove(operation.offset, operation.text)
      }
    }
    return modifications
  }

  /**
   * Derives template ranges from the same Markdown structure that drives the base PSI and editor
   * highlighter. This follows RMarkdown's template-data pattern and deliberately does not rescan JSX.
   */
  private fun collectTemplateStructure(sourceCode: CharSequence): TemplateStructure {
    val root = MarkdownParser(MdxFlavourDescriptor, cancellationToken = CancellationToken.NonCancellable)
      .parse(MarkdownElementType("MDX_TEMPLATE_ROOT"), sourceCode)
    val roots = mutableListOf<TemplateRoot>()
    val opaqueRanges = mutableListOf<TextRange>()
    var templateRootDepth = 0
    var opaqueDepth = 0
    root.accept(object : RecursiveVisitor() {
      override fun visitNode(node: ASTNode) {
        val rootKind = node.type.rootKind()
        if (rootKind != null && templateRootDepth == 0) {
          roots.add(TemplateRoot(TextRange(node.startOffset, node.endOffset), rootKind))
        }
        if (rootKind != null) templateRootDepth++
        val opaque = node.type in OPAQUE_MARKDOWN_ELEMENTS
        if (opaque && opaqueDepth == 0) {
          opaqueRanges.add(TextRange(opaqueStart(sourceCode, node.startOffset), node.endOffset))
        }
        if (opaque) opaqueDepth++
        super.visitNode(node)
        if (opaque) opaqueDepth--
        if (rootKind != null) templateRootDepth--
      }
    })
    val normalizedRoots = roots
      .map { it.withExactExpressionRange(sourceCode) }
      .mergeAdjacentEsm(sourceCode)
    val embeddedRanges = normalizedRoots.flatMap { templateRoot ->
      if (templateRoot.kind == RootKind.JSX) {
        templateRoot.range.subtract(opaqueRanges)
      }
      else {
        listOf(templateRoot.range)
      }
    }
    return TemplateStructure(embeddedRanges, normalizedRoots, opaqueRanges)
  }

  private fun TemplateRoot.withExactExpressionRange(sourceCode: CharSequence): TemplateRoot {
    if (kind != RootKind.EXPRESSION) return this
    val expressionEnd = MdxExpressionBoundaryScanner.findExpressionEnd(sourceCode, range.startOffset, range.endOffset)
    return if (expressionEnd == -1) this else copy(range = TextRange(range.startOffset, expressionEnd))
  }

  private fun List<TemplateRoot>.mergeAdjacentEsm(sourceCode: CharSequence): List<TemplateRoot> {
    if (isEmpty()) return emptyList()
    val result = mutableListOf<TemplateRoot>()
    var current = first()
    for (next in drop(1)) {
      val gap = sourceCode.subSequence(current.range.endOffset, next.range.startOffset)
      if (current.kind == RootKind.ESM && next.kind == RootKind.ESM &&
          gap.isNotEmpty() && gap.all { it.isWhitespace() } && gap.count { it == '\n' } == 1) {
        current = current.copy(range = TextRange(current.range.startOffset, next.range.endOffset))
      }
      else {
        result.add(current)
        current = next
      }
    }
    result.add(current)
    return result
  }

  private fun opaqueStart(sourceCode: CharSequence, startOffset: Int): Int {
    var lineStart = startOffset
    while (lineStart > 0 && sourceCode[lineStart - 1] != '\n') lineStart--
    return if (sourceCode.subSequence(lineStart, startOffset).all { it.isWhitespace() }) lineStart else startOffset
  }

  private fun TextRange.subtract(exclusions: List<TextRange>): List<TextRange> {
    val result = mutableListOf<TextRange>()
    var offset = startOffset
    for (exclusion in exclusions) {
      if (exclusion.endOffset <= offset || exclusion.startOffset >= endOffset) continue
      val exclusionStart = exclusion.startOffset.coerceAtLeast(startOffset)
      val exclusionEnd = exclusion.endOffset.coerceAtMost(endOffset)
      if (offset < exclusionStart) {
        result.add(TextRange(offset, exclusionStart))
      }
      offset = maxOf(offset, exclusionEnd)
    }
    if (offset < endOffset) {
      result.add(TextRange(offset, endOffset))
    }
    return result
  }

  private fun collectHtmlComments(sourceCode: CharSequence, baseLexer: Lexer, opaqueRanges: List<TextRange>): HtmlComments {
    val hardRanges = mutableListOf<TextRange>()
    baseLexer.start(sourceCode)
    while (baseLexer.tokenType != null) {
      if (baseLexer.tokenType in HARD_CODE_TOKENS) {
        hardRanges.add(TextRange(baseLexer.tokenStart, baseLexer.tokenEnd))
      }
      baseLexer.advance()
    }

    val invalid = mutableListOf<TextRange>()
    val opaque = mutableListOf<TextRange>()
    var start = sourceCode.indexOf("<!--")
    while (start >= 0) {
      val endMarker = sourceCode.indexOf("-->", start + 4)
      if (endMarker < 0) break
      val end = endMarker + 3
      val singleLine = (start until end).none { sourceCode[it] == '\n' }
      val inCode = (hardRanges + opaqueRanges).any { it.startOffset <= start && end <= it.endOffset }
      if (!inCode) {
        (if (singleLine) invalid else opaque).add(TextRange(start, end))
      }
      start = sourceCode.indexOf("<!--", end)
    }
    return HtmlComments(invalid, opaque)
  }

  private fun IElementType.rootKind(): RootKind? = when (this) {
    MdxMarkdownLibElementTypes.MDX_ESM_BLOCK -> RootKind.ESM
    MdxMarkdownLibElementTypes.MDX_JSX_FLOW_ELEMENT,
    MdxMarkdownLibElementTypes.MDX_JSX_TEXT_ELEMENT -> RootKind.JSX
    MdxMarkdownLibElementTypes.MDX_EXPRESSION -> RootKind.EXPRESSION
    else -> null
  }

  private fun List<TextRange>.mergeTouchingRanges(): List<TextRange> {
    if (isEmpty()) return emptyList()
    val result = mutableListOf<TextRange>()
    var current = first()
    for (range in drop(1)) {
      if (range.startOffset <= current.endOffset) {
        current = TextRange(current.startOffset, maxOf(current.endOffset, range.endOffset))
      }
      else {
        result.add(current)
        current = range
      }
    }
    result.add(current)
    return result
  }

  private fun endsWithSemicolon(sourceCode: CharSequence, range: TextRange): Boolean {
    var offset = range.endOffset - 1
    while (offset >= range.startOffset && sourceCode[offset].isWhitespace()) offset--
    return offset >= range.startOffset && sourceCode[offset] == ';'
  }

  private data class TemplateStructure(val embeddedRanges: List<TextRange>,
                                       val roots: List<TemplateRoot>,
                                       val opaqueRanges: List<TextRange>)

  private data class TemplateRoot(val range: TextRange, val kind: RootKind)

  private data class HtmlComments(val invalid: List<TextRange>, val opaque: List<TextRange>)

  private sealed class Modification(val offset: Int, val priority: Int) {
    class Remove(offset: Int, val text: String) : Modification(offset, 0)
    class Outer(val range: TextRange) : Modification(range.startOffset, 1)
  }

  private enum class RootKind(val requiresStatementSeparator: Boolean) {
    ESM(true),
    JSX(true),
    EXPRESSION(false),
  }

  private companion object {
    private val OPAQUE_MARKDOWN_ELEMENTS = setOf(
      MarkdownElementTypes.CODE_BLOCK,
      MarkdownElementTypes.CODE_FENCE,
      MarkdownElementTypes.CODE_SPAN,
    )

    private val HARD_CODE_TOKENS = TokenSet.create(
      MarkdownTokenTypes.CODE_FENCE_START,
      MarkdownTokenTypes.CODE_FENCE_CONTENT,
      MarkdownTokenTypes.CODE_FENCE_END,
      MarkdownTokenTypes.BACKTICK,
      MarkdownTokenTypes.ESCAPED_BACKTICKS,
      MarkdownTokenTypes.CODE_LINE,
    )
  }
}
