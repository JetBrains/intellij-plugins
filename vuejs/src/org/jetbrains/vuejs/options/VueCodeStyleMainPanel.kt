// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.options

import com.intellij.application.options.TabbedLanguageCodeStylePanel
import com.intellij.psi.codeStyle.CodeStyleSettings
import org.jetbrains.vuejs.lang.html.VueLanguage

class VueCodeStyleMainPanel(currentSettings: CodeStyleSettings?, settings: CodeStyleSettings)
  : TabbedLanguageCodeStylePanel(VueLanguage.INSTANCE, currentSettings, settings) {

  override fun initTabs(settings: CodeStyleSettings) {
    addIndentOptionsTab(settings)
    addSpacesTab(settings)
    addWrappingAndBracesTab(settings)
  }
}
