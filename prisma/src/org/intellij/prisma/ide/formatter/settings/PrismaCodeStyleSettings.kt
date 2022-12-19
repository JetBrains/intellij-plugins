package org.intellij.prisma.ide.formatter.settings

import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CustomCodeStyleSettings

@Suppress("PropertyName")
class PrismaCodeStyleSettings(container: CodeStyleSettings) :
  CustomCodeStyleSettings(PrismaCodeStyleSettings::class.java.simpleName, container) {

  @JvmField
  var RUN_PRISMA_FMT_ON_REFORMAT: Boolean = true
}