package tanvd.grazi.language

import org.languagetool.*
import tanvd.grazi.spellcheck.SpellDictionary
import java.util.*
import java.util.concurrent.TimeUnit

object LangChecker {
    private val langs: MutableMap<Lang, JLanguageTool> = HashMap()

    private const val cacheMaxSize = 25_000L
    private const val cacheExpireAfterMinutes = 5

    var motherTongue: Lang? = null

    operator fun get(lang: Lang): JLanguageTool {
        return langs.getOrPut(lang) {
            val cache = ResultCache(cacheMaxSize, cacheExpireAfterMinutes, TimeUnit.MINUTES)
            val config = UserConfig(SpellDictionary.usersCustom().words)
            (if (motherTongue == null)
                JLanguageTool(lang.toLanguage(), cache, config)
            else
                JLanguageTool(lang.toLanguage(), motherTongue!!.toLanguage(), cache, config)
                    ).apply {
                lang.configure(this)

            }
        }
    }

    fun clear() {
        langs.clear()
    }

    fun init(enabledLangs: List<Lang>) {
        for (lang in enabledLangs) {
            get(lang)
        }
    }
}
