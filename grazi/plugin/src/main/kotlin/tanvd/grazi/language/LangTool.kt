package tanvd.grazi.language

import com.intellij.openapi.project.Project
import org.languagetool.*
import tanvd.grazi.GraziConfig
import tanvd.grazi.ide.msg.GraziStateLifecycle
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object LangTool : GraziStateLifecycle {
    private val langs: MutableMap<Lang, JLanguageTool> = ConcurrentHashMap()

    private const val cacheMaxSize = 25_000L
    private const val cacheExpireAfterMinutes = 5
    private val rulesToLanguages = HashMap<String, MutableSet<Lang>>()

    operator fun get(lang: Lang): JLanguageTool? {
        return lang.jLanguage?.let {
            langs.getOrPut(lang) {
                val cache = ResultCache(cacheMaxSize, cacheExpireAfterMinutes, TimeUnit.MINUTES)
                JLanguageTool(it, GraziConfig.get().nativeLanguage.jLanguage,
                        cache, UserConfig(GraziConfig.get().userWords.toList())).apply {
                    lang.configure(this)

                    allRules.forEach { rule ->
                        if (rule.id in GraziConfig.get().userDisabledRules) disableRule(rule.id)
                        if (rule.id in GraziConfig.get().userEnabledRules) enableRule(rule.id)
                    }
                }
            }
        }
    }

    override fun init(state: GraziConfig.State, project: Project) {
        for (lang in state.enabledLanguages) {
            get(lang)?.let { tool ->
                tool.allRules.distinctBy { it.id }.forEach { rule ->
                    rulesToLanguages.getOrPut(rule.id, ::HashSet).add(lang)
                }
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
