package tanvd.grazi.ide.ui

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.options.ConfigurableBase
import tanvd.grazi.GraziConfig

class GraziConfigurable : ConfigurableBase<GraziSettingsPanel, GraziConfig>(
        "reference.settingsdialog.project.grazi", "Grazi", null) {

    private lateinit var ui: GraziSettingsPanel

    override fun getSettings(): GraziConfig = ServiceManager.getService(GraziConfig::class.java)

    override fun createUi(): GraziSettingsPanel = GraziSettingsPanel().also { ui = it }

    override fun enableSearch(option: String?) = ui.showOption(option)
}
