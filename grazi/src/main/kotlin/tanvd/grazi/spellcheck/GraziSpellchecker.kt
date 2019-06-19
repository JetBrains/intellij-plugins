package tanvd.grazi.spellcheck

import org.languagetool.*
import tanvd.grazi.GraziConfig
import tanvd.grazi.grammar.Typo
import tanvd.grazi.language.Lang
import tanvd.grazi.utils.*
import tanvd.kex.buildSet
import tanvd.kex.tryRun
import java.util.concurrent.TimeUnit


object GraziSpellchecker {
    private const val cacheMaxSize = 25_000L
    private const val cacheExpireAfterMinutes = 5
    private val checkerLang = Lang.AMERICAN_ENGLISH

    private val whiteSpaceSeparators = listOf(' ', '\t')
    private val nameSeparators = listOf('.', '_', '-', '&')
    private val trimmed = listOf('$', '%', '{', '}', ';', '\\', '@', '[', ']', '<', '>')

    private val ignorePatters: List<(String) -> Boolean> = listOf(
            { it -> it.startsWith(".") },
            { it -> it.isUrl() },
            { it -> it.isHtmlPlainTextTag() },
            { it -> it.isFilePath() })

    private fun createChecker(): JLanguageTool {
        val cache = ResultCache(cacheMaxSize, cacheExpireAfterMinutes, TimeUnit.MINUTES)
        return JLanguageTool(checkerLang.jlanguage, GraziConfig.state.nativeLanguage.jlanguage,
                cache, UserConfig(GraziConfig.state.userWords.toList())).apply {
            disableRules(allActiveRules.filter { !it.isDictionaryBasedSpellingRule }.map { it.id })
        }
    }

    private var checker: JLanguageTool = createChecker()

    /**
     * Checks text for spelling mistakes.
     * It separates text by whitespaces, then words in it by default name separators (like `_`, `.`), then trims the word.
     * Firstly word is checked with LanguageTool spellcheck, then with IDEA built-in (if typos were found).
     * If LanguageTool considers word as a mistake and IDEA built-in spellcheck agrees typo is returned.
     *
     * Note, that casing typos (suggestion includes the same word but in different casing) are ignored.
     */
    fun check(text: String) = buildSet<Typo> {
        if (!GraziConfig.state.enabledSpellcheck) return@buildSet

        for ((bigWordRange, bigWord) in text.splitWithRanges(whiteSpaceSeparators)) {
            if (ignorePatters.any { it(bigWord) }) continue

            for ((onePieceWordRange, onePieceWord) in bigWord.splitWithRanges(nameSeparators, insideOf = bigWordRange)) {
                val (trimmedWordRange, trimmedWord) = onePieceWord.trimWithRange(trimmed, insideOf = onePieceWordRange)
                if (trimmedWordRange == null) continue

                for ((inWordRange, word) in trimmedWord.splitCamelCase(insideOf = trimmedWordRange)) {
                    val typo = tryRun { checker.check(word) }?.firstOrNull()
                            ?.let { Typo(it, checkerLang, inWordRange.start) }
                    if (typo != null && IdeaSpellchecker.hasProblem(word) && !isCasingProblem(word, typo)) {
                        add(typo)
                    }
                }
            }
        }
    }

    private fun isCasingProblem(word: String, typo: Typo): Boolean {
        val lowerWord = word.toLowerCase()
        return typo.fixes.any { it.toLowerCase() == lowerWord }
    }

    fun reset() {
        checker = createChecker()
    }
}
