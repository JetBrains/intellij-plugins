package org.intellij.plugin.mdx.lang.parse

import com.intellij.psi.PsiFile
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.tree.IStubFileElementType
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementType
import org.intellij.plugin.mdx.lang.MdxLanguage

class MdxElementTypes {
    companion object {
        @JvmField
        val JSX_BLOCK: IElementType = MarkdownElementType("JSX_BLOCK")
        val MDX_FILE_NODE_TYPE = IStubFileElementType<PsiFileStub<PsiFile>>("MDX", MdxLanguage)
    }
}