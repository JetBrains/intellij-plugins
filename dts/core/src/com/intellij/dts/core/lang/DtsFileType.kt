package com.intellij.dts.core.lang

import com.intellij.dts.core.DtsBundle
import com.intellij.dts.core.icons.DtsCoreIcons
import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

object DtsFileType : LanguageFileType(DtsLanguage) {
  override fun getName(): String = DtsLanguage.id

  override fun getDescription(): String = DtsBundle.message("language.file_name")

  override fun getDefaultExtension(): String = "dts"

  override fun getIcon(): Icon = DtsCoreIcons.Dts
}