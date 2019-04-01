package tanvd.grazi.spellcheck

import org.languagetool.*
import tanvd.grazi.GraziConfig
import tanvd.grazi.grammar.GrammarCache
import tanvd.grazi.grammar.Typo
import tanvd.grazi.language.Lang
import tanvd.grazi.utils.buildSet
import tanvd.grazi.utils.splitCamelCase
import java.util.concurrent.TimeUnit


object SpellChecker {
    private const val cacheMaxSize = 30_000L
    private const val cacheExpireAfterMinutes = 5
    private val checkerLang = Lang.ENGLISH

    private val ignorePatters: List<(String) -> Boolean> = listOf({ it -> it.startsWith(".") })

    private fun createChecker(): JLanguageTool {
        val cache = ResultCache(cacheMaxSize, cacheExpireAfterMinutes, TimeUnit.MINUTES)
        val userConfig = UserConfig(SpellDictionary.usersCustom().words)
        return JLanguageTool(checkerLang.toLanguage(), GraziConfig.state.motherTongue.toLanguage(), cache, userConfig).apply {
            disableRules(allActiveRules.filter { !it.isDictionaryBasedSpellingRule }.map { it.id })
        }
    }

    var checker: JLanguageTool = createChecker()

    fun check(bigWord: String) = buildSet<Typo> {
        if (ignorePatters.any { it(bigWord) }) {
            return@buildSet
        }

        var cumulativeIndex = 0
        for (word in bigWord.splitCamelCase()) {
            val match = checker.check(word).firstOrNull()
            match?.let { add(Typo(it, checkerLang, GrammarCache.hash(word), cumulativeIndex)) }
            cumulativeIndex += word.length
        }
    }


    fun reset() {
        checker = createChecker()
    }
}
