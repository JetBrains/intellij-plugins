package tanvd.grazi.ide.ui

import com.intellij.openapi.components.BaseComponent

class GraziSettingsComponent : BaseComponent {
    override fun initComponent() {
        GraziApplicationSettings.instance.init()
    }
}
