package tanvd.grazi.spellcheck

import org.languagetool.*
import tanvd.grazi.GraziConfig
import tanvd.grazi.grammar.GrammarCache
import tanvd.grazi.grammar.Typo
import tanvd.grazi.language.Lang
import tanvd.grazi.utils.*
import java.util.concurrent.TimeUnit


object SpellChecker {
    private const val cacheMaxSize = 30_000L
    private const val cacheExpireAfterMinutes = 5
    private val checkerLang = Lang.ENGLISH

    private val whiteSpaceSeparators = listOf(' ', '\t')
    private val nameSeparators = listOf('.', '_')

    private val ignorePatters: List<(String) -> Boolean> = listOf({ it -> it.startsWith(".") }, { it -> it.isUrl() })

    private fun createChecker(): JLanguageTool {
        val cache = ResultCache(cacheMaxSize, cacheExpireAfterMinutes, TimeUnit.MINUTES)
        val userConfig = UserConfig(SpellDictionary.usersCustom().words)
        return JLanguageTool(checkerLang.toLanguage(), GraziConfig.state.motherTongue.toLanguage(), cache, userConfig).apply {
            disableRules(allActiveRules.filter { !it.isDictionaryBasedSpellingRule }.map { it.id })
        }
    }

    private var checker: JLanguageTool = createChecker()

    fun check(bigWord: String) = buildSet<Typo> {
        var word = ""
        var cumulativeIndex = 0
        for (char in bigWord) {
            if (char in whiteSpaceSeparators) {
                if (word.isNotEmpty()) {
                    addAll(checkSplitting(word, cumulativeIndex))
                }
                cumulativeIndex++
                continue
            }
            word += char
        }
        if (word.isNotEmpty()) {
            addAll(checkSplitting(word, cumulativeIndex))
        }
    }

    private fun checkSplitting(bigWord: String, offset: Int) = buildSet<Typo> {
        if (ignorePatters.any { it(bigWord) }) {
            return@buildSet
        }

        var word = ""
        var cumulativeIndex = offset
        for (char in bigWord) {
            if (char in nameSeparators) {
                if (word.isNotEmpty()) {
                    addAll(checkOnePiece(word, cumulativeIndex))
                }
                cumulativeIndex++
                continue
            }
            word += char
        }
        if (word.isNotEmpty()) {
            addAll(checkOnePiece(word, cumulativeIndex))
        }
    }

    private fun checkOnePiece(onePieceWord: String, offset: Int) = buildSet<Typo> {
        var cumulativeIndex = 0
        for (word in onePieceWord.splitCamelCase()) {
            val match = checker.check(word).firstOrNull()
            match?.let { add(Typo(it, checkerLang, GrammarCache.hash(word), offset + cumulativeIndex)) }
            cumulativeIndex += word.length
        }
    }


    fun reset() {
        checker = createChecker()
    }
}
