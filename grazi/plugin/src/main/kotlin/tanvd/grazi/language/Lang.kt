package tanvd.grazi.language

import org.languagetool.JLanguageTool
import org.languagetool.Language
import tanvd.grazi.GraziBundle
import tanvd.grazi.GraziPlugin
import tanvd.grazi.remote.RemoteLangDescriptor
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.regex.Pattern

@Suppress("unused")
enum class Lang(val displayName: String, val shortCode: String, private val className: String, val descriptor: RemoteLangDescriptor,
                private val enabledRules: Set<String> = emptySet(),
                private val disabledRules: Set<String> = emptySet(),
                private val disabledCategories: Set<String> = emptySet()) {
    BRITISH_ENGLISH("English (GB)", "en", "BritishEnglish", RemoteLangDescriptor.ENGLISH,
            GraziBundle.langConfig("en.rules.enabled"), GraziBundle.langConfig("en.rules.disabled"), GraziBundle.langConfig("en.categories.disabled")),
    AMERICAN_ENGLISH("English (US)", "en", "AmericanEnglish", RemoteLangDescriptor.ENGLISH,
            GraziBundle.langConfig("en.rules.enabled"), GraziBundle.langConfig("en.rules.disabled"), GraziBundle.langConfig("en.categories.disabled")),
    CANADIAN_ENGLISH("English (Canadian)", "en", "CanadianEnglish", RemoteLangDescriptor.ENGLISH,
            GraziBundle.langConfig("en.rules.enabled"), GraziBundle.langConfig("en.rules.disabled"), GraziBundle.langConfig("en.categories.disabled")),
    RUSSIAN("Russian", "ru", "Russian", RemoteLangDescriptor.RUSSIAN, GraziBundle.langConfig("ru.rules.enabled")),
    PERSIAN("Persian", "fa", "Persian", RemoteLangDescriptor.PERSIAN),
    FRENCH("French", "fr", "French", RemoteLangDescriptor.FRENCH),
    GERMANY_GERMAN("German (Germany)", "de", "GermanyGerman", RemoteLangDescriptor.GERMAN),
    AUSTRIAN_GERMAN("German (Austria)", "de", "AustrianGerman", RemoteLangDescriptor.GERMAN),
    POLISH("Polish", "pl", "Polish", RemoteLangDescriptor.POLISH),
    ITALIAN("Italian", "it", "Italian", RemoteLangDescriptor.ITALIAN),
    DUTCH("Dutch", "nl", "Dutch", RemoteLangDescriptor.DUTCH),
    PORTUGAL_PORTUGUESE("Portuguese (Portugal)", "pt", "PortugalPortuguese", RemoteLangDescriptor.PORTUGUESE),
    BRAZILIAN_PORTUGUESE("Portuguese (Brazil)", "pt", "BrazilianPortuguese", RemoteLangDescriptor.PORTUGUESE),
    CHINESE("Chinese", "zh", "Chinese", RemoteLangDescriptor.CHINESE),
    GREEK("Greek", "el", "Greek", RemoteLangDescriptor.GREEK),
    JAPANESE("Japanese", "ja", "Japanese", RemoteLangDescriptor.JAPANESE),
    ROMANIAN("Romanian", "ro", "Romanian", RemoteLangDescriptor.ROMANIAN),
    SLOVAK("Slovak", "sk", "Slovak", RemoteLangDescriptor.SLOVAK),
    SPANISH("Spanish", "es", "Spanish", RemoteLangDescriptor.SPANISH),
    UKRAINIAN("Ukrainian", "uk", "Ukrainian", RemoteLangDescriptor.UKRAINIAN);

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

                if (_jLanguage != null) {
                    when {
                        this == RUSSIAN ->
                            Class.forName("org.languagetool.rules.ru.MorfologikRussianSpellerRule").getDeclaredField("RUSSIAN_LETTERS")
                        this == UKRAINIAN ->
                            Class.forName("org.languagetool.rules.uk.MorfologikUkrainianSpellerRule").getDeclaredField("UKRAINIAN_LETTERS")
                        else -> null
                    }?.let { pattern ->
                        pattern.isAccessible = true

                        val mods = Field::class.java.getDeclaredField("modifiers")
                        mods.isAccessible = true
                        mods.setInt(pattern, pattern.modifiers and Modifier.FINAL.inv())

                        pattern.set(null, Pattern.compile(".*"))
                    }
                }
            }

            return _jLanguage
        }

    fun isEnglish() = shortCode == "en"

    override fun toString() = displayName

    fun configure(tool: JLanguageTool) {
        tool.allRules.filter { rule -> enabledRules.any { rule.id.contains(it) } }.forEach {
            tool.enableRule(it.id)
        }

        // disable all spellchecker rules also (use them directly in GraziSpellchecker)
        tool.allRules.filter { rule ->
            rule.isDictionaryBasedSpellingRule || disabledRules.any { rule.id.contains(it) }
                    || disabledCategories.any { rule.category.id.toString().contains(it) }
        }.forEach {
            tool.disableRule(it.id)
        }
    }
}

