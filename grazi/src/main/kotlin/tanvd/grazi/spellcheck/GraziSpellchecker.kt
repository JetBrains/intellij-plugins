package tanvd.grazi.spellcheck

import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.spellchecker.inspections.Splitter
import com.intellij.spellchecker.tokenizer.TokenConsumer
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

    private val ignorePatters: List<(String) -> Boolean> = listOf(Text::isHiddenFile, Text::isURL, Text::isHtmlUnicodeSymbol, Text::isFilePath)

    private fun createChecker(): JLanguageTool {
        val cache = ResultCache(cacheMaxSize, cacheExpireAfterMinutes, TimeUnit.MINUTES)
        return JLanguageTool(checkerLang.jLanguage, GraziConfig.state.nativeLanguage.jLanguage,
                cache, UserConfig(GraziConfig.state.userWords.toList())).apply {
            disableRules(allActiveRules.filter { !it.isDictionaryBasedSpellingRule }.map { it.id })
        }
    }

    private var checker: JLanguageTool = createChecker()

    private class GraziTokenConsumer(val project: Project, val language: Language) : TokenConsumer() {
        val result = HashSet<Typo>()

        override fun consumeToken(element: PsiElement, text: String, useRename: Boolean,
                                  offset: Int, rangeToCheck: TextRange, splitter: Splitter) {
            splitter.split(text, rangeToCheck) { partRange ->
                if (partRange != null) {
                    val part = partRange.substring(text)
                    if (!ignorePatters.any { it(part) }) {
                        val typos = check(part, project, language)
                        result.addAll(typos.map { typo ->
                            typo.copy(location = typo.location.copy(range = typo.location.range.withOffset(offset + partRange.startOffset),
                                    pointer = element.toPointer(), shouldUseRename = useRename))
                        })
                    }
                }
            }
        }
    }

    fun getFixes(element: PsiElement): Set<Typo> {
        val strategy = IdeaSpellchecker.getSpellcheckingStrategy(element)
        if (strategy != null) {
            val consumer = GraziTokenConsumer(element.project, element.language)
            strategy.getTokenizer(element).tokenize(element, consumer)
            return consumer.result
        }
        return emptySet()
    }

    /**
     * Checks text for spelling mistakes.
     * It separates text by whitespaces, then words in it by default name separators (like `_`, `.`), then trims the word.
     * Firstly word is checked with LanguageTool spellcheck, then with IDEA built-in (if typos were found).
     * If LanguageTool considers word as a mistake and IDEA built-in spellcheck agrees typo is returned.
     *
     * Note, that casing typos (suggestion includes the same word but in different casing) are ignored.
     */
    private fun check(word: String, project: Project, language: Language) = buildSet<Typo> {
        val typo = tryRun { checker.check(word) }?.firstOrNull()?.let { Typo(it, checkerLang, 0) }
        if (typo != null && IdeaSpellchecker.hasProblem(word, project, language) && !isCasingProblem(word, typo)) {
            add(typo)
        }
    }

    private fun isCasingProblem(word: String, typo: Typo): Boolean {
        return typo.fixes.any { it.toLowerCase() == word.toLowerCase() }
    }

    fun reset() {
        checker = createChecker()
    }
}
