package tanvd.grazi.language

import org.languagetool.JLanguageTool
import org.languagetool.Language
import org.languagetool.language.*
import tanvd.grazi.GraziBundle

enum class Lang(val jLanguage: Language,
                private val enabledRules: Set<String> = emptySet(),
                private val disabledRules: Set<String> = emptySet()) {
    BRITISH_ENGLISH(BritishEnglish(), GraziBundle.langConfig("en.enabled"), GraziBundle.langConfig("en.disabled")),
    AMERICAN_ENGLISH(AmericanEnglish(), GraziBundle.langConfig("en.enabled"), GraziBundle.langConfig("en.disabled")),
    CANADIAN_ENGLISH(CanadianEnglish(), GraziBundle.langConfig("en.enabled"), GraziBundle.langConfig("en.disabled")),
    RUSSIAN(Russian(), GraziBundle.langConfig("ru.enabled")),
    PERSIAN(Persian()),
    FRENCH(French()),
    GERMANY_GERMAN(GermanyGerman()),
    AUSTRIAN_GERMAN(AustrianGerman()),
    POLISH(Polish()),
    ITALIAN(Italian()),
    DUTCH(Dutch()),
    PORTUGAL_PORTUGUESE(PortugalPortuguese()),
    BRAZILIAN_PORTUGUESE(BrazilianPortuguese()),
    CHINESE(Chinese()),
    GREEK(Greek()),
    JAPANESE(Japanese()),
    ROMANIAN(Romanian()),
    SLOVAK(Slovak()),
    SPANISH(Spanish()),
    UKRAINIAN(Ukrainian());

    companion object {
        operator fun get(lang: Language): Lang? = values().find { it.shortCode == lang.shortCode }
        operator fun get(code: String): Lang? = values().find { it.shortCode == code }

        val sortedValues = values().sortedBy { it.displayName }
    }

    val shortCode = jLanguage.shortCode!!

    val displayName = jLanguage.name!!

    fun isEnglish() = this.shortCode == "en"

    override fun toString() = displayName

    fun configure(tool: JLanguageTool) {
        tool.allRules.filter { rule -> enabledRules.any { rule.id.contains(it) } }.forEach {
            tool.enableRule(it.id)
        }

        tool.allRules.filter { rule -> disabledRules.any { rule.id.contains(it) } }.forEach {
            tool.disableRule(it.id)
        }
    }
}

