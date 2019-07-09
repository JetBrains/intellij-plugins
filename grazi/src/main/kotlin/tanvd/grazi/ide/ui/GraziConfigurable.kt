package tanvd.grazi.ide.ui

import com.intellij.ide.ui.search.SearchableOptionsRegistrar
import com.intellij.openapi.options.ConfigurableBase
import tanvd.grazi.GraziConfig
import tanvd.grazi.language.Lang

class GraziConfigurable : ConfigurableBase<GraziSettingsPanel, GraziConfig>(
        "reference.settingsdialog.project.grazi", "Grazi", null) {

    private lateinit var ui: GraziSettingsPanel

    init {
        // TODO search
        val registrar = SearchableOptionsRegistrar.getInstance();
        registrar.addOption("spellcheck", displayName, displayName, id, displayName)
        registrar.addOption("enable", displayName, displayName, id, displayName)
        registrar.addOption("configuration", displayName, displayName, id, displayName)
        registrar.addOption("rules", displayName, displayName, id, displayName)
        registrar.addOption("languages", displayName, displayName, id, displayName)
        registrar.addOption("native", displayName, displayName, id, displayName)

        // FIXME not working
        Lang.values().forEach {
            registrar.addOption(it.displayName.toLowerCase(), displayName, displayName, id, displayName)
        }
    }

    override fun getSettings() = GraziConfig.instance


    override fun createUi(): GraziSettingsPanel {
        ui = GraziSettingsPanel()
        return ui
    }

    override fun enableSearch(option: String?): Runnable? {
        return ui.showOption(option)
    }
}
