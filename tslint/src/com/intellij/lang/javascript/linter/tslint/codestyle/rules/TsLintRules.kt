// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.tslint.codestyle.rules

import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.openapi.editor.ex.EditorSettingsExternalizable
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.util.LineSeparator

val TslintRulesSet: Set<TsLintRule> = setOf(ImportDestructuringSpacingRule(),
                                                           QuotemarkRule(),
                                                           ForceQuotemarkRule(),
                                                           SemicolonRule(),
                                                           ForceSemicolonRule(),
                                                           OneLineCatchRule(),
                                                           OneLineElseRule(),
                                                           OneLineFinallyRule(),
                                                           NamedFunctionSpacesRule(),
                                                           WhitespaceTypeRule(),
                                                           AnonymousFunctionSpacesRule(),
                                                           AsyncFunctionSpacesRule(),
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
                                                           WhitespaceImportsRule(),
                                                           WhitespaceInTypeAssertionRule(),
                                                           MaxLineLengthRule(),
                                                           ImportBlacklistRule(),
                                                           SortedImportPartsRule(),
                                                           SortedImportPathsRule(),
                                                           IndentRule(),
                                                           LinebreakStyleRule(),
                                                           NewlineAtEndOfFileRule(),
                                                           AlignFunctionDeclarationParametersRule(),
                                                           AlignFunctionCallParametersRule(),
                                                           MaxBlankLinesRule(),
                                                           WhitespaceAtEndOfLineRule(),
                                                           SpaceAtLineCommentStartRule(),
                                                           MergeImportsFromSameModuleRule(),
                                                           FilenameConventionRule(),
                                                           ForceCurlyBracesRule()
)

class ImportDestructuringSpacingRule : TsLintSimpleRule<Boolean>("import-destructuring-spacing") {
  override fun getConfigValue(option: TslintJsonOption): Boolean = true

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
  override fun getConfigValue(option: TslintJsonOption): String? {
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

class ForceQuotemarkRule : TsLintSimpleRule<Boolean>("quotemark") {
  override fun getConfigValue(option: TslintJsonOption): Boolean? {
    val stringValues = option.getStringValues()
    if (stringValues.contains("avoid-escape")) return null


    if (stringValues.contains("single")) return true
    if (stringValues.contains("double")) return true

    return null
  }

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings,
                                codeStyleSettings: JSCodeStyleSettings): Boolean {
    return codeStyleSettings.FORCE_QUOTE_STYlE
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings,
                        codeStyleSettings: JSCodeStyleSettings,
                        value: Boolean) {
    codeStyleSettings.FORCE_QUOTE_STYlE = value
  }
}

class SemicolonRule : TsLintSimpleRule<Boolean>("semicolon") {
  override fun getConfigValue(option: TslintJsonOption): Boolean {
    val stringValues = option.getStringValues()
    if (stringValues.contains("never")) return false

    return true
  }

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return codeStyleSettings.USE_SEMICOLON_AFTER_STATEMENT
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    codeStyleSettings.USE_SEMICOLON_AFTER_STATEMENT = value
  }

}

class ForceSemicolonRule : TsLintSimpleRule<Boolean>("semicolon") {
  override fun getConfigValue(option: TslintJsonOption): Boolean {
    val stringValues = option.getStringValues()
    if (stringValues.contains("never")) return true

    return true
  }


  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return codeStyleSettings.FORCE_SEMICOLON_STYLE
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    codeStyleSettings.FORCE_SEMICOLON_STYLE = value
  }
}

abstract class MergedArrayRule(id: String) : TsLintSimpleRule<Boolean>(id) {

  override fun getConfigValue(option: TslintJsonOption): Boolean? {
    val stringValues = option.getStringValues()

    if (stringValues.contains(getCode())) return defaultValue()

    return null
  }

  abstract fun getCode(): String
  abstract fun defaultValue(): Boolean
}

class OneLineCatchRule : MergedArrayRule("one-line") {
  override fun defaultValue(): Boolean = false

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings
  ): Boolean = languageSettings.CATCH_ON_NEW_LINE

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.CATCH_ON_NEW_LINE = value
  }

  override fun getCode(): String = "check-catch"
}

