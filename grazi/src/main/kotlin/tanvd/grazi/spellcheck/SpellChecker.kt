package tanvd.grazi.spellcheck

import org.languagetool.*
import org.languagetool.language.BritishEnglish
import tanvd.grazi.GraziConfig
import tanvd.grazi.grammar.GrammarCache
import tanvd.grazi.grammar.Typo
import tanvd.grazi.utils.splitCamelCase
import java.util.concurrent.TimeUnit


object SpellChecker {
    private const val cacheMaxSize = 30_000L
    private const val cacheExpireAfterMinutes = 5
    private val checkerLang = BritishEnglish()

    private val ignorePatters: List<(String) -> Boolean> = listOf({ it -> it.startsWith(".") })

    private fun createChecker(): JLanguageTool {
        val cache = ResultCache(cacheMaxSize, cacheExpireAfterMinutes, TimeUnit.MINUTES)
        val userConfig = UserConfig(SpellDictionary.usersCustom().words)
        return JLanguageTool(checkerLang, GraziConfig.state.motherTongue.toLanguage(), cache, userConfig).apply {
            disableRules(allActiveRules.filter { !it.isDictionaryBasedSpellingRule }.map { it.id })
        }
    }

    var checker: JLanguageTool = createChecker()

    fun check(bigWord: String): List<Typo> {
        if (ignorePatters.any { it(bigWord) }) {
            return emptyList()
        }
        val typos = ArrayList<Typo>()
        val words = bigWord.splitCamelCase()
        var cumulativeIndex = 0
        for (word in words) {
            val match = checker.check(word).firstOrNull()
            match?.let { Typo(it, GrammarCache.hash(word)).withOffset(cumulativeIndex) }?.let { typos.add(it) }
            cumulativeIndex += word.length
        }
        return typos
    }


    fun init() {
        checker = createChecker()
    }
}
