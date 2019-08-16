package tanvd.grazi.ide.ui.components.settings

import com.intellij.ide.BrowserUtil
import com.intellij.ui.components.labels.LinkListener
import kotlinx.html.*
import org.languagetool.rules.IncorrectExample
import org.languagetool.rules.Rule
import tanvd.grazi.ide.ui.components.dsl.msg
import tanvd.grazi.ide.ui.components.rules.ComparableCategory
import tanvd.grazi.ide.ui.components.rules.RuleWithLang
import tanvd.grazi.language.Lang
import tanvd.grazi.language.LangTool
import tanvd.grazi.utils.*
import tanvd.kex.orFalse

private const val MINIMUM_EXAMPLES_SIMILARITY = 0.2

fun CharSequence.isSimilarTo(sequence: CharSequence): Boolean {
    return Text.Levenshtein.distance(this, sequence).toDouble() / length < MINIMUM_EXAMPLES_SIMILARITY
}

fun GraziSettingsPanel.hasDescription(rule: Rule) = rule.url != null || rule.incorrectExamples?.isNotEmpty().orFalse() || LangTool.getRuleLanguages(rule.id)?.let { it.size > 1 }.orFalse()

fun GraziSettingsPanel.getLinkLabelListener(it: Any): LinkListener<Any?>? {
    return when (it) {
        is RuleWithLang -> it.rule.url?.let { LinkListener { _: Any?, _: Any? -> BrowserUtil.browse(it) } }
        else -> null
    }
}

fun GraziSettingsPanel.getDescriptionPaneContent(it: Any): String {
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
