package tanvd.grazi.ide.ui

import com.intellij.openapi.options.ConfigurableUi
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.panel.ComponentPanelBuilder
import com.intellij.ui.*
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import kotlinx.html.*
import org.apache.commons.text.similarity.LevenshteinDistance
import org.jdesktop.swingx.HorizontalLayout
import org.jdesktop.swingx.VerticalLayout
import org.languagetool.rules.*
import org.picocontainer.Disposable
import tanvd.grazi.GraziConfig
import tanvd.grazi.ide.ui.rules.GraziRulesTree
import tanvd.grazi.language.Lang
import tanvd.grazi.language.LangTool
import tanvd.grazi.utils.*
import java.awt.Desktop
import javax.swing.JComponent
import javax.swing.JEditorPane
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
    private val cmbNativeLanguage = ComboBox<Lang>()
    private val cblEnabledLanguages = CheckBoxList<String>()

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
                    p {
                        unsafe { +msg("grazi.ui.settings.rules.rule.template", it.description, it.category.name) }
                    }

                    it.url?.let {
                        br
                        p {
                            +msg("grazi.ui.settings.rules.rule.description")
                            +" "
                            a(it.toString()) {
                                unsafe { +it.toString() }
                            }
                        }
                    }

                    LangTool.getRuleLanguages(it.id)?.let { languages ->
                        if (languages.size > 1) {
                            br
                            p {
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

                    br

                    p {
                        it.incorrectExamples?.let { examples ->
                            if (examples.isNotEmpty()) {
                                i {
                                    +msg("grazi.ui.settings.rules.rule.examples")
                                }

                                table {
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
                                                style = "color: gray;"
                                                +msg("grazi.ui.settings.rules.rule.incorrect")
                                            }
                                            td {
                                                style = "text-align: left;"
                                                toIncorrectHtml(example)
                                            }
                                        }

                                        if (example.corrections.any { it.isNotBlank() }) {
                                            tr {
                                                td {
                                                    style = "color: gray;"
                                                    +msg("grazi.ui.settings.rules.rule.correct")
                                                }
                                                td {
                                                    style = "text-align: left"
                                                    toCorrectHtml(example)
                                                }
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
        return !Lang.values().all {
            settings.state.enabledLanguages.contains(it) == cblEnabledLanguages.isItemSelected(it.name)
        }
                .and(settings.state.nativeLanguage == cmbNativeLanguage.selectedItem)
                .and(settings.state.enabledSpellcheck == cbEnableGraziSpellcheck.isSelected)
                .and(!rulesTree.isModified)
    }

    override fun apply(settings: GraziConfig) {
        GraziConfig.update { state ->
            val enabledLanguages = state.enabledLanguages.toMutableSet()
            val userDisabledRules = state.userDisabledRules.toMutableSet()
            val userEnabledRules = state.userEnabledRules.toMutableSet()

            Lang.values().forEach {
                if (cblEnabledLanguages.isItemSelected(it.name)) {
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
        Lang.values().forEach {
            cblEnabledLanguages.setItemSelected(it.name, settings.state.enabledLanguages.contains(it))
        }

        cmbNativeLanguage.selectedItem = settings.state.nativeLanguage
        cbEnableGraziSpellcheck.isSelected = settings.state.enabledSpellcheck
        rulesTree.reset()
    }

    override fun getComponent(): JComponent {
        Lang.sortedValues.forEach {
            cmbNativeLanguage.addItem(it)
            cblEnabledLanguages.addItem(it.name, it.displayName, false)
        }

        return panelGridBag {
            panel(grid(rows = 1, cols = 2), gridBagConstraint(gridx = 0, gridy = 0, weightx = 1.0, weighty = 0.25, gridheight = 1)) {
                border = border(msg("grazi.ui.settings.languages.text"), false, JBUI.insetsBottom(10))

                panel {
                    border = padding(JBUI.insetsRight(10))
                    add(JBScrollPane(cblEnabledLanguages))
                }

                panel(VerticalLayout()) {
                    border = padding(JBUI.insetsLeft(10))

                    panel(HorizontalLayout()) {
                        border = padding(JBUI.insetsBottom(10))
                        panel(VerticalLayout()) {
                            border = padding(JBUI.insetsTop(5))
                            label(msg("grazi.ui.settings.languages.native.text"))
                        }
                        panel(VerticalLayout()) {
                            border = padding(JBUI.insetsLeft(5))
                            add(cmbNativeLanguage)
                            comment(msg("grazi.ui.settings.languages.native.tooltip"))
                        }
                    }

                    panel(VerticalLayout()) {
                        border = padding(JBUI.insetsTop(10))

                        add(cbEnableGraziSpellcheck)
                        add(ComponentPanelBuilder.createCommentComponent(msg("grazi.ui.settings.enable.note"), true))
                    }
                }
            }

            panel(grid(rows = 1, cols = 2), gridBagConstraint(gridx = 0, gridy = 1, weightx = 1.0, weighty = 1.0, gridheight = 2)) {
                border = border(msg("grazi.ui.settings.rules.configuration.text"), false, JBUI.insetsTop(10))

                panel {
                    border = padding(JBUI.insetsRight(10))
                    add(rulesTree.panel)
                }
                panel {
                    border = padding(JBUI.insets(30, 10, 0, 0))
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
