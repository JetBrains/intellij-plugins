package org.intellij.plugin.mdx.lang.parse

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementType

object MdxMarkdownLibElementTypes {
  @JvmField
  val MDX_BLOCK: IElementType = MarkdownElementType("JSX_BLOCK")
  @JvmField
  val MDX_ESM_BLOCK: IElementType = MarkdownElementType("MDX_ESM_BLOCK")
  @JvmField
  val MDX_JSX_FLOW_ELEMENT: IElementType = MarkdownElementType("MDX_JSX_FLOW_ELEMENT")
  @JvmField
  val MDX_JSX_TEXT_ELEMENT: IElementType = MarkdownElementType("MDX_JSX_TEXT_ELEMENT")
  @JvmField
  val MDX_JSX_OPENING_ELEMENT: IElementType = MarkdownElementType("MDX_JSX_OPENING_ELEMENT")
  @JvmField
  val MDX_JSX_CLOSING_ELEMENT: IElementType = MarkdownElementType("MDX_JSX_CLOSING_ELEMENT")
  @JvmField
  val MDX_JSX_SELF_CLOSING_ELEMENT: IElementType = MarkdownElementType("MDX_JSX_SELF_CLOSING_ELEMENT")
  @JvmField
  val MDX_JSX_ATTRIBUTE: IElementType = MarkdownElementType("MDX_JSX_ATTRIBUTE")
  @JvmField
  val MDX_EXPRESSION: IElementType = MarkdownElementType("MDX_JSX_EXPRESSION")
}
