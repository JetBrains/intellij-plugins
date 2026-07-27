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
import com.intellij.psi.formatter.xml.HtmlPolicy
import com.intellij.psi.formatter.xml.SyntheticBlock
import com.intellij.psi.templateLanguages.SimpleTemplateLanguageFormattingModelBuilder
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlTag
import org.intellij.plugin.mdx.lang.parse.MdxElementTypes
import org.intellij.plugin.mdx.lang.parse.MdxTokenTypes
import org.intellij.plugins.markdown.injection.MarkdownCodeFenceUtils
import org.intellij.plugins.markdown.lang.MarkdownElementType
import org.intellij.plugins.markdown.lang.MarkdownElementTypes
import org.intellij.plugins.markdown.lang.MarkdownTokenTypeSets

internal class MdxFormattingModelBuilder : TemplateLanguageFormattingModelBuilder() {
  override fun createTemplateLanguageBlock(node: ASTNode,
                                           wrap: Wrap?,
                                           alignment: Alignment?,
                                           foreignChildren: MutableList<DataLanguageBlockWrapper>?,
                                           codeStyleSettings: CodeStyleSettings): TemplateLanguageBlock {
    val documentModel = FormattingDocumentModelImpl.createOn(node.psi.containingFile)
    val mdxForeignChildren = filterForeignChildren(node, foreignChildren)
    return MdxBlock(this, codeStyleSettings, node, mdxForeignChildren, HtmlPolicy(codeStyleSettings, documentModel))
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
    collectSingleInlineJsxParagraphRanges(node, ignoredRanges)
    collectIgnoredElementRanges(node, ignoredRanges)
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

  private fun collectSingleInlineJsxParagraphRanges(node: ASTNode, ranges: MutableSet<TextRange>) {
    var child = node.firstChildNode
    while (child != null) {
      if (isSingleInlineJsxParagraph(child)) {
        ranges.add(child.textRange)
      }
      else {
        collectSingleInlineJsxParagraphRanges(child, ranges)
      }
      child = child.treeNext
    }
  }

  private fun isSingleInlineJsxParagraph(node: ASTNode): Boolean {
    if (node.elementType !== MarkdownElementTypes.PARAGRAPH) {
      return false
    }

    val jsxElementType = MarkdownElementType.platformType(MdxElementTypes.MDX_JSX_TEXT_ELEMENT)
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

  // Element types whose embedded JavaScript must not be reformatted by the JS formatter: ESM statements
  // and inline JSX/expressions in prose.
  private val ignoredForeignElementTypes = setOf(
    MarkdownElementType.platformType(MdxElementTypes.MDX_ESM_BLOCK),
    MarkdownElementType.platformType(MdxElementTypes.MDX_JSX_EXPRESSION),
    MarkdownElementType.platformType(MdxElementTypes.MDX_JSX_TEXT_ELEMENT),
  )

  private fun collectIgnoredElementRanges(node: ASTNode, ranges: MutableSet<TextRange>) {
    var child = node.firstChildNode
    while (child != null) {
      if (child.elementType in ignoredForeignElementTypes) {
        ranges.add(child.textRange)
      }
      else {
        collectIgnoredElementRanges(child, ranges)
      }
      child = child.treeNext
    }
  }

  private class MdxBlock(blockFactory: TemplateLanguageBlockFactory,
                         settings: CodeStyleSettings,
                         node: ASTNode,
                         foreignChildren: List<DataLanguageBlockWrapper>?,
                         private val myHtmlPolicy: HtmlPolicy) : TemplateLanguageBlock(blockFactory, settings, node,
                                                                                                            foreignChildren) {

    override fun getTemplateTextElementType(): IElementType {
      return MarkdownElementType.platformType(MdxTokenTypes.JSX_BLOCK_CONTENT)
    }

    override fun getIndent(): Indent? {
      // Content nested inside a foreign XmlTag is already indented by that tag's own XmlTagBlock; if this
      // template block also indented, foreign-nested content would double-indent (e.g. <div><div><div>).
      // So the template layer contributes no indent of its own and defers to the foreign formatter.
      return Indent.getNoneIndent()
    }

    override fun isRequiredRange(range: TextRange): Boolean {
      return !range.subSequence(myNode.psi.containingFile.text).isBlank()
    }

    override fun isLeaf(): Boolean {
      if (myNode.elementType === MarkdownElementTypes.LINK_DEFINITION) {
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
      return if (myNode.elementType === MarkdownElementType.platformType(MdxElementTypes.MDX_JSX_FLOW_ELEMENT)) {
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


    private fun shouldWrapBeforeClosingTag(parent: DataLanguageBlockWrapper? = getForeignBlockParent()): Boolean {
      if (myNode.elementType !== MarkdownElementType.platformType(MdxElementTypes.MDX_JSX_CLOSING_ELEMENT)) {
        return false
      }
      val parentTag = parent?.node?.psi as? XmlTag ?: return false
      return parentTag.subTags.isNotEmpty() && !isInlineTag(parentTag)
    }

    private fun isInlineTag(tag: XmlTag): Boolean {
      return tag.name.firstOrNull()?.isUpperCase() == true || myHtmlPolicy.isTextElement(tag)
    }

    private fun createLineBreakSpacing(): Spacing {
      return Spacing.createSpacing(0, 0, 1, true, myHtmlPolicy.keepBlankLines)
    }

    // Mirrors MarkdownSpacingBuilder's own (module-internal, so not reusable here) rule enforcing exactly one
    // blank line after front matter; keepLineBreaks=false/keepBlankLines=0 makes the target fixed, not a floor.
    private fun createBlankLineAfterFrontMatterSpacing(): Spacing {
      return Spacing.createSpacing(0, 0, 2, false, 0)
    }
  }
}
