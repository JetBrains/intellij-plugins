package com.intellij.lang.javascript.linter.tslint.config.style.rules

import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings

val TslintRulesSet = setOf(ImportDestructuringSpacingRule(),
                           QuotemarkRule(),
                           SemicolonRule(),
                           ForceAlwaysSemicolonRule(),
                           ForceNeverSemicolonRule(),
                           OneLineCatchRule(),
                           OneLineElseRule(),
                           OneLineFinallyRule(),
                           NamedFunctionSpacesRule(),
                           WhitespaceTypeRule(),
                           AnonymousFunctionSpacesRule(),
                           WhitespaceIfRule(),
                           WhitespaceForRule(),
                           WhitespaceWhileRule(),
                           WhitespaceAssignmentRule(),
                           WhitespaceArrowOperatorRule(),
                           WhitespaceLogicalOperatorRule(),
                           WhitespaceEqOperatorRule(),
                           WhitespaceBitwiseOperatorRule(),
                           WhitespaceRelationOperatorRule(),
                           WhitespaceAdditiveOperatorRule(),
                           WhitespaceMultplyOperatorRule(),
                           WhitespaceShiftOperatorRule(),
                           WhitespaceCommaRule(),
                           IndentRule()
)

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

class SemicolonRule() : TsLintSimpleRule<Boolean>("semicolon") {
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

open class ForceAlwaysSemicolonRule() : TsLintSimpleRule<Boolean>("semicolon") {
  override fun getConfigValue(config: TsLintConfigWrapper): Boolean? {
    val option = config.getOption(optionId) ?: return null

    val stringValues = option.getStringValues()
    if (stringValues.contains(getCode())) return true

    return null
  }

  open fun getCode(): String {
    return "always"
  }

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return codeStyleSettings.FORCE_SEMICOLON_STYLE
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    codeStyleSettings.FORCE_SEMICOLON_STYLE = value
  }
}

class ForceNeverSemicolonRule() : ForceAlwaysSemicolonRule() {
  override fun getCode(): String {
    return "never"
  }
}

abstract class MergedArrayRule(id: String) : TsLintSimpleRule<Boolean>(id) {

  override fun getConfigValue(config: TsLintConfigWrapper): Boolean? {
    val option = config.getOption(optionId) ?: return null
    val stringValues = option.getStringValues()

    if (stringValues.contains(getCode())) return defaultValue()

    return null
  }

  abstract fun getCode(): String
  abstract fun defaultValue(): Boolean
}

class OneLineCatchRule() : MergedArrayRule("one-line") {
  override fun defaultValue(): Boolean = false

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings
  ): Boolean = languageSettings.CATCH_ON_NEW_LINE

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.CATCH_ON_NEW_LINE = value
  }

  override fun getCode(): String = "check-catch"
}

class OneLineFinallyRule() : MergedArrayRule("one-line") {
  override fun defaultValue(): Boolean = false

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings
  ): Boolean = languageSettings.FINALLY_ON_NEW_LINE

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.FINALLY_ON_NEW_LINE = value
  }

  override fun getCode(): String = "check-finally"
}

class OneLineElseRule() : MergedArrayRule("one-line") {
  override fun defaultValue(): Boolean = false

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings
  ): Boolean = languageSettings.ELSE_ON_NEW_LINE

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.ELSE_ON_NEW_LINE = value
  }

  override fun getCode(): String = "check-else"
}

abstract class FunctionSpacesRule() : TsLintSimpleRule<Boolean>("space-before-function-paren") {
  val ALWAYS = "always"
  val NEVER = "never"

  override fun getConfigValue(config: TsLintConfigWrapper): Boolean? {
    val option = config.getOption(optionId) ?: return null
    val stringValues = option.getStringValues()
    if (stringValues.count() == 1) {
      if (stringValues.contains(ALWAYS)) {
        return true
      }
      if (stringValues.contains(NEVER)) {
        return false
      }
    }

    val secondIndexValues = option.getSecondIndexValues()
    val valueFromLiteral = secondIndexValues[getCode()]
    when (valueFromLiteral) {
      ALWAYS -> return true
      NEVER -> return false
    }

    return null
  }

  abstract fun getCode(): String
}

class NamedFunctionSpacesRule() : FunctionSpacesRule() {
  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return languageSettings.SPACE_BEFORE_METHOD_PARENTHESES
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.SPACE_BEFORE_METHOD_PARENTHESES = value
  }

  override fun getCode(): String = "named"
}

class AnonymousFunctionSpacesRule() : FunctionSpacesRule() {
  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return codeStyleSettings.SPACE_BEFORE_FUNCTION_LEFT_PARENTH
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    codeStyleSettings.SPACE_BEFORE_FUNCTION_LEFT_PARENTH = value
  }

  override fun getCode(): String = "anonymous"
}

