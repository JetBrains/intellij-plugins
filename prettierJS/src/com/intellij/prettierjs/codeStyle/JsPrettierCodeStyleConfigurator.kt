// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.codeStyle

import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.prettierjs.PrettierConfig
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings

internal class JsPrettierCodeStyleConfigurator<T : JSCodeStyleSettings>(
  private val customSettingsClass: Class<T>,
) : DefaultPrettierCodeStyleConfigurator() {
  override fun applySettings(settings: CodeStyleSettings, psiFile: PsiFile, prettierConfig: PrettierConfig) {
    super.applySettings(settings, psiFile, prettierConfig)

    settings.getCustomSettings(customSettingsClass).apply {
      USE_DOUBLE_QUOTES = !prettierConfig.singleQuote
      USE_SEMICOLON_AFTER_STATEMENT = prettierConfig.semi
      SPACES_WITHIN_OBJECT_LITERAL_BRACES = prettierConfig.bracketSpacing
      SPACES_WITHIN_OBJECT_TYPE_BRACES = prettierConfig.bracketSpacing
      SPACES_WITHIN_IMPORTS = prettierConfig.bracketSpacing
      ENFORCE_TRAILING_COMMA = convertTrailingCommaOption(prettierConfig.trailingComma)

      // Default prettier settings
      FORCE_QUOTE_STYlE = true
      FORCE_SEMICOLON_STYLE = true
      SPACE_BEFORE_FUNCTION_LEFT_PARENTH = false
    }
  }

  override fun isApplied(settings: CodeStyleSettings, psiFile: PsiFile, prettierConfig: PrettierConfig): Boolean {
    val customSettings = settings.getCustomSettings(customSettingsClass)

    return super.isApplied(settings, psiFile, prettierConfig) &&
           customSettings.USE_DOUBLE_QUOTES == !prettierConfig.singleQuote &&
           customSettings.USE_SEMICOLON_AFTER_STATEMENT == prettierConfig.semi &&
           customSettings.SPACES_WITHIN_OBJECT_LITERAL_BRACES == prettierConfig.bracketSpacing &&
           customSettings.SPACES_WITHIN_OBJECT_TYPE_BRACES == prettierConfig.bracketSpacing &&
           customSettings.SPACES_WITHIN_IMPORTS == prettierConfig.bracketSpacing &&
           customSettings.ENFORCE_TRAILING_COMMA == convertTrailingCommaOption(prettierConfig.trailingComma) &&
           customSettings.FORCE_QUOTE_STYlE &&
           customSettings.FORCE_SEMICOLON_STYLE &&
           !customSettings.SPACE_BEFORE_FUNCTION_LEFT_PARENTH
  }

}

private fun convertTrailingCommaOption(option: PrettierConfig.TrailingCommaOption): JSCodeStyleSettings.TrailingCommaOption {
  when (option) {
    PrettierConfig.TrailingCommaOption.none -> return JSCodeStyleSettings.TrailingCommaOption.Remove
    PrettierConfig.TrailingCommaOption.es5,
    PrettierConfig.TrailingCommaOption.all,
      -> return JSCodeStyleSettings.TrailingCommaOption.WhenMultiline
  }
}