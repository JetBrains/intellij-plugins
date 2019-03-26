package tanvd.grazi.model

import org.languagetool.JLanguageTool
import org.languagetool.Language
import org.languagetool.language.LanguageIdentifier
import org.languagetool.rules.RuleMatch
import java.util.stream.Collectors

object GrammarEngine {
    private var langToolsByLang: MutableMap<Language, JLanguageTool> = HashMap()
    var removeUnknownWords = true

    fun getFixes(str: String): List<TextFix> {
        val lang = LanguageIdentifier(500).detectLanguage(str)
        if (lang != null && !langToolsByLang.containsKey(lang)) {
            langToolsByLang[lang] = JLanguageTool(lang)
        }
        return langToolsByLang[lang]!!
                .check(str)
                .stream()
                .filter { it != null }
                .filter { !removeUnknownWords || removeUnknownWords && it.type != RuleMatch.Type.UnknownWord }
                .map { TextFix(IntRange(it.fromPos, it.toPos), it.shortMessage, it.suggestedReplacements) }
                .collect(Collectors.toList<TextFix>())
    }
}
