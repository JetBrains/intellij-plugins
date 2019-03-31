package tanvd.grazi.grammar

import org.languagetool.*
import tanvd.grazi.GraziConfig
import tanvd.grazi.language.Lang
import tanvd.grazi.spellcheck.SpellDictionary
import java.util.*
import java.util.concurrent.TimeUnit

object GrammarChecker {
    private val langs: MutableMap<Lang, JLanguageTool> = HashMap()

    private const val cacheMaxSize = 25_000L
    private const val cacheExpireAfterMinutes = 5


    operator fun get(lang: Lang): JLanguageTool {
        return langs.getOrPut(lang) {
            val cache = ResultCache(cacheMaxSize, cacheExpireAfterMinutes, TimeUnit.MINUTES)
            val userConfig = UserConfig(SpellDictionary.usersCustom().words)
            JLanguageTool(lang.toLanguage(), GraziConfig.state.motherTongue.toLanguage(), cache, userConfig).apply {
                lang.configure(this)
            }
        }
    }

    fun clear() {
        langs.clear()
    }

    fun init(enabledLangs: Collection<Lang>) {
        for (lang in enabledLangs) {
            get(lang)
        }
    }
}
