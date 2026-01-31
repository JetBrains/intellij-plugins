package com.jetbrains.lang.makefile

import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CustomCodeStyleSettings

class MakefileCodeStyleSettings(container: CodeStyleSettings) : CustomCodeStyleSettings(MakefileLanguage.id, container)