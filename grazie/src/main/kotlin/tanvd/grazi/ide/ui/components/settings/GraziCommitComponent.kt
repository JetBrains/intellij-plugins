// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package tanvd.grazi.ide.ui.components.settings

import com.intellij.ui.components.JBCheckBox
import tanvd.grazi.ide.ui.components.dsl.msg
import tanvd.grazi.ide.ui.components.dsl.wrapWithComment

class GraziCommitComponent {
  private val checkbox = JBCheckBox(msg("grazi.ui.settings.vcs.enable.text"))
  var isCommitIntegrationEnabled: Boolean
    get() = checkbox.isSelected
    set(value) {
      checkbox.isSelected = value
    }

  val component = wrapWithComment(checkbox, msg("grazi.ui.settings.vcs.enable.note"))
}
