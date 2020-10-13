// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.eslint

import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.javascript.linter.eslint.importer.EslintSettingsConverter
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.formatter.xml.HtmlCodeStyleSettings

class EslintHtmlSettingsConverter(
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