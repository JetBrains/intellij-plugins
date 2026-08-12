package org.intellij.plugin.mdx.format

import com.intellij.formatting.ASTBlock
import com.intellij.formatting.Alignment
import com.intellij.formatting.Block
import com.intellij.formatting.ChildAttributes
import com.intellij.formatting.FormattingContext
import com.intellij.formatting.FormattingModel
import com.intellij.formatting.Indent
import com.intellij.formatting.Spacing
import com.intellij.formatting.Wrap
import com.intellij.formatting.WrapType
import com.intellij.formatting.templateLanguages.DataLanguageBlockWrapper
import com.intellij.formatting.templateLanguages.TemplateLanguageBlock
import com.intellij.formatting.templateLanguages.TemplateLanguageBlockFactory
import com.intellij.formatting.templateLanguages.TemplateLanguageFormattingModelBuilder
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.DocumentBasedFormattingModel
import com.intellij.psi.formatter.FormatterUtil
import com.intellij.psi.formatter.FormattingDocumentModelImpl
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.formatter.xml.SyntheticBlock
import com.intellij.psi.templateLanguages.SimpleTemplateLanguageFormattingModelBuilder
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import org.intellij.plugin.mdx.lang.parse.MdxElementTypes
import org.intellij.plugin.mdx.lang.parse.MdxTokenTypes
import org.intellij.plugins.markdown.injection.MarkdownCodeFenceUtils
import org.intellij.plugins.markdown.lang.MarkdownElementTypes
import org.intellij.plugins.markdown.lang.MarkdownTokenTypeSets
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes
import org.intellij.plugins.markdown.lang.formatter.settings.MarkdownCustomCodeStyleSettings

internal class MdxFormattingModelBuilder : TemplateLanguageFormattingModelBuilder() {
  override fun createTemplateLanguageBlock(node: ASTNode,
                                           wrap: Wrap?,
                                           alignment: Alignment?,
                                           foreignChildren: MutableList<DataLanguageBlockWrapper>?,
                                           codeStyleSettings: CodeStyleSettings): TemplateLanguageBlock {
    if (isWrapEligibleMarkdownText(node, foreignChildren)) {
      return MdxTextWrappingBlock(this, codeStyleSettings, node, wrap, alignment)
    }

    val documentModel = FormattingDocumentModelImpl.createOn(node.psi.containingFile)
    val mdxForeignChildren = filterForeignChildren(node, foreignChildren)
    return MdxBlock(this, codeStyleSettings, node, mdxForeignChildren, MdxJsxHtmlPolicy(codeStyleSettings, documentModel))
  }

  private fun isWrapEligibleMarkdownText(node: ASTNode, foreignChildren: List<DataLanguageBlockWrapper>?): Boolean {
    if (node.elementType !== MarkdownTokenTypes.TEXT ||
        node.treeParent?.elementType !== MarkdownElementTypes.PARAGRAPH ||
        !foreignChildren.isNullOrEmpty()) {
      return false
    }

    var ancestor = node.treeParent
    while (ancestor != null) {
      if (ancestor.elementType === MdxElementTypes.MDX_JSX_FLOW_ELEMENT ||
          ancestor.elementType === MdxElementTypes.MDX_JSX_TEXT_ELEMENT) {
        return false
      }
      ancestor = ancestor.treeParent
    }
    return true
  }

  override fun createModel(formattingContext: FormattingContext): FormattingModel {
    val file = formattingContext.containingFile
    val node = formattingContext.psiElement.node
    if (node.elementType === MdxTokenTypes.OUTER_ELEMENT_TYPE) {
      return SimpleTemplateLanguageFormattingModelBuilder().createModel(formattingContext)
    }
    // Fall back to a no-op dummy model only while the PSI is transiently broken (e.g. an unbalanced `{`
    // typed before auto-close inserts `}`), where the parser mislabels tokens and the real formatter crashes.
    if (!isMdxPsiComplete(file)) {
      val cs = formattingContext.codeStyleSettings
      return DocumentBasedFormattingModel(createDummyBlock(file.node), formattingContext.project, cs, file.fileType, file)
    }
    return DocumentBasedFormattingModel(getRootBlock(file, file.viewProvider, formattingContext.codeStyleSettings), formattingContext.project, formattingContext.codeStyleSettings, file.fileType, file)
  }

