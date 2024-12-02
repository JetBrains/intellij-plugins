// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.codeStyle

import com.intellij.prettierjs.PrettierConfig
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.xml.HtmlCodeStyleSettings

internal class HtmlPrettierCodeStyleConfigurator : DefaultPrettierCodeStyleConfigurator() {
  override fun applySettings(settings: CodeStyleSettings, psiFile: PsiFile, prettierConfig: PrettierConfig) {
    super.applySettings(settings, psiFile, prettierConfig)

    val customSettings = settings.getCustomSettings(HtmlCodeStyleSettings::class.java)

    // Default prettier settings
    customSettings.HTML_SPACE_INSIDE_EMPTY_TAG = true
  }

  override fun isApplied(settings: CodeStyleSettings, psiFile: PsiFile, prettierConfig: PrettierConfig): Boolean {
    val customSettings = settings.getCustomSettings(HtmlCodeStyleSettings::class.java)
    return customSettings.HTML_SPACE_INSIDE_EMPTY_TAG && super.isApplied(settings, psiFile, prettierConfig)
  }
}