class OneLineFinallyRule : MergedArrayRule("one-line") {
  override fun defaultValue(): Boolean = false

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings
  ): Boolean = languageSettings.FINALLY_ON_NEW_LINE

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.FINALLY_ON_NEW_LINE = value
  }

  override fun getCode(): String = "check-finally"
}

class OneLineElseRule : MergedArrayRule("one-line") {
  override fun defaultValue(): Boolean = false

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings
  ): Boolean = languageSettings.ELSE_ON_NEW_LINE

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.ELSE_ON_NEW_LINE = value
  }

  override fun getCode(): String = "check-else"
}

abstract class FunctionSpacesRule : TsLintSimpleRule<Boolean>("space-before-function-paren") {
  private val ALWAYS: String = "always"
  private val NEVER: String = "never"

  override fun getConfigValue(option: TslintJsonOption): Boolean? {
    val stringValues = option.getStringValues()
    if (stringValues.count() == 1) {
      if (stringValues.contains(ALWAYS)) {
        return true
      }
      if (stringValues.contains(NEVER)) {
        return false
      }
    }

    val secondIndexValues = option.getStringMapValue()
    when (secondIndexValues[getCode()]) {
      ALWAYS -> return true
      NEVER -> return false
    }

    return null
  }

  abstract fun getCode(): String
}

class NamedFunctionSpacesRule : FunctionSpacesRule() {
  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return languageSettings.SPACE_BEFORE_METHOD_PARENTHESES
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.SPACE_BEFORE_METHOD_PARENTHESES = value
  }

  override fun getCode(): String = "named"
}

class AnonymousFunctionSpacesRule : FunctionSpacesRule() {
  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return codeStyleSettings.SPACE_BEFORE_FUNCTION_LEFT_PARENTH
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    codeStyleSettings.SPACE_BEFORE_FUNCTION_LEFT_PARENTH = value
  }

  override fun getCode(): String = "anonymous"
}
class AsyncFunctionSpacesRule : FunctionSpacesRule() {
  override fun getCode(): String = "asyncArrow"

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return codeStyleSettings.SPACE_BEFORE_ASYNC_ARROW_LPAREN
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    codeStyleSettings.SPACE_BEFORE_ASYNC_ARROW_LPAREN = value
  }
}

class WhitespaceTypeRule : MergedArrayRule("whitespace") {
  override fun defaultValue(): Boolean = true

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return codeStyleSettings.SPACE_AFTER_TYPE_COLON
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    codeStyleSettings.SPACE_AFTER_TYPE_COLON = value
  }

  override fun getCode(): String = "check-type"
}

class WhitespaceIfRule : MergedArrayRule("whitespace") {
  override fun defaultValue(): Boolean = true

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return languageSettings.SPACE_BEFORE_IF_PARENTHESES
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.SPACE_BEFORE_IF_PARENTHESES = value
  }

  override fun getCode(): String = "check-branch"
}

class WhitespaceForRule : MergedArrayRule("whitespace") {
  override fun defaultValue(): Boolean = true

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return languageSettings.SPACE_BEFORE_FOR_PARENTHESES
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.SPACE_BEFORE_FOR_PARENTHESES = value
  }

  override fun getCode(): String = "check-branch"
}

class WhitespaceWhileRule : MergedArrayRule("whitespace") {
  override fun defaultValue(): Boolean = true

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return languageSettings.SPACE_BEFORE_WHILE_PARENTHESES
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.SPACE_BEFORE_WHILE_PARENTHESES = value
  }

  override fun getCode(): String = "check-branch"
}

class WhitespaceAssignmentRule : MergedArrayRule("whitespace") {
  override fun defaultValue(): Boolean = true

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return languageSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS = value
  }

  override fun getCode(): String = "check-decl"
}

abstract class WhitespaceOperatorRule : MergedArrayRule("whitespace") {
  final override fun getCode(): String = "check-operator"
  final override fun defaultValue(): Boolean = true
}

