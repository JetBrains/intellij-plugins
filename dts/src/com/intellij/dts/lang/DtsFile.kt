package com.intellij.dts.lang

import com.intellij.dts.lang.psi.DtsContainer
import com.intellij.dts.lang.psi.DtsEntry
import com.intellij.dts.lang.psi.FileInclude
import com.intellij.dts.lang.psi.PsiFileInclude
import com.intellij.dts.lang.resolve.files.DtsOverlayFile
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

sealed class DtsFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, DtsLanguage), DtsContainer {
    override fun getFileType(): FileType = DtsFileType

    override val dtsEntries: List<DtsEntry>
        get() = findChildrenByClass(DtsEntry::class.java).toList()

    override val isDtsRootContainer: Boolean
        get() = true

    open val dtsTopLevelIncludes: List<FileInclude>
        get() = findChildrenByClass(PsiFileInclude::class.java).mapNotNull { it.fileInclude }

    class Source(viewProvider: FileViewProvider) : DtsFile(viewProvider) {
        override fun toString(): String = "DTS file"

        override val dtsAffiliation: DtsAffiliation
            get() = DtsAffiliation.ROOT
    }

    open class Include(viewProvider: FileViewProvider) : DtsFile(viewProvider) {
        override fun toString(): String = "DTSI file"

        override val dtsAffiliation: DtsAffiliation
            get() = dtsEntries.map { it.dtsStatement.dtsAffiliation }.firstOrNull { it != DtsAffiliation.UNKNOWN }
                ?: DtsAffiliation.UNKNOWN
    }

    class Overlay(viewProvider: FileViewProvider) : Include(viewProvider) {
        override fun toString(): String = "Zephyr overlay file"

        override val dtsTopLevelIncludes: List<FileInclude>
            get() = super.dtsTopLevelIncludes + DtsOverlayFile
    }
}