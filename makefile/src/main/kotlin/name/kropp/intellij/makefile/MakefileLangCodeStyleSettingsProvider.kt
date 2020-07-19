package name.kropp.intellij.makefile

import com.intellij.application.options.*
import com.intellij.psi.codeStyle.*

class MakefileLangCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {
  override fun customizeDefaults(commonSettings: CommonCodeStyleSettings, indentOptions: CommonCodeStyleSettings.IndentOptions) {
    indentOptions.TAB_SIZE = 4
    indentOptions.USE_TAB_CHARACTER = true
  }

  override fun customizeSettings(consumer: CodeStyleSettingsCustomizable, settingsType: SettingsType) {
    if (settingsType == SettingsType.INDENT_SETTINGS) {
      consumer.showStandardOptions("TAB_SIZE")
    }
  }

  override fun getCodeSample(settingsType: SettingsType): String {
    return """# Simple Makefile
include make.mk

all: hello

GCC = gcc \
           -O2

<target>.o.c</target>:
ifeq ($(FOO),'bar')
${'\t'}$(GCC) -c qwe \
              -Wall
else
${'\t'}echo "Hello World"
endif"""
  }

  override fun getIndentOptionsEditor(): IndentOptionsEditor = IndentOptionsEditor()

  override fun getLanguage() = MakefileLanguage
}