class WhitespaceArrowOperatorRule : WhitespaceOperatorRule() {
  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return codeStyleSettings.SPACE_AROUND_ARROW_FUNCTION_OPERATOR
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    codeStyleSettings.SPACE_AROUND_ARROW_FUNCTION_OPERATOR = value
  }
}

class WhitespaceLogicalOperatorRule : WhitespaceOperatorRule() {
  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return languageSettings.SPACE_AROUND_LOGICAL_OPERATORS
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.SPACE_AROUND_LOGICAL_OPERATORS = value
  }
}

class WhitespaceEqOperatorRule : WhitespaceOperatorRule() {
  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return languageSettings.SPACE_AROUND_EQUALITY_OPERATORS
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.SPACE_AROUND_EQUALITY_OPERATORS = value
  }
}

class WhitespaceBitwiseOperatorRule : WhitespaceOperatorRule() {
  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return languageSettings.SPACE_AROUND_BITWISE_OPERATORS
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.SPACE_AROUND_BITWISE_OPERATORS = value
  }
}

class WhitespaceRelationOperatorRule : WhitespaceOperatorRule() {
  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return languageSettings.SPACE_AROUND_RELATIONAL_OPERATORS
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.SPACE_AROUND_RELATIONAL_OPERATORS = value
  }
}

class WhitespaceAdditiveOperatorRule : WhitespaceOperatorRule() {
  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return languageSettings.SPACE_AROUND_ADDITIVE_OPERATORS
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.SPACE_AROUND_ADDITIVE_OPERATORS = value
  }
}

class WhitespaceMultplyOperatorRule : WhitespaceOperatorRule() {
  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return languageSettings.SPACE_AROUND_MULTIPLICATIVE_OPERATORS
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.SPACE_AROUND_MULTIPLICATIVE_OPERATORS = value
  }
}

class WhitespaceShiftOperatorRule : WhitespaceOperatorRule() {
  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return languageSettings.SPACE_AROUND_SHIFT_OPERATORS
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.SPACE_AROUND_SHIFT_OPERATORS = value
  }
}

class WhitespaceCommaRule : MergedArrayRule("whitespace") {
  override fun defaultValue(): Boolean = true

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return languageSettings.SPACE_AFTER_COMMA
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.SPACE_AFTER_COMMA = value
  }

  override fun getCode(): String = "check-separator"
}

class WhitespaceImportsRule : MergedArrayRule("whitespace") {
  override fun getCode(): String = "check-module"
  override fun defaultValue(): Boolean = true

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return codeStyleSettings.SPACES_WITHIN_IMPORTS
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    codeStyleSettings.SPACES_WITHIN_IMPORTS = value
  }
}

class WhitespaceInTypeAssertionRule : MergedArrayRule("whitespace") {
  override fun getCode(): String = "check-typecast"

  override fun defaultValue(): Boolean = true

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return codeStyleSettings.SPACE_WITHIN_TYPE_ASSERTION
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    codeStyleSettings.SPACE_WITHIN_TYPE_ASSERTION = value
  }
}

class IndentRule : TsLintRule {
  override val optionId: String = "indent"

  override fun isAvailable(languageSettings: CommonCodeStyleSettings,
                           codeStyleSettings: JSCodeStyleSettings,
                           option: TslintJsonOption): Boolean {
    val stored = languageSettings.indentOptions ?: languageSettings.initIndentOptions()
    val optionsList = option.getOptionsList()
    val configContainsTabs = getUseTabs(optionsList)
    val indentSize = getIndentSize(optionsList)
    return (stored.USE_TAB_CHARACTER != configContainsTabs) ||
           (!configContainsTabs && indentSize != null && (stored.CONTINUATION_INDENT_SIZE != indentSize || stored.INDENT_SIZE != indentSize))
  }

