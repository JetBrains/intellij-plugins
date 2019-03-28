package tanvd.grazi.ui

import com.intellij.openapi.options.ConfigurableBase

class GraziToolConfigurable : ConfigurableBase<GraziSettingsPanel, GraziApplicationSettings>("reference.settingsdialog.project.grazi", "Grazi", null) {

    override fun getSettings(): GraziApplicationSettings {
        return GraziApplicationSettings.instance
    }

    override fun createUi(): GraziSettingsPanel {
        return GraziSettingsPanel()
    }
}
