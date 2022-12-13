package org.intellij.prisma.lang

import com.intellij.openapi.fileTypes.LanguageFileType
import org.intellij.prisma.PrismaIcons
import javax.swing.Icon

object PrismaFileType : LanguageFileType(PrismaLanguage) {
  override fun getName(): String = "Prisma"

  override fun getDescription(): String = "Prisma"

  override fun getDefaultExtension(): String = "prisma"

  override fun getIcon(): Icon = PrismaIcons.PRISMA
}