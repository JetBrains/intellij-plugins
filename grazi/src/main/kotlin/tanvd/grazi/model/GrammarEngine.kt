package tanvd.grazi.model

import org.languagetool.JLanguageTool
import org.languagetool.Language
import org.languagetool.language.AmericanEnglish
import org.languagetool.language.LanguageIdentifier
import org.languagetool.rules.RuleMatch
import java.util.*
import kotlin.collections.ArrayList

object GrammarEngine {
    private val langToolsByLang: MutableMap<Language, JLanguageTool> = HashMap()
    private val americanEnglish by lazy { AmericanEnglish() }

    val disabledRules = arrayListOf(RuleMatch.Type.UnknownWord)
    val disabledCategories = arrayListOf(Typo.Category.TYPOGRAPHY)
    val disabledLangs = ArrayList<String>()

    private const val charsForLangDetection = 500

    fun getFixes(str: String): List<Typo> {
        if (str.length < 2) {
            return emptyList()
        }

        val lang = LanguageIdentifier(charsForLangDetection).detectLanguage(str, disabledLangs)?.detectedLanguage ?: americanEnglish

        if (lang !in langToolsByLang) {
            langToolsByLang[lang] = JLanguageTool(lang)
        }
        return langToolsByLang.getOrPut(lang) { JLanguageTool(lang) }
                .check(str)
                .filterNotNull()
                .filter { it.type !in disabledRules && it.typoCategory !in disabledCategories }
                .map { Typo(it.toIntRange(), it.shortMessage, it.typoCategory, it.suggestedReplacements) }
    }
}
