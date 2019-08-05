package tanvd.grazi.language

import com.intellij.openapi.application.ApplicationManager
import org.languagetool.JLanguageTool
import org.languagetool.Language
import tanvd.grazi.GraziBundle
import tanvd.grazi.GraziPlugin
import tanvd.kex.tryRun

@Suppress("unused")
enum class Lang(val displayName: String, val shortCode: String, val className: String,
                private val enabledRules: Set<String> = emptySet(),
                private val disabledRules: Set<String> = emptySet(),
                private val disabledCategories: Set<String> = emptySet()) {
    BRITISH_ENGLISH("English (GB)", "en", "BritishEnglish",
            GraziBundle.langConfig("en.rules.enabled"), GraziBundle.langConfig("en.rules.disabled"), GraziBundle.langConfig("en.categories.disabled")),
    AMERICAN_ENGLISH("English (US)", "en", "AmericanEnglish",
            GraziBundle.langConfig("en.rules.enabled"), GraziBundle.langConfig("en.rules.disabled"), GraziBundle.langConfig("en.categories.disabled")),
    CANADIAN_ENGLISH("English (Canadian)", "en", "CanadianEnglish",
            GraziBundle.langConfig("en.rules.enabled"), GraziBundle.langConfig("en.rules.disabled"), GraziBundle.langConfig("en.categories.disabled")),
    RUSSIAN("Russian", "ru", "Russian", GraziBundle.langConfig("ru.rules.enabled")),
    PERSIAN("Persian", "fa", "Persian"),
    FRENCH("French", "fr", "French"),
    GERMANY_GERMAN("German (Germany)", "de", "GermanyGerman"),
    AUSTRIAN_GERMAN("German (Austria)", "de", "AustrianGerman"),
    POLISH("Polish", "pl", "Polish"),
    ITALIAN("Italian", "it", "Italian"),
    DUTCH("Dutch", "nl", "Dutch"),
    PORTUGAL_PORTUGUESE("Portuguese (Portugal)", "pt", "PortugalPortuguese"),
    BRAZILIAN_PORTUGUESE("Portuguese (Brazil)", "pt", "BrazilianPortuguese"),
    CHINESE("Chinese", "zh", "Chinese"),
    GREEK("Greek", "el", "Greek"),
    JAPANESE("Japanese", "ja", "Japanese"),
    ROMANIAN("Romanian", "ro", "Romanian"),
    SLOVAK("Slovak", "sk", "Slovak"),
    SPANISH("Spanish", "es", "Spanish"),
    UKRAINIAN("Ukrainian", "uk", "Ukrainian");

    companion object {
        operator fun get(lang: Language): Lang? = values().find { lang.name == it.displayName }
        operator fun get(code: String): Lang? = values().find { it.shortCode == code }

        fun sortedValues() = values().sortedBy(Lang::displayName)
    }

    private var _jLanguage: Language? = null
    val jLanguage: Language?
        get() {
            if (_jLanguage == null) {
                _jLanguage = GraziPlugin.loadClass("org.languagetool.language.$className")?.newInstance() as Language?
            }

            return _jLanguage
        }

    fun isEnglish() = shortCode == "en"

    override fun toString() = displayName

    fun configure(tool: JLanguageTool) {
        tool.allRules.filter { rule -> enabledRules.any { rule.id.contains(it) } }.forEach {
            tool.enableRule(it.id)
        }

        tool.allRules.filter { rule ->
            disabledRules.any { rule.id.contains(it) } || disabledCategories.any { rule.category.id.toString().contains(it) }
        }.forEach {
            tool.disableRule(it.id)
        }
    }
}

