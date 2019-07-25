package tanvd.grazi.language

import com.optimaize.langdetect.LanguageDetectorBuilder
import com.optimaize.langdetect.ngram.NgramExtractors
import com.optimaize.langdetect.profiles.LanguageProfile
import com.optimaize.langdetect.profiles.LanguageProfileReader

object LangDetector {
    private const val charsForLangDetection = 500
    private val detector = LanguageDetectorBuilder.create(NgramExtractors.standard())
            .minimalConfidence(0.9)
            .shortTextAlgorithm(50)
            .withProfiles(profiles)
            .build()

    fun getLang(str: String, enabledLanguages: List<Lang>): Lang? {
        return detector.getProbabilities(str.take(charsForLangDetection))
                .filter { it.locale.language in enabledLanguages.map { it.shortCode } }
                .maxBy { it.probability }
                ?.let { Lang[it.locale.language] } ?: enabledLanguages.firstOrNull()
    }


    private val profiles: List<LanguageProfile>
        get() = LanguageProfileReader().read((Lang.values().filter { it != Lang.CHINESE }.map { it.shortCode } + listOf("zh-CN", "zh-TW")).toSet())
}
