// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.jlanguage

import com.intellij.grazie.GrazieConfig
import com.intellij.grazie.ide.msg.GrazieStateLifecycle
import com.intellij.grazie.jlanguage.broker.GrazieDynamicClassBroker
import com.intellij.grazie.jlanguage.broker.GrazieDynamicDataBroker
import com.intellij.openapi.project.Project
import org.languagetool.JLanguageTool
import org.languagetool.config.UserConfig
import org.languagetool.rules.UppercaseMatchFilter
import org.languagetool.rules.spelling.SpellingCheckRule

object LangTool : GrazieStateLifecycle {
  private val langs: MutableMap<Lang, JLanguageTool> = HashMap()
  private val spellers: MutableMap<Lang, SpellingCheckRule?> = HashMap()

  private val rulesToLanguages = HashMap<String, MutableSet<Lang>>()

  init {
    JLanguageTool.setDataBroker(GrazieDynamicDataBroker)
    JLanguageTool.setClassBroker(GrazieDynamicClassBroker)
  }

  val allRules: Set<String>
    get() = rulesToLanguages.keys

  fun getTool(lang: Lang, state: GrazieConfig.State = GrazieConfig.get()): JLanguageTool {
    require(lang.jLanguage != null) { "Trying to get LangTool for not available language" }

    return langs.getOrPut(lang) {
      JLanguageTool(lang.jLanguage!!, state.nativeLanguage.jLanguage, UserConfig(state.userWords.toList())).apply {
        addMatchFilter(UppercaseMatchFilter())

        state.userDisabledRules.forEach { id -> disableRule(id) }
        state.userEnabledRules.forEach { id -> enableRule(id) }

        allRules.filter { rule -> rule.isDictionaryBasedSpellingRule }.forEach {
          disableRule(it.id)
        }
      }
    }
  }

  fun getSpeller(lang: Lang, state: GrazieConfig.State = GrazieConfig.get()): SpellingCheckRule? = spellers.getOrPut(lang) {
    getTool(lang, state).allRules.find { it.isDictionaryBasedSpellingRule } as SpellingCheckRule?
  }

  override fun init(state: GrazieConfig.State, project: Project) {
    for (lang in state.availableLanguages) {
      getTool(lang, state).allRules.distinctBy { it.id }.forEach { rule ->
        rulesToLanguages.getOrPut(rule.id, ::HashSet).add(lang)
      }
    }
  }

  override fun update(prevState: GrazieConfig.State, newState: GrazieConfig.State, project: Project) {
    langs.clear()
    spellers.clear()
    rulesToLanguages.clear()

    init(newState, project)
  }

  fun getRuleLanguages(ruleId: String) = rulesToLanguages[ruleId]
}
