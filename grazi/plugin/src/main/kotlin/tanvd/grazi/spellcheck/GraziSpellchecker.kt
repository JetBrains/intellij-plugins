package tanvd.grazi.spellcheck

import com.intellij.codeInspection.ex.modifyAndCommitProjectProfile
import com.intellij.lang.Language
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.spellchecker.inspections.SpellCheckingInspection
import com.intellij.spellchecker.inspections.Splitter
import com.intellij.spellchecker.tokenizer.TokenConsumer
import com.intellij.util.Consumer
import com.intellij.vcs.commit.message.CommitMessageInspectionProfile
import com.intellij.vcs.commit.message.CommitMessageSpellCheckingInspection
import org.languagetool.JLanguageTool
import org.languagetool.ResultCache
import org.languagetool.UserConfig
import org.languagetool.rules.RuleMatch
import org.slf4j.LoggerFactory
import tanvd.grazi.GraziConfig
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.msg.GraziStateLifecycle
import tanvd.grazi.language.Lang
import tanvd.grazi.utils.Text
import tanvd.grazi.utils.spellcheckOnly
import tanvd.grazi.utils.toPointer
import tanvd.grazi.utils.withOffset
import tanvd.kex.buildSet
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates


object GraziSpellchecker : GraziStateLifecycle {
    private val logger = LoggerFactory.getLogger(GraziSpellchecker::class.java)

    private const val cacheMaxSize = 25_000L
    private const val cacheExpireAfterMinutes = 5
    private val checkerLang = Lang.AMERICAN_ENGLISH

    private val ignorePatters: List<(String) -> Boolean> = listOf(Text::isHiddenFile, Text::isURL, Text::isHtmlUnicodeSymbol, Text::isFilePath)

    private fun createChecker(state: GraziConfig.State): JLanguageTool {
        val cache = ResultCache(cacheMaxSize, cacheExpireAfterMinutes, TimeUnit.MINUTES)
        // AMERICAN_ENGLISH must always be present
        return JLanguageTool(checkerLang.jLanguage!!, state.nativeLanguage.jLanguage,
                cache, UserConfig(state.userWords.toList())).apply {
            disableRules(allRules.filter { !it.isDictionaryBasedSpellingRule }.map { it.id })
        }
    }

    private var checker: JLanguageTool by Delegates.notNull()

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

    fun getTypos(element: PsiElement): Set<Typo> {
        val strategy = IdeaSpellchecker.getSpellcheckingStrategy(element)
        if (strategy != null) {
            val consumer = GraziTokenConsumer(element.project, element.language)
            strategy.getTokenizer(element).tokenize(element, consumer)
            return consumer.result.spellcheckOnly()
        }
        return emptySet()
    }

    /**
     * Checks text for spelling mistakes.
     * Note, that casing and plural typos are ignored.
     */
    private fun check(word: String, project: Project, language: Language) = buildSet<Typo> {
        val typo = try {
            checker.check(word)
        } catch (e: Throwable) {
            logger.trace("GraziSpellchecker exception", e)
            emptyList<RuleMatch>()
        }?.firstOrNull()?.let { Typo(it, checkerLang, 0) }
        if (typo != null && IdeaSpellchecker.hasProblem(word, project, language)
                && !isCasingProblem(word, typo) && !isPluralProblem(word, typo)) {
            add(typo)
        }
    }

    private fun isCasingProblem(word: String, typo: Typo) = typo.fixes.any { it.toLowerCase() == word.toLowerCase() }

    private fun isPluralProblem(word: String, typo: Typo) = typo.fixes.any { "${it.toLowerCase()}s" == word.toLowerCase() }

    override fun init(state: GraziConfig.State, project: Project) {
        checker = createChecker(state)

        if (ApplicationManager.getApplication().isUnitTestMode || !state.enabledSpellcheck) return

        modifyAndCommitProjectProfile(project, Consumer {
            it.getTools(SpellCheckingInspection.SPELL_CHECKING_INSPECTION_TOOL_NAME, project).isEnabled = false
        })

        with(CommitMessageInspectionProfile.getInstance(project)) {
            getTools(getTool(CommitMessageSpellCheckingInspection::class.java).shortName, project).isEnabled = false
        }
    }


    override fun update(prevState: GraziConfig.State, newState: GraziConfig.State, project: Project) {
        checker = createChecker(newState)

        if (ApplicationManager.getApplication().isUnitTestMode || prevState.enabledSpellcheck == newState.enabledSpellcheck) return

        if (newState.enabledSpellcheck) {
            modifyAndCommitProjectProfile(project, Consumer {
                it.getTools(SpellCheckingInspection.SPELL_CHECKING_INSPECTION_TOOL_NAME, project).isEnabled = false
            })

            with(CommitMessageInspectionProfile.getInstance(project)) {
                getTools(getTool(CommitMessageSpellCheckingInspection::class.java).shortName, project).isEnabled = false
            }
        } else {
            modifyAndCommitProjectProfile(project, Consumer {
                it.getTools(SpellCheckingInspection.SPELL_CHECKING_INSPECTION_TOOL_NAME, project).isEnabled = true
            })

            with(CommitMessageInspectionProfile.getInstance(project)) {
                getTools(getTool(CommitMessageSpellCheckingInspection::class.java).shortName, project).isEnabled = true
            }
        }
    }
}
