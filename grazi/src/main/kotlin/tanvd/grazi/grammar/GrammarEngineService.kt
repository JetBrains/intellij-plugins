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
    private val separators = listOf("\n", ".", " ")
    var enabledLangs = arrayListOf("en")
        set(value) {
            field = value
            grammarCache.reset()
        }

    val disabledRules = arrayListOf(RuleMatch.Type.UnknownWord)
    val disabledCategories = arrayListOf(Typo.Category.TYPOGRAPHY)

    private fun isSmall(str: String) = str.length < minChars
    private fun isBig(str: String) = str.length > maxChars

    fun getFixes(str: String) = getFixes(str, 0)

    private fun getFixes(str: String, sepInd: Int = 0): List<Typo> {
        val result: MutableList<Typo> = ArrayList()
        var cumLen = 0
        for (s in str.split(separators[sepInd])) {
            val stringFixes: List<Typo> = if (isBig(s) && sepInd + 1 < separators.size) {
                getFixes(s, sepInd + 1)
            } else {
                getFixesSmall(s)
            }.map {
                Typo(it.range.withOffset(cumLen), it.description, it.category, it.fix)
            }
            result.addAll(stringFixes)
            cumLen += s.length + separators[sepInd].length
        }
        return result
    }

    private fun getFixesSmall(str: String): List<Typo> {
        if (isSmall(str) || isBig(str)) return emptyList()

        if (grammarCache.contains(str)) return grammarCache.get(str)

        ProgressManager.checkCanceled()

        val fixes = tryRun { languages.getLangChecker(str, enabledLangs).check(str) }.orEmpty()
                .filterNotNull()
                .filter { it.type !in disabledRules && it.typoCategory !in disabledCategories }
                .map { Typo(it) }

        grammarCache.set(str, fixes)

        return fixes
    }
}
