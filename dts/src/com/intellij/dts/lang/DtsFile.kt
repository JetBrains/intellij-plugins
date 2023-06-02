package com.intellij.dts.lang

import com.intellij.dts.lang.psi.DtsContainer
import com.intellij.dts.lang.psi.DtsContent
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

sealed class DtsFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, DtsLanguage), DtsContainer {
    override fun getFileType(): FileType = DtsFileType.INSTANCE

    override val isDtsRootContainer: Boolean = true

    override val dtsContent: DtsContent?
        get() = findChildByClass(DtsContent::class.java)

    class Source(viewProvider: FileViewProvider) : DtsFile(viewProvider) {
        override fun toString(): String = "DTS file"
    }

    class Include(viewProvider: FileViewProvider) : DtsFile(viewProvider) {
        override fun toString(): String = "DTSI file"
    }
}