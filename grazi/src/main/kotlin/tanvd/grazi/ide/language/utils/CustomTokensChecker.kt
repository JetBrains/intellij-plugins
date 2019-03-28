package tanvd.grazi.ide.language.utils

import com.intellij.psi.PsiElement
import tanvd.grazi.grammar.GrammarEngineService
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.model.Typo

class CustomTokensChecker<T : PsiElement>(private val ignoreIfPreviousEqual: List<Char>,
                                          private val ignores: List<Char>,
                                          private val replaces: Map<Char, Char>) {

    companion object {
        val default = CustomTokensChecker<PsiElement>(listOf(' '), listOf('\t', '*'), mapOf('\n' to ' '))
    }

    fun check(vararg tokens: T) = check(tokens.toList())

    fun check(tokens: List<T>): Set<LanguageSupport.Result> {
        var resultText = ""

        val indexMapping = HashMap<Int, Int>()
        val tokenMapping = HashMap<Int, T>()

        var index = 0
        var previous: Char? = null
        for (token in tokens) {
            for ((realIndex, char) in token.text.withIndex()) {
                val newChar = if (char in replaces.keys) {
                    replaces[char]
                } else {
                    char
                }

                if (newChar in ignores) {
                    continue
                }

                if (newChar in ignoreIfPreviousEqual && previous == newChar) {
                    continue
                }

                indexMapping[index] = realIndex
                tokenMapping[index] = token
                resultText += newChar

                index++
                previous = newChar
            }
            resultText += " "
            index++
        }

        val fixes = GrammarEngineService.getInstance().getFixes(resultText)

        return fixes.mapNotNull { typo ->
            val firstToken = tokenMapping[typo.range.start]!!
            val secondToken = tokenMapping[typo.range.endInclusive]!!
            if (firstToken == secondToken) {
                val newRange = IntRange(indexMapping[typo.range.start]!!, indexMapping[typo.range.endInclusive]!!)
                LanguageSupport.Result(Typo(newRange, typo.description, typo.category, typo.fix), firstToken)
            } else null
        }.toSet()
    }
}
