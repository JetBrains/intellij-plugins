package tanvd.grazi.ide.ui.components.settings

import com.intellij.icons.AllIcons
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.util.ui.JBUI
import tanvd.grazi.GraziConfig
import tanvd.grazi.ide.ui.components.dsl.msg
import tanvd.grazi.ide.ui.components.dsl.padding
import tanvd.grazi.ide.ui.components.dsl.panel
import tanvd.grazi.ide.ui.components.langlist.GraziAddDeleteListPanel
import tanvd.grazi.language.Lang
import java.awt.BorderLayout

class GraziLanguagesComponent(download: (Lang) -> Boolean, onLanguageAdded: (Lang) -> Unit, onLanguageRemoved: (Lang) -> Unit) {
    private val link: LinkLabel<Any?> = LinkLabel<Any?>(msg("grazi.languages.action"), AllIcons.General.Warning).apply {
        border = padding(JBUI.insetsTop(10))
        setListener({ _, _ -> GraziConfig.get().missedLanguages.forEach { download(it) } }, null)
    }

    private val languages = GraziAddDeleteListPanel(download, onLanguageAdded, { onLanguageRemoved(it); update() })

    val values
        get() = languages.listItems.toSet()

    val component = panel {
        add(languages, BorderLayout.CENTER)
        add(link, BorderLayout.SOUTH)
    }

    fun reset(langs: Collection<Lang>) = languages.reset(langs)

    fun update() {
        link.isVisible = GraziConfig.get().hasMissedLanguages(withNative = false)
    }
}
