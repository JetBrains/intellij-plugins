package com.intellij.dts.lang

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

sealed class DtsFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, DtsLanguage) {
    override fun getFileType(): FileType = DtsFileType.INSTANCE

    class Source(viewProvider: FileViewProvider) : DtsFile(viewProvider) {
        override fun toString(): String = "DTS file"
    }

    class Include(viewProvider: FileViewProvider) : DtsFile(viewProvider) {
        override fun toString(): String = "DTSI file"
    }
}