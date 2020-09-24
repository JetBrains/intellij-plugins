package name.kropp.intellij.makefile;

import com.intellij.application.options.*
import com.intellij.lang.*
import com.intellij.psi.codeStyle.*

class MakefileCodeStyleSettingsProvider : CodeStyleSettingsProvider() {
  override fun createConfigurable(settings: CodeStyleSettings, modelSettings: CodeStyleSettings): CodeStyleConfigurable {
    return object : CodeStyleAbstractConfigurable(settings, modelSettings, "Makefile") {
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
  override fun createCustomSettings(settings: CodeStyleSettings?): CustomCodeStyleSettings = MakefileCodeStyleSettings(settings)
}