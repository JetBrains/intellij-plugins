package tanvd.grazi.language

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.js.inline.util.IdentitySet
import org.languagetool.JLanguageTool
import org.languagetool.ResultCache
import org.languagetool.UserConfig
import tanvd.grazi.GraziConfig
import tanvd.grazi.ide.msg.GraziStateLifecycle
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object LangTool : GraziStateLifecycle {
    private val langs: MutableMap<Lang, JLanguageTool> = ConcurrentHashMap()

    private const val cacheMaxSize = 25_000L
    private const val cacheExpireAfterMinutes = 5
    private val rulesInLanguages = HashMap<String, MutableSet<Lang>>()

    operator fun get(lang: Lang): JLanguageTool {
        return langs.getOrPut(lang) {
            val cache = ResultCache(cacheMaxSize, cacheExpireAfterMinutes, TimeUnit.MINUTES)
            JLanguageTool(lang.jLanguage, GraziConfig.state.nativeLanguage.jLanguage,
                    cache, UserConfig(GraziConfig.state.userWords.toList())).apply {
                lang.configure(this)
                disableRules(allActiveRules.map { it.id }.filter { it in GraziConfig.state.userDisabledRules })
                //In case of English spellcheck will be done by Grazi spellchecker
                if (lang.isEnglish()) {
                    disableRules(allActiveRules.filter { it.isDictionaryBasedSpellingRule }.map { it.id })
                }
            }
        }
    }

    override fun init(state: GraziConfig.State, project: Project) {
        for (lang in state.enabledLanguages) {
            get(lang).allRules.distinctBy { it.id }.forEach { rule ->
                rulesInLanguages.getOrPut(rule.id, ::IdentitySet).add(lang)
            }
        }
    }

    override fun update(prevState: GraziConfig.State, newState: GraziConfig.State, project: Project) {
        langs.clear()
        rulesInLanguages.clear()

        init(newState, project)
    }

    fun getRuleLanguages(ruleId: String) = rulesInLanguages[ruleId]
}
