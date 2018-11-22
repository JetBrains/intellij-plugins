package com.intellij.lang.javascript.linter.tslint.codestyle.rules

import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.openapi.project.Project
import com.intellij.psi.codeStyle.CommonCodeStyleSettings


abstract class TsLintSimpleRule<T>(val optionId: String) : TsLintRule {
  override fun isAvailable(project: Project,
                           languageSettings: CommonCodeStyleSettings,
                           codeStyleSettings: JSCodeStyleSettings,
                           config: TsLintConfigWrapper): Boolean {
    val option = config.getOption(optionId)
    if (option == null || !option.isEnabled()) return false

    val configValue = getConfigValue(option)
    return configValue != null && getSettingsValue(languageSettings, codeStyleSettings) != configValue
  }

  override fun apply(project: Project,
                     languageSettings: CommonCodeStyleSettings,
                     codeStyleSettings: JSCodeStyleSettings,
                     config: TsLintConfigWrapper) {
    val option = config.getOption(optionId)
    if (option == null) return
    val value = getConfigValue(option) ?: return

    setValue(languageSettings, codeStyleSettings, value)
  }

  protected abstract fun getConfigValue(option: TslintJsonOption): T?

  abstract fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): T

  @Suppress("UNCHECKED_CAST")
  fun setDirectValue(languageSettings: CommonCodeStyleSettings,
                     codeStyleSettings: JSCodeStyleSettings, value: Any?) {
    setValue(languageSettings, codeStyleSettings, value as T)
  }
  abstract fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: T)
}