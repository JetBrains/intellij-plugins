package tanvd.grazi.grammar

import com.intellij.psi.PsiElement
import tanvd.grazi.utils.isBlankWithNewLines
import java.util.*
import kotlin.collections.HashMap

class SanitizingGrammarChecker(private val ignoreIfPreviousEqual: List<Char>,
                               private val ignores: List<Char>,
                               private val replaces: Map<Char, Char>,
                               private val shouldIgnoreBlanks: Boolean = true) {

    companion object {
        val default = SanitizingGrammarChecker(listOf(' '), listOf('\t', '*'), mapOf('\n' to ' '))
    }

    fun <T : PsiElement> check(vararg tokens: T) = check(tokens.toList())

    fun <T : PsiElement> check(tokens: Collection<T>, getText: (T) -> String = { it.text }): Set<Typo> {
        var resultText = ""

        val indexesShift = TreeMap<Int, Int> { ind, _ -> ind }
        val tokenMapping = HashMap<IntRange, T>()

        var index = 0
        var previous: Char? = null
        for (token in tokens.filter { !shouldIgnoreBlanks || it.text.isBlankWithNewLines().not() }) {
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
            val (range, firstToken) = tokenMapping.filter { typo.location.range.start in it.key }.entries.first()
            val secondToken = tokenMapping.filter { typo.location.range.endInclusive in it.key }.values.firstOrNull()
            if (firstToken == secondToken) {
                val startShift = indexesShift.filter { it.key <= typo.location.range.start }.values.lastOrNull() ?: 0
                val endShift = indexesShift.filter { it.key <= typo.location.range.endInclusive }.values.lastOrNull() ?: 0
                val newRange = IntRange(typo.location.range.start + startShift - range.start, typo.location.range.endInclusive + endShift - range.start)
                typo.copy(location = typo.location.copy(range = newRange, element = firstToken))
            } else null
        }.toSet()
    }
}
