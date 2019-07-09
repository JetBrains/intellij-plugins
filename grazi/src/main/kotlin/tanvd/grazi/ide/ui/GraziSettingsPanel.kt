package tanvd.grazi.ide.ui

import com.intellij.openapi.options.ConfigurableUi
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.CheckBoxList
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import org.apache.commons.text.similarity.LevenshteinDistance
import org.languagetool.rules.Category
import org.languagetool.rules.Rule
import org.picocontainer.Disposable
import tanvd.grazi.GraziConfig
import tanvd.grazi.ide.GraziLifecycle
import tanvd.grazi.language.Lang
import tanvd.grazi.utils.toCorrectHtml
import tanvd.grazi.utils.toIncorrectHtml
import java.awt.BorderLayout
import java.awt.Desktop
import java.awt.Font
import java.awt.GridLayout
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JEditorPane
import javax.swing.event.HyperlinkEvent


class GraziSettingsPanel : ConfigurableUi<GraziConfig>, Disposable {
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
            is Rule -> createHTML(false).html {
                body {
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

                    br

                    p {
                        it.incorrectExamples?.let { examples ->
                            if (examples.isNotEmpty()) {
                                i {
                                    +msg("grazi.ui.settings.rules.rule.examples")
                                }

                                table {
                                    val accepted = ArrayList<String>()
                                    examples.forEach {
                                        // remove very similar examples
                                        if (!accepted.any { example -> LevenshteinDistance().apply(it.example, example).toDouble() / it.example.length < 0.2 }) {
                                            accepted.add(it.example)
                                            val corrections = it.corrections.filter { it?.isNotBlank() ?: false }
                                            if (corrections.isEmpty()) {
                                                tr {
                                                    style = "padding-top: 5px;"
                                                    td {
                                                        style = "color: gray;"
                                                        +msg("grazi.ui.settings.rules.rule.incorrect")
                                                    }
                                                    td { unsafe { +it.toIncorrectHtml() } }
                                                }
                                            } else {
                                                tr {
                                                    td {
                                                        style = "color: gray;"
                                                        +msg("grazi.ui.settings.rules.rule.incorrect")
                                                    }
                                                    td {
                                                        style = "text-align: left; width:99.9%"
                                                        unsafe { +it.toIncorrectHtml() }
                                                    }
                                                }

                                                tr {
                                                    td {
                                                        style = "color: gray;"
                                                        +msg("grazi.ui.settings.rules.rule.correct")
                                                    }
                                                    td {
                                                        style = "text-align: left"
                                                        unsafe { +it.toCorrectHtml() }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is Lang -> createHTML(false).html {
                body {
                    unsafe { +msg("grazi.ui.settings.rules.language.template", it.displayName) }
                }
            }
            is Category -> createHTML(false).html {
                body {
                    unsafe { +msg("grazi.ui.settings.rules.category.template", it.name) }
                }
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
        Lang.values().forEach {
            if (cblEnabledLanguages.isItemSelected(it.name)) {
                settings.state.enabledLanguages.add(it)
            } else {
                settings.state.enabledLanguages.remove(it)
            }
        }

        settings.state.nativeLanguage = cmbNativeLanguage.selectedItem as Lang
        settings.state.enabledSpellcheck = cbEnableGraziSpellcheck.isSelected
        rulesTree.apply()
        GraziLifecycle.publisher.reInit()

        rulesTree.reset() // refresh languages
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

                            label(msg("grazi.ui.settings.enable.note")) {
                                font = font.deriveFont(Font.ITALIC)
                            }
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
