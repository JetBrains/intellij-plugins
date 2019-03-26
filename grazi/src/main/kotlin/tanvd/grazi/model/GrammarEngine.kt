package tanvd.grazi.model

import com.intellij.openapi.components.ApplicationComponent
import org.jetbrains.annotations.NotNull
import org.languagetool.JLanguageTool
import org.languagetool.Language
import org.languagetool.Languages
import org.languagetool.language.AmericanEnglish
import org.languagetool.language.LanguageIdentifier
import org.languagetool.rules.RuleMatch
import java.util.*
import java.util.stream.Collectors

object GrammarEngine : ApplicationComponent {
    private var langToolsByLang: MutableMap<Language, JLanguageTool> = HashMap()
    var removeUnknownWords = true
    var charsForLangDetection = 500
    var noopLangs = emptyList<String>()

    fun getFixes(str: String): List<Typo> {
        if (str.length < 2) {
            return emptyList()
        }
        var lang: Language
        try {
            lang = LanguageIdentifier(charsForLangDetection).detectLanguage(str, noopLangs)?.detectedLanguage ?: AmericanEnglish()
        } catch (e: ClassNotFoundException) {
            lang = AmericanEnglish()
        }
        if (!langToolsByLang.containsKey(lang)) {
            langToolsByLang[lang] = JLanguageTool(lang)
        }
        return langToolsByLang[lang]!!
                .check(str)
                .stream()
                .filter { it != null }
                .filter { !removeUnknownWords || removeUnknownWords && it.type != RuleMatch.Type.UnknownWord }
                .map {
                    Typo(
                            IntRange(it.fromPos, it.toPos),
                            it.shortMessage,
                            Typo.Category[it.rule.category.id.toString()],
                            it.suggestedReplacements
                    )
                }
                .collect(Collectors.toList<Typo>())
    }

    override fun initComponent() {
        for (langName in listOf("English", "Russian")) {
            val lang = Languages.getLanguageForName(langName)
            if (lang != null) {
                langToolsByLang[lang] = JLanguageTool(lang)
            }
        }
    }

    @NotNull
    override fun getComponentName(): String {
        return "GrammarEngine"
    }
}
