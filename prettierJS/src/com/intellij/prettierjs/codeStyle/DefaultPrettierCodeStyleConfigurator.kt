// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.codeStyle

import com.intellij.prettierjs.PrettierConfig
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings.IndentOptions

open class DefaultPrettierCodeStyleConfigurator : PrettierCodeStyleConfigurator {
  override fun applySettings(settings: CodeStyleSettings, psiFile: PsiFile, prettierConfig: PrettierConfig) {
    val commonSettings = settings.getCommonSettings(psiFile.language)

    commonSettings.indentOptions?.let { indentOptions ->
      applyIndentOptions(indentOptions, prettierConfig)
    }

    prettierConfig.lineSeparator?.let { lineSeparator ->
      settings.LINE_SEPARATOR = lineSeparator
    }

    commonSettings.softMargins = listOf(prettierConfig.printWidth)
  }

  override fun isApplied(settings: CodeStyleSettings, psiFile: PsiFile, prettierConfig: PrettierConfig): Boolean {
    val commonSettings = settings.getCommonSettings(psiFile.language)
    val indentOptions = commonSettings.indentOptions
    val softMargins = settings.getSoftMargins(psiFile.language)

    return isIndentOptionsApplied(indentOptions, prettierConfig) &&
           isSoftMarginsApplied(softMargins, prettierConfig) &&
           isLineSeparatorApplied(settings.LINE_SEPARATOR, prettierConfig.lineSeparator)
  }

  private fun isIndentOptionsApplied(indentOptions: IndentOptions?, prettierConfig: PrettierConfig): Boolean {
    return indentOptions == null || (
      indentOptions.INDENT_SIZE == prettierConfig.tabWidth &&
      indentOptions.CONTINUATION_INDENT_SIZE == prettierConfig.tabWidth &&
      indentOptions.TAB_SIZE == prettierConfig.tabWidth &&
      indentOptions.USE_TAB_CHARACTER == prettierConfig.useTabs
                                    )
  }

  private fun isSoftMarginsApplied(softMargins: List<Int>, prettierConfig: PrettierConfig): Boolean {
    return softMargins.isEmpty() || (softMargins.size == 1 && softMargins[0] == prettierConfig.printWidth)
  }

  private fun isLineSeparatorApplied(currentLineSeparator: String?, configLineSeparator: String?): Boolean {
    return configLineSeparator != null && currentLineSeparator == configLineSeparator
  }
}

fun applyIndentOptions(indentOptions: IndentOptions, prettierConfig: PrettierConfig) {
  indentOptions.INDENT_SIZE = prettierConfig.tabWidth
  indentOptions.CONTINUATION_INDENT_SIZE = prettierConfig.tabWidth
  indentOptions.TAB_SIZE = prettierConfig.tabWidth
  indentOptions.USE_TAB_CHARACTER = prettierConfig.useTabs
}