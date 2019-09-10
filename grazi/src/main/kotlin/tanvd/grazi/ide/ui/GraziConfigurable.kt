// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package tanvd.grazi.ide.ui

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.options.ConfigurableBase
import tanvd.grazi.GraziConfig
import tanvd.grazi.ide.ui.components.settings.GraziSettingsPanel

class GraziConfigurable : ConfigurableBase<GraziSettingsPanel, GraziConfig>("reference.settingsdialog.project.grazi", "Grazi", null) {
    private lateinit var ui: GraziSettingsPanel

    override fun getSettings(): GraziConfig = ServiceManager.getService(GraziConfig::class.java)

    override fun createUi(): GraziSettingsPanel = GraziSettingsPanel().also { ui = it }

    override fun enableSearch(option: String?) = ui.showOption(option)
}
