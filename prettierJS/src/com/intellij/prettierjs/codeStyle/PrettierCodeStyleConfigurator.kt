// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.codeStyle

import com.intellij.prettierjs.PrettierConfig
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings

interface PrettierCodeStyleConfigurator {
  fun applySettings(settings: CodeStyleSettings, psiFile: PsiFile, prettierConfig: PrettierConfig)
  fun isApplied(settings: CodeStyleSettings, psiFile: PsiFile, prettierConfig: PrettierConfig): Boolean
}