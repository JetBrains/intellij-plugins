package tanvd.grazi.grammar

import com.intellij.psi.PsiElement
import tanvd.grazi.utils.*

class SanitizingGrammarChecker(private val ignore: List<(CharSequence, Char) -> Boolean>,
                               private val replace: List<(CharSequence, Char) -> Char?>,
                               private val ignoreToken: List<(String) -> Boolean>,
                               private val trim: (String) -> Pair<IntRange?, String>) {

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
                }),
                trim = { str -> str.trimWithRange(emptyList()) })
    }

    fun <T : PsiElement> check(vararg tokens: T, getText: (T) -> String = { it.text }) = check(tokens.toList(), getText)

    fun <T : PsiElement> check(tokens: Collection<T>, getText: (T) -> String = { it.text }): Set<Typo> {
        if (tokens.isEmpty()) return emptySet()

        val indexesShift = HashMap<Int, Int>()
        val tokenMapping = HashMap<IntRange, T>()

        val resultText = buildString {
            var index = 0
            //iterate through non-ignored tokens
            for (token in tokens.filter { token -> !ignoreToken.any { it(getText(token)) } }) {
                val tokenStartIndex = index

                //trim text
                val text = getText(token)
                val (range, trimmedText) = trim(text)
                if (range == null) continue

                var totalExcluded = range.start
                indexesShift[index] = totalExcluded
                for (char in trimmedText) {
                    //perform replacing of chan (depending on already seen string)
                    @Suppress("NAME_SHADOWING")
                    val char = replace.firstNotNull { it(this, char) } ?: char

                    //check if char should be ignored
                    if (ignore.any { it(this, char) }) {
                        indexesShift[index] = ++totalExcluded
                        continue
                    }

                    append(char)

                    index++
                }
                if (tokenStartIndex < index) {
                    tokenMapping[IntRange(tokenStartIndex, index - 1)] = token
                    continue
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
