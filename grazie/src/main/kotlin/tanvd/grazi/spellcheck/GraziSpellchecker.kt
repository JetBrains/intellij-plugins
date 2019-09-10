// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
import org.languagetool.rules.Rule
import org.languagetool.rules.RuleMatch
import org.languagetool.rules.en.speller.MorfologikAmericanSpellerRule
import org.slf4j.LoggerFactory
import tanvd.grazi.GraziConfig
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.msg.GraziStateLifecycle
import tanvd.grazi.language.Lang
import tanvd.grazi.language.LangTool
import tanvd.grazi.utils.Text
import tanvd.grazi.utils.toPointer
import tanvd.grazi.utils.withOffset
import tanvd.kex.LinkedSet

object GraziSpellchecker : GraziStateLifecycle {
  private const val MAX_SUGGESTIONS_COUNT = 5
  private val BASE_SPELLCHECKER_LANGUAGE = Lang.AMERICAN_ENGLISH
  private val logger = LoggerFactory.getLogger(GraziSpellchecker::class.java)

  data class SpellerTool(val tool: JLanguageTool, val speller: Rule) {
    // FIXME: Synchronized because morfologik.speller.Speller can't work in concurrent mode
    fun check(text: String): Set<RuleMatch> = synchronized(speller) { speller.match(tool.getRawAnalyzedSentence(text)).toSet() }
  }

  private val checkers: LinkedSet<SpellerTool>
    get() = GraziConfig.get().availableLanguages.plus(BASE_SPELLCHECKER_LANGUAGE).mapNotNull { lang ->
      val rule = LangTool.getSpeller(lang)
      rule?.let { SpellerTool(LangTool.getTool(lang), rule) }
    }.let { LinkedSet(it) }

  private val ignorePatters: List<(String) -> Boolean> = listOf(Text::isHiddenFile, Text::isURL, Text::isHtmlUnicodeSymbol,
                                                                Text::isFilePath)

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
      return consumer.result
    }
    return emptySet()
  }

  /**
   * Checks text for spelling mistakes.
   */
  private fun check(word: String, project: Project, language: Language): Set<Typo> {
    if (!IdeaSpellchecker.hasProblem(word, project, language) && Text.isLatin(word)) return emptySet()

    var match: RuleMatch? = null
    val fixes = checkers.map { speller ->
      try {
        speller.check(word)
      }
      catch (t: Throwable) {
        logger.warn("Got exception during check for spelling mistakes by LanguageTool with word: $word", t)
        null
      }?.firstOrNull() ?: return emptySet()
    }.onEach {
      if (it.rule is MorfologikAmericanSpellerRule) match = it
    }.flatMap { it.suggestedReplacements.take(MAX_SUGGESTIONS_COUNT) }.toList()

    return setOf(Typo(RuleMatch(match, fixes), BASE_SPELLCHECKER_LANGUAGE, 0))
  }

  override fun init(state: GraziConfig.State, project: Project) {
    if (ApplicationManager.getApplication().isUnitTestMode || !state.enabledSpellcheck) return

    //Eagerly init speller language
    LangTool.getSpeller(BASE_SPELLCHECKER_LANGUAGE, state)

    modifyAndCommitProjectProfile(project, Consumer {
      it.getTools(SpellCheckingInspection.SPELL_CHECKING_INSPECTION_TOOL_NAME, project).isEnabled = false
    })

    if (state.enabledCommitIntegration) {
      with(CommitMessageInspectionProfile.getInstance(project)) {
        getTools(getTool(CommitMessageSpellCheckingInspection::class.java).shortName, project).isEnabled = false
      }
    }
  }

  override fun update(prevState: GraziConfig.State, newState: GraziConfig.State, project: Project) {
    if (ApplicationManager.getApplication().isUnitTestMode || (prevState.enabledSpellcheck == newState.enabledSpellcheck
                                                               && prevState.enabledCommitIntegration == newState.enabledCommitIntegration)) return

    //Eagerly init speller language
    LangTool.getSpeller(BASE_SPELLCHECKER_LANGUAGE, newState)

    if (newState.enabledSpellcheck) {
      modifyAndCommitProjectProfile(project, Consumer {
        it.getTools(SpellCheckingInspection.SPELL_CHECKING_INSPECTION_TOOL_NAME, project).isEnabled = false
      })

      if (newState.enabledCommitIntegration) {
        with(CommitMessageInspectionProfile.getInstance(project)) {
          getTools(getTool(CommitMessageSpellCheckingInspection::class.java).shortName, project).isEnabled = false
        }
      }
    }
    else {
      modifyAndCommitProjectProfile(project, Consumer {
        it.getTools(SpellCheckingInspection.SPELL_CHECKING_INSPECTION_TOOL_NAME, project).isEnabled = true
      })

      if (newState.enabledCommitIntegration) {
        with(CommitMessageInspectionProfile.getInstance(project)) {
          getTools(getTool(CommitMessageSpellCheckingInspection::class.java).shortName, project).isEnabled = true
        }
      }
    }
  }
}
