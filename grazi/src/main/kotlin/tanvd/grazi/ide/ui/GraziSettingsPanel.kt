package tanvd.grazi.ide.ui

import com.intellij.openapi.options.ConfigurableUi
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.panel.ComponentPanelBuilder
import com.intellij.ui.CheckBoxList
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import kotlinx.html.*
import org.apache.commons.text.similarity.LevenshteinDistance
import org.languagetool.rules.Category
import org.languagetool.rules.ExampleSentence
import org.languagetool.rules.IncorrectExample
import org.languagetool.rules.Rule
import org.picocontainer.Disposable
import tanvd.grazi.GraziConfig
import tanvd.grazi.language.Lang
import tanvd.grazi.language.LangTool
import tanvd.grazi.utils.html
import tanvd.grazi.utils.toCorrectHtml
import tanvd.grazi.utils.toIncorrectHtml
import java.awt.BorderLayout
import java.awt.Desktop
import java.awt.GridLayout
import javax.swing.BorderFactory
import javax.swing.BoxLayout
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
        isOpaque = false
        border = JBUI.Borders.empty(UIUtil.DEFAULT_VGAP, UIUtil.DEFAULT_HGAP)
    }.apply {
        addHyperlinkListener { event ->
            if (event?.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                Desktop.getDesktop()?.browse(event?.url?.toURI())
            }
        }
    }

    private val rulesTree = GraziRulesTree {
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

    override fun isModified(settings: GraziConfig): Boolean {
        return !Lang.values().all {
            settings.state.enabledLanguages.contains(it) == cblEnabledLanguages.isItemSelected(it.name)
        }
                .and(settings.state.nativeLanguage == cmbNativeLanguage.selectedItem)
                .and(settings.state.enabledSpellcheck == cbEnableGraziSpellcheck.isSelected)
                .and(!rulesTree.isModified())
    }

    override fun apply(settings: GraziConfig) {
        val enabledLanguages = settings.state.enabledLanguages.toMutableSet()

        Lang.values().forEach {
            if (cblEnabledLanguages.isItemSelected(it.name)) {
                enabledLanguages.add(it)
            } else {
                enabledLanguages.remove(it)
            }
        }

        var state = settings.state.copy(enabledLanguages = enabledLanguages, nativeLanguage = cmbNativeLanguage.selectedItem as Lang,
                enabledSpellcheck = cbEnableGraziSpellcheck.isSelected)

        with(rulesTree) {
            state = apply(state)
            reset()
        }

        GraziConfig.update(state)
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

        return panel {
            tabs {
                tab(msg("grazi.ui.settings.config.text")) {
                    panel(BorderLayout()) {
                        panel(BorderLayout(0, 0), BorderLayout.PAGE_START) {
                            layout = BoxLayout(this, BoxLayout.PAGE_AXIS)
                            border = BorderFactory.createEmptyBorder(0, 0, 10, 0)
                            //border = border(msg("grazi.ui.settings.spellcheck.text"), true)


                            add(cbEnableGraziSpellcheck)
                            add(ComponentPanelBuilder.createCommentComponent(msg("grazi.ui.settings.enable.note"), true))
                        }

                        panel(GridLayout(1, 2), BorderLayout.CENTER) {
                            border = border(msg("grazi.ui.settings.rules.configuration.text"), false, JBUI.emptyInsets())

                            add(rulesTree.panel)
                            add(panel {
                                add(ScrollPaneFactory.createScrollPane(descriptionPane))
                                border = border(msg("grazi.ui.settings.config.description"),
                                        false, JBUI.insets(6, 15, 0, 0))

                            })
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
                            border = border(msg("grazi.ui.settings.languages.text"), false, JBUI.emptyInsets())
                            add(JBScrollPane(cblEnabledLanguages))
                        }
                    }
                }
            }
        }
    }

    fun showOption(option: String?): Runnable {
        return Runnable {
            rulesTree.filter(option)
            rulesTree.setFilter(option)
        }
    }

    override fun dispose() {
        rulesTree.dispose()
    }
}
