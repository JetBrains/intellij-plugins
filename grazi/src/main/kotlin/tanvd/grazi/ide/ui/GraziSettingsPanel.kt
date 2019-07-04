package tanvd.grazi.ide.ui

import com.intellij.ide.plugins.newui.VerticalLayout
import com.intellij.openapi.options.ConfigurableUi
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.CheckBoxList
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTabbedPane
import tanvd.grazi.GraziBundle
import tanvd.grazi.GraziConfig
import tanvd.grazi.GraziPlugin
import tanvd.grazi.language.Lang
import java.awt.*
import javax.swing.*


class GraziSettingsPanel : ConfigurableUi<GraziConfig> {

    private val panelEmptyBorder = BorderFactory.createEmptyBorder(3, 5, 5, 5)

    private val cbEnableGraziSpellcheck = JBCheckBox(GraziBundle.message("grazi.ui.settings.enable.text"))
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
        Lang.values().forEach {
            if (cblEnabledLanguages.isItemSelected(it.name)) {
                settings.state.enabledLanguages.add(it)
            } else {
                settings.state.enabledLanguages.remove(it)
            }
        }

        settings.state.nativeLanguage = cmbNativeLanguage.selectedItem as Lang
        settings.state.enabledSpellcheck = cbEnableGraziSpellcheck.isSelected
        GraziPlugin.reinit()
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

        return JPanel(BorderLayout(0, 0)).apply {
            add(JBTabbedPane().apply {
                addTab(GraziBundle.message("grazi.ui.settings.config.text"), JPanel(VerticalLayout(0))
                        .apply {
                            val spellcheckPanel = JPanel()
                            add(spellcheckPanel.apply {
                                layout = BoxLayout(spellcheckPanel, BoxLayout.PAGE_AXIS)
                                border = BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(
                                        GraziBundle.message("grazi.ui.settings.spellcheck.text")), panelEmptyBorder)
                                add(cbEnableGraziSpellcheck)
                                add(JLabel(GraziBundle.message("grazi.ui.settings.enable.note")).apply {
                                    font = font.deriveFont(Font.ITALIC)
                                })
                            }, VerticalLayout.FILL_HORIZONTAL)
                        })

                addTab(GraziBundle.message("grazi.ui.settings.languages.text"), JPanel(BorderLayout(0, 0))
                        .apply {
                            add(JPanel(BorderLayout(0, 0)).apply {
                                border = BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(
                                        GraziBundle.message("grazi.ui.settings.languages.native.text")), panelEmptyBorder)

                                add(cmbNativeLanguage)
                            }, BorderLayout.PAGE_START)

                            add(JPanel(BorderLayout(0, 0)).apply {
                                border = BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(
                                        GraziBundle.message("grazi.ui.settings.languages.text")), panelEmptyBorder)

                                add(JBScrollPane(cblEnabledLanguages))
                            }, BorderLayout.CENTER)
                        })
            })
        }
    }
}
