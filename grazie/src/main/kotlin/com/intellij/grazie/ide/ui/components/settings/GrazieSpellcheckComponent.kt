// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.ide.ui.components.settings

import com.intellij.grazie.ide.ui.components.dsl.msg
import com.intellij.grazie.ide.ui.components.dsl.padding
import com.intellij.grazie.ide.ui.components.dsl.panel
import com.intellij.ui.ContextHelpLabel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.panels.HorizontalLayout
import com.intellij.util.ui.JBUI

class GrazieSpellcheckComponent {
  private val checkbox = JBCheckBox(msg("grazie.ui.settings.spellcheck.enable.text"))
  var isSpellcheckEnabled: Boolean
    get() = checkbox.isSelected
    set(value) {
      checkbox.isSelected = value
    }

  val component = panel(HorizontalLayout(10)) {
    border = padding(JBUI.insetsBottom(10))
    add(checkbox)
    add(ContextHelpLabel.create(msg("grazie.ui.settings.spellcheck.enable.note")))
  }
}
