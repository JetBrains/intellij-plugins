package tanvd.grazi.grammar

import com.intellij.openapi.progress.ProgressManager
import tanvd.grazi.GraziConfig
import tanvd.grazi.language.LangDetector
import tanvd.grazi.spellcheck.SpellChecker
import tanvd.grazi.utils.*

object GrammarEngine {
    private const val maxChars = 1000
    private const val minChars = 2

    private val separators = listOf("\n", "?", "!", ".", " ")


    /** Grammar checker will perform only spellcheck for sentences with less words */
    private const val minNumberOfWords = 3

    private fun isSmall(str: String) = str.length < minChars
    private fun isBig(str: String) = str.length > maxChars

    fun getFixes(str: String, seps: List<String> = separators): List<Typo> = buildList {
        if (str.split(" ").size < minNumberOfWords) {
            if (str.isBlankWithNewLines().not()) {
                addAll(SpellChecker.check(str))
            }
            return@buildList
        }


        val curSeparator = seps.first()

        var cumulativeLen = 0
        for (s in str.split(curSeparator)) {
            val stringFixes: List<Typo> = if (isBig(s) && seps.isNotEmpty()) {
                getFixes(s, seps.dropFirst())
            } else {
                getFixesSmall(s)
            }.map {
                Typo(it.location.withOffset(cumulativeLen), it.info, it.fix)
            }
            addAll(stringFixes)
            cumulativeLen += s.length + curSeparator.length
        }
    }

    private fun getFixesSmall(str: String): LinkedHashSet<Typo> {
        if (isSmall(str)) return LinkedHashSet()

        if (GrammarCache.contains(str)) return GrammarCache.get(str)

        ProgressManager.checkCanceled()

        val lang = LangDetector.getLang(str, GraziConfig.state.enabledLanguages.toList())
        val allFixes = tryRun { GrammarChecker[lang].check(str) }
                .orEmpty()
                .filterNotNull()
                .map { Typo(it, lang, GrammarCache.hash(str)) }
                .let { LinkedHashSet(it) }

        val withoutTypos = allFixes.filterNot { it.info.rule.isDictionaryBasedSpellingRule }
        val verifiedTypos = allFixes.filter { it.info.rule.isDictionaryBasedSpellingRule }
                .filter {
                    str.subSequence(it.location.range).split(Regex("\\s"))
                            .flatMap { part -> SpellChecker.check(part) }.isNotEmpty()
                }


        val result = LinkedHashSet(withoutTypos + verifiedTypos)

        GrammarCache.put(str, result)

        return result
    }
}
