package com.intellij.dts.settings

import com.intellij.application.options.IndentOptionsEditor
import com.intellij.application.options.SmartIndentOptionsEditor
import com.intellij.lang.Language
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizableOptions
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider
import com.intellij.dts.lang.DtsLanguage

class DtsLangCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {
    override fun getLanguage(): Language = DtsLanguage

    override fun getIndentOptionsEditor(): IndentOptionsEditor = SmartIndentOptionsEditor()

    override fun customizeSettings(consumer: CodeStyleSettingsCustomizable, settingsType: SettingsType) {
        when (settingsType) {
            SettingsType.SPACING_SETTINGS -> customizeSpacingSettings(consumer)
            SettingsType.WRAPPING_AND_BRACES_SETTINGS -> customizeWrappingSettings(consumer)
            else -> {}
        }
    }

    private fun customizeSpacingSettings(consumer: CodeStyleSettingsCustomizable) {
        consumer.showStandardOptions(
            "SPACE_AROUND_ASSIGNMENT_OPERATORS",
            "SPACE_AROUND_RELATIONAL_OPERATORS",
            "SPACE_AROUND_BITWISE_OPERATORS",
            "SPACE_AROUND_ADDITIVE_OPERATORS",
            "SPACE_AROUND_MULTIPLICATIVE_OPERATORS",
            "SPACE_AROUND_SHIFT_OPERATORS",
            "SPACE_AROUND_LOGICAL_OPERATORS",
            "SPACE_WITHIN_BRACKETS",
            "SPACE_WITHIN_PARENTHESES",
            "SPACE_AFTER_COMMA",
            "SPACE_BEFORE_COMMA",
        )

        consumer.renameStandardOption("SPACE_AROUND_ASSIGNMENT_OPERATORS", "Assignment operator (=)")
        consumer.renameStandardOption("SPACE_AROUND_SHIFT_OPERATORS", "Shift operators (<<, >>)")
        consumer.renameStandardOption("SPACE_WITHIN_BRACKETS", "Byte array")
        consumer.renameStandardOption("SPACE_WITHIN_PARENTHESES", "Parentheses")

        val options = CodeStyleSettingsCustomizableOptions.getInstance()

        consumer.showCustomOption(
            DtsCodeStyleSettings::class.java,
            "SPACE_WITHIN_ANGULAR_BRACKETS",
            "Cell array",
            options.SPACES_WITHIN,
        )
        consumer.showCustomOption(
            DtsCodeStyleSettings::class.java,
            "SPACE_WITHIN_EMPTY_NODE",
            "Empty node",
            options.SPACES_WITHIN,
        )
        consumer.showCustomOption(
            DtsCodeStyleSettings::class.java,
            "SPACE_BETWEEN_BYTES",
            "Between bytes",
            options.SPACES_OTHER,
        )
        consumer.showCustomOption(
            DtsCodeStyleSettings::class.java,
            "SPACE_AFTER_LABEL",
            "After label",
            options.SPACES_OTHER,
        )
    }

    private fun customizeWrappingSettings(consumer: CodeStyleSettingsCustomizable) {
        val alignmentGroup = "Alignment"

        consumer.showStandardOptions(
            "WRAP_ON_TYPING",
            "KEEP_LINE_BREAKS",
            "WRAP_LONG_LINES",
        )

        consumer.showCustomOption(
            DtsCodeStyleSettings::class.java,
            "ALIGN_PROPERTY_ASSIGNMENT",
            "Property assignments",
            alignmentGroup,
        )
        consumer.showCustomOption(
            DtsCodeStyleSettings::class.java,
            "ALIGN_PROPERTY_VALUES",
            "Property values",
            alignmentGroup,
        )
    }

    override fun getCodeSample(settingsType: SettingsType): String {
        return when (settingsType) {
            SettingsType.SPACING_SETTINGS -> Codesamples.spacing
            SettingsType.INDENT_SETTINGS -> Codesamples.indenting
            SettingsType.WRAPPING_AND_BRACES_SETTINGS -> Codesamples.wrapping
            else -> Codesamples.spacing
        }
    }
}