  override fun apply(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, option: TslintJsonOption) {
    val stored = languageSettings.indentOptions ?: languageSettings.initIndentOptions()
    val optionsList = option.getOptionsList()
    val configContainsTabs = getUseTabs(optionsList)
    val indentSize = getIndentSize(optionsList)
    stored.USE_TAB_CHARACTER = configContainsTabs
    if (!configContainsTabs && indentSize != null) {
      stored.CONTINUATION_INDENT_SIZE = indentSize
      stored.INDENT_SIZE = indentSize
    }
  }

  private fun getUseTabs(optionsList: List<Any>): Boolean = "tabs" == optionsList.getOrNull(0)
  private fun getIndentSize(optionsList: List<Any>): Int? = (optionsList.getOrNull(1)as? Number)?.toInt()
}

class MaxLineLengthRule : TsLintSimpleRule<Int>("max-line-length") {
  override fun getConfigValue(option: TslintJsonOption): Int? {
    return option.getNumberValue()
  }

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Int {
    return languageSettings.RIGHT_MARGIN
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Int) {
    languageSettings.RIGHT_MARGIN = value
  }

}

class ImportBlacklistRule : TsLintSimpleRule<Collection<String>>("import-blacklist") {
  override fun getConfigValue(option: TslintJsonOption): Collection<String>? {
    if (!option.isEnabled()) return null

    val stringValues = option.getStringValues()
    if (stringValues.isNotEmpty()) {
      return stringValues.toHashSet()
    }
    return null
  }

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Collection<String> {
    return codeStyleSettings.blacklistImports.toHashSet()
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Collection<String>) {
    codeStyleSettings.BLACKLIST_IMPORTS = StringUtil.join(value, ",")
  }
}

class SortedImportPartsRule : TsLintSimpleRule<Boolean>("ordered-imports") {
  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return codeStyleSettings.IMPORT_SORT_MEMBERS
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    codeStyleSettings.IMPORT_SORT_MEMBERS = value
  }

  override fun getConfigValue(option: TslintJsonOption): Boolean {
    return option.getStringMapValue()["named-imports-order"] != "any"
  }
}

class SortedImportPathsRule : TsLintSimpleRule<Boolean>("ordered-imports") {
  override fun getConfigValue(option: TslintJsonOption): Boolean {
    return option.getStringMapValue()["import-sources-order"] != "any"
  }

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return codeStyleSettings.IMPORT_SORT_MODULE_NAME
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    codeStyleSettings.IMPORT_SORT_MODULE_NAME = value
  }
}

class LinebreakStyleRule : TsLintSimpleRule<String>("linebreak-style") {
  override fun getConfigValue(option: TslintJsonOption): String? {
    val values = option.getStringValues()
    if (values.contains("CRLF")) {
      return LineSeparator.CRLF.separatorString
    }
    if (values.contains("LF")) {
      return LineSeparator.LF.separatorString
    }
    return null
  }

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): String {
    return languageSettings.rootSettings.lineSeparator
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: String) {
    languageSettings.rootSettings.LINE_SEPARATOR = value
  }
}

class NewlineAtEndOfFileRule : TsLintSimpleRule<Boolean>("eofline") {
  override fun getConfigValue(option: TslintJsonOption): Boolean {
    return true
  }

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return EditorSettingsExternalizable.getInstance().isEnsureNewLineAtEOF
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    EditorSettingsExternalizable.getInstance().isEnsureNewLineAtEOF = value
  }
}

class AlignFunctionDeclarationParametersRule : MergedArrayRule("align") {
  override fun getCode(): String = "parameters"
  override fun defaultValue(): Boolean = true

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return languageSettings.ALIGN_MULTILINE_PARAMETERS
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.ALIGN_MULTILINE_PARAMETERS = value
  }
}

class AlignFunctionCallParametersRule : MergedArrayRule("align") {
  override fun getCode(): String = "arguments"
  override fun defaultValue(): Boolean = true

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return languageSettings.ALIGN_MULTILINE_PARAMETERS_IN_CALLS
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.ALIGN_MULTILINE_PARAMETERS_IN_CALLS = value
  }
}

