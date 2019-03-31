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

    private fun isSmall(str: String) = str.length < minChars
    private fun isBig(str: String) = str.length > maxChars

    fun getFixes(str: String, seps: List<String> = separators): List<Typo> {
        val curSeparator = seps.first()

        val result = LinkedHashSet<Typo>()
        var cumulativeLen = 0
        for (s in str.split(curSeparator)) {
            val stringFixes: List<Typo> = if (isBig(s) && seps.isNotEmpty()) {
                getFixes(s, seps.dropFirst())
            } else {
                getFixesSmall(s)
            }.map {
                Typo(it.range.withOffset(cumulativeLen), it.hash, it.description, it.category, it.fix)
            }
            result.addAll(stringFixes)
            cumulativeLen += s.length + curSeparator.length
        }
        return result.toList()
    }

    private fun getFixesSmall(str: String): LinkedHashSet<Typo> {
        if (isSmall(str)) return LinkedHashSet()

        if (GrammarCache.contains(str)) return GrammarCache.get(str)

        ProgressManager.checkCanceled()

        val allFixes = tryRun { GrammarChecker[LangDetector.getLang(str, GraziConfig.state.enabledLanguages.toList())].check(str) }
                .orEmpty()
                .filterNotNull()
                .map { Typo(it, GrammarCache.hash(str)) }
                .let { LinkedHashSet(it) }

        val withoutTypos = allFixes.filter { it.category != Typo.Category.TYPOS }
        val verifiedTypos = allFixes.filter { it.category == Typo.Category.TYPOS }.filter { SpellChecker.check(str.subSequence(it.range).toString()).isNotEmpty() }


        val result = LinkedHashSet(withoutTypos + verifiedTypos)

        GrammarCache.put(str, result)

        return result
    }
}
