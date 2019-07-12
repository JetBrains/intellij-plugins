package tanvd.grazi.ide.ui

import com.intellij.ide.plugins.newui.VerticalLayout
import com.intellij.openapi.options.ConfigurableUi
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.CheckBoxList
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBScrollPane
import tanvd.grazi.GraziConfig
import tanvd.grazi.language.Lang
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.BoxLayout
import javax.swing.JComponent


class GraziSettingsPanel : ConfigurableUi<GraziConfig> {
    private val cbEnableGraziSpellcheck = JBCheckBox(msg("grazi.ui.settings.enable.text"))
    private val cmbNativeLanguage = ComboBox<Lang>()
    private val cblEnabledLanguages = CheckBoxList<String>()

    override fun isModified(settings: GraziConfig): Boolean {
        return !Lang.values().all {
            settings.state.enabledLanguages.contains(it) == cblEnabledLanguages.isItemSelected(it.name)
        }
                .and(settings.state.nativeLanguage == cmbNativeLanguage.selectedItem)
                .and(settings.state.enabledSpellcheck == cbEnableGraziSpellcheck.isSelected)
    }

    override fun apply(settings: GraziConfig) {
        val state = settings.state.copy()

        Lang.values().forEach {
            if (cblEnabledLanguages.isItemSelected(it.name)) {
                state.enabledLanguages.add(it)
            } else {
                state.enabledLanguages.remove(it)
            }
        }

        state.nativeLanguage = cmbNativeLanguage.selectedItem as Lang
        state.enabledSpellcheck = cbEnableGraziSpellcheck.isSelected

        GraziConfig.instance.loadState(state)
    }

    override fun reset(settings: GraziConfig) {
        Lang.values().forEach {
            cblEnabledLanguages.setItemSelected(it.name, settings.state.enabledLanguages.contains(it))
        }

        cmbNativeLanguage.selectedItem = settings.state.nativeLanguage
        cbEnableGraziSpellcheck.isSelected = settings.state.enabledSpellcheck
    }

    override fun getComponent(): JComponent {
        Lang.sortedValues.forEach {
            cmbNativeLanguage.addItem(it)
            cblEnabledLanguages.addItem(it.name, it.displayName, false)
        }

        return panel {
            tabs {
                tab(msg("grazi.ui.settings.config.text")) {
                    panel(VerticalLayout(0)) {
                        panel(BorderLayout(0, 0), VerticalLayout.FILL_HORIZONTAL) {
                            layout = BoxLayout(this, BoxLayout.PAGE_AXIS)
                            border = border(msg("grazi.ui.settings.spellcheck.text"))

                            add(cbEnableGraziSpellcheck)

                            label(msg("grazi.ui.settings.enable.note")) {
                                font = font.deriveFont(Font.ITALIC)
                            }
                        }
                    }
                }

                tab(msg("grazi.ui.settings.languages.text")) {
                    panel {
                        panel(BorderLayout(0, 0), BorderLayout.PAGE_START) {
                            border = border(msg("grazi.ui.settings.languages.native.text"))
                            add(cmbNativeLanguage)
                        }

                        panel(BorderLayout(0, 0), BorderLayout.CENTER) {
                            border = border(msg("grazi.ui.settings.languages.text"))
                            add(JBScrollPane(cblEnabledLanguages))
                        }
                    }
                }
            }
        }
    }
}
