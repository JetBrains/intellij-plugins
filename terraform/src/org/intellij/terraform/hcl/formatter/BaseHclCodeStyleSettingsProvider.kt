// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.formatter

import com.intellij.application.options.CodeStyleAbstractConfigurable
import com.intellij.application.options.CodeStyleAbstractPanel
import com.intellij.application.options.TabbedLanguageCodeStylePanel
import com.intellij.lang.Language
import com.intellij.psi.codeStyle.CodeStyleConfigurable
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider
import com.intellij.psi.codeStyle.CustomCodeStyleSettings
import org.intellij.terraform.config.TerraformLanguage
import org.intellij.terraform.hcl.HCLLanguage

sealed class BaseHclCodeStyleSettingsProvider : CodeStyleSettingsProvider() {
  abstract override fun getLanguage(): Language

  override fun createConfigurable(settings: CodeStyleSettings, originalSettings: CodeStyleSettings): CodeStyleConfigurable {
    return object : CodeStyleAbstractConfigurable(settings, originalSettings, language.displayName) {
      override fun createPanel(settings: CodeStyleSettings): CodeStyleAbstractPanel {
        val currentSettings = currentSettings
        return object : TabbedLanguageCodeStylePanel(language, currentSettings, settings) {
          override fun initTabs(settings: CodeStyleSettings) {
            addIndentOptionsTab(settings)
            addSpacesTab(settings)
            addBlankLinesTab(settings)
            addWrappingAndBracesTab(settings)
            addTab(HclCodeStyleOtherPanel(settings, language))
          }
        }
      }
    }
  }

  override fun createCustomSettings(settings: CodeStyleSettings): CustomCodeStyleSettings? {
    return HclCodeStyleSettings(settings, language)
  }
}

class HclCodeStyleSettingsProvider : BaseHclCodeStyleSettingsProvider() {
  override fun getLanguage(): Language = HCLLanguage
}

class TfCodeStyleSettingsProvider : BaseHclCodeStyleSettingsProvider() {
  override fun getLanguage(): Language = TerraformLanguage
}