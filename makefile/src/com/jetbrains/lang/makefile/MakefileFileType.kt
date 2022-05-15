package com.jetbrains.lang.makefile

import com.intellij.openapi.fileTypes.LanguageFileType
import icons.MakefileIcons

object MakefileFileType : LanguageFileType(MakefileLanguage) {
  override fun getIcon() = MakefileIcons.Makefile
  override fun getName() = "Makefile"
  override fun getDescription() = MakefileLangBundle.message("filetype.makefile.description")
  override fun getDefaultExtension() = "mk"
}
