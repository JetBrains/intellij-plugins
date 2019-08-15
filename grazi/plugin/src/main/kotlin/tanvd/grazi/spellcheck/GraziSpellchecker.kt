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
import org.languagetool.UserConfig
import org.languagetool.rules.Rule
import tanvd.grazi.GraziConfig
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.msg.GraziStateLifecycle
import tanvd.grazi.language.Lang
import tanvd.grazi.utils.Text
import tanvd.grazi.utils.spellcheckOnly
import tanvd.grazi.utils.toPointer
import tanvd.grazi.utils.withOffset
import tanvd.kex.buildSet
import tanvd.kex.tryRun


object GraziSpellchecker : GraziStateLifecycle {
    private val checkerLang = Lang.AMERICAN_ENGLISH
    private var checkers: Set<Pair<JLanguageTool, Rule>> = emptySet()

    private val ignorePatters: List<(String) -> Boolean> = listOf(Text::isHiddenFile, Text::isURL, Text::isHtmlUnicodeSymbol, Text::isFilePath)

    private fun createCheckers(state: GraziConfig.State): Set<Pair<JLanguageTool, Rule>> = state.availableLanguages.plus(checkerLang)
            .mapNotNull { lang ->
                // TODO we probably don't really need to create tools each update
                val tool = JLanguageTool(lang.jLanguage, null, UserConfig(state.userWords.toList()))
                val checker = tool.allRules.find { it.isDictionaryBasedSpellingRule }
                checker?.let { tool to checker }
            }.toSet()

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
        val typos = checkers.mapNotNull { (tool, checker) ->
            tryRun { checker.match(tool.getAnalyzedSentence(word)) }?.firstOrNull()?.let { Typo(it, Lang[tool.language]!!, 0) }
        }.filter { typo ->
            !isCasingProblem(word, typo) && !isPluralProblem(word, typo) &&
                    (IdeaSpellchecker.hasProblem(word, project, language) || !Text.isLatin(word))
        }.toSet()

        if (typos.map { it.info.lang }.toSet().size == checkers.size) {
            typos.find { it.info.lang == checkerLang }?.let { typo ->
                val fixes = typos.flatMap(Typo::fixes).sortedWith(Text.Levenshtein.Comparator(word)).toList()
                add(Typo(typo.location, typo.info, fixes))
            }
        }
    }

    private fun isCasingProblem(word: String, typo: Typo) = typo.fixes.any { it.toLowerCase() == word.toLowerCase() }

    private fun isPluralProblem(word: String, typo: Typo) = typo.fixes.any { "${it.toLowerCase()}s" == word.toLowerCase() }

    override fun init(state: GraziConfig.State, project: Project) {
        checkers = createCheckers(state)

        if (ApplicationManager.getApplication().isUnitTestMode || !state.enabledSpellcheck) return

        modifyAndCommitProjectProfile(project, Consumer {
            it.getTools(SpellCheckingInspection.SPELL_CHECKING_INSPECTION_TOOL_NAME, project).isEnabled = false
        })

        with(CommitMessageInspectionProfile.getInstance(project)) {
            getTools(getTool(CommitMessageSpellCheckingInspection::class.java).shortName, project).isEnabled = false
        }
    }


    override fun update(prevState: GraziConfig.State, newState: GraziConfig.State, project: Project) {
        checkers = createCheckers(newState)

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
