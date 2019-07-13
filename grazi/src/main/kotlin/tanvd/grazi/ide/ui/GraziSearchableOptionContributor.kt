package tanvd.grazi.ide.ui

import com.intellij.ide.ui.search.SearchableOptionContributor
import com.intellij.ide.ui.search.SearchableOptionProcessor
import tanvd.grazi.language.Lang

class GraziSearchableOptionContributor : SearchableOptionContributor() {
    private val id = "reference.settingsdialog.project.grazi"
    private val displayName = "Grazi"

    private fun SearchableOptionProcessor.addOptions(text: String, path: String, hit: String = text) {
        addOptions(text, path, hit, id, displayName, true)
    }

    override fun processOptions(processor: SearchableOptionProcessor) {
        with(processor) {
            addOptions(msg("grazi.ui.settings.enable.text"), msg("grazi.ui.settings.config.text"))
            addOptions(msg("grazi.ui.settings.rules.configuration.text"), msg("grazi.ui.settings.config.text"))
            addOptions(msg("grazi.ui.settings.languages.native.text"), msg("grazi.ui.settings.languages.text"))

            Lang.values().forEach { lang ->
                addOptions(lang.displayName, msg("grazi.ui.settings.languages.text"))
            }
        }
    }
}
