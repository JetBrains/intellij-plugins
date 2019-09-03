package tanvd.grazi.language

import com.intellij.openapi.project.Project
import tanvd.grazi.GraziConfig
import tanvd.grazi.ide.fus.GraziFUCounterCollector
import tanvd.grazi.ide.msg.GraziStateLifecycle
import tanvd.grazi.langdetect.detector.LanguageDetector
import tanvd.grazi.langdetect.detector.LanguageDetectorBuilder
import tanvd.grazi.langdetect.ngram.NgramExtractor
import tanvd.grazi.langdetect.profiles.LanguageProfileReader

object LangDetector : GraziStateLifecycle {
    private const val charsForLangDetection = 1_000
    private lateinit var languages: Set<Lang>
    private var detector: LanguageDetector? = null

    fun getLang(text: String) = detector?.getProbabilities(text.take(charsForLangDetection))
        ?.maxBy { it.probability }
        ?.let { detectedLanguage -> languages.find { it.shortCode == detectedLanguage.locale.language } }
        .also { GraziFUCounterCollector.languageDetected(it) }

    override fun init(state: GraziConfig.State, project: Project) {
        languages = state.availableLanguages
        val profiles = LanguageProfileReader().read(languages.filter { it.shortCode != "zh" }.map { it.shortCode } + "zh-CN").toSet()

        detector = LanguageDetectorBuilder.create(NgramExtractor.standard)
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
