package tanvd.grazi.language

import org.languagetool.JLanguageTool
import org.languagetool.Language
import org.languagetool.Languages.getLanguageForShortCode

enum class Lang(val shortCode: String, private val enableRules: List<String> = emptyList()) {
    ENGLISH("en", listOf("CAN_NOT", "ARTICLE_MISSING", "ARTICLE_UNNECESSARY", "COMMA_BEFORE_AND", "COMMA_WHICH", "USELESS_THAT", "AND_ALSO", "And", "PASSIVE_VOICE")),
    RUSSIAN("ru", listOf("ABREV_DOT2", "KAK_VVODNOE", "PARTICLE_JE", "po_povodu_togo", "tak_skazat", "kak_bi", "O_tom_chto", "kosvennaja_rech")),
    PERSIAN("fa"),
    FRENCH("fr"),
    GERMAN("de"),
    POLISH("pl"),
    CATALAN("ca"),
    ITALIAN("it"),
    BRETON("br"),
    DUTCH("nl"),
    PORTUGUES("pt"),
    BELARUSIAN("be"),
    CHINESE("zh"),
    DANISH("da"),
    GALICIAN("gl"),
    GREEK("el"),
    JAPANESE("ja"),
    KHMER("km"),
    ROMANIAN("ro"),
    SLOVAK("sk"),
    SLOVENIAN("sl"),
    SPANISH("es"),
    SWEDISH("sv"),
    TAMIL("ta"),
    TAGALOG("tl"),
    UKRANIAN("uk");

    companion object {
        operator fun get(lang: Language): Lang? = values().find { it.shortCode == lang.shortCode }
        operator fun get(code: String): Lang? = values().find { it.shortCode == code }
    }

    fun toLanguage() = getLanguageForShortCode(shortCode, emptyList())!!

    fun configure(tool: JLanguageTool) {
        val toEnable = tool.allRules.filter { rule -> enableRules.any { rule.id.contains(it) } }
        toEnable.forEach {
            tool.enableRule(it.id)
        }
    }
}
