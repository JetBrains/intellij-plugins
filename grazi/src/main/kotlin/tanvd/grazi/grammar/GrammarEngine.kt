package tanvd.grazi.grammar

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.progress.ProgressManager
import tanvd.grazi.language.*

class GrammarEngine {
    companion object {
        fun getInstance(): GrammarEngine {
            return ServiceManager.getService(GrammarEngine::class.java)
        }

        private const val maxChars = 1000
        private const val minChars = 2
    }

    private val separators = listOf("\n", "?", "!", ".", " ")

    var enabledLangs = listOf(Lang.ENGLISH)
        set(value) {
            field = value
            GrammarCache.reset()
            LangChecker.init(value)
        }

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
                Typo(it.range.withOffset(cumulativeLen), it.description, it.category, it.fix)
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

        val fixes = tryRun { LangChecker[LangDetector.getLang(str, enabledLangs)].check(str) }
                .orEmpty()
                .filterNotNull()
                .map { Typo(it) }.let { LinkedHashSet(it) }

        GrammarCache.put(str, fixes)

        return fixes
    }
}
