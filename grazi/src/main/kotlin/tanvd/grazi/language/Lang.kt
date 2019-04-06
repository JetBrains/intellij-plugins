package tanvd.grazi.language

import org.languagetool.JLanguageTool
import org.languagetool.Language
import org.languagetool.language.*
import tanvd.grazi.GraziBundle

enum class Lang(val jlanguage: Language,
                private val enabledRules: Set<String> = emptySet(),
                private val disabledRules: Set<String> = emptySet()) {
    BRITISH_ENGLISH(BritishEnglish(), GraziBundle.langConfigSet<String>("en.enabled"), GraziBundle.langConfigSet<String>("en.disabled")),
    AMERICAN_ENGLISH(AmericanEnglish(), GraziBundle.langConfigSet<String>("en.enabled"), GraziBundle.langConfigSet<String>("en.disabled")),
    CANADIAN_ENGLISH(CanadianEnglish(), GraziBundle.langConfigSet<String>("en.enabled"), GraziBundle.langConfigSet<String>("en.disabled")),
    RUSSIAN(Russian(), GraziBundle.langConfigSet<String>("ru.enabled")),
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

        val sortedValues = Lang.values().sortedBy { it.displayName }
    }

    val shortCode = jlanguage.shortCode!!

    val displayName = jlanguage.name!!

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

fun Lang.isEnglish() = this.shortCode == "en"
