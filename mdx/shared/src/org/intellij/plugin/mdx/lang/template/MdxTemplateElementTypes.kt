package org.intellij.plugin.mdx.lang.template

import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.OuterLanguageElementType
import org.intellij.plugin.mdx.lang.MdxLanguage

object MdxTemplateElementTypes {
  @JvmField
  val OUTER_MARKDOWN_CONTENT: IElementType = OuterLanguageElementType("OUTER_MARKDOWN_CONTENT", MdxLanguage)
}
