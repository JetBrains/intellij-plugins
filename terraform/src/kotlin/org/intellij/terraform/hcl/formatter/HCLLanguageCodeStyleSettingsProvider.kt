/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.terraform.hcl.formatter

import com.intellij.application.options.IndentOptionsEditor
import com.intellij.application.options.SmartIndentOptionsEditor
import com.intellij.lang.Language
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizableOptions
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.HCLLanguage

open class HCLLanguageCodeStyleSettingsProvider(private val language:Language = HCLLanguage) : LanguageCodeStyleSettingsProvider() {
  override fun getLanguage() = language

  companion object {
    const val SAMPLE = """
    name = value
    block 'name' {
      long_array = [ 'a', 100, "b", 1234567890, 1234567890, 1234567890, 1234567890, 10e100, true, false ]
      arr = []
      empty_object = {}
      one_line_object = { something : "Yep!", other : "Nope!" }
      object = { 
        yeah : "Yay"
        meh : "Nein" 
      }
    }
    some_object = {
      echo = true
    }
    """
  }

  override fun getCodeSample(settingsType: SettingsType): String {
    return SAMPLE
  }

  override fun getIndentOptionsEditor(): IndentOptionsEditor? = SmartIndentOptionsEditor()

  override fun customizeDefaults(commonSettings: CommonCodeStyleSettings, indentOptions: CommonCodeStyleSettings.IndentOptions) {
    indentOptions.INDENT_SIZE = 2
  }

  override fun customizeSettings(consumer: CodeStyleSettingsCustomizable, settingsType: SettingsType) {
    when (settingsType) {
      SettingsType.SPACING_SETTINGS -> {
        consumer.showStandardOptions("SPACE_WITHIN_BRACKETS", "SPACE_WITHIN_BRACES", "SPACE_AFTER_COMMA", "SPACE_BEFORE_COMMA", "SPACE_AROUND_ASSIGNMENT_OPERATORS")
        consumer.renameStandardOption("SPACE_WITHIN_BRACES", HCLBundle.message("hcl.code.style.settings.braces"))
        consumer.renameStandardOption("SPACE_AROUND_ASSIGNMENT_OPERATORS", HCLBundle.message("hcl.code.style.settings.equals"))
      }
      SettingsType.BLANK_LINES_SETTINGS -> {
        consumer.showStandardOptions("KEEP_BLANK_LINES_IN_CODE")
      }
      SettingsType.WRAPPING_AND_BRACES_SETTINGS -> {
        consumer.showStandardOptions("RIGHT_MARGIN", "KEEP_LINE_BREAKS", "WRAP_LONG_LINES")
        consumer.showCustomOption(HCLCodeStyleSettings::class.java, "ARRAY_WRAPPING", HCLBundle.message("hcl.code.style.settings.arrays"), null,
                                  CodeStyleSettingsCustomizableOptions.getInstance().WRAP_OPTIONS,
                                  CodeStyleSettingsCustomizable.WRAP_VALUES)
        consumer.showCustomOption(HCLCodeStyleSettings::class.java, "OBJECT_WRAPPING", HCLBundle.message("hcl.code.style.settings.objects"), null,
                                  CodeStyleSettingsCustomizableOptions.getInstance().WRAP_OPTIONS,
                                  CodeStyleSettingsCustomizable.WRAP_VALUES)
      }
      else -> {}
    }
  }
}
