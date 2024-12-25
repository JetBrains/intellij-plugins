package com.jetbrains.lang.makefile

import com.intellij.application.options.CodeStyleAbstractConfigurable
import com.intellij.application.options.CodeStyleAbstractPanel
import com.intellij.application.options.TabbedLanguageCodeStylePanel
import com.intellij.lang.Language
import com.intellij.psi.codeStyle.CodeStyleConfigurable
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider
import com.intellij.psi.codeStyle.CustomCodeStyleSettings

private class MakefileCodeStyleSettingsProvider : CodeStyleSettingsProvider() {
  override fun createConfigurable(settings: CodeStyleSettings, modelSettings: CodeStyleSettings): CodeStyleConfigurable {
    return object : CodeStyleAbstractConfigurable(settings, modelSettings, MakefileLangBundle.message("configurable.name")) {
      override fun createPanel(settings: CodeStyleSettings): CodeStyleAbstractPanel {
        return object : TabbedLanguageCodeStylePanel(MakefileLanguage, currentSettings, settings) {
          override fun initTabs(settings: CodeStyleSettings) {
            addIndentOptionsTab(settings)
          }
        }
      }
      override fun getHelpTopic(): String? = null
    }
  }

  override fun getLanguage(): Language = MakefileLanguage
  override fun createCustomSettings(settings: CodeStyleSettings): CustomCodeStyleSettings = MakefileCodeStyleSettings(settings)
}