package tanvd.grazi.grammar

import com.intellij.openapi.progress.ProgressManager
import tanvd.grazi.GraziConfig
import tanvd.grazi.language.LangDetector
import tanvd.grazi.language.LangTool
import tanvd.grazi.utils.splitWithRanges
import tanvd.kex.*

object GrammarEngine {
    //TODO In case of very big texts we may analyze only visible by user text, not all. Then we will not need such limitations
    private const val tooBigChars = 50_000
    private const val maxChars = 10_000
    private const val minChars = 2

    private val separators = listOf('?', '!', '.', ';', ',', ' ', '\t')

    /** Grammar checker will perform only spellcheck for sentences with fewer words */
    private const val minNumberOfWords = 3

    private fun isSmall(str: String) = str.length < minChars
    private fun isBig(str: String) = str.length > maxChars
    private fun isTooBig(str: String) = str.length > tooBigChars

    fun getTypos(str: String, seps: List<Char> = separators.filter { it in str }): Set<Typo> = buildSet {
        if (str.isBlank() || isTooBig(str)) return@buildSet

        if (str.split(Regex("\\s+")).size < minNumberOfWords) {
            return@buildSet
        }

        if (!isBig(str)) {
            addAll(getTyposSmall(str).map {
                Typo(it.location, it.info, it.fixes)
            })
        } else {
            val head = seps.first()
            val tail = seps.drop(1)

            str.splitWithRanges(head) { range, sentence ->
                addAll(getTypos(sentence, tail).map {
                    Typo(it.location.withOffset(range.start), it.info, it.fixes)
                })

                ProgressManager.checkCanceled()
            }
        }
    }

    private fun getTyposSmall(str: String): LinkedSet<Typo> {
        if (isSmall(str)) return LinkedSet()

        val lang = LangDetector.getLang(str, GraziConfig.get().enabledLanguages.toList()) ?: return LinkedSet()

        return tryRun { LangTool[lang].check(str) }
                .orEmpty()
                .filterNotNull()
                .map { Typo(it, lang) }
                .let { LinkedHashSet(it) }
    }
}
