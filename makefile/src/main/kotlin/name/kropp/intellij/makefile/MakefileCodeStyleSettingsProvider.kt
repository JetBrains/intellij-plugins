package name.kropp.intellij.makefile

import com.intellij.psi.codeStyle.*

class MakefileCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {
  override fun customizeDefaults(commonSettings: CommonCodeStyleSettings, indentOptions: CommonCodeStyleSettings.IndentOptions) {
    indentOptions.TAB_SIZE = 4
    indentOptions.USE_TAB_CHARACTER = true
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

  override fun getLanguage() = MakefileLanguage
}