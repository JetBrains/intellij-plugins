package com.intellij.dts.lang

import com.intellij.dts.DtsBundle
import com.intellij.dts.DtsIcons
import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

object DtsFileType : LanguageFileType(DtsLanguage) {
    override fun getName(): String = DtsLanguage.id

    override fun getDescription(): String = DtsBundle.message("file.description")

    override fun getDefaultExtension(): String = "dts"

    override fun getIcon(): Icon = DtsIcons.Dts
}