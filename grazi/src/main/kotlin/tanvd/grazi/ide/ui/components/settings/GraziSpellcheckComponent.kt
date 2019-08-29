package tanvd.grazi.ide.ui.components.settings

import com.intellij.ui.components.JBCheckBox
import tanvd.grazi.ide.ui.components.dsl.msg
import tanvd.grazi.ide.ui.components.dsl.wrapWithComment

class GraziSpellcheckComponent {
    private val checkbox = JBCheckBox(msg("grazi.ui.settings.enable.text"))
    var isSpellcheckEnabled: Boolean
        get() = checkbox.isSelected
        set(value) {
            checkbox.isSelected = value
        }

    val component = wrapWithComment(checkbox, msg("grazi.ui.settings.enable.note"))
}
