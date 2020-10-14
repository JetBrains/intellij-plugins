// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.eslint

import com.intellij.json.psi.JsonNumberLiteral
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.json.psi.JsonValue
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.javascript.linter.eslint.importer.EslintRuleMapper
import com.intellij.lang.javascript.linter.eslint.importer.EslintRuleMappersFactory
import com.intellij.lang.javascript.linter.eslint.importer.EslintSettingsConverter
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.formatter.xml.HtmlCodeStyleSettings
import com.intellij.util.castSafelyTo
import com.intellij.xml.util.HtmlUtil
import org.jetbrains.vuejs.lang.html.VueLanguage
import org.jetbrains.vuejs.lang.html.psi.formatter.VueCodeStyleSettings

class VueEslintRuleMappersFactory : EslintRuleMappersFactory {

  override fun createMappers(): List<EslintRuleMapper> = listOf(
    VueHtmlClosingBracketNewline(),
    VueHtmlIndent(),
    VueHtmlQuotes(),
    VueMustacheInterpolationSpacing(),
    VueNoSpacesAroundEqualSignsInAttribute(),
    VueScriptIndent()
  )

  private class VueHtmlClosingBracketNewline : EslintRuleMapper("vue/html-closing-bracket-newline") {
    override fun create(values: MutableList<JsonValue>?, eslintConfig: EslintConfig): EslintSettingsConverter {
      val options = values?.getOrNull(0) as? JsonObject
      // "singleline" not supported
      val multiLine = getAlwaysNeverOption(options?.findProperty("multiline"), true) ?: return EslintSettingsConverter.MISCONFIGURATION

      val newlineSetting = if (!multiLine)
        CodeStyleSettings.HtmlTagNewLineStyle.Never
      else
        CodeStyleSettings.HtmlTagNewLineStyle.WhenMultiline

      return EslintHtmlSettingsConverter(
        inSync = { _, custom ->
          custom.HTML_NEWLINE_AFTER_LAST_ATTRIBUTE == newlineSetting
        },
        applier = { _, custom ->
          custom.HTML_NEWLINE_AFTER_LAST_ATTRIBUTE = newlineSetting
        })
    }
  }

  private class VueHtmlIndent : EslintRuleMapper("vue/html-indent") {
    override fun create(values: MutableList<JsonValue>?, eslintConfig: EslintConfig): EslintSettingsConverter {
      val isTab = values?.getOrNull(0)?.castSafelyTo<JsonStringLiteral>()?.value == "tab"
      val base = if (isTab) 1 else values?.getOrNull(0)?.castSafelyTo<JsonNumberLiteral>()?.value?.toInt() ?: 2
      val config = values?.getOrNull(1)
      val indent = getIntOptionValue(config, "baseIndent", 1) ?: return EslintSettingsConverter.MISCONFIGURATION
      val attributeIndent = getIntOptionValue(config, "attribute", 1) ?: return EslintSettingsConverter.MISCONFIGURATION
      val alignAttributes = getBooleanOptionValue(config as? JsonObject, "alignAttributesVertically", true)
                            ?: return EslintSettingsConverter.MISCONFIGURATION
      val getIndentOptions = { settings: CodeStyleSettings ->
        if (settings.getLanguageIndentOptions(VueLanguage.INSTANCE).isOverrideLanguageOptions) {
          settings.getLanguageIndentOptions(VueLanguage.INSTANCE)
        }
        else {
          settings.getLanguageIndentOptions(HTMLLanguage.INSTANCE)
        }
      }
      // "closeBracket" not supported
      return object : EslintSettingsConverter {
        override fun inSync(settings: CodeStyleSettings): Boolean =
          getIndentOptions(settings).let {
            it.USE_TAB_CHARACTER == isTab
            && (!isTab || it.TAB_SIZE == 1)
            && it.INDENT_SIZE == indent * base
            && it.CONTINUATION_INDENT_SIZE == attributeIndent * base
          }
          && settings.getCustomSettings(HtmlCodeStyleSettings::class.java)
            .HTML_ALIGN_ATTRIBUTES == alignAttributes

        override fun apply(settings: CodeStyleSettings) {
          getIndentOptions(settings).let {
            it.USE_TAB_CHARACTER = isTab
            if (isTab) it.TAB_SIZE = 1
            it.INDENT_SIZE = indent * base
            it.CONTINUATION_INDENT_SIZE = attributeIndent * base
          }
          settings.getCustomSettings(HtmlCodeStyleSettings::class.java)
            .HTML_ALIGN_ATTRIBUTES = alignAttributes
        }
      }
    }
  }

