package tanvd.grazi.utils

import kotlinx.html.FlowOrPhrasingContent
import kotlinx.html.strong
import org.languagetool.Language
import org.languagetool.Languages
import org.languagetool.rules.ExampleSentence
import org.languagetool.rules.IncorrectExample
import org.languagetool.rules.Rule
import org.languagetool.rules.RuleMatch
import tanvd.grazi.GraziPlugin
import tanvd.grazi.grammar.Typo
import tanvd.grazi.language.Lang
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.regex.Pattern

fun Iterable<Typo>.spellcheckOnly(): Set<Typo> = filter { it.isSpellingTypo }.toSet()
val Typo.isSpellingTypo: Boolean
    get() = info.rule.isDictionaryBasedSpellingRule

val RuleMatch.typoCategory: Typo.Category
    get() = Typo.Category[rule.category.id.toString()]

val ExampleSentence.text: CharSequence
    get() = example

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
                +mistake.trim()
            }
        }
    }
}

fun FlowOrPhrasingContent.toCorrectHtml(example: IncorrectExample) {
    toHtml(example) { mistake ->
        if (mistake.isNotEmpty() && example.corrections.isNotEmpty()) {
            strong {
                +example.corrections.first().trim()
            }
        }
    }
}

object LangToolInstrumentation {
    fun enableLatinLettersInSpellchecker(lang: Lang) {
        when (lang) {
            Lang.RUSSIAN ->
                Class.forName("org.languagetool.rules.ru.MorfologikRussianSpellerRule").getDeclaredField("RUSSIAN_LETTERS")
            Lang.UKRAINIAN ->
                Class.forName("org.languagetool.rules.uk.MorfologikUkrainianSpellerRule").getDeclaredField("UKRAINIAN_LETTERS")
            else -> null
        }?.let { pattern ->
            pattern.isAccessible = true

            val mods = Field::class.java.getDeclaredField("modifiers")
            mods.isAccessible = true
            mods.setInt(pattern, pattern.modifiers and Modifier.FINAL.inv())

            pattern.set(null, Pattern.compile(".*"))
        }
    }

    fun registerLanguage(lang: Lang) {
        val dynLanguages = Languages::class.java.getDeclaredField("dynLanguages")
        dynLanguages.isAccessible = true

        @Suppress("UNCHECKED_CAST")
        val langs = dynLanguages.get(null) as MutableList<Language>

        lang.descriptor.langsClasses.forEach { className ->
            val qualifiedName = "org.languagetool.language.$className"
            if (langs.all { it::class.java.canonicalName != qualifiedName }) {
                langs.add(GraziPlugin.loadClass(qualifiedName)!!.newInstance() as Language)
            }
        }
    }
}
