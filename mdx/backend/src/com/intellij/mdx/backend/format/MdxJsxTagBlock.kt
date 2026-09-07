package com.intellij.mdx.backend.format

import com.intellij.formatting.Alignment
import com.intellij.formatting.Block
import com.intellij.formatting.Indent
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.formatter.blocks.JSBlock
import com.intellij.lang.javascript.formatter.blocks.SubBlockVisitor
import com.intellij.psi.formatter.xml.XmlFormattingPolicy
import com.intellij.psi.formatter.xml.XmlTagBlock
import com.intellij.psi.xml.XmlTag
import org.intellij.plugin.mdx.lang.parse.MdxElementTypes
import org.intellij.plugins.markdown.lang.MarkdownTokenTypeSets

internal class MdxJsxTagBlock(
  node: ASTNode,
  wrap: Wrap?,
  alignment: Alignment?,
  policy: XmlFormattingPolicy,
  indent: Indent?,
  outerJsBlock: JSBlock?,
  private val inheritedPreserveSpace: Boolean = false,
) : SubBlockVisitor.JSXmlTagBlock(node, wrap, alignment, policy, indent, outerJsBlock) {
  override fun createTagBlock(child: ASTNode, indent: Indent?, wrap: Wrap?, alignment: Alignment?): XmlTagBlock {
    return MdxJsxTagBlock(child, wrap, alignment, myXmlFormattingPolicy, indent ?: Indent.getNoneIndent(), myOuterJsBlock,
                          isPreserveSpace)
  }

  override fun isPreserveSpace(): Boolean {
    // The public JSX constructor does not accept inherited xml:space.
    return when ((node.psi as? XmlTag)?.getAttributeValue("xml:space")) {
      "preserve" -> true
      "default" -> false
      else -> inheritedPreserveSpace || super.isPreserveSpace()
    }
  }

  override fun processChild(result: MutableList<Block>, child: ASTNode, wrap: Wrap?, alignment: Alignment?, indent: Indent?): ASTNode? {
    // The Markdown list supplies the body indentation. Set the child indentation before the template formatter creates its wrappers.
    val childIndent = if (indent?.type === Indent.Type.NORMAL && hasMarkdownListParent(child)) Indent.getNoneIndent() else indent
    return super.processChild(result, child, wrap, alignment, childIndent)
  }

  private fun hasMarkdownListParent(child: ASTNode): Boolean {
    val viewProvider = child.psi.containingFile.viewProvider
    val hostFile = viewProvider.getPsi(viewProvider.baseLanguage) ?: return false
    // PsiFile.findElementAt can select the JSX tree. Read the Markdown tree directly.
    val leaf = hostFile.node.findLeafElementAt(child.startOffset) ?: return false
    val range = child.textRange
    val hostNode = generateSequence(leaf) { it.treeParent }.firstOrNull { it.textRange.contains(range) } ?: return false
    for (ancestor in generateSequence(hostNode.treeParent) { it.treeParent }) {
      if (ancestor.elementType in MarkdownTokenTypeSets.LISTS) return true
      if (ancestor.elementType === MdxElementTypes.MDX_JSX_FLOW_ELEMENT ||
          ancestor.elementType === MdxElementTypes.MDX_JSX_TEXT_ELEMENT ||
          ancestor.elementType === MdxElementTypes.MDX_EXPRESSION) return false
    }
    return false
  }
}
