package tanvd.grazi.grammar

import com.intellij.psi.PsiElement
import tanvd.grazi.utils.*
import java.util.*
import kotlin.collections.HashMap

class SanitizingGrammarChecker(private val ignore: List<(CharSequence, Char) -> Boolean>,
                               private val replace: List<(CharSequence, Char) -> Char?>,
                               private val ignoreBlanks: Boolean = true) {

    companion object {
        val default = SanitizingGrammarChecker(listOf({ str, cur ->
            str.lastOrNull()?.let { blankCharRegex.matches(it) }.orTrue() && blankCharRegex.matches(cur)
        }, { _, cur -> cur == '*' }), listOf({ _, cur ->
            newLineCharRegex.matches(cur).ifTrue { ' ' }
        }))
    }

    fun <T : PsiElement> check(vararg tokens: T) = check(tokens.toList())

    fun <T : PsiElement> check(tokens: Collection<T>, getText: (T) -> String = { it.text }): Set<Typo> {
        val indexesShift = TreeMap<Int, Int> { ind, _ -> ind }
        val tokenMapping = HashMap<IntRange, T>()

        val resultText = buildString {
            var index = 0
            for (token in tokens.filter { !ignoreBlanks || !it.text.isBlankWithNewLines() }) {
                val tokenStartIndex = index
                var totalExcluded = 0
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

                if (this.lastOrNull()?.let { blankCharRegex.matches(it) }.orFalse()) {
                    append(' ')
                    index++
                }
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
