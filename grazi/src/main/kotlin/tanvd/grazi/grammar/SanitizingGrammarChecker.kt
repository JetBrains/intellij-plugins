package tanvd.grazi.grammar

import com.intellij.psi.PsiElement
import tanvd.grazi.ide.language.LanguageSupport
import java.util.*
import kotlin.collections.HashMap

class SanitizingGrammarChecker(private val ignoreIfPreviousEqual: List<Char>,
                               private val ignores: List<Char>,
                               private val replaces: Map<Char, Char>) {

    companion object {
        val default = SanitizingGrammarChecker(listOf(' '), listOf('\t', '*'), mapOf('\n' to ' '))
    }

    fun <T : PsiElement> check(vararg tokens: T) = check(tokens.toList())

    fun <T : PsiElement> check(tokens: List<T>, getText: (T) -> String = { it.text }): Set<LanguageSupport.Result> {
        var resultText = ""

        val indexesShift = TreeMap<Int, Int> { ind, _ -> ind }
        val tokenMapping = HashMap<IntRange, T>()

        var index = 0
        var previous: Char? = null
        for (token in tokens.filter { it.text.isNotBlank() }) {
            val tokenStartIndex = index
            var totalExcluded = 0
            for (char in getText(token)) {
                val newChar = if (char in replaces.keys) {
                    replaces[char]
                } else {
                    char
                }

                if (newChar in ignores) {
                    indexesShift[index] = ++totalExcluded
                    continue
                }

                if (newChar in ignoreIfPreviousEqual && previous == newChar) {
                    indexesShift[index] = ++totalExcluded
                    continue
                }

                resultText += newChar

                index++
                previous = newChar
            }
            tokenMapping[IntRange(tokenStartIndex, index)] = token

            if (previous != ' ') {
                resultText += " "
                previous = ' '
                index++
            }
        }

        val fixes = GrammarEngine.getFixes(resultText)

        return fixes.mapNotNull { typo ->
            val (range, firstToken) = tokenMapping.filter { typo.range.start in it.key }.entries.first()
            val secondToken = tokenMapping.filter { typo.range.endInclusive in it.key }.values.firstOrNull()
            if (firstToken == secondToken) {
                val startShift = indexesShift.filter { it.key <= typo.range.start }.values.lastOrNull() ?: 0
                val endShift = indexesShift.filter { it.key <= typo.range.endInclusive }.values.lastOrNull() ?: 0
                val newRange = IntRange(typo.range.start + startShift - range.start, typo.range.endInclusive + endShift - range.start)
                LanguageSupport.Result(Typo(newRange, typo.hash, typo.description, typo.category, typo.fix), firstToken)
            } else null
        }.toSet()
    }
}
