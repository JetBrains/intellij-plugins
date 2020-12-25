package com.jetbrains.lang.makefile

import com.intellij.icons.*
import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.util.*
import javax.swing.*

val MakefileIcon = IconLoader.getIcon("/icons/makefile.png")
val MakefileTargetIcon: Icon = AllIcons.RunConfigurations.TestState.Run

object MakefileFileType : LanguageFileType(MakefileLanguage) {
  override fun getIcon() = MakefileIcon
  override fun getName() = "Makefile"
  @Suppress("DialogTitleCapitalization")
  override fun getDescription() = MakefileLangBundle.message("file.type.description")
  override fun getDefaultExtension() = "mk"
}
