package tanvd.grazi.language

import org.languagetool.JLanguageTool
import org.languagetool.Language
import org.languagetool.Languages.getLanguageForShortCode
import org.languagetool.language.*

enum class Lang(val shortCode: String, val displayName: String, val fullCode: String = shortCode,
                private val lang: Language = getLanguageForShortCode(shortCode, emptyList())!!,
                private val enabledRules: Set<String> = emptySet(),
                private val disabledRules: Set<String> = emptySet()) {
    BRITISH_ENGLISH("en", "British English", "en_UK", BritishEnglish(),
            setOf("CAN_NOT", "ARTICLE_MISSING", "ARTICLE_UNNECESSARY", "COMMA_BEFORE_AND", "COMMA_WHICH", "USELESS_THAT", "AND_ALSO", "And", "PASSIVE_VOICE"),
            setOf("WORD_CONTAINS_UNDERSCORE", "EN_QUOTES")),
    AMERICAN_ENGLISH("en", "American English", "en_US", AmericanEnglish(),
            setOf("CAN_NOT", "ARTICLE_MISSING", "ARTICLE_UNNECESSARY", "COMMA_BEFORE_AND", "COMMA_WHICH", "USELESS_THAT", "AND_ALSO", "And", "PASSIVE_VOICE"),
            setOf("WORD_CONTAINS_UNDERSCORE", "EN_QUOTES")),
    RUSSIAN("ru", "Russian", "ru", Russian(),
            setOf("ABREV_DOT2", "KAK_VVODNOE", "PARTICLE_JE", "po_povodu_togo", "tak_skazat", "kak_bi", "O_tom_chto", "kosvennaja_rech")),
    PERSIAN("fa", "Persian"),
    FRENCH("fr", "French"),
    GERMAN("de", "German"),
    POLISH("pl", "Polish"),
    CATALAN("ca", "Catalan"),
    ITALIAN("it", "Italian"),
    BRETON("br", "Breton"),
    DUTCH("nl", "Dutch"),
    PORTUGUESE("pt", "Portuguese"),
    BELORUSSIAN("be", "Belorussian"),
    CHINESE("zh", "Chinese"),
    DANISH("da", "Danish"),
    GALICIAN("gl", "Galician"),
    GREEK("el", "Greek"),
    JAPANESE("ja", "Japanese"),
    KHMER("km", "Khmer"),
    ROMANIAN("ro", "Romanian"),
    SLOVAK("sk", "Slovak"),
    SLOVENIAN("sl", "Slovenian"),
    SPANISH("es", "Spanish"),
    SWEDISH("sv", "Swedish"),
    TAMIL("ta", "Tamil"),
    TAGALOG("tl", "Tagalog"),
    UKRAINIAN("uk", "Ukrainian");

    companion object {
        operator fun get(lang: Language): Lang? = values().find { it.shortCode == lang.shortCode }
        operator fun get(code: String): Lang? = values().find { it.shortCode == code }

        fun sortedValues() = Lang.values().sortedBy { it.name }
    }

    fun toLanguage() = lang

    override fun toString() = displayName

    fun configure(tool: JLanguageTool) {
        val toEnable = tool.allRules.filter { rule -> enabledRules.any { rule.id.contains(it) } }
        toEnable.forEach {
            tool.enableRule(it.id)
        }

        val toDisable = tool.allRules.filter { rule -> disabledRules.any { rule.id.contains(it) } }
        toDisable.forEach {
            tool.disableRule(it.id)
        }
    }
}
