package tanvd.grazi.model

import org.languagetool.JLanguageTool
import org.languagetool.Language
import org.languagetool.language.AmericanEnglish
import org.languagetool.language.LanguageIdentifier
import org.languagetool.rules.RuleMatch
import java.util.*
import kotlin.collections.ArrayList
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache


object GrammarEngine {
    private val langToolsByLang: MutableMap<Language, JLanguageTool> = HashMap()
    private val americanEnglish by lazy { AmericanEnglish() }
    private const val charsForLangDetection = 500
    private val separators = listOf("\n\n", "\n", ".")
    private val cache: LoadingCache<Int, Int> = Caffeine.newBuilder()
            .maximumSize(10000)
            .build { key -> key }

    val disabledRules = arrayListOf(RuleMatch.Type.UnknownWord)
    val disabledCategories = arrayListOf(Typo.Category.TYPOGRAPHY)
    val disabledLangs = ArrayList<String>()

    private fun wrongSize(str: String) = isSmall(str) || isBig(str)
    private fun isSmall(str: String) = str.length < 2
    private fun isBig(str: String) = str.length > 1000000

    private fun getFixesSmall(str: String): List<Typo> {
        if (isSmall(str)) return emptyList()
        val hash = str.hashCode()
        val g = cache.get(hash)
        val gp = cache.getIfPresent(hash)
        if (cache.getIfPresent(hash) != null) return emptyList()
        val lang = LanguageIdentifier(charsForLangDetection).detectLanguage(str, disabledLangs)?.detectedLanguage
                ?: americanEnglish

        if(lang !in langToolsByLang) {
            langToolsByLang[lang] = JLanguageTool(lang)
        }
        cache.put(hash, hash)
        return langToolsByLang.getOrPut(lang) { JLanguageTool(lang) }
                .check(str)
                .filterNotNull()
                .filter { it.type !in disabledRules && it.typoCategory !in disabledCategories }
                .map { Typo(it.toIntRange(), it.shortMessage, it.typoCategory, it.suggestedReplacements) }
    }

    fun getFixes(str: String) = getFixes(str, 0)

    private fun getFixes(str: String, sepInd: Int = 0): List<Typo> {
        val result: MutableList<Typo> = ArrayList()
        var cumLen = 0
        for (s in str.split(separators[sepInd])) {
            val stringFixes: List<Typo> = if (isBig(s)) {
                getFixes(s, sepInd + 1)
            } else {
                getFixesSmall(s)
            }.map { Typo(
                    IntRange(it.range.start + cumLen, it.range.endInclusive + cumLen),
                    it.description,
                    it.category,
                    it.fix) }
            result.addAll(stringFixes)
            cumLen += s.length
        }
        return result
    }
}
