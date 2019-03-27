package tanvd.grazi.grammar

import org.languagetool.JLanguageTool
import org.languagetool.Language
import org.languagetool.language.AmericanEnglish
import org.languagetool.language.LanguageIdentifier
import java.util.*

class Languages {
    private val langs: MutableMap<Language, JLanguageTool> = HashMap()

    private val americanEnglish by lazy { AmericanEnglish() }
    private val charsForLangDetection = 500

    fun getLangChecker(str: String, enabledLangs: List<String>): JLanguageTool {
        var lang = LanguageIdentifier(charsForLangDetection).detectLanguage(str, emptyList())?.detectedLanguage ?: americanEnglish
        if (lang.shortCode !in enabledLangs) {
            lang = americanEnglish
        }

        return langs.getOrPut(lang) {
            JLanguageTool(lang).apply {
                Family[lang]?.configure(this)
            }
        }
    }

    enum class Family(val shortCode: String, val enableRules: List<String>) {
        ENGLISH("en", listOf("CAN_NOT", "ARTICLE_MISSING", "ARTICLE_UNNECESSARY", "COMMA_BEFORE_AND", "COMMA_WHICH", "USELESS_THAT", "AND_ALSO", "And", "PASSIVE_VOICE")),
        RUSSIAN("ru", listOf("ABREV_DOT2", "KAK_VVODNOE", "PARTICLE_JE", "po_povodu_togo", "tak_skazat", "kak_bi", "O_tom_chto", "kosvennaja_rech"));

        companion object {
            operator fun get(lang: Language): Family? = values().find { it.shortCode == lang.shortCode }
        }

        fun configure(tool: JLanguageTool) {
            val toEnable = tool.allRules.filter { rule -> enableRules.any { rule.id.contains(it) } }
            toEnable.forEach {
                tool.enableRule(it.id)
            }
        }
    }
}
