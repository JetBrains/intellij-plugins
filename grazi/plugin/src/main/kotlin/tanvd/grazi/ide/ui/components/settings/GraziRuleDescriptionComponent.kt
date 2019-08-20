package tanvd.grazi.ide.ui.components.settings

import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.SideBorder
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.ui.components.labels.LinkListener
import com.intellij.ui.components.panels.HorizontalLayout
import com.intellij.ui.layout.migLayout.createLayoutConstraints
import com.intellij.util.ui.JBUI
import kotlinx.html.*
import net.miginfocom.layout.CC
import net.miginfocom.swing.MigLayout
import org.languagetool.rules.IncorrectExample
import org.languagetool.rules.Rule
import tanvd.grazi.ide.ui.components.dsl.msg
import tanvd.grazi.ide.ui.components.dsl.padding
import tanvd.grazi.ide.ui.components.dsl.pane
import tanvd.grazi.ide.ui.components.dsl.panel
import tanvd.grazi.ide.ui.components.rules.ComparableCategory
import tanvd.grazi.ide.ui.components.rules.RuleWithLang
import tanvd.grazi.language.Lang
import tanvd.grazi.language.LangTool
import tanvd.grazi.utils.*
import tanvd.kex.orFalse
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.ScrollPaneConstants

class GraziRuleDescriptionComponent {
    companion object {
        private const val MINIMUM_EXAMPLES_SIMILARITY = 0.2

        private fun CharSequence.isSimilarTo(sequence: CharSequence): Boolean {
            return Text.Levenshtein.distance(this, sequence).toDouble() / length < MINIMUM_EXAMPLES_SIMILARITY
        }
    }

    private class GraziRuleLinkComponent {
        private val link = LinkLabel<Any?>(msg("grazi.ui.settings.rules.rule.description"), null)
        var listener: LinkListener<Any?>
            @Deprecated("Property can only be written", level = DeprecationLevel.ERROR)
            get() = throw NotImplementedError()
            set(value) {
                link.setListener(value, null)
            }

        val component = panel(HorizontalLayout(0)) {
            border = padding(JBUI.insetsBottom(7))
            name = "GRAZI_LINK_PANEL"
            isVisible = false

            add(link)
            add(JLabel(AllIcons.Ide.External_link_arrow))
        }
    }

    private val link = GraziRuleLinkComponent()
    private val description = pane()

    val listener: (Any) -> Unit
        get() = { selection ->
            link.component.isVisible = getLinkLabelListener(selection)?.let { listener ->
                link.listener = listener
                true
            } ?: false

            description.text = getDescriptionPaneContent(selection).also {
                description.isVisible = it.isNotBlank()
            }
        }

    val component = panel(MigLayout(createLayoutConstraints().flowY().fillX())) {
        border = padding(JBUI.insets(30, 20, 0, 0))
        add(link.component, CC().grow().hideMode(3))

        val descriptionPanel = JBPanelWithEmptyText(BorderLayout(0, 0)).withEmptyText(msg("grazi.ui.settings.rules.no-description"))
        descriptionPanel.add(description)
        add(ScrollPaneFactory.createScrollPane(descriptionPanel, SideBorder.NONE).also {
            it.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        }, CC().grow().push())
    }


    private fun getLinkLabelListener(it: Any): LinkListener<Any?>? {
        return when (it) {
            is RuleWithLang -> it.rule.url?.let { LinkListener { _: Any?, _: Any? -> BrowserUtil.browse(it) } }
            else -> null
        }
    }

    private fun hasDescription(rule: Rule) = rule.url != null || rule.incorrectExamples?.isNotEmpty().orFalse() || LangTool.getRuleLanguages(rule.id)?.let { it.size > 1 }.orFalse()

    private fun getDescriptionPaneContent(it: Any): String {
        return when {
            it is Lang -> html {
                unsafe { +msg("grazi.ui.settings.rules.language.template", it.displayName) }
            }
            it is ComparableCategory -> html {
                unsafe { +msg("grazi.ui.settings.rules.category.template", it.name) }
            }
            it is RuleWithLang && hasDescription(it.rule) -> {
                html {
                    table {
                        cellpading = "0"
                        cellspacing = "0"
                        style = "width:100%;"

                        LangTool.getRuleLanguages(it.rule.id)?.let { languages ->
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

                        it.rule.incorrectExamples?.let { examples ->
                            if (examples.isNotEmpty()) {
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
                                            valign = "top"
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
                                                valign = "top"
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
            }
            else -> ""
        }
    }
}
