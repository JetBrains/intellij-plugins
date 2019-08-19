package tanvd.grazi.ide.ui.components.settings

import com.intellij.icons.AllIcons
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.SideBorder
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.ui.components.labels.LinkListener
import com.intellij.ui.components.panels.HorizontalLayout
import com.intellij.ui.layout.migLayout.createLayoutConstraints
import com.intellij.util.ui.JBUI
import net.miginfocom.layout.CC
import net.miginfocom.swing.MigLayout
import tanvd.grazi.ide.ui.components.dsl.msg
import tanvd.grazi.ide.ui.components.dsl.padding
import tanvd.grazi.ide.ui.components.dsl.pane
import tanvd.grazi.ide.ui.components.dsl.panel
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.ScrollPaneConstants

class GraziRuleDescriptionComponent {
    private class GraziRuleLinkComponent {
        private val link = LinkLabel<Any?>(msg("grazi.ui.settings.rules.rule.description"), null)
        var listener: LinkListener<Any?>
            @Deprecated("Property can only be written", level = DeprecationLevel.ERROR)
            get() = throw NotImplementedError()
            set(value) {
                link.setListener(value, null)
            }

        val component = panel(HorizontalLayout(0)) {
            border = padding(JBUI.insetsBottom(7))
            name = "GRAZI_LINK_PANEL"
            isVisible = false

            add(link)
            add(JLabel(AllIcons.Ide.External_link_arrow))
        }
    }

    private val link = GraziRuleLinkComponent()
    private val description = pane()

    val listener: (Any) -> Unit
        get() = { selection ->
            link.component.isVisible = getLinkLabelListener(selection)?.let { listener ->
                link.listener = listener
                true
            } ?: false

            description.text = getDescriptionPaneContent(selection).also {
                description.isVisible = it.isNotBlank()
            }
        }

    val component = panel(MigLayout(createLayoutConstraints().flowY().fillX())) {
        border = padding(JBUI.insets(30, 20, 0, 0))
        add(link.component, CC().grow().hideMode(3))

        val descriptionPanel = JBPanelWithEmptyText(BorderLayout(0, 0)).withEmptyText(msg("grazi.ui.settings.rules.no-description"))
        descriptionPanel.add(description)
        add(ScrollPaneFactory.createScrollPane(descriptionPanel, SideBorder.NONE).also {
            it.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        }, CC().grow().push())
    }
}
