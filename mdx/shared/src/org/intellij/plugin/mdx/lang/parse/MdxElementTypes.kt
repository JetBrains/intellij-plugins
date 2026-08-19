package org.intellij.plugin.mdx.lang.parse

import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType
import org.intellij.plugin.mdx.lang.MdxLanguage
import org.intellij.plugins.markdown.lang.MarkdownElementType

object MdxElementTypes {
  @JvmField
  val MDX_FILE_NODE_TYPE: IFileElementType = IFileElementType("MDX", MdxLanguage)
  @JvmField
  val MDX_BLOCK: IElementType = MarkdownElementType.platformType(MdxMarkdownLibElementTypes.MDX_BLOCK)
  @JvmField
  val MDX_ESM_BLOCK: IElementType = MarkdownElementType.platformType(MdxMarkdownLibElementTypes.MDX_ESM_BLOCK)
  @JvmField
  val MDX_JSX_FLOW_ELEMENT: IElementType = MarkdownElementType.platformType(MdxMarkdownLibElementTypes.MDX_JSX_FLOW_ELEMENT)
  @JvmField
  val MDX_JSX_TEXT_ELEMENT: IElementType = MarkdownElementType.platformType(MdxMarkdownLibElementTypes.MDX_JSX_TEXT_ELEMENT)
  @JvmField
  val MDX_JSX_OPENING_ELEMENT: IElementType = MarkdownElementType.platformType(MdxMarkdownLibElementTypes.MDX_JSX_OPENING_ELEMENT)
  @JvmField
  val MDX_JSX_CLOSING_ELEMENT: IElementType = MarkdownElementType.platformType(MdxMarkdownLibElementTypes.MDX_JSX_CLOSING_ELEMENT)
  @JvmField
  val MDX_JSX_SELF_CLOSING_ELEMENT: IElementType = MarkdownElementType.platformType(MdxMarkdownLibElementTypes.MDX_JSX_SELF_CLOSING_ELEMENT)
  @JvmField
  val MDX_JSX_ATTRIBUTE: IElementType = MarkdownElementType.platformType(MdxMarkdownLibElementTypes.MDX_JSX_ATTRIBUTE)
  @JvmField
  val MDX_EXPRESSION: IElementType = MarkdownElementType.platformType(MdxMarkdownLibElementTypes.MDX_EXPRESSION)
}
