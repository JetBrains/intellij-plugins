package tanvd.grazi.grammar

import com.intellij.psi.PsiElement
import tanvd.grazi.utils.*

class SanitizingGrammarChecker(private val ignore: List<(CharSequence, Char) -> Boolean>,
                               private val replace: List<(CharSequence, Char) -> Char?>,
                               private val ignoreToken: List<(String) -> Boolean>) {

    companion object {
        val default = SanitizingGrammarChecker(
                ignore = listOf({ str, cur ->
                    str.lastOrNull()?.let { blankOrNewLineCharRegex.matches(it) }.orTrue() && blankOrNewLineCharRegex.matches(cur)
                }, { _, cur -> cur == '*' }),
                replace = listOf({ _, cur ->
                    newLineCharRegex.matches(cur).ifTrue { ' ' }
                }),
                ignoreToken = listOf({ str ->
                    str.all { !it.isLetter() }
                }))
    }

    fun <T : PsiElement> check(vararg tokens: T) = check(tokens.toList())

    fun <T : PsiElement> check(tokens: Collection<T>, getText: (T) -> String = { it.text }): Set<Typo> {
        if (tokens.isEmpty()) return emptySet()

        val indexesShift = HashMap<Int, Int>()
        val tokenMapping = HashMap<IntRange, T>()

        val resultText = buildString {
            var index = 0
            for (token in tokens.filter { token -> !ignoreToken.any { it(getText(token)) } }) {
                val tokenStartIndex = index
                var totalExcluded = 0
                indexesShift[index] = totalExcluded
                for (char in getText(token)) {
                    @Suppress("NAME_SHADOWING")
                    val char = replace.firstNotNull { it(this, char) } ?: char

                    if (ignore.any { it(this, char) }) {
                        indexesShift[index] = ++totalExcluded
                        continue
                    }

                    append(char)

                    index++
                }
                tokenMapping[IntRange(tokenStartIndex, index)] = token

                if (!this.lastOrNull()?.let { blankCharRegex.matches(it) }.orTrue()) {
                    append(' ')
                    index++
                }
            }
        }

        val fixes = GrammarEngine.getFixes(resultText, tokens.first().project)

        val sortedIndexesShift = indexesShift.toList().sortedBy { it.first }

        return fixes.mapNotNull { typo ->
            val (range, firstToken) = tokenMapping.filter { typo.location.range.start in it.key }.entries.first()
            val secondToken = tokenMapping.filter { typo.location.range.endInclusive in it.key }.values.firstOrNull()
            if (firstToken == secondToken) {
                val startShift = sortedIndexesShift.lastOrNull { it.first <= typo.location.range.start }?.second ?: 0
                val endShift = sortedIndexesShift.lastOrNull { it.first <= typo.location.range.endInclusive }?.second ?: 0
                val newRange = IntRange(typo.location.range.start + startShift - range.start, typo.location.range.endInclusive + endShift - range.start)
                typo.copy(location = typo.location.copy(range = newRange, element = firstToken))
            } else null
        }.toSet()
    }
}
