package tanvd.grazi.grammar

import com.intellij.psi.PsiElement
import tanvd.grazi.utils.Text
import tanvd.grazi.utils.toPointer
import tanvd.kex.*

class GrammarChecker(private val charRules: CharRules = CharRules(), private val textRules: TextRules = TextRules()) {

    /** Rules working on a level of chars and previous part of string */
    data class CharRules(val ignore: LinkedSet<(CharSequence, Char) -> Boolean> = LinkedSet(),
                         val replace: LinkedSet<(CharSequence, Char) -> Char?> = LinkedSet()) {
        constructor(fst: CharRules, snd: CharRules) : this(LinkedSet(fst.ignore + snd.ignore), LinkedSet(fst.replace + snd.replace))
    }

    /** Rules working on a level of the whole text */
    data class TextRules(val ignoreFully: LinkedSet<(String) -> Boolean> = LinkedSet(),
                         val ignoreByIndex: LinkedSet<(String, Int) -> Boolean> = LinkedSet()) {
        constructor(fst: TextRules, snd: TextRules) : this(LinkedSet(fst.ignoreFully + snd.ignoreFully), LinkedSet(fst.ignoreByIndex + snd.ignoreByIndex))
    }

    data class TokenRules<T : PsiElement>(val ignoreFully: LinkedSet<(T) -> Boolean> = LinkedSet(),
                                          val ignoreByIndex: LinkedSet<(T, Int) -> Boolean> = LinkedSet()) {
        constructor(fst: TokenRules<T>, snd: TokenRules<T>) : this(LinkedSet(fst.ignoreFully + snd.ignoreFully), LinkedSet(fst.ignoreByIndex + snd.ignoreByIndex))
    }


    constructor(checker: GrammarChecker, charRules: CharRules = CharRules(), textRules: TextRules = TextRules())
            : this(CharRules(checker.charRules, charRules), TextRules(checker.textRules, textRules))

    companion object {
        object Rules {
            /** Deduplicate sequential blanks */
            val flatBlanks: (CharSequence, Char) -> Boolean = { prefix, cur ->
                prefix.lastOrNull()?.isWhitespace().orTrue() && cur.isWhitespace()
            }

            //TODO probably we need to flat them only if previous char is not end of sentence punctuation mark
            /** Replace all newlines with space characters */
            val flatNewlines: (CharSequence, Char) -> Char? = { _, cur ->
                Text.isNewline(cur).ifTrue { ' ' }
            }

            val ignoreFullyBlank: (String) -> Boolean = { it.isBlank() }

            val ignoreBorderQuotes: (String, Int) -> Boolean = { text, idx ->
                fun isSymmetricallyQuotedPart(idx: Int): Boolean {
                    val prev = text[idx]
                    val oppositePrev = text[text.length - idx - 1]
                    return prev == oppositePrev && Text.isQuote(prev)
                }

                val leftRange = (0 until idx)
                val rightRange = (idx + 1) until text.length

                //Trim quotes only if all previous were quotes that should be trimmed too
                //`all` is lazy and will return false once see not quote
                isSymmetricallyQuotedPart(idx) && (leftRange.all { isSymmetricallyQuotedPart(it) } || rightRange.all { isSymmetricallyQuotedPart(it) })
            }
        }

        val default = GrammarChecker(
                CharRules(linkedSetOf(Rules.flatBlanks), linkedSetOf(Rules.flatNewlines)),
                TextRules(ignoreFully = linkedSetOf(Rules.ignoreFullyBlank), ignoreByIndex = linkedSetOf(Rules.ignoreBorderQuotes))
        )
    }

    fun <T : PsiElement> check(vararg tokens: T, tokenRules: TokenRules<T> = TokenRules()): Set<Typo> = check(tokens.toList(), tokenRules)

    fun <T : PsiElement> check(tokens: Collection<T>, tokenRules: TokenRules<T> = TokenRules()): Set<Typo> {
        if (tokens.isEmpty()) return emptySet()

        val indicesShift = HashMap<Int, Int>()
        val tokenMapping = HashMap<IntRange, T>()

        val resultText = buildString {
            var index = 0
            //iterate through non-ignored tokens
            for ((token, text) in tokens.map { it to it.text }.filter { (token, text) -> !textRules.ignoreFully.any { it(text) } && !tokenRules.ignoreFully.any { it(token) } }) {
                val tokenStartIndex = index

                var excluded = 0
                indicesShift[index] = excluded
                for ((tokenIndex, char) in text.withIndex()) {
                    //perform replacing of char (depending on already seen string)
                    @Suppress("NAME_SHADOWING") val char = charRules.replace.untilNotNull { it(this, char) } ?: char

                    //check if char should be ignored
                    if (charRules.ignore.any { it(this, char) }
                            || textRules.ignoreByIndex.any { it(text, tokenIndex) }
                            || tokenRules.ignoreByIndex.any { it(token, tokenIndex) }) {
                        indicesShift[index] = ++excluded
                        continue
                    }

                    append(char)

                    index++
                }
                if (tokenStartIndex < index) {
                    tokenMapping[IntRange(tokenStartIndex, index - 1)] = token
                }

                if (!lastOrNull()?.isWhitespace().orTrue()) {
                    append(' ')
                    index++
                }
            }
        }

        val typos = GrammarEngine.getTypos(resultText)

        val sortedIndexesShift = indicesShift.toList().sortedBy { it.first }

        return typos.mapNotNull { typo ->
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
