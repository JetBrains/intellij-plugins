package tanvd.grazi.ide.ui

import com.intellij.openapi.options.ConfigurableBase
import tanvd.grazi.GraziConfig

class GraziConfigurable : ConfigurableBase<GraziSettingsPanel, GraziConfig>(
        "reference.settingsdialog.project.grazi", "Grazi", null) {

    override fun getSettings(): GraziConfig {
        return GraziConfig.instance
    }

    override fun createUi(): GraziSettingsPanel {
        return GraziSettingsPanel()
    }
}
