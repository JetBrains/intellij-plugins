package org.intellij.plugin.mdx.lang.parse

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementType

object MdxMarkdownLibTokenTypes {
  @JvmField
  val EMBEDDED_JS_CONTENT: IElementType = MarkdownElementType("JSX_BLOCK_CONTENT", true)
}
