package com.intellij.lang.javascript.linter.tslint.config.style.rules

import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.openapi.project.Project
import com.intellij.psi.codeStyle.CommonCodeStyleSettings


abstract class TsLintSimpleRule(val optionId: String) : TsLintRule {
  override fun isAvailable(project: Project,
                           languageSettings: CommonCodeStyleSettings,
                           codeStyleSettings: JSCodeStyleSettings,
                           config: TsLintConfigWrapper): Boolean {
    if (!hasOption(config)) return false

    return !getValue(languageSettings, codeStyleSettings)
  }

  override fun apply(project: Project,
                     languageSettings: CommonCodeStyleSettings,
                     codeStyleSettings: JSCodeStyleSettings,
                     config: TsLintConfigWrapper) {
    setValue(languageSettings, codeStyleSettings)
  }

  fun hasOption(config: TsLintConfigWrapper): Boolean {
    val option = config.getOption(optionId)
    return option?.isTrue() ?: false
  }

  abstract fun getValue(languageSettings: CommonCodeStyleSettings,
                        codeStyleSettings: JSCodeStyleSettings): Boolean

  abstract fun setValue(languageSettings: CommonCodeStyleSettings,
                        codeStyleSettings: JSCodeStyleSettings)
}