  // False when the MDX PSI is transiently incomplete (e.g. an unclosed `{`/code fence swallows a following
  // JSX tag), so the parser mislabels JSX tokens as PsiWhiteSpace, leaving a non-blank gap that crashes the
  // formatter.
  private fun isMdxPsiComplete(file: PsiFile): Boolean {
    var child: ASTNode? = file.node?.firstChildNode ?: return true
    var sawSubstantialChild = false
    while (child != null) {
      if (FormatterUtil.containsWhiteSpacesOnly(child)) {
        // containsWhiteSpacesOnly trusts the node's declared WHITE_SPACE type without checking its actual
        // characters, so a mislabeled node needs its text verified here to be caught.
        if (!child.chars.isBlank()) {
          return false
        }
      }
      else if (child.textLength > 0 && !sawSubstantialChild) {
        if (child.startOffset != 0) {
          return false
        }
        sawSubstantialChild = true
      }
      child = child.treeNext
    }
    return true
  }

  override fun dontFormatMyModel(): Boolean {
    return false
  }

  private fun filterForeignChildren(node: ASTNode, foreignChildren: MutableList<DataLanguageBlockWrapper>?): List<DataLanguageBlockWrapper>? {
    if (foreignChildren.isNullOrEmpty() || isSingleInlineJsxParagraph(node)) {
      return null
    }

    val ignoredRanges = mutableSetOf<TextRange>()
    collectIgnoredForeignRanges(node, foreignChildren, ignoredRanges)
    if (ignoredRanges.isEmpty()) {
      return foreignChildren
    }

    val result = mutableListOf<DataLanguageBlockWrapper>()
    for (foreignChild in foreignChildren) {
      if (!isIgnoredForeignChild(foreignChild, ignoredRanges)) {
        result.add(foreignChild)
      }
    }
    return result
  }

  private fun isIgnoredForeignChild(foreignChild: DataLanguageBlockWrapper, ignoredRanges: Set<TextRange>): Boolean {
    return ignoredRanges.any { it.contains(foreignChild.textRange) }
  }

  private fun isSingleInlineJsxParagraph(node: ASTNode): Boolean {
    if (node.elementType !== MarkdownElementTypes.PARAGRAPH) {
      return false
    }

    val jsxElementType = MdxElementTypes.MDX_JSX_TEXT_ELEMENT
    var child = node.firstChildNode
    var jsxChild: ASTNode? = null
    while (child != null) {
      if (!FormatterUtil.containsWhiteSpacesOnly(child)) {
        if (child.elementType !== jsxElementType || jsxChild != null) {
          return false
        }
        jsxChild = child
      }
      child = child.treeNext
    }
    return jsxChild?.textRange == node.textRange
  }

  // Element types whose embedded JavaScript must not be reformatted by the JS formatter: inline
  // JSX/expressions in Markdown text.
  private val ignoredForeignElementTypes = setOf(
    MdxElementTypes.MDX_EXPRESSION,
    MdxElementTypes.MDX_JSX_TEXT_ELEMENT,
  )

  private val esmBlockElementType = MdxElementTypes.MDX_ESM_BLOCK

  /**
   * Single walk collecting every host-tree range whose embedded JavaScript must stay untouched by the JS
   * formatter: a lone inline JSX paragraph, an inline JSX/expression element in Markdown text, or a plain ESM
   * (import/export) statement with no JSX in it. A plain import/export declaration stays opaque because
   * reformatting one can trip a formatter block-covering assertion when multiple ESM statements are packed
   * onto one line with no separating whitespace; an ESM statement whose body actually contains JSX is
   * exempted so e.g. `export function f() { return <div/> }` is formatter-aware like a real .tsx file,
   * matching how real TS/TSX reformats import/export statements without touching the import line itself.
   * A matched node is a boundary (added, not recursed into); the three conditions never overlap since
   * they key off disjoint element types.
   */
  private fun collectIgnoredForeignRanges(node: ASTNode, foreignChildren: List<DataLanguageBlockWrapper>, ranges: MutableSet<TextRange>) {
    var child = node.firstChildNode
    while (child != null) {
      when {
        isSingleInlineJsxParagraph(child) -> ranges.add(child.textRange)
        child.elementType in ignoredForeignElementTypes -> ranges.add(child.textRange)
        child.elementType === esmBlockElementType && !esmBlockContainsJsx(child.textRange, foreignChildren) -> ranges.add(child.textRange)
        else -> collectIgnoredForeignRanges(child, foreignChildren, ranges)
      }
      child = child.treeNext
    }
  }

  private fun esmBlockContainsJsx(esmRange: TextRange, foreignChildren: List<DataLanguageBlockWrapper>): Boolean {
    return foreignChildren.any { esmRange.contains(it.textRange) && containsJsxTag(it) }
  }

