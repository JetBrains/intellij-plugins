package com.intellij.lang.javascript.linter.tslint.codestyle.rules

import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings


interface TsLintRule {
  val optionId: String
  fun isAvailable(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, option: TslintJsonOption): Boolean

  fun apply(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, option: TslintJsonOption)
}