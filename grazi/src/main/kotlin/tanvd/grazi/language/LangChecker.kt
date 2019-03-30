package tanvd.grazi.language

import org.languagetool.*
import tanvd.grazi.spellcheck.SpellDictionary
import java.util.*
import java.util.concurrent.TimeUnit

object LangChecker {
    private val langs: MutableMap<Lang, JLanguageTool> = HashMap()

    private const val cacheMaxSize = 50_000L
    private const val cacheExpireAfterMinutes = 5

    operator fun get(lang: Lang): JLanguageTool {
        return langs.getOrPut(lang) {
            JLanguageTool(lang.toLanguage(), ResultCache(cacheMaxSize, cacheExpireAfterMinutes, TimeUnit.MINUTES),
                    UserConfig(SpellDictionary.usersCustom().words)).apply {
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
