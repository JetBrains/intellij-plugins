package com.intellij.lang.javascript.linter.tslint.config.style.rules

import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings

val TslintRulesSet = setOf(ImportDestructuringSpacingRule(), QuotemarkRule(), SemicolonRule())

class ImportDestructuringSpacingRule : TsLintSimpleRule<Boolean>("import-destructuring-spacing") {
  override fun getConfigValue(config: TsLintConfigWrapper): Boolean = true

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings,
                                codeStyleSettings: JSCodeStyleSettings
  ): Boolean = codeStyleSettings.SPACES_WITHIN_IMPORTS

  override fun setValue(languageSettings: CommonCodeStyleSettings,
                        codeStyleSettings: JSCodeStyleSettings,
                        value: Boolean) {
    codeStyleSettings.SPACES_WITHIN_IMPORTS = value
  }
}

class QuotemarkRule : TsLintSimpleRule<String>("quotemark") {
  override fun getConfigValue(config: TsLintConfigWrapper): String? {
    val option = config.getOption(optionId) ?: return null

    val stringValues = option.getStringValues()
    if (stringValues.contains("single")) return "'"
    if (stringValues.contains("double")) return "\""

    return null
  }

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings,
                                codeStyleSettings: JSCodeStyleSettings): String {
    return if (codeStyleSettings.USE_DOUBLE_QUOTES) "\"" else "'"
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings,
                        codeStyleSettings: JSCodeStyleSettings,
                        value: String) {
    codeStyleSettings.USE_DOUBLE_QUOTES = value == "\""
  }

}

//
class SemicolonRule : TsLintSimpleRule<Boolean>("semicolon") {
  override fun getConfigValue(config: TsLintConfigWrapper): Boolean? {
    val option = config.getOption(optionId) ?: return null

    val stringValues = option.getStringValues()
    if (stringValues.contains("always")) return true
    if (stringValues.contains("never")) return false

    return null
  }

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return codeStyleSettings.USE_SEMICOLON_AFTER_STATEMENT
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    codeStyleSettings.USE_SEMICOLON_AFTER_STATEMENT = value
  }

}
//
//class OneLineRule : TsLintRule {
//
//}
//
//class SpaceBeforeFunctionParenRule : TsLintRule {
//
//}
//
//class WhitespaceRule : TsLintRule {
//
//}
