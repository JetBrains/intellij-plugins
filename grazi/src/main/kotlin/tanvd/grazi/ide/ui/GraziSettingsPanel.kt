package tanvd.grazi.ide.ui

import com.intellij.openapi.options.ConfigurableUi
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.panel.ComponentPanelBuilder
import com.intellij.ui.AddDeleteListPanel
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.SideBorder
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.layout.migLayout.createLayoutConstraints
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import kotlinx.html.*
import net.miginfocom.layout.AC
import net.miginfocom.layout.CC
import net.miginfocom.swing.MigLayout
import org.apache.commons.text.similarity.LevenshteinDistance
import org.jdesktop.swingx.VerticalLayout
import org.languagetool.rules.Category
import org.languagetool.rules.ExampleSentence
import org.languagetool.rules.IncorrectExample
import org.languagetool.rules.Rule
import org.picocontainer.Disposable
import tanvd.grazi.GraziConfig
import tanvd.grazi.ide.ui.rules.GraziRulesTree
import tanvd.grazi.language.Lang
import tanvd.grazi.language.LangTool
import tanvd.grazi.utils.html
import tanvd.grazi.utils.toCorrectHtml
import tanvd.grazi.utils.toIncorrectHtml
import java.awt.Component
import java.awt.Desktop
import javax.swing.*
import javax.swing.event.HyperlinkEvent

private val ExampleSentence.text: CharSequence
    get() = example

class GraziSettingsPanel : ConfigurableUi<GraziConfig>, Disposable {
    companion object {
        private const val MINIMUM_EXAMPLES_SIMILARITY = 0.2
        private val levenshtein = LevenshteinDistance()

        private fun CharSequence.isSimilarTo(sequence: CharSequence): Boolean {
            return levenshtein.apply(this, sequence).toDouble() / length < MINIMUM_EXAMPLES_SIMILARITY
        }
    }

