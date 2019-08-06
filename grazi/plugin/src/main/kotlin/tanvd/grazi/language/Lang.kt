package tanvd.grazi.language

import com.intellij.openapi.application.ApplicationManager
import org.languagetool.JLanguageTool
import org.languagetool.Language
import tanvd.grazi.GraziBundle
import tanvd.grazi.GraziPlugin
import tanvd.kex.tryRun

@Suppress("unused")
enum class Lang(val displayName: String, val shortCode: String, val className: String, val size: String = "",
                private val enabledRules: Set<String> = emptySet(),
                private val disabledRules: Set<String> = emptySet(),
                private val disabledCategories: Set<String> = emptySet()) {
    BRITISH_ENGLISH("English (GB)", "en", "BritishEnglish", "14 MB",
            GraziBundle.langConfig("en.rules.enabled"), GraziBundle.langConfig("en.rules.disabled"), GraziBundle.langConfig("en.categories.disabled")),
    AMERICAN_ENGLISH("English (US)", "en", "AmericanEnglish", "14 MB",
            GraziBundle.langConfig("en.rules.enabled"), GraziBundle.langConfig("en.rules.disabled"), GraziBundle.langConfig("en.categories.disabled")),
    CANADIAN_ENGLISH("English (Canadian)", "en", "CanadianEnglish", "14 MB",
            GraziBundle.langConfig("en.rules.enabled"), GraziBundle.langConfig("en.rules.disabled"), GraziBundle.langConfig("en.categories.disabled")),
    RUSSIAN("Russian", "ru", "Russian", "3 MB", GraziBundle.langConfig("ru.rules.enabled")),
    PERSIAN("Persian", "fa", "Persian", "1 MB"),
    FRENCH("French", "fr", "French", "4 MB"),
    GERMANY_GERMAN("German (Germany)", "de", "GermanyGerman", "19 MB"),
    AUSTRIAN_GERMAN("German (Austria)", "de", "AustrianGerman", "19 MB"),
    POLISH("Polish", "pl", "Polish", "5 MB"),
    ITALIAN("Italian", "it", "Italian", "1 MB"),
    DUTCH("Dutch", "nl", "Dutch", "17 MB"),
    PORTUGAL_PORTUGUESE("Portuguese (Portugal)", "pt", "PortugalPortuguese", "5 MB"),
    BRAZILIAN_PORTUGUESE("Portuguese (Brazil)", "pt", "BrazilianPortuguese", "5 MB"),
    CHINESE("Chinese", "zh", "Chinese", "3 MB"),
    GREEK("Greek", "el", "Greek", "1 MB"),
    JAPANESE("Japanese", "ja", "Japanese", "1 MB"),
    ROMANIAN("Romanian", "ro", "Romanian", "1 MB"),
    SLOVAK("Slovak", "sk", "Slovak", "3 MB"),
    SPANISH("Spanish", "es", "Spanish", "2 MB"),
    UKRAINIAN("Ukrainian", "uk", "Ukrainian", "6 MB");

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

