package org.intellij.prisma.ide.formatter

import com.intellij.psi.codeStyle.CodeStyleSettings
import org.intellij.prisma.ide.formatter.settings.PrismaCodeStyleSettings
import org.intellij.prisma.lang.PrismaLanguage

class PrismaFormatBlockContext(settings: CodeStyleSettings) {
  val commonSettings = settings.getCommonSettings(PrismaLanguage)
  val customSettings = settings.getCustomSettings(PrismaCodeStyleSettings::class.java)
}