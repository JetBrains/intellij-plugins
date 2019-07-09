package tanvd.grazi.utils

import kotlinx.html.*
import org.languagetool.rules.*
import tanvd.grazi.grammar.Typo

fun Iterable<Typo>.spellcheckOnly(): Set<Typo> = filter { it.isSpellingTypo }.toSet()
val Typo.isSpellingTypo: Boolean
    get() = info.rule.isDictionaryBasedSpellingRule

val RuleMatch.typoCategory: Typo.Category
    get() = Typo.Category[rule.category.id.toString()]

fun Rule.toDescriptionSanitized() = this.description.replace("**", "")

private fun FlowOrPhrasingContent.toHtml(example: IncorrectExample, mistakeHandler: FlowOrPhrasingContent.(String) -> Unit) {
    Regex("(.*?)<marker>(.*?)</marker>|(.*)").findAll(example.example).forEach {
        val (prefix, mistake, suffix) = it.destructured

        +prefix
        mistakeHandler(mistake)
        +suffix
    }
}

fun FlowOrPhrasingContent.toIncorrectHtml(example: IncorrectExample) {
    toHtml(example) { mistake ->
        if (mistake.isNotEmpty()) {
            strong {
                +mistake
            }
        }
    }
}

fun FlowOrPhrasingContent.toCorrectHtml(example: IncorrectExample) {
    toHtml(example) { mistake ->
        if (mistake.isNotEmpty() && example.corrections.isNotEmpty()) {
            strong {
                +example.corrections.first()
            }
        }
    }
}
