package com.intellij.dts.lang

import com.intellij.dts.DtsIcons
import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

class DtsFileType : LanguageFileType(DtsLanguage) {
    companion object {
        val INSTANCE = DtsFileType()
    }

    override fun getName(): String = DtsLanguage.id

    override fun getDescription(): String = "Device tree source file"

    override fun getDefaultExtension(): String = "dts"

    override fun getIcon(): Icon = DtsIcons.Dts
}