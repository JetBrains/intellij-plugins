package org.intellij.plugin.mdx.lang.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import org.intellij.plugin.mdx.lang.MdxFileType
import org.intellij.plugin.mdx.lang.MdxLanguage

class MdxFile(viewProvider: FileViewProvider?) : PsiFileBase(viewProvider!!, MdxLanguage) {
    override fun getFileType(): FileType {
        return MdxFileType.INSTANCE
    }

    override fun toString(): String {
        return "MDX File"
    }
}