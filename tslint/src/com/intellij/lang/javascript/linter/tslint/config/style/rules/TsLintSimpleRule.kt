package com.intellij.lang.javascript.linter.tslint.config.style.rules

import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.openapi.project.Project
import com.intellij.psi.codeStyle.CommonCodeStyleSettings


abstract class TsLintSimpleRule<T>(val optionId: String) : TsLintRule {
  override fun isAvailable(project: Project,
                           languageSettings: CommonCodeStyleSettings,
                           codeStyleSettings: JSCodeStyleSettings,
                           config: TsLintConfigWrapper): Boolean {
    if (!hasOption(config)) return false

    val configValue = getConfigValue(config)
    return configValue != null && getSettingsValue(languageSettings, codeStyleSettings) != configValue
  }

  abstract fun getConfigValue(config: TsLintConfigWrapper): T?

  override fun apply(project: Project,
                     languageSettings: CommonCodeStyleSettings,
                     codeStyleSettings: JSCodeStyleSettings,
                     config: TsLintConfigWrapper) {
    val value = getConfigValue(config) ?: return

    setValue(languageSettings, codeStyleSettings, value)
  }

  open fun hasOption(config: TsLintConfigWrapper): Boolean = config.getOption(optionId)?.isEnabled() ?: false

  abstract fun getSettingsValue(languageSettings: CommonCodeStyleSettings,
                                codeStyleSettings: JSCodeStyleSettings): T

  @Suppress("UNCHECKED_CAST")
  fun setDirectValue(languageSettings: CommonCodeStyleSettings,
                     codeStyleSettings: JSCodeStyleSettings, value: Any?) {
    setValue(languageSettings, codeStyleSettings, value as T)
  }
  abstract fun setValue(languageSettings: CommonCodeStyleSettings,
                        codeStyleSettings: JSCodeStyleSettings, value: T)
}