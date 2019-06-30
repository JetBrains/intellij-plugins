package tanvd.grazi.grammar

import tanvd.grazi.GraziConfig
import tanvd.grazi.language.LangDetector
import tanvd.grazi.language.LangTool
import tanvd.grazi.utils.isBlankWithNewLines
import tanvd.grazi.utils.splitWithRanges
import tanvd.kex.*

object GrammarEngine {
    private const val maxChars = 10_000
    private const val minChars = 2

    private val separators = listOf('\n', '?', '!', '.', ';', ',', ' ', '\t')

    /** Grammar checker will perform only spellcheck for sentences with fewer words */
    private const val minNumberOfWords = 3

    private fun isSmall(str: String) = str.length < minChars
    private fun isBig(str: String) = str.length > maxChars

    fun getFixes(str: String, seps: List<Char> = separators): Set<Typo> = buildSet {
        if (str.isBlankWithNewLines()) return@buildSet

        if (str.split(Regex("\\s+")).size < minNumberOfWords) {
            return@buildSet
        }

        val head = seps.first()
        val tail = seps.drop(1)

        for ((range, sentence) in str.splitWithRanges(head)) {
            val stringFixes = if (isBig(sentence) && tail.isNotEmpty()) {
                getFixes(sentence, tail)
            } else {
                getFixesSmall(sentence)
            }.map {
                Typo(it.location.withOffset(range.start), it.info, it.fixes)
            }
            addAll(stringFixes)
        }
    }

    private fun getFixesSmall(str: String): LinkedSet<Typo> {
        if (isSmall(str)) return LinkedSet()

        val lang = LangDetector.getLang(str, GraziConfig.state.enabledLanguages.toList()) ?: return LinkedSet()

        return tryRun { LangTool[lang].check(str) }
                .orEmpty()
                .filterNotNull()
                .map { Typo(it, lang) }
                .let { LinkedHashSet(it) }
    }
}
