// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.formatter

import com.intellij.application.options.CodeStyleAbstractConfigurable
import com.intellij.application.options.CodeStyleAbstractPanel
import com.intellij.application.options.TabbedLanguageCodeStylePanel
import com.intellij.lang.Language
import com.intellij.psi.codeStyle.CodeStyleConfigurable
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider
import com.intellij.psi.codeStyle.CustomCodeStyleSettings
import org.intellij.terraform.hcl.HCLLanguage

open class HCLCodeStyleSettingsProvider(val _language: Language = HCLLanguage) : CodeStyleSettingsProvider() {
  override fun createConfigurable(settings: CodeStyleSettings, originalSettings: CodeStyleSettings): CodeStyleConfigurable {
    return object : CodeStyleAbstractConfigurable(settings, originalSettings, _language.displayName) {
      override fun createPanel(settings: CodeStyleSettings): CodeStyleAbstractPanel {
        val currentSettings = currentSettings
        return object : TabbedLanguageCodeStylePanel(_language, currentSettings, settings) {
          override fun initTabs(settings: CodeStyleSettings) {
            addIndentOptionsTab(settings)
            addSpacesTab(settings)
            addBlankLinesTab(settings)
            addWrappingAndBracesTab(settings)
            addTab(HCLCodeStylePanel(_language, settings))
          }
        }
      }
    }
  }

  override fun getLanguage(): Language? {
    return _language
  }

  override fun createCustomSettings(settings: CodeStyleSettings): CustomCodeStyleSettings? {
    return HCLCodeStyleSettings(settings, _language)
  }
}
