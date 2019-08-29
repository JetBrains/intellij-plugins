package tanvd.grazi.ide.ui.components.settings

import com.intellij.icons.AllIcons
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.ContextHelpLabel
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.ui.layout.migLayout.createLayoutConstraints
import com.intellij.util.ui.JBUI
import net.miginfocom.layout.AC
import net.miginfocom.layout.CC
import net.miginfocom.swing.MigLayout
import tanvd.grazi.ide.ui.components.dsl.msg
import tanvd.grazi.ide.ui.components.dsl.padding
import tanvd.grazi.ide.ui.components.dsl.panel
import tanvd.grazi.ide.ui.components.dsl.wrapWithLabel
import tanvd.grazi.language.Lang

class GraziNativeLanguageComponent(download: (Lang) -> Boolean) {
    private val link: LinkLabel<Any?> = LinkLabel<Any?>("", AllIcons.General.Warning).apply {
        setListener({ _, _ -> download(language) }, null)
    }

    private val combobox = ComboBox(Lang.sortedValues().toTypedArray()).apply {
        addItemListener { event ->
            val lang = (event.item as Lang)
            link.text = msg("grazi.ui.settings.languages.native.warning", lang.displayName)
            link.isVisible = lang.jLanguage == null
        }
    }

    var language: Lang
        get() = combobox.selectedItem as Lang
        set(value) {
            combobox.selectedItem = value
        }

    val component = panel(MigLayout(createLayoutConstraints(), AC())) {
        border = padding(JBUI.insetsBottom(10))
        add(wrapWithLabel(combobox, msg("grazi.ui.settings.languages.native.text")), CC().minWidth("220px").maxWidth("380px"))
        add(ContextHelpLabel.create(msg("grazi.ui.settings.languages.native.help")).apply {
            border = padding(JBUI.insetsLeft(5))
        }, CC().width("30px").alignX("left").wrap())
        add(link, CC().hideMode(3))
    }

    fun update() {
        link.isVisible = language.jLanguage == null
    }
}