  private class VueHtmlQuotes : EslintRuleMapper("vue/html-quotes") {
    override fun create(values: MutableList<JsonValue>?, eslintConfig: EslintConfig): EslintSettingsConverter {
      val quoteStyle = when (values?.getOrNull(0)
        ?.castSafelyTo<JsonStringLiteral>()
        ?.value) {
        "double" -> CodeStyleSettings.QuoteStyle.Double
        "single" -> CodeStyleSettings.QuoteStyle.Single
        null -> CodeStyleSettings.QuoteStyle.Double
        else -> return EslintSettingsConverter.MISCONFIGURATION
      }
      // "avoid escape" not supported
      return EslintHtmlSettingsConverter(
        inSync = { _, custom ->
          custom.HTML_QUOTE_STYLE == quoteStyle && custom.HTML_ENFORCE_QUOTES
        },
        applier = { _, custom ->
          custom.HTML_QUOTE_STYLE = quoteStyle
          custom.HTML_ENFORCE_QUOTES = true
        })
    }
  }

  private class VueMustacheInterpolationSpacing : EslintRuleMapper("vue/mustache-interpolation-spacing") {
    override fun create(values: MutableList<JsonValue>?, eslintConfig: EslintConfig): EslintSettingsConverter {
      val spaces = getAlwaysNeverOption(values, true) ?: return EslintSettingsConverter.MISCONFIGURATION
      return object : EslintSettingsConverter {
        override fun inSync(settings: CodeStyleSettings): Boolean =
          settings.getCustomSettings(VueCodeStyleSettings::class.java)
            .SPACES_WITHIN_INTERPOLATION_EXPRESSIONS == spaces

        override fun apply(settings: CodeStyleSettings) {
          settings.getCustomSettings(VueCodeStyleSettings::class.java)
            .SPACES_WITHIN_INTERPOLATION_EXPRESSIONS = spaces
        }
      }
    }
  }

  private class VueNoSpacesAroundEqualSignsInAttribute : EslintRuleMapper("vue/no-spaces-around-equal-signs-in-attribute") {
    override fun create(values: MutableList<JsonValue>?, eslintConfig: EslintConfig): EslintSettingsConverter {
      return EslintHtmlSettingsConverter(
        inSync = { _, custom -> !custom.HTML_SPACE_AROUND_EQUALITY_IN_ATTRIBUTE },
        applier = { _, custom -> custom.HTML_SPACE_AROUND_EQUALITY_IN_ATTRIBUTE = false }
      )
    }
  }

  private class VueScriptIndent : EslintRuleMapper("vue/script-indent") {
    override fun create(values: MutableList<JsonValue>?, eslintConfig: EslintConfig): EslintSettingsConverter {
      val isTab = values?.getOrNull(0)?.castSafelyTo<JsonStringLiteral>()?.value == "tab"
      val base = if (isTab) 1 else values?.getOrNull(0)?.castSafelyTo<JsonNumberLiteral>()?.value?.toInt() ?: 2
      val indent = getIntOptionValue(values?.getOrNull(1), "baseIndent", 0)
                   ?: return EslintSettingsConverter.MISCONFIGURATION

      // The mapping is only partial, we do not support complex scenarios here, nor should we
      val scriptIndentation = base * indent > 0

      return object : EslintSettingsConverter {
        override fun inSync(settings: CodeStyleSettings): Boolean =
          settings.getCustomSettings(VueCodeStyleSettings::class.java)
            .INDENT_CHILDREN_OF_TOP_LEVEL.splitToSequence(',')
            .map(String::trim).contains(HtmlUtil.SCRIPT_TAG_NAME) == scriptIndentation

        override fun apply(settings: CodeStyleSettings) {
          settings.getCustomSettings(VueCodeStyleSettings::class.java).let { custom ->
            custom.INDENT_CHILDREN_OF_TOP_LEVEL = custom.INDENT_CHILDREN_OF_TOP_LEVEL
              .splitToSequence(',').let {
                if (scriptIndentation) it.plus(HtmlUtil.SCRIPT_TAG_NAME)
                else it.filter { tagName -> tagName.trim() != HtmlUtil.SCRIPT_TAG_NAME }
              }
              .joinToString(",")
          }
        }
      }
    }
  }

  private class EslintHtmlSettingsConverter(
    private val inSync: (common: CommonCodeStyleSettings, custom: HtmlCodeStyleSettings) -> Boolean,
    private val applier: (common: CommonCodeStyleSettings, custom: HtmlCodeStyleSettings) -> Unit
  ) : EslintSettingsConverter {

    override fun inSync(settings: CodeStyleSettings): Boolean {
      val common = settings.getCommonSettings(HTMLLanguage.INSTANCE)
      val custom = settings.getCustomSettings(HtmlCodeStyleSettings::class.java)
      return inSync(common, custom)
    }

    override fun apply(settings: CodeStyleSettings) {
      val common = settings.getCommonSettings(HTMLLanguage.INSTANCE)
      val custom = settings.getCustomSettings(HtmlCodeStyleSettings::class.java)
      applier(common, custom)
    }

  }
}
