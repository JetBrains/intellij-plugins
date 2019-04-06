package tanvd.grazi.language

import org.languagetool.*
import tanvd.grazi.GraziConfig
import java.util.*
import java.util.concurrent.TimeUnit

object LangTool {
    private val langs: MutableMap<Lang, JLanguageTool> = HashMap()

    private const val cacheMaxSize = 25_000L
    private const val cacheExpireAfterMinutes = 5

    operator fun get(lang: Lang): JLanguageTool {
        return langs.getOrPut(lang) {
            val cache = ResultCache(cacheMaxSize, cacheExpireAfterMinutes, TimeUnit.MINUTES)
            JLanguageTool(lang.jlanguage, GraziConfig.state.nativeLanguage.jlanguage,
                    cache, UserConfig(GraziConfig.state.userWords.toList())).apply {
                lang.configure(this)
                disableRules(allActiveRules.map { it.id }.filter { it in GraziConfig.state.userDisabledRules })
            }
        }
    }

    fun reset() {
        langs.clear()
    }

    fun init(enabledLangs: Collection<Lang>) {
        for (lang in enabledLangs) {
            get(lang)
        }
    }
}
