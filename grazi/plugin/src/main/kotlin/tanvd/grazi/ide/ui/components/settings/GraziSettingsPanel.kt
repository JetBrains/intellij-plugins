package tanvd.grazi.ide.ui.components.settings

import com.intellij.icons.AllIcons
import com.intellij.openapi.options.ConfigurableUi
import com.intellij.openapi.project.guessCurrentProject
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.*
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.ui.components.panels.HorizontalLayout
import com.intellij.ui.layout.migLayout.createLayoutConstraints
import com.intellij.util.ui.JBUI
import net.miginfocom.layout.AC
import net.miginfocom.layout.CC
import net.miginfocom.swing.MigLayout
import org.jdesktop.swingx.VerticalLayout
import org.picocontainer.Disposable
import tanvd.grazi.GraziConfig
import tanvd.grazi.ide.ui.components.langlist.GraziAddDeleteListPanel
import tanvd.grazi.ide.ui.components.dsl.*
import tanvd.grazi.ide.ui.components.rules.GraziRulesTree
import tanvd.grazi.language.Lang
import tanvd.grazi.remote.GraziRemote
import java.awt.BorderLayout
import javax.swing.*

class GraziSettingsPanel : ConfigurableUi<GraziConfig>, Disposable {
    private val cbEnableGraziSpellcheck = JBCheckBox(msg("grazi.ui.settings.enable.text"))

    private val nativeLangLink: LinkLabel<Any?> = LinkLabel<Any?>("", AllIcons.General.Warning).apply {
        setListener({ _, _ ->
            GraziRemote.resolve((cmbNativeLanguage.selectedItem as Lang), guessCurrentProject(cmbNativeLanguage))
            updateWarnings()
            rulesTree.reset()
        }, null)
    }

    private val cmbNativeLanguage = ComboBox(Lang.sortedValues().toTypedArray()).apply {
        addItemListener { e ->
            val lang = (e.item as Lang)

            if (lang.jLanguage == null) {
                nativeLangLink.text = "Download ${lang.displayName}"
                nativeLangLink.isVisible = true
            } else {
                nativeLangLink.isVisible = false
            }
        }
    }

    private val langLink: LinkLabel<Any?> = LinkLabel<Any?>(msg("grazi.languages.action"), AllIcons.General.Warning).apply {
        border = padding(JBUI.insetsTop(10))
        setListener({ _, _ ->
            GraziRemote.resolveMissing(guessCurrentProject(descriptionPane))
            updateWarnings()
            rulesTree.reset()
        }, null)
    }
    private val ruleLink = LinkLabel<Any?>(msg("grazi.ui.settings.rules.rule.description"), null)
    private val linkPanel = panel(HorizontalLayout(0)) {
        border = padding(JBUI.insetsBottom(7))
        isVisible = false
        name = "GRAZI_LINK_PANEL"

        add(ruleLink)
        add(JLabel(AllIcons.Ide.External_link_arrow))
    }

    private val descriptionPane = pane()

    private val rulesTree by lazy {
        GraziRulesTree {
            linkPanel.isVisible = getLinkLabelListener(it)?.let { listener ->
                ruleLink.setListener(listener, null)
                true
            } ?: false

            descriptionPane.text = getDescriptionPaneContent(it).also {
                descriptionPane.isVisible = it.isNotBlank()
            }
        }
    }

    private val adpEnabledLanguages by lazy {
        GraziAddDeleteListPanel({ rulesTree.addLang(it) }, { rulesTree.removeLang(it); updateWarnings() })
    }

    private fun updateWarnings() {
        langLink.isVisible = GraziConfig.get().hasMissedLanguages(withNative = false)
        nativeLangLink.isVisible = (cmbNativeLanguage.selectedItem as Lang).jLanguage == null
    }

    override fun isModified(settings: GraziConfig): Boolean {
        return !(settings.state.enabledLanguages == adpEnabledLanguages.listItems.toSet())
                .and(settings.state.nativeLanguage == cmbNativeLanguage.selectedItem)
                .and(settings.state.enabledSpellcheck == cbEnableGraziSpellcheck.isSelected)
                .and(!rulesTree.isModified)
    }

