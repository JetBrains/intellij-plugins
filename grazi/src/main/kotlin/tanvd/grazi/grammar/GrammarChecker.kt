package tanvd.grazi.grammar

import com.intellij.psi.PsiElement
import tanvd.grazi.utils.*
import tanvd.kex.*

class GrammarChecker(private val ignore: List<(CharSequence, Char) -> Boolean>,
                     private val replace: List<(CharSequence, Char) -> Char?>,
                     private val ignoreToken: List<(String) -> Boolean>) {

    companion object {
        val default = GrammarChecker(
                ignore = listOf({ str, cur ->
                    str.lastOrNull()?.let { blankOrNewLineCharRegex.matches(it) }.orTrue() && blankOrNewLineCharRegex.matches(cur)
                }, { _, cur -> cur == '*' || cur == '`' }),
                replace = listOf({ _, cur ->
                    newLineCharRegex.matches(cur).ifTrue { ' ' }
                }),
                ignoreToken = listOf({ str ->
                    str.all { !it.isLetter() }
                }))
    }

    fun <T : PsiElement> check(vararg tokens: T, getText: (T) -> String = { it.text }) = check(tokens.toList(), getText)

    fun <T : PsiElement> check(tokens: Collection<T>, getText: (T) -> String = { it.text },
                               indexBasedIgnore: (T, Int) -> Boolean = { _, _ -> false }): Set<Typo> {
        if (tokens.isEmpty()) return emptySet()

        val indexesShift = HashMap<Int, Int>()
        val tokenMapping = HashMap<IntRange, T>()

        val resultText = buildString {
            var index = 0
            //iterate through non-ignored tokens
            for (token in tokens.filter { token -> !ignoreToken.any { it(getText(token)) } }) {
                val tokenStartIndex = index

                var totalExcluded = 0
                indexesShift[index] = totalExcluded
                for ((tokenIndex, char) in getText(token).withIndex()) {
                    //perform replacing of chan (depending on already seen string)
                    @Suppress("NAME_SHADOWING")
                    val char = replace.untilNotNull { it(this, char) } ?: char

                    //check if char should be ignored
                    if (ignore.any { it(this, char) } || indexBasedIgnore(token, tokenIndex)) {
                        indexesShift[index] = ++totalExcluded
                        continue
                    }

                    append(char)

                    index++
                }
                if (tokenStartIndex < index) {
                    tokenMapping[IntRange(tokenStartIndex, index - 1)] = token
                }

                if (!lastOrNull()?.let { blankCharRegex.matches(it) }.orTrue()) {
                    append(' ')
                    index++
                }
            }
        }

        val fixes = GrammarEngine.getFixes(resultText)

        val sortedIndexesShift = indexesShift.toList().sortedBy { it.first }

        return fixes.mapNotNull { typo ->
            tokenMapping.filter { typo.location.range.start in it.key }.entries.firstOrNull()?.let { (range, firstToken) ->
                val secondToken = tokenMapping.filter { typo.location.range.endInclusive in it.key }.values.firstOrNull()
                if (firstToken == secondToken) {
                    val startShift = sortedIndexesShift.lastOrNull { it.first <= typo.location.range.start }?.second ?: 0
                    val endShift = sortedIndexesShift.lastOrNull { it.first <= typo.location.range.endInclusive }?.second ?: 0
                    val newRange = IntRange(typo.location.range.start + startShift - range.start, typo.location.range.endInclusive + endShift - range.start)
                    typo.copy(location = typo.location.copy(range = newRange, pointer = firstToken.toPointer()))
                } else null
            }
        }.toSet()
    }
}