class MaxBlankLinesRule : TsLintSimpleRule<Int>("no-consecutive-blank-lines") {
  override fun getConfigValue(option: TslintJsonOption): Int {
    return option.getNumberValue() ?: 1
  }

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Int {
    return languageSettings.KEEP_BLANK_LINES_IN_CODE
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Int) {
    languageSettings.KEEP_BLANK_LINES_IN_CODE = value
  }
}

class WhitespaceAtEndOfLineRule : TsLintSimpleRule<Boolean>("no-trailing-whitespace") {
  override fun getConfigValue(option: TslintJsonOption): Boolean = true

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return EditorSettingsExternalizable.getInstance().stripTrailingSpaces != EditorSettingsExternalizable.STRIP_TRAILING_SPACES_NONE
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    EditorSettingsExternalizable.getInstance().stripTrailingSpaces = EditorSettingsExternalizable.STRIP_TRAILING_SPACES_WHOLE
  }
}

class SpaceAtLineCommentStartRule : TsLintSimpleRule<Boolean>("comment-format") {
  override fun getConfigValue(option: TslintJsonOption): Boolean? {
    if (option.getStringValues().contains("check-space"))
      return true
    return null
  }

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return languageSettings.LINE_COMMENT_ADD_SPACE
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    languageSettings.LINE_COMMENT_ADD_SPACE = value
    languageSettings.LINE_COMMENT_AT_FIRST_COLUMN = false
  }
}

class MergeImportsFromSameModuleRule : TsLintSimpleRule<Boolean>("no-duplicate-imports") {
  override fun getConfigValue(option: TslintJsonOption): Boolean = true
  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Boolean {
    return codeStyleSettings.isMergeImports
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Boolean) {
    codeStyleSettings.isMergeImports = value
  }
}

class FilenameConventionRule : TsLintSimpleRule<JSCodeStyleSettings.JSFileNameStyle>("file-name-casing") {
  override fun getConfigValue(option: TslintJsonOption): JSCodeStyleSettings.JSFileNameStyle? {
    val string = option.getStringValues().elementAtOrNull(0)
    if (string == "camel-case") {
      return JSCodeStyleSettings.JSFileNameStyle.CAMEL_CASE
    }
    if (string == "pascal-case") {
      return JSCodeStyleSettings.JSFileNameStyle.PASCAL_CASE
    }
    if (string == "kebab-case") {
      return JSCodeStyleSettings.JSFileNameStyle.LISP_CASE
    }
    return null

  }

  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings,
                                codeStyleSettings: JSCodeStyleSettings): JSCodeStyleSettings.JSFileNameStyle {
    return codeStyleSettings.FILE_NAME_STYLE
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings,
                        codeStyleSettings: JSCodeStyleSettings,
                        value: JSCodeStyleSettings.JSFileNameStyle) {
    codeStyleSettings.FILE_NAME_STYLE = value
  }
}

class ForceCurlyBracesRule : TsLintSimpleRule<Int>("curly") {
  override fun getSettingsValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings): Int {
    val ifBraceForce = languageSettings.IF_BRACE_FORCE
    if (languageSettings.FOR_BRACE_FORCE == ifBraceForce &&
        languageSettings.WHILE_BRACE_FORCE == ifBraceForce &&
        languageSettings.DOWHILE_BRACE_FORCE == ifBraceForce) {
      return ifBraceForce
    }
    return -1
  }

  override fun setValue(languageSettings: CommonCodeStyleSettings, codeStyleSettings: JSCodeStyleSettings, value: Int) {
    languageSettings.IF_BRACE_FORCE = value
    languageSettings.FOR_BRACE_FORCE = value
    languageSettings.WHILE_BRACE_FORCE = value
    languageSettings.DOWHILE_BRACE_FORCE = value
  }

  override fun getConfigValue(option: TslintJsonOption): Int {
    val values = option.getStringValues()
    if (values.contains("as-needed")) {
      return CommonCodeStyleSettings.DO_NOT_FORCE
    }
    if (values.contains("ignore-same-line")){
      return CommonCodeStyleSettings.FORCE_BRACES_IF_MULTILINE
    }
    return CommonCodeStyleSettings.FORCE_BRACES_ALWAYS
  }
}