  private fun containsJsxTag(foreignChild: DataLanguageBlockWrapper): Boolean {
    val psi = foreignChild.node?.psi ?: return false
    return psi is XmlTag || PsiTreeUtil.findChildOfType(psi, XmlTag::class.java) != null
  }

  /**
   * Markdown's formatter reflows paragraphs by splitting each text token into word-range blocks. Template-language
   * formatting would otherwise expose the entire token as one block, so Reformat Code has nowhere to insert a wrap.
   */
  private class MdxTextWrappingBlock(
    blockFactory: TemplateLanguageBlockFactory,
    settings: CodeStyleSettings,
    node: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
  ) : TemplateLanguageBlock(node, wrap, alignment, blockFactory, settings, null) {
    override fun getTemplateTextElementType(): IElementType = MdxTokenTypes.EMBEDDED_JS_CONTENT

    override fun getIndent(): Indent = Indent.getNoneIndent()

    override fun isLeaf(): Boolean = false

    override fun buildChildren(): List<Block> {
      val markdownSettings = settings.getCustomSettings(MarkdownCustomCodeStyleSettings::class.java)
      val wrapping = Wrap.createWrap(
        if (markdownSettings.WRAP_TEXT_IF_LONG) WrapType.NORMAL else WrapType.NONE,
        false,
      )
      return splitTextForWrapping(node.text).map { range ->
        MdxTextRangeBlock(node, range.shiftRight(node.startOffset), wrapping)
      }.toList()
    }

    override fun getSpacing(child1: Block?, child2: Block): Spacing {
      val markdownSettings = settings.getCustomSettings(MarkdownCustomCodeStyleSettings::class.java)
      val maxSpaces = if (markdownSettings.FORCE_ONE_SPACE_BETWEEN_WORDS) 1 else Integer.MAX_VALUE
      return Spacing.createSpacing(1, maxSpaces, 0, markdownSettings.KEEP_LINE_BREAKS_INSIDE_TEXT_BLOCKS, 0)
    }
  }

  private class MdxTextRangeBlock(node: ASTNode, private val range: TextRange, wrap: Wrap) : AbstractBlock(node, wrap, null) {
    override fun getTextRange(): TextRange = range

    override fun buildChildren(): List<Block> = emptyList()

    override fun getSpacing(child1: Block?, child2: Block): Spacing? = null

    override fun getIndent(): Indent = Indent.getNoneIndent()

    override fun isLeaf(): Boolean = true
  }

  private class MdxBlock(blockFactory: TemplateLanguageBlockFactory,
                         settings: CodeStyleSettings,
                         node: ASTNode,
                         foreignChildren: List<DataLanguageBlockWrapper>?,
                         private val myJsxPolicy: MdxJsxHtmlPolicy) : TemplateLanguageBlock(blockFactory, settings, node,
                                                                                                            foreignChildren) {

    override fun getTemplateTextElementType(): IElementType {
      return MdxTokenTypes.EMBEDDED_JS_CONTENT
    }

    override fun getIndent(): Indent? {
      // Content nested inside a foreign XmlTag is already indented by that tag's own XmlTagBlock; if this
      // template block also indented, foreign-nested content would double-indent (e.g. <div><div><div>).
      // So the template layer contributes no indent of its own and defers to the foreign formatter.
      if (MarkdownCodeFenceUtils.isCodeFence(myNode)) {
        val flowType = MdxElementTypes.MDX_JSX_FLOW_ELEMENT
        var ancestor = myNode.treeParent
        while (ancestor != null) {
          if (ancestor.elementType === flowType) return Indent.getNormalIndent()
          ancestor = ancestor.treeParent
        }
      }
      return Indent.getNoneIndent()
    }

    override fun isLeaf(): Boolean {
      if (myNode.elementType === MarkdownElementTypes.LINK_DEFINITION) {
        return true
      }
      // The Markdown parser represents block quote continuation markers (`>`) as whitespace nodes. The
      // template formatter normally omits whitespace-only children, which leaves those markers uncovered
      // when it is asked for indentation while hard-wrapping a quoted line. Keep the quote opaque instead:
      // Markdown's enter handler still adds the continuation marker after the indentation is calculated.
      if (myNode.elementType === MarkdownElementTypes.BLOCK_QUOTE) {
        return true
      }
      // A code fence is opaque to the formatter (its body is reformatted by MdxCodeFencePostFormatProcessor),
      // which also avoids tripping the "nonempty text is not covered by block" assertion when a
      // less-indented body line is mis-lexed so its leading chars land in a non-blank whitespace token.
      if (MarkdownCodeFenceUtils.isCodeFence(myNode)) {
        return true
      }
      return super.isLeaf()
    }

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
      // Read-only spacing preserves a top-level Markdown list's relative item depth, but a list inside a JSX
      // flow element must be re-indented to the element's body level by the foreign XmlTagBlock instead —
      // freezing it there would leave later items flush while the first indents.
      if (isMarkdownListNode(myNode) && getForeignBlockParent() == null) {
        return Spacing.getReadOnlySpacing()
      }
      // MDX's own block tree never consults MarkdownSpacingBuilder, so its "single blank line after front
      // matter" rule never fires here; without this the gap can grow on repeated reformat instead of settling.
      if ((child1 as? ASTBlock)?.node?.elementType === MarkdownElementTypes.FRONT_MATTER_HEADER) {
        return createBlankLineAfterFrontMatterSpacing()
      }
      if (child1 != null && child2 is MdxBlock && child2.shouldWrapBeforeClosingTag()) {
        return createLineBreakSpacing()
      }
      return super.getSpacing(child1, child2)
    }

