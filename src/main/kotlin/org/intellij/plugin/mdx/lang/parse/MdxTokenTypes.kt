package org.intellij.plugin.mdx.lang.parse

import com.intellij.psi.tree.OuterLanguageElementType
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementType
import org.intellij.plugin.mdx.lang.MdxLanguage
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes

class MdxTokenTypes: MarkdownTokenTypes {
    companion object {
        @JvmField val JSX_BLOCK_CONTENT: IElementType = MarkdownElementType("JSX_BLOCK_CONTENT", true)
        @JvmField val OUTER_ELEMENT_TYPE = OuterLanguageElementType("OUTER_BLOCK", MdxLanguage)

    }
}