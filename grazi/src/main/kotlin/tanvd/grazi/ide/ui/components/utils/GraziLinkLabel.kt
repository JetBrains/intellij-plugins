package tanvd.grazi.ide.ui.components.utils

import com.intellij.icons.AllIcons
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.ui.components.labels.LinkListener
import com.intellij.ui.components.panels.HorizontalLayout
import tanvd.grazi.ide.ui.components.dsl.panel
import javax.swing.Icon
import javax.swing.JLabel

class GraziLinkLabel(text: String, icon: Icon = AllIcons.Ide.External_link_arrow) {
    private val link = LinkLabel<Any?>(text, null)
    var listener: LinkListener<Any?>
        @Deprecated("Property can only be written", level = DeprecationLevel.ERROR)
        get() = throw NotImplementedError()
        set(value) {
            link.setListener(value, null)
        }

    val component = panel(HorizontalLayout(0)) {
        add(link)
        add(JLabel(icon))
    }
}
