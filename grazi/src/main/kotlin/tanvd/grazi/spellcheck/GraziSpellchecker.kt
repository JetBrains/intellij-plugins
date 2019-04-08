package tanvd.grazi.spellcheck

import org.languagetool.*
import tanvd.grazi.GraziConfig
import tanvd.grazi.grammar.Typo
import tanvd.grazi.language.Lang
import tanvd.grazi.utils.*
import java.util.concurrent.TimeUnit


object GraziSpellchecker {
    private const val cacheMaxSize = 25_000L
    private const val cacheExpireAfterMinutes = 5
    private val checkerLang = Lang.AMERICAN_ENGLISH

    private val whiteSpaceSeparators = listOf(' ', '\t')
    private val nameSeparators = listOf('.', '_', '-', '&')
    private val trimmed = listOf('$', '%', '{', '}', ';', '\\')

    private val ignorePatters: List<(String) -> Boolean> = listOf(
            { it -> it.startsWith(".") },
            { it -> it.isUrl() },
            { it -> it.isHtmlPlainTextTag() })

    private fun createChecker(): JLanguageTool {
        val cache = ResultCache(cacheMaxSize, cacheExpireAfterMinutes, TimeUnit.MINUTES)
        return JLanguageTool(checkerLang.jlanguage, GraziConfig.state.nativeLanguage.jlanguage,
                cache, UserConfig(GraziConfig.state.userWords.toList())).apply {
            disableRules(allActiveRules.filter { !it.isDictionaryBasedSpellingRule }.map { it.id })
        }
    }

    private var checker: JLanguageTool = createChecker()

    fun check(text: String) = buildSet<Typo> {
        if (!GraziConfig.state.enabledSpellcheck) return@buildSet

        for ((bigWordRange, bigWord) in text.splitWithRanges(whiteSpaceSeparators)) {
            if (ignorePatters.any { it(bigWord) }) continue

            for ((onePieceWordRange, onePieceWord) in bigWord.splitWithRanges(nameSeparators, insideOf = bigWordRange)) {
                val (trimmedWordRange, trimmedWord) = onePieceWord.trimWithRange(trimmed, insideOf = onePieceWordRange)
                if (trimmedWordRange == null) continue

                for ((inWordRange, word) in trimmedWord.splitCamelCase(insideOf = trimmedWordRange)) {
                    val typo = tryRun { checker.check(word.toLowerCase()) }?.firstOrNull()
                            ?.let { Typo(it, checkerLang, inWordRange.start) }
                    if (typo != null && IdeaSpellchecker.hasProblem(word)) {
                        add(typo)
                    }
                }
            }
        }
    }

    fun reset() {
        checker = createChecker()
    }
}
