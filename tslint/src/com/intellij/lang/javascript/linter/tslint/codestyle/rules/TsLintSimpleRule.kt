package com.intellij.lang.javascript.linter.tslint.codestyle.rules

import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings


abstract class TsLintSimpleRule<T>(override val optionId: String) : TsLintRule {
  override fun isAvailable(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, option: TslintJsonOption): Boolean {
    val configValue = getConfigValue(option)
    return configValue != null && getSettingsValue(languageSettings, codeStyleSettings) != configValue
  }

  override fun apply(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, option: TslintJsonOption) {
    val value = getConfigValue(option) ?: return

    setValue(languageSettings, codeStyleSettings, value)
  }

  protected abstract fun getConfigValue(option: TslintJsonOption): T?

  protected abstract fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): T

  protected abstract fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: T)
}