package com.intellij.lang.javascript.linter.tslint.config.style.rules

import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings

val TslintRulesSet = setOf(ImportDestructuringSpacingRule())

class ImportDestructuringSpacingRule : TsLintSimpleRule("import-destructuring-spacing") {
  override fun getValue(languageSettings: CommonCodeStyleSettings,
                        codeStyleSettings: JSCodeStyleSettings
  ): Boolean = codeStyleSettings.SPACES_WITHIN_IMPORTS

  override fun setValue(languageSettings: CommonCodeStyleSettings,
                        codeStyleSettings: JSCodeStyleSettings) {
    codeStyleSettings.SPACES_WITHIN_IMPORTS = true
  }
}

//
//class QuotemarkRule : TsLintRule {
//
//}
//
//class SemicolonRule : TsLintRule {
//
//}
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
