package org.intellij.plugin.mdx.lang.parse

import com.intellij.psi.tree.IElementType
import org.intellij.plugins.markdown.lang.MarkdownElementType

object MdxTokenTypes {
  @JvmField
  val EMBEDDED_JS_CONTENT: IElementType = MarkdownElementType.platformType(MdxMarkdownLibTokenTypes.EMBEDDED_JS_CONTENT)
}
