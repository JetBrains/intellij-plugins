package com.intellij.dts.settings

import com.intellij.application.options.CodeStyleAbstractConfigurable
import com.intellij.application.options.CodeStyleAbstractPanel
import com.intellij.application.options.TabbedLanguageCodeStylePanel
import com.intellij.dts.lang.DtsLanguage
import com.intellij.psi.codeStyle.CodeStyleConfigurable
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider
import com.intellij.psi.codeStyle.CustomCodeStyleSettings

private class DtsCodeStyleSettingsProvider : CodeStyleSettingsProvider() {
  private class MainPanel(currentSettings: CodeStyleSettings, settings: CodeStyleSettings) :
    TabbedLanguageCodeStylePanel(DtsLanguage, currentSettings, settings)

  private class Configurable(settings: CodeStyleSettings, modelSettings: CodeStyleSettings) :
    CodeStyleAbstractConfigurable(settings, modelSettings, DtsLanguage.displayName) {

    override fun getHelpTopic(): String = DtsHelpIds.CODESTYLE_SETTINGS

    override fun createPanel(settings: CodeStyleSettings): CodeStyleAbstractPanel = MainPanel(currentSettings, settings)
  }

  override fun getConfigurableDisplayName(): String = DtsLanguage.displayName

  override fun createCustomSettings(settings: CodeStyleSettings): CustomCodeStyleSettings = DtsCodeStyleSettings(settings)

  override fun createConfigurable(
    settings: CodeStyleSettings, modelSettings: CodeStyleSettings
  ): CodeStyleConfigurable = Configurable(settings, modelSettings)
}