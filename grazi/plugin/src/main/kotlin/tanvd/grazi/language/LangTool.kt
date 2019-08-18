package tanvd.grazi.language

import com.intellij.openapi.project.Project
import org.languagetool.JLanguageTool
import org.languagetool.UserConfig
import org.languagetool.rules.Rule
import tanvd.grazi.GraziConfig
import tanvd.grazi.ide.msg.GraziStateLifecycle
import java.util.concurrent.ConcurrentHashMap

object LangTool : GraziStateLifecycle {
    private val langs: MutableMap<Lang, JLanguageTool> = ConcurrentHashMap()

    private val rulesToLanguages = HashMap<String, MutableSet<Lang>>()

    fun getTool(lang: Lang): JLanguageTool {
        require(lang.jLanguage != null) { "Trying to get LangTool for not available language" }

        return langs.getOrPut(lang) {
            JLanguageTool(lang.jLanguage!!, GraziConfig.get().nativeLanguage.jLanguage, null, UserConfig(GraziConfig.get().userWords.toList())).apply {
                lang.configure(this)

                GraziConfig.get().userDisabledRules.forEach { id -> disableRule(id) }
                GraziConfig.get().userEnabledRules.forEach { id -> enableRule(id) }
            }
        }
    }

    fun getSpeller(lang: Lang): Rule? = getTool(lang).allRules.find { it.isDictionaryBasedSpellingRule }

    override fun init(state: GraziConfig.State, project: Project) {
        for (lang in state.availableLanguages) {
            getTool(lang).allRules.distinctBy { it.id }.forEach { rule ->
                rulesToLanguages.getOrPut(rule.id, ::HashSet).add(lang)
            }
        }
    }

    override fun update(prevState: GraziConfig.State, newState: GraziConfig.State, project: Project) {
        langs.clear()
        rulesToLanguages.clear()

        init(newState, project)
    }

    fun getRuleLanguages(ruleId: String) = rulesToLanguages[ruleId]
}
