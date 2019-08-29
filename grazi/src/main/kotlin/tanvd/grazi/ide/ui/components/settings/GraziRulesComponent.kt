package tanvd.grazi.ide.ui.components.settings

import org.picocontainer.Disposable
import tanvd.grazi.ide.ui.components.rules.GraziRulesPanel
import tanvd.grazi.language.Lang

class GraziRulesComponent(onSelectionChanged: (Any) -> Unit) : Disposable {
    private val rules = GraziRulesPanel(onSelectionChanged)

    val component = rules.panel
    val state
        get() = rules.state()
    val isModified
        get() = rules.isModified

    fun reset() = rules.reset()
    fun filter(str: String?) = rules.filter(str ?: "")
    fun addLang(lang: Lang) = rules.addLang(lang)
    fun removeLang(lang: Lang) = rules.removeLang(lang)

    override fun dispose() = rules.dispose()
}
