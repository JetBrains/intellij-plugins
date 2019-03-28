package tanvd.grazi.grammar

import org.languagetool.JLanguageTool
import org.languagetool.Language
import org.languagetool.Languages
import org.languagetool.language.AmericanEnglish
import org.languagetool.language.LanguageIdentifier
import java.io.Closeable
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.*
import java.util.stream.Collectors

@Suppress("UNCHECKED_CAST")
class Languages {
    private val langs: MutableMap<Language, JLanguageTool> = HashMap()

    private val americanEnglish by lazy { AmericanEnglish() }
    private val charsForLangDetection = 500

    fun getLangChecker(str: String, enabledLangs: List<String>): JLanguageTool {
        PatchedLanguages(enabledLangs).use {
            val lang = LanguageIdentifier(charsForLangDetection).detectLanguage(str, emptyList())?.detectedLanguage
                    ?: americanEnglish

            return langs.getOrPut(lang) {
                JLanguageTool(lang).apply {
                    Family[lang]?.configure(this)
                }
            }
        }
    }

    fun initLangs(enabledLangs: List<String>) {
        enabledLangs.map { Languages.getLanguageForShortCode(it) }.forEach {
            langs[it] = JLanguageTool(it).apply {
                Family[it]?.configure(this)
            }
        }
    }

    enum class Family(val shortCode: String, private val enableRules: List<String>) {
        ENGLISH("en", listOf("CAN_NOT", "ARTICLE_MISSING", "ARTICLE_UNNECESSARY", "COMMA_BEFORE_AND", "COMMA_WHICH", "USELESS_THAT", "AND_ALSO", "And", "PASSIVE_VOICE")),
        RUSSIAN("ru", listOf("ABREV_DOT2", "KAK_VVODNOE", "PARTICLE_JE", "po_povodu_togo", "tak_skazat", "kak_bi", "O_tom_chto", "kosvennaja_rech"));

        companion object {
            operator fun get(lang: Language): Family? = values().find { it.shortCode == lang.shortCode }
        }

        fun configure(tool: JLanguageTool) {
            val toEnable = tool.allRules.filter { rule -> enableRules.any { rule.id.contains(it) } }
            toEnable.forEach {
                tool.enableRule(it.id)
            }
        }
    }

    private class PatchedLanguages(enabledLangs: List<String>) : Closeable {
        companion object {
            val field = org.languagetool.Languages::class.java.getDeclaredField("LANGUAGES")!!
            val modifiersField = Field::class.java.getDeclaredField("modifiers")!!
            val oldValue: List<Language>

            init {
                field.isAccessible = true
                modifiersField.isAccessible = true
                modifiersField.setInt(field, field.modifiers and Modifier::FINAL.get().inv())
                oldValue = field.get(null) as List<Language>
            }
        }

        init {
            field.set(null, oldValue.stream().filter { language -> enabledLangs.contains(language.shortCode) }
                    .collect(Collectors.toList()))
        }

        override fun close() {
            field.set(null, oldValue)
        }
    }
}
