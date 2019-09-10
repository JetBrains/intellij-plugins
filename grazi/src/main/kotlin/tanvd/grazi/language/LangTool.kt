// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package tanvd.grazi.language

import com.intellij.openapi.project.Project
import org.languagetool.JLanguageTool
import org.languagetool.config.UserConfig
import org.languagetool.rules.Rule
import tanvd.grazi.GraziConfig
import tanvd.grazi.ide.msg.GraziStateLifecycle

object LangTool : GraziStateLifecycle {
    private val langs: MutableMap<Lang, JLanguageTool> = HashMap()
    private val spellers: MutableMap<Lang, Rule?> = HashMap()

    private val rulesToLanguages = HashMap<String, MutableSet<Lang>>()

    val allRules: Set<String>
        get() = rulesToLanguages.keys

    fun getTool(lang: Lang, state: GraziConfig.State = GraziConfig.get()): JLanguageTool {
        require(lang.jLanguage != null) { "Trying to get LangTool for not available language" }

        return langs.getOrPut(lang) {
            JLanguageTool(lang.jLanguage!!, state.nativeLanguage.jLanguage, UserConfig(state.userWords.toList())).apply {
                lang.configure(this)

                state.userDisabledRules.forEach { id -> disableRule(id) }
                state.userEnabledRules.forEach { id -> enableRule(id) }
            }
        }
    }

    fun getSpeller(lang: Lang, state: GraziConfig.State = GraziConfig.get()): Rule? {
        return spellers.getOrPut(lang) {
            getTool(lang, state).allRules.find { it.isDictionaryBasedSpellingRule }
        }
    }

    override fun init(state: GraziConfig.State, project: Project) {
        for (lang in state.availableLanguages) {
            getTool(lang, state).allRules.distinctBy { it.id }.forEach { rule ->
                rulesToLanguages.getOrPut(rule.id, ::HashSet).add(lang)
            }
        }
    }

    override fun update(prevState: GraziConfig.State, newState: GraziConfig.State, project: Project) {
        langs.clear()
        spellers.clear()
        rulesToLanguages.clear()

        init(newState, project)
    }

    fun getRuleLanguages(ruleId: String) = rulesToLanguages[ruleId]
}