    private val cbEnableGraziSpellcheck = JBCheckBox(msg("grazi.ui.settings.enable.text"))
    private val cmbNativeLanguage = ComboBox<Lang>(Lang.sortedValues.toTypedArray())
    private val adpEnabledLanguages by lazy {
        object : AddDeleteListPanel<Lang>(null, GraziConfig.get().enabledLanguages.toList()) {
            private val cbLanguage = ComboBox<Lang>()

            init {
                emptyText.text = msg("grazi.ui.settings.language.empty.text")
            }

            override fun getListCellRenderer(): ListCellRenderer<*> {
                return object : DefaultListCellRenderer() {
                    override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
                        val component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JComponent
                        component.border = padding(JBUI.insets(5))
                        return component
                    }
                }
            }

            override fun findItemToAdd(): Lang? {
                val langsInList = listItems.toSet()
                cbLanguage.removeAllItems()
                Lang.sortedValues.forEach {
                    if (it !in langsInList) cbLanguage.addItem(it)
                }

                val dialog = DialogBuilder(this)
                        .title(msg("grazi.ui.settings.language.list.text"))
                        .centerPanel(wrap(cbLanguage, msg("grazi.ui.settings.language.choose.text"), msg("grazi.ui.settings.language.text")))

                return when (dialog.show()) {
                    DialogWrapper.OK_EXIT_CODE -> (cbLanguage.selectedItem as Lang)
                    else -> null
                }
            }

            fun reset(settings: GraziConfig) {
                val model = myList.model as DefaultListModel
                model.clear()
                settings.state.enabledLanguages.forEach {
                    model.addElement(it)
                }
            }
        }
    }

    private val descriptionPane = JEditorPane().apply {
        editorKit = UIUtil.getHTMLEditorKit()
        isEditable = false
        isOpaque = true
        border = null
        background = null
    }.apply {
        addHyperlinkListener { event ->
            if (event?.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                Desktop.getDesktop()?.browse(event?.url?.toURI())
            }
        }
    }

    private val rulesTree by lazy {
        GraziRulesTree {
            descriptionPane.text = when (it) {
                is Rule -> html {
                    table {
                        attributes["cellpadding"] = "0"
                        attributes["cellspacing"] = "0"

                        tr {
                            td {
                                colSpan = "2"
                                style = "padding-bottom: 10px;"
                                unsafe { +msg("grazi.ui.settings.rules.rule.template", it.description, it.category.name) }
                            }
                        }


                        it.url?.let {
                            tr {
                                td {
                                    colSpan = "2"
                                    style = "padding-bottom: 10px;"
                                    a(it.toString()) {
                                        unsafe { +msg("grazi.ui.settings.rules.rule.description") }
                                    }
                                }
                            }
                        }

                        LangTool.getRuleLanguages(it.id)?.let { languages ->
                            if (languages.size > 1) {
                                tr {
                                    td {
                                        colSpan = "2"
                                        style = "padding-bottom: 10px;"

                                        +msg("grazi.ui.settings.rules.rule.multilanguage.start")
                                        +" "
                                        strong {
                                            +languages.first().displayName
                                            languages.drop(1).forEach {
                                                +", ${it.displayName}"
                                            }
                                        }
                                        +" "
                                        +msg("grazi.ui.settings.rules.rule.multilanguage.end")
                                    }
                                }
                            }
                        }

                        it.incorrectExamples?.let { examples ->
                            if (examples.isNotEmpty()) {
                                tr {
                                    td {
                                        colSpan = "2"
                                        style = "padding-bottom: 5px;"
                                        +msg("grazi.ui.settings.rules.rule.examples")
                                    }
                                }

                                val accepted = ArrayList<IncorrectExample>()
                                // remove very similar examples
                                examples.forEach { example ->
                                    if (accepted.none { it.text.isSimilarTo(example.text) }) {
                                        accepted.add(example)
                                    }
                                }

                                accepted.forEach { example ->
                                    tr {
                                        td {
                                            attributes["valign"] = "top"
                                            style = "padding-bottom: 5px; padding-right: 5px; color: gray;"
                                            +msg("grazi.ui.settings.rules.rule.incorrect")
                                        }
                                        td {
                                            style = "padding-bottom: 5px; width: 100%;"
                                            toIncorrectHtml(example)
                                        }
                                    }

                                    if (example.corrections.any { it.isNotBlank() }) {
                                        tr {
                                            td {
                                                attributes["valign"] = "top"
                                                style = "padding-bottom: 10px; padding-right: 5px; color: gray;"
                                                +msg("grazi.ui.settings.rules.rule.correct")
                                            }
                                            td {
                                                style = "padding-bottom: 10px; width: 100%;"
                                                toCorrectHtml(example)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                is Lang -> html {
                    unsafe { +msg("grazi.ui.settings.rules.language.template", it.displayName) }
                }
                is Category -> html {
                    unsafe { +msg("grazi.ui.settings.rules.category.template", it.name) }
                }
                else -> ""
            }
        }
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
        rulesTree.reset()
    }

    override fun reset(settings: GraziConfig) {
        adpEnabledLanguages.reset(settings)
        cmbNativeLanguage.selectedItem = settings.state.nativeLanguage
        cbEnableGraziSpellcheck.isSelected = settings.state.enabledSpellcheck
        adpEnabledLanguages.reset(settings)
        rulesTree.reset()
    }

    override fun getComponent(): JComponent {
        return panel(MigLayout(createLayoutConstraints(), AC().grow(), AC().index(1).grow())) {
            panel(MigLayout(createLayoutConstraints(), AC().grow()), constraint = CC().growX().wrap()) {
                border = border(msg("grazi.ui.settings.languages.text"), false, JBUI.insetsBottom(10), false)

                add(adpEnabledLanguages, CC().growX().maxHeight("").width("45%").minWidth("250px").minHeight("120px").maxHeight("120px").alignY("top"))

                panel(VerticalLayout(), CC().grow().width("55%").minWidth("250px").alignY("top")) {
                    border = padding(JBUI.insetsLeft(20))

                    add(wrap(cmbNativeLanguage, msg("grazi.ui.settings.languages.native.tooltip"), msg("grazi.ui.settings.languages.native.text")))
                    add(wrap(cbEnableGraziSpellcheck, msg("grazi.ui.settings.enable.note")))
                }
            }

            panel(MigLayout(createLayoutConstraints(), AC().grow(), AC().grow()), constraint = CC().grow()) {
                border = border(msg("grazi.ui.settings.rules.configuration.text"), false, JBUI.emptyInsets())

                panel(constraint = CC().grow().width("45%").minWidth("250px")) {
                    add(rulesTree.panel)
                }

                panel(constraint = CC().grow().width("55%")) {
                    border = padding(JBUI.insets(30, 20, 0, 0))
                    add(ScrollPaneFactory.createScrollPane(descriptionPane, SideBorder.NONE))
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
