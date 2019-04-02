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

    fun check(text: String) = buildSet<Typo> {
        for ((bigWordRange, bigWord) in text.splitWithRanges(whiteSpaceSeparators)) {
            if (ignorePatters.any { it(bigWord) }) continue

            if (SpellCheckerCache.contains(bigWord)) {
                addAll(SpellCheckerCache.get(bigWord))
                return@buildSet
            }

            for ((onePieceWordRange, onePieceWord) in bigWord.splitWithRanges(nameSeparators, insideOf = bigWordRange)) {
                for ((inWordRange, word) in onePieceWord.splitCamelCase(insideOf = onePieceWordRange)) {
                    val match = tryRun { checker.check(word.toLowerCase()) }?.firstOrNull()
                    match?.let { add(Typo(it, checkerLang, GrammarCache.hash(word), inWordRange.start)) }
                }
            }

            SpellCheckerCache.put(bigWord, LinkedHashSet(this))
        }
    }

    fun reset() {
        checker = createChecker()
    }
}
