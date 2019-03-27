package tanvd.grazi.grammar

import com.intellij.openapi.components.ServiceManager
import org.languagetool.rules.RuleMatch
import tanvd.grazi.model.Typo

class GrammarEngineService {
    companion object {
        fun getInstance(): GrammarEngineService {
            return ServiceManager.getService(GrammarEngineService::class.java)
        }
    }

    private val grammarCache = GrammarCache()
    private val languages = Languages()
    private val separators = listOf("\n\n", "\n", ".", " ")
    var enabledLangs = arrayListOf("en")
    private val newChecksPerTime = 10000

    val disabledRules = arrayListOf(RuleMatch.Type.UnknownWord)
    val disabledCategories = arrayListOf(Typo.Category.TYPOGRAPHY)

    private fun isSmall(str: String) = str.length < 2
    private fun isBig(str: String) = str.length > 1000

    fun getFixes(str: String): List<Typo> = getFixes(str, 0, 0).first

    private fun getFixes(str: String, sepInd: Int = 0, numChecksDone: Int = 0): Pair<List<Typo>, Int> {
        var checksDone = numChecksDone
        val result: MutableList<Typo> = ArrayList()
        var cumLen = 0
        for (s in str.split(separators[sepInd])) {
            val stringFixes: List<Typo> = if (isBig(s) && sepInd + 1 < separators.size) {
                val (list, checks) = getFixes(s, sepInd + 1, checksDone)
                checksDone = checks
                list
            } else {
                val (list, checks) = getFixesSmall(s, checksDone)
                checksDone = checks
                list
            }.map {
                Typo(it.range.withOffset(cumLen), it.description, it.category, it.fix)
            }
            result.addAll(stringFixes)
            cumLen += s.length
        }
        return result to checksDone
    }

    private fun getFixesSmall(str: String, numChecksDone: Int = 0): Pair<List<Typo>, Int> {
        var checksDone = numChecksDone
        if (isSmall(str) || isBig(str) || checksDone > newChecksPerTime) return emptyList<Typo>() to checksDone

        if (grammarCache.contains(str)) return grammarCache.get(str) to checksDone

//        ProgressManager.checkCanceled()

        val fixes = languages.getLangChecker(str, enabledLangs).check(str).filterNotNull()
                .filter { it.type !in disabledRules && it.typoCategory !in disabledCategories }
                .map { Typo(it.toIntRange(), it.shortMessage, it.typoCategory, it.suggestedReplacements) }
        checksDone++

        grammarCache.set(str, fixes)

        return fixes to checksDone
    }
}
