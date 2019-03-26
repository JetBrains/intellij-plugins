package tanvd.grazi.grammar

import org.languagetool.rules.RuleMatch
import tanvd.grazi.model.Typo


object GrammarEngine {
    private val separators = listOf("\n\n", "\n", ".")


    val disabledRules = arrayListOf(RuleMatch.Type.UnknownWord)
    val disabledCategories = arrayListOf(Typo.Category.TYPOGRAPHY)

    private fun isSmall(str: String) = str.length < 2
    private fun isBig(str: String) = str.length > 1_000_000

    fun getFixes(str: String) = getFixes(str, 0)

    private fun getFixes(str: String, sepInd: Int = 0): List<Typo> {
        val result: MutableList<Typo> = ArrayList()
        var cumLen = 0
        for (s in str.split(separators[sepInd])) {
            val stringFixes: List<Typo> = if (isBig(s)) {
                getFixes(s, sepInd + 1)
            } else {
                getFixesSmall(s)
            }.map {
                Typo(it.range.withOffset(cumLen), it.description, it.category, it.fix)
            }
            result.addAll(stringFixes)
            cumLen += s.length
        }
        return result
    }

    private fun getFixesSmall(str: String): List<Typo> {
        if (isSmall(str)) return emptyList()

        if (GrammarCache.isValid(str)) return emptyList()

        val fixes = Languages.getLangChecker(str).check(str).filterNotNull()
                .filter { it.type !in disabledRules && it.typoCategory !in disabledCategories }
                .map { Typo(it.toIntRange(), it.shortMessage, it.typoCategory, it.suggestedReplacements) }

        if (fixes.isEmpty()) {
            GrammarCache.setValid(str)
        }

        return fixes
    }
}