    override fun apply(settings: GraziConfig) {
        GraziConfig.update { state ->
            val enabledLanguages = state.enabledLanguages.toMutableSet()
            val userDisabledRules = state.userDisabledRules.toMutableSet()
            val userEnabledRules = state.userEnabledRules.toMutableSet()

            val chosenEnabledLanguages = adpEnabledLanguages.listItems.toSet()
            Lang.values().forEach {
                if (chosenEnabledLanguages.contains(it)) {
                    enabledLanguages.add(it)
                } else {
                    enabledLanguages.remove(it)
                }
            }

            val (enabledRules, disabledRules) = rulesTree.state()

            enabledRules.forEach { id ->
                userDisabledRules.remove(id)
                userEnabledRules.add(id)
            }

            disabledRules.forEach { id ->
                userDisabledRules.add(id)
                userEnabledRules.remove(id)
            }

            state.copy(
                    enabledLanguages = enabledLanguages,
                    userEnabledRules = userEnabledRules,
                    userDisabledRules = userDisabledRules,
                    nativeLanguage = cmbNativeLanguage.selectedItem as Lang,
                    enabledSpellcheck = cbEnableGraziSpellcheck.isSelected
            )
        }

        updateWarnings()
        rulesTree.reset()
    }

    override fun reset(settings: GraziConfig) {
        cmbNativeLanguage.selectedItem = settings.state.nativeLanguage
        cbEnableGraziSpellcheck.isSelected = settings.state.enabledSpellcheck
        adpEnabledLanguages.reset(settings)
        rulesTree.reset()
        rulesTree.resetSelection()

        updateWarnings()
    }

    override fun getComponent(): JComponent {
        return panel(MigLayout(createLayoutConstraints(), AC().grow(), AC().index(1).grow())) {
            panel(MigLayout(createLayoutConstraints(), AC().grow()), constraint = CC().growX().wrap()) {
                border = border(msg("grazi.ui.settings.languages.text"), false, JBUI.insetsBottom(10), false)

                panel(BorderLayout(), CC().growX().maxHeight("").width("45%").minWidth("250px").minHeight("120px").maxHeight("120px").alignY("top")) {
                    add(adpEnabledLanguages, BorderLayout.CENTER)
                    add(langLink, BorderLayout.SOUTH)
                }

                panel(VerticalLayout(), CC().grow().width("55%").minWidth("250px").alignY("top")) {
                    border = padding(JBUI.insetsLeft(20))

                    panel(MigLayout(createLayoutConstraints(), AC()), constraint = "") {
                        border = padding(JBUI.insetsBottom(10))
                        add(wrapWithLabel(cmbNativeLanguage, msg("grazi.ui.settings.languages.native.text")), CC().minWidth("220px").maxWidth("380px"))
                        add(ContextHelpLabel.create(msg("grazi.ui.settings.languages.native.help")).apply { border = padding(JBUI.insetsLeft(5)) }, CC().width("30px").alignX("left").wrap())
                        add(nativeLangLink, CC().hideMode(3))
                    }

                    add(wrapWithComment(cbEnableGraziSpellcheck, msg("grazi.ui.settings.enable.note")))
                }

                updateWarnings()
            }

            panel(MigLayout(createLayoutConstraints(), AC().grow(), AC().grow()), constraint = CC().grow()) {
                border = border(msg("grazi.ui.settings.rules.configuration.text"), false, JBUI.emptyInsets())

                panel(constraint = CC().grow().width("45%").minWidth("250px")) {
                    add(rulesTree.panel)
                }

                panel(MigLayout(createLayoutConstraints().flowY().fillX()), constraint = CC().grow().width("55%")) {
                    border = padding(JBUI.insets(30, 20, 0, 0))
                    add(linkPanel, CC().grow().hideMode(3))

                    val descriptionPanel = JBPanelWithEmptyText(BorderLayout(0, 0)).withEmptyText(msg("grazi.ui.settings.rules.no-description")).also {
                        it.add(descriptionPane)
                    }
                    add(ScrollPaneFactory.createScrollPane(descriptionPanel, SideBorder.NONE).also { it.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER }, CC().grow().push())
                }
            }
        }
    }

    fun showOption(option: String?) = Runnable {
        rulesTree.filterTree(option)
        rulesTree.filter = option
    }

    override fun dispose() {
        rulesTree.dispose()
    }
}