class WhitespaceTypeRule() : MergedArrayRule("whitespace") {
  override fun defaultValue(): Boolean = true

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return codeStyleSettings.SPACE_AFTER_TYPE_COLON
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    codeStyleSettings.SPACE_AFTER_TYPE_COLON = value
  }

  override fun getCode(): String = "check-type"
}

class WhitespaceIfRule() : MergedArrayRule("whitespace") {
  override fun defaultValue(): Boolean = true

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return languageSettings.SPACE_BEFORE_IF_PARENTHESES
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.SPACE_BEFORE_IF_PARENTHESES = value
  }

  override fun getCode(): String = "check-branch"
}

class WhitespaceForRule() : MergedArrayRule("whitespace") {
  override fun defaultValue(): Boolean = true

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return languageSettings.SPACE_BEFORE_FOR_PARENTHESES
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.SPACE_BEFORE_FOR_PARENTHESES = value
  }

  override fun getCode(): String = "check-branch"
}

class WhitespaceWhileRule() : MergedArrayRule("whitespace") {
  override fun defaultValue(): Boolean = true

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return languageSettings.SPACE_BEFORE_WHILE_PARENTHESES
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.SPACE_BEFORE_WHILE_PARENTHESES = value
  }

  override fun getCode(): String = "check-branch"
}

class WhitespaceAssignmentRule() : MergedArrayRule("whitespace") {
  override fun defaultValue(): Boolean = true

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return languageSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS = value
  }

  override fun getCode(): String = "check-decl"
}

abstract class WhitespaceOperatorRule() : MergedArrayRule("whitespace") {
  final override fun getCode(): String = "check-operator"
  final override fun defaultValue(): Boolean = true
}

class WhitespaceArrowOperatorRule() : WhitespaceOperatorRule() {
  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return codeStyleSettings.SPACE_AROUND_ARROW_FUNCTION_OPERATOR
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    codeStyleSettings.SPACE_AROUND_ARROW_FUNCTION_OPERATOR = value
  }
}

class WhitespaceLogicalOperatorRule() : WhitespaceOperatorRule() {
  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return languageSettings.SPACE_AROUND_LOGICAL_OPERATORS
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.SPACE_AROUND_LOGICAL_OPERATORS = value
  }
}

class WhitespaceEqOperatorRule() : WhitespaceOperatorRule() {
  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return languageSettings.SPACE_AROUND_EQUALITY_OPERATORS
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.SPACE_AROUND_EQUALITY_OPERATORS = value
  }
}

class WhitespaceBitwiseOperatorRule() : WhitespaceOperatorRule() {
  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return languageSettings.SPACE_AROUND_BITWISE_OPERATORS
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.SPACE_AROUND_BITWISE_OPERATORS = value
  }
}

class WhitespaceRelationOperatorRule() : WhitespaceOperatorRule() {
  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return languageSettings.SPACE_AROUND_RELATIONAL_OPERATORS
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.SPACE_AROUND_RELATIONAL_OPERATORS = value
  }
}

class WhitespaceAdditiveOperatorRule() : WhitespaceOperatorRule() {
  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return languageSettings.SPACE_AROUND_ADDITIVE_OPERATORS
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.SPACE_AROUND_ADDITIVE_OPERATORS = value
  }
}

class WhitespaceMultplyOperatorRule() : WhitespaceOperatorRule() {
  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return languageSettings.SPACE_AROUND_MULTIPLICATIVE_OPERATORS
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.SPACE_AROUND_MULTIPLICATIVE_OPERATORS = value
  }
}

class WhitespaceShiftOperatorRule() : WhitespaceOperatorRule() {
  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return languageSettings.SPACE_AROUND_SHIFT_OPERATORS
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.SPACE_AROUND_SHIFT_OPERATORS = value
  }
}

class WhitespaceCommaRule() : MergedArrayRule("whitespace") {
  override fun defaultValue(): Boolean = true

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return languageSettings.SPACE_AFTER_COMMA
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.SPACE_AFTER_COMMA = value
  }

  override fun getCode(): String = "check-separator"
}

class IndentRule() : TsLintSimpleRule<String>("indent") {
  override fun getConfigValue(config: TsLintConfigWrapper): String? {
    val option = config.getOption(optionId) ?: return null
    val stringValues = option.getStringValues()

    if (stringValues.contains("spaces")) return "spaces"
    if (stringValues.contains("tabs")) return "tabs"

    return null
  }

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): String {
    var indentOptions = languageSettings.indentOptions
    if (indentOptions == null) {
      indentOptions = languageSettings.initIndentOptions()
    }

    return if (indentOptions.USE_TAB_CHARACTER) "tabs" else "spaces"
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: String) {
    var indentOptions = languageSettings.indentOptions
    if (indentOptions == null) {
      indentOptions = languageSettings.initIndentOptions()
    }
    when (value) {
      "tabs" -> indentOptions.USE_TAB_CHARACTER = true
      "spaces" -> indentOptions.USE_TAB_CHARACTER = false
    }
  }

}
