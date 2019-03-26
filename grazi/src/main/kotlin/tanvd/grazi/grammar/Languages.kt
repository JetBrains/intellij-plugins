package tanvd.grazi.grammar

import org.languagetool.JLanguageTool
import org.languagetool.Language
import org.languagetool.language.AmericanEnglish
import org.languagetool.language.LanguageIdentifier
import java.util.HashMap

object Languages {
    private val langs: MutableMap<Language, JLanguageTool> = HashMap()

    var enabledLangs = arrayListOf("en")

    private val americanEnglish by lazy { AmericanEnglish() }
    private const val charsForLangDetection = 500

    fun getLangChecker(str: String) : JLanguageTool {
        var lang = LanguageIdentifier(charsForLangDetection).detectLanguage(str, emptyList())?.detectedLanguage ?: americanEnglish
        if (lang.shortCode !in enabledLangs) {
           lang = americanEnglish
        }

        return langs.getOrPut(lang) { JLanguageTool(lang)}
    }
}
