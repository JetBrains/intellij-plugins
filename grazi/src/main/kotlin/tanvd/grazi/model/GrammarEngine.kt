package tanvd.grazi.model

import org.languagetool.JLanguageTool
import org.languagetool.language.BritishEnglish
import java.util.stream.Collectors

object GrammarEngine {
    fun getFixes(str: String): List<TextFix> {
//        val langTool = JLanguageTool(LanguageIdentifier().detectLanguage(str))

        val langTool = JLanguageTool(BritishEnglish())
        return langTool.check(str)
                .stream()
                .filter { it != null }
                .map { TextFix(IntRange(it.fromPos, it.toPos), it.shortMessage, it.suggestedReplacements) }
                .collect(Collectors.toList<TextFix>())
    }
}
