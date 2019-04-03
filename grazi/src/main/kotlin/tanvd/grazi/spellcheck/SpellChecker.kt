package tanvd.grazi.spellcheck

import com.intellij.openapi.project.Project
import com.intellij.spellchecker.SpellCheckerManager
import org.languagetool.*
import tanvd.grazi.GraziConfig
import tanvd.grazi.grammar.Typo
import tanvd.grazi.language.Lang
import tanvd.grazi.utils.*
import java.util.concurrent.TimeUnit


object SpellChecker {
    private const val cacheMaxSize = 30_000L
    private const val cacheExpireAfterMinutes = 5
    private val checkerLang = Lang.AMERICAN_ENGLISH

    private val cache = TypoCache(50_000L)

    private val whiteSpaceSeparators = listOf(' ', '\t')
    private val nameSeparators = listOf('.', '_', '-')
    private val trimmed = listOf('$', '%', '{', '}')

    private val ignorePatters: List<(String) -> Boolean> = listOf(
            { it -> it.startsWith(".") },
            { it -> it.isUrl() },
            { it -> it.isHtmlPlainTextTag() })

    private fun createChecker(): JLanguageTool {
        val cache = ResultCache(cacheMaxSize, cacheExpireAfterMinutes, TimeUnit.MINUTES)
        return JLanguageTool(checkerLang.toLanguage(), GraziConfig.state.nativeLanguage.toLanguage(),
                cache, UserConfig(GraziConfig.state.userWords)).apply {
            disableRules(allActiveRules.filter { !it.isDictionaryBasedSpellingRule }.map { it.id })
        }
    }

    private var checker: JLanguageTool = createChecker()

    fun check(text: String, project: Project) = buildSet<Typo> {
        for ((bigWordRange, bigWord) in text.splitWithRanges(whiteSpaceSeparators)) {
            if (ignorePatters.any { it(bigWord) }) continue

            if (cache.contains(bigWord)) {
                addAll(cache.get(bigWord))
                return@buildSet
            }

            for ((onePieceWordRange, onePieceWord) in bigWord.splitWithRanges(nameSeparators, insideOf = bigWordRange)) {
                val (trimmedWordRange, trimmedWord) = onePieceWord.trimWithRange(trimmed, insideOf = onePieceWordRange)
                if (trimmedWordRange == null) continue

                for ((inWordRange, word) in trimmedWord.splitCamelCase(insideOf = trimmedWordRange)) {

                    checkSingleWord(word, inWordRange, project)?.let { add(it) }
                }
            }

            cache.put(bigWord, LinkedHashSet(this))
        }
    }

    private fun checkSingleWord(word: String, inWordRange: IntRange, project: Project): Typo? {

        val typo = tryRun { checker.check(word.toLowerCase()) }?.firstOrNull()
                ?.let { Typo(it, checkerLang, TypoCache.hash(word), inWordRange.start) }

        if (typo != null) {
            val spellchecker = SpellCheckerManager.getInstance(project)
            if (spellchecker.hasProblem(word)) {
                return typo
            }
        }
        return null
    }

    fun reset() {
        checker = createChecker()
        cache.reset()
    }
}
