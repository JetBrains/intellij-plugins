package tanvd.grazi.grammar

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.progress.ProgressManager
import org.languagetool.rules.RuleMatch
import tanvd.grazi.model.Typo

class GrammarEngineService {
    companion object {
        fun getInstance(): GrammarEngineService {
            return ServiceManager.getService(GrammarEngineService::class.java)
        }

        private const val maxChars = 1000
        private const val minChars = 2
    }

    private val grammarCache = GrammarCache()
    private val languages = Languages()
    private val separators = listOf("\n", "?", "!", ".", " ")

    var enabledLangs = arrayListOf("en")
        set(value) {
            field = value
            grammarCache.reset()
            languages.initLangs(value)
        }
    private val disabledRules = arrayListOf(RuleMatch.Type.UnknownWord)
    private val disabledCategories = arrayListOf(Typo.Category.TYPOGRAPHY)

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

        if (grammarCache.contains(str)) return grammarCache.get(str)

        ProgressManager.checkCanceled()

        val fixes = tryRun { languages.getLangChecker(str, enabledLangs).check(str) }.orEmpty()
                .filterNotNull()
                .filter { it.type !in disabledRules && it.typoCategory !in disabledCategories }
                .map { Typo(it) }.let { LinkedHashSet(it) }

        grammarCache.put(str, fixes)

        return fixes
    }
}
