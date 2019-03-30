package tanvd.grazi.language

import org.languagetool.JLanguageTool
import java.util.*

object LangChecker {
    private val langs: MutableMap<Lang, JLanguageTool> = HashMap()

    operator fun get(lang: Lang): JLanguageTool {
        return langs.getOrPut(lang) {
            JLanguageTool(lang.toLanguage()).apply {
                lang.configure(this)
            }
        }
    }

    fun init(enabledLangs: List<Lang>) {
        for (lang in enabledLangs) {
            get(lang)
        }
    }
}