    override fun getLeftNeighborSpacing(leftNeighbor: Block?, parent: DataLanguageBlockWrapper, thisBlockIndex: Int): Spacing? {
      if (leftNeighbor != null && shouldWrapBeforeClosingTag(parent)) {
        return createLineBreakSpacing()
      }
      return super.getLeftNeighborSpacing(leftNeighbor, parent, thisBlockIndex)
    }

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
      // A new line inside a JSX flow element's body (e.g. Enter after a nested code fence) indents one level
      // under the opening tag; other blocks defer, preserving the caret line's existing indentation.
      return if (myNode.elementType === MdxElementTypes.MDX_JSX_FLOW_ELEMENT) {
        ChildAttributes(Indent.getNormalIndent(), null)
      }
      else {
        ChildAttributes(Indent.getNoneIndent(), null)
      }
    }

    private fun getForeignBlockParent(): DataLanguageBlockWrapper? {
      var foreignBlockParent: DataLanguageBlockWrapper? = null
      var parent = parent
      while (parent != null) {
        if (parent is DataLanguageBlockWrapper && parent.original !is SyntheticBlock) {
          foreignBlockParent = parent
          break
        }
        parent = parent.parent
      }
      return foreignBlockParent
    }

    private fun isMarkdownListNode(node: ASTNode): Boolean {
      return node.elementType === MarkdownElementTypes.LIST_ITEM || node.elementType in MarkdownTokenTypeSets.LISTS
    }

    // A closing tag lives in the host tree as its own MDX_JSX_CLOSING_ELEMENT block, separate from the
    // continuous foreign XmlTag block a real .tsx file's closing tag is part of — so unlike a real .tsx file,
    // it doesn't automatically inherit a line break from a child tag that wrapped. Mirror what the same JSX
    // policy would decide for a continuous tree: wrap before the closing tag iff at least one subtag would
    // itself force its own line (matches real TSX, e.g. `<div><span>a</span></div>` stays on one line, but
    // `<div><span>a</span><div>b</div></div>` puts `<div>b</div>` and the closing `</div>` on their own lines).
    private fun shouldWrapBeforeClosingTag(parent: DataLanguageBlockWrapper? = getForeignBlockParent()): Boolean {
      if (myNode.elementType !== MdxElementTypes.MDX_JSX_CLOSING_ELEMENT) {
        return false
      }
      val parentTag = parent?.node?.psi as? XmlTag ?: return false
      return parentTag.subTags.any { myJsxPolicy.getWrappingTypeForTagBegin(it) == WrapType.ALWAYS }
    }

    private fun createLineBreakSpacing(): Spacing {
      return Spacing.createSpacing(0, 0, 1, true, myJsxPolicy.keepBlankLines)
    }

    // Mirrors MarkdownSpacingBuilder's own (module-internal, so not reusable here) rule enforcing exactly one
    // blank line after front matter; keepLineBreaks=false/keepBlankLines=0 makes the target fixed, not a floor.
    private fun createBlankLineAfterFrontMatterSpacing(): Spacing {
      return Spacing.createSpacing(0, 0, 2, false, 0)
    }
  }
}

private fun splitTextForWrapping(text: String): Sequence<TextRange> = sequence {
  var start = -1
  var length = -1
  for ((index, char) in text.withIndex()) {
    if (char == ' ' || char == '\t' || char == '\n') {
      if (length > 0) {
        yield(TextRange.from(start, length))
      }
      start = -1
      length = -1
    }
    else {
      if (start == -1) {
        start = index
        length = 0
      }
      length++
    }
  }
  if (length > 0) {
    yield(TextRange.from(start, length))
  }
}
