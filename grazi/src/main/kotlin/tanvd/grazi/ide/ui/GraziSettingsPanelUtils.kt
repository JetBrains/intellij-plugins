package tanvd.grazi.ide.ui

import com.intellij.ide.BrowserUtil
import com.intellij.ui.components.labels.LinkListener
import kotlinx.html.*
import org.apache.commons.text.similarity.LevenshteinDistance
import org.languagetool.rules.IncorrectExample
import org.languagetool.rules.Rule
import tanvd.grazi.ide.ui.rules.ComparableCategory
import tanvd.grazi.language.Lang
import tanvd.grazi.language.LangTool
import tanvd.grazi.utils.*

private const val MINIMUM_EXAMPLES_SIMILARITY = 0.2
private val levenshtein = LevenshteinDistance()

fun CharSequence.isSimilarTo(sequence: CharSequence): Boolean {
    return levenshtein.apply(this, sequence).toDouble() / length < MINIMUM_EXAMPLES_SIMILARITY
}

fun getSmallInfoPaneContent(it: Any): String {
    return when (it) {
        is Rule -> html {
            unsafe { +msg("grazi.ui.settings.rules.rule.template", it.description, it.category.name) }
        }
        is Lang -> html {
            unsafe { +msg("grazi.ui.settings.rules.language.template", it.displayName) }
        }
        is ComparableCategory -> html {
            unsafe { +msg("grazi.ui.settings.rules.category.template", it.name) }
        }
        else -> ""
    }
}

fun getLinkLabelListener(it: Any): LinkListener<Any?>? {
    return when (it) {
        is Rule -> it.url?.let { LinkListener { _: Any?, _: Any? -> BrowserUtil.browse(it) } }
        else -> null
    }
}

fun getDescriptionPaneContent(it: Any): String {
    return when (it) {
        is Rule -> html {
            table {
                cellpading = "0"
                cellspacing = "0"

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
        else -> ""
    }
}
