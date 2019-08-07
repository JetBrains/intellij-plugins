package tanvd.grazi.language

import org.languagetool.JLanguageTool
import org.languagetool.Language
import tanvd.grazi.GraziBundle
import tanvd.grazi.GraziPlugin
import tanvd.grazi.remote.LangLibDescriptor

@Suppress("unused")
enum class Lang(val displayName: String, val shortCode: String, private val className: String, val descriptor: LangLibDescriptor,
                private val enabledRules: Set<String> = emptySet(),
                private val disabledRules: Set<String> = emptySet(),
                private val disabledCategories: Set<String> = emptySet()) {
    BRITISH_ENGLISH("English (GB)", "en", "BritishEnglish", LangLibDescriptor.ENGLISH,
            GraziBundle.langConfig("en.rules.enabled"), GraziBundle.langConfig("en.rules.disabled"), GraziBundle.langConfig("en.categories.disabled")),
    AMERICAN_ENGLISH("English (US)", "en", "AmericanEnglish", LangLibDescriptor.ENGLISH,
            GraziBundle.langConfig("en.rules.enabled"), GraziBundle.langConfig("en.rules.disabled"), GraziBundle.langConfig("en.categories.disabled")),
    CANADIAN_ENGLISH("English (Canadian)", "en", "CanadianEnglish", LangLibDescriptor.ENGLISH,
            GraziBundle.langConfig("en.rules.enabled"), GraziBundle.langConfig("en.rules.disabled"), GraziBundle.langConfig("en.categories.disabled")),
    RUSSIAN("Russian", "ru", "Russian", LangLibDescriptor.RUSSIAN, GraziBundle.langConfig("ru.rules.enabled")),
    PERSIAN("Persian", "fa", "Persian", LangLibDescriptor.PERSIAN),
    FRENCH("French", "fr", "French", LangLibDescriptor.FRENCH),
    GERMANY_GERMAN("German (Germany)", "de", "GermanyGerman", LangLibDescriptor.GERMAN),
    AUSTRIAN_GERMAN("German (Austria)", "de", "AustrianGerman", LangLibDescriptor.GERMAN),
    POLISH("Polish", "pl", "Polish", LangLibDescriptor.POLISH),
    ITALIAN("Italian", "it", "Italian", LangLibDescriptor.ITALIAN),
    DUTCH("Dutch", "nl", "Dutch", LangLibDescriptor.DUTCH),
    PORTUGAL_PORTUGUESE("Portuguese (Portugal)", "pt", "PortugalPortuguese", LangLibDescriptor.PORTUGUESE),
    BRAZILIAN_PORTUGUESE("Portuguese (Brazil)", "pt", "BrazilianPortuguese", LangLibDescriptor.PORTUGUESE),
    CHINESE("Chinese", "zh", "Chinese", LangLibDescriptor.CHINESE),
    GREEK("Greek", "el", "Greek", LangLibDescriptor.GREEK),
    JAPANESE("Japanese", "ja", "Japanese", LangLibDescriptor.JAPANESE),
    ROMANIAN("Romanian", "ro", "Romanian", LangLibDescriptor.ROMANIAN),
    SLOVAK("Slovak", "sk", "Slovak", LangLibDescriptor.SLOVAK),
    SPANISH("Spanish", "es", "Spanish", LangLibDescriptor.SPANISH),
    UKRAINIAN("Ukrainian", "uk", "Ukrainian", LangLibDescriptor.UKRAINIAN);

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

