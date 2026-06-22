package org.intellij.plugin.mdx.format

import com.intellij.formatting.Alignment
import com.intellij.formatting.Block
import com.intellij.formatting.ChildAttributes
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
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.DocumentBasedFormattingModel
import com.intellij.psi.formatter.FormattingDocumentModelImpl
import com.intellij.psi.formatter.FormatterUtil
import com.intellij.psi.formatter.xml.HtmlPolicy
import com.intellij.psi.formatter.xml.SyntheticBlock
import com.intellij.psi.templateLanguages.SimpleTemplateLanguageFormattingModelBuilder
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlTag
import org.intellij.plugin.mdx.lang.parse.MdxElementTypes
import org.intellij.plugin.mdx.lang.parse.MdxTokenTypes
import org.intellij.plugins.markdown.lang.MarkdownElementType
import org.intellij.plugins.markdown.lang.MarkdownElementTypes

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

  override fun createModel(element: PsiElement, settings: CodeStyleSettings): FormattingModel {
    val file = element.containingFile
    val node = element.node
    val rootBlock = if (node.elementType === MdxTokenTypes.OUTER_ELEMENT_TYPE) {
      return SimpleTemplateLanguageFormattingModelBuilder().createModel(element, settings)
    }
    else {
      getRootBlock(file, file.viewProvider, settings)
    }
    return DocumentBasedFormattingModel(rootBlock, element.project, settings, file.fileType, file)
  }

  override fun dontFormatMyModel(): Boolean {
    return false
  }

  private fun filterForeignChildren(node: ASTNode, foreignChildren: MutableList<DataLanguageBlockWrapper>?): List<DataLanguageBlockWrapper>? {
    if (foreignChildren == null || foreignChildren.isEmpty() || isSingleInlineJsxParagraph(node)) {
      return null
    }

    val ignoredRanges = mutableSetOf<TextRange>()
    collectSingleInlineJsxParagraphRanges(node, ignoredRanges)
    collectEsmBlockRanges(node, ignoredRanges)
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

  private fun collectEsmBlockRanges(node: ASTNode, ranges: MutableSet<TextRange>) {
    val esmElementType = MarkdownElementType.platformType(MdxElementTypes.MDX_ESM_BLOCK)
    var child = node.firstChildNode
    while (child != null) {
      if (child.elementType === esmElementType) {
        ranges.add(child.textRange)
      }
      else {
        collectEsmBlockRanges(child, ranges)
      }
      child = child.treeNext
    }
  }

  private class MdxBlock internal constructor(blockFactory: TemplateLanguageBlockFactory,
                                              settings: CodeStyleSettings,
                                              node: ASTNode,
                                              foreignChildren: List<DataLanguageBlockWrapper>?,
                                              private val myHtmlPolicy: HtmlPolicy) : TemplateLanguageBlock(blockFactory, settings, node,
                                                                                                            foreignChildren) {

    override fun getTemplateTextElementType(): IElementType {
      return MarkdownElementType.platformType(MdxTokenTypes.JSX_BLOCK_CONTENT)
    }

    override fun getIndent(): Indent? {
      if (myNode.text.trim { it <= ' ' }.isEmpty()) {
        return Indent.getNoneIndent()
      }
      if (isMdxJsxTagNode(myNode)) {
        return Indent.getNoneIndent()
      }

      val foreignParent = getForeignBlockParent(true)
      return if (foreignParent != null) {
        val foreignTag = foreignParent.node?.psi as? XmlTag
        if (foreignTag == null || myNode.textRange == foreignTag.textRange || !myHtmlPolicy.indentChildrenOf(foreignTag)) {
          Indent.getNoneIndent()
        }
        else Indent.getNormalIndent()
      }
      else Indent.getNoneIndent()
    }

    override fun isRequiredRange(range: TextRange): Boolean {
      return !range.subSequence(myNode.psi.containingFile.text).isBlank()
    }

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
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
      return if (myNode.elementType === MdxTokenTypes.JSX_BLOCK_CONTENT) {
        ChildAttributes(Indent.getNormalIndent(), null)
      }
      else {
        ChildAttributes(Indent.getNoneIndent(), null)
      }
    }

    private fun getForeignBlockParent(immediate: Boolean): DataLanguageBlockWrapper? {
      var foreignBlockParent: DataLanguageBlockWrapper? = null
      var parent = parent
      while (parent != null) {
        if (parent is DataLanguageBlockWrapper && parent.original !is SyntheticBlock) {
          foreignBlockParent = parent
          break
        }
        else if (immediate && parent is MdxBlock) {
          break
        }
        parent = parent.parent
      }
      return foreignBlockParent
    }

    private fun isMdxJsxTagNode(node: ASTNode): Boolean {
      return node.elementType === MarkdownElementType.platformType(MdxElementTypes.MDX_JSX_OPENING_ELEMENT) ||
             node.elementType === MarkdownElementType.platformType(MdxElementTypes.MDX_JSX_CLOSING_ELEMENT) ||
             node.elementType === MarkdownElementType.platformType(MdxElementTypes.MDX_JSX_SELF_CLOSING_ELEMENT)
    }

    private fun shouldWrapBeforeClosingTag(parent: DataLanguageBlockWrapper? = getForeignBlockParent(false)): Boolean {
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

  }
}
