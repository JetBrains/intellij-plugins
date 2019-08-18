package tanvd.grazi.language

import com.intellij.openapi.project.Project
import com.optimaize.langdetect.LanguageDetector
import com.optimaize.langdetect.LanguageDetectorBuilder
import com.optimaize.langdetect.ngram.NgramExtractors
import com.optimaize.langdetect.profiles.LanguageProfileReader
import tanvd.grazi.GraziConfig
import tanvd.grazi.ide.msg.GraziStateLifecycle

object LangDetector : GraziStateLifecycle {
    private const val charsForLangDetection = 1_000
    private lateinit var languages: Set<Lang>
    private lateinit var detector: LanguageDetector

    fun getLang(text: String) = detector.getProbabilities(text.take(charsForLangDetection))
            .maxBy { it.probability }
            ?.let { detectedLanguage -> languages.find { it.shortCode == detectedLanguage.locale.language } }

    override fun init(state: GraziConfig.State, project: Project) {
        languages = state.availableLanguages
        val profiles = LanguageProfileReader().read(languages.filter { it.shortCode != "zh" }.map { it.shortCode } + "zh-CN").toSet()

        detector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                .probabilityThreshold(0.90)
                .prefixFactor(1.5)
                .suffixFactor(2.0)
                .withProfiles(profiles)
                .build()
    }

    override fun update(prevState: GraziConfig.State, newState: GraziConfig.State, project: Project) {
        if (prevState.availableLanguages != newState.availableLanguages) {
            init(newState, project)
        }
    }
}
