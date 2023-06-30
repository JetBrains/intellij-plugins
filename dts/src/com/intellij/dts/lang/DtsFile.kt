package com.intellij.dts.lang

import com.intellij.dts.lang.psi.DtsContainer
import com.intellij.dts.lang.psi.DtsEntry
import com.intellij.dts.lang.psi.FileInclude
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

sealed class DtsFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, DtsLanguage), DtsContainer {
    override fun getFileType(): FileType = DtsFileType

    override val dtsEntries: List<DtsEntry>
        get() = findChildrenByClass(DtsEntry::class.java).toList()

    override val isDtsRootContainer: Boolean
        get() = true

    val dtsTopLevelIncludes: List<FileInclude>
        get() = findChildrenByClass(FileInclude::class.java).toList()

    class Source(viewProvider: FileViewProvider) : DtsFile(viewProvider) {
        override fun toString(): String = "DTS file"

        override val dtsAffiliation: DtsAffiliation
            get() = DtsAffiliation.ROOT
    }

    class Include(viewProvider: FileViewProvider) : DtsFile(viewProvider) {
        override fun toString(): String = "DTSI file"

        override val dtsAffiliation: DtsAffiliation
            get() = dtsEntries.map { it.dtsStatement.dtsAffiliation }.firstOrNull { it != DtsAffiliation.UNKNOWN }
                ?: DtsAffiliation.UNKNOWN
    }
}