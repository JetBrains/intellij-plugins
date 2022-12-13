package org.intellij.prisma.ide.formatter.settings

import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CustomCodeStyleSettings

class PrismaCodeStyleSettings(container: CodeStyleSettings) :
  CustomCodeStyleSettings(PrismaCodeStyleSettings::class.java.simpleName, container)