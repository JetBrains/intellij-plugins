package com.intellij.grazie.detection

import com.intellij.util.xmlb.annotations.Property
import tanvd.grazie.langdetect.ChainLanguageDetector
import tanvd.grazie.langdetect.LanguageDetector
import tanvd.grazie.langdetect.model.Language
import java.util.*
import kotlin.collections.HashSet

object DetectionContext {
  data class State(@Property val disabled: Set<Language> = HashSet()) {
    /** Disable from detection */
    fun disable(langs: Iterable<Language>) = State(disabled + langs)

    /** Enable for detection */
    fun enable(langs: Iterable<Language>) = State(disabled - langs)
  }

  data class Local(val counter: EnumMap<Language, Int> = EnumMap(Language::class.java)) {
    companion object {
      //Each text gives SIZE / SCORE_SIZE + 1 scores to own language.
      //If LANGUAGE_SCORES / TOTAL_SCORES > NOTIFICATION_PROPORTION_THRESHOLD we suggest language
      const val SCORE_SIZE = 100

      const val NOTIFICATION_PROPORTION_THRESHOLD = 0.10
      const val NOTIFICATION_TOTAL_THRESHOLD = 3

      const val NGRAM_CONFIDENCE_THRESHOLD = 0.98
      const val LIST_CONFIDENCE_THRESHOLD = 0.30
      const val TEXT_SIZE_THRESHOLD = 40
    }

    fun getToNotify(disabled: Set<Language>): Set<Language> {
      val total = counter.values.sum()

      val filtered = counter.asSequence().filter {
        val myProportion = it.value.toDouble() / total
        val myTotal = it.value
        myTotal > NOTIFICATION_TOTAL_THRESHOLD && myProportion > NOTIFICATION_PROPORTION_THRESHOLD
      }.map { it.key }

      val langs = filtered.filter { it != Language.UNKNOWN && it !in disabled }

      return langs.toSet()
    }

    fun update(size: Int, details: ChainLanguageDetector.ChainDetectionResult) {
      val result = details.result

      //Check if not unknown
      if (result.preferred == Language.UNKNOWN) return

      //Check threshold by text size is not met
      if (size < TEXT_SIZE_THRESHOLD) return

      //Check if threshold by list detector is not met
      val maxList = details[LanguageDetector.Type.List]?.detected?.maxBy { it.probability }
      //Null if rule detector found language. We do believe rule detector and don't check its threshold
      if (maxList != null && (maxList.probability < LIST_CONFIDENCE_THRESHOLD || maxList.lang != result.preferred)) return

      //Check if threshold by ngram detector is not met
      val maxNgram = details[LanguageDetector.Type.Ngram]?.detected?.maxBy { it.probability }
      if (maxNgram != null && (maxNgram.probability < NGRAM_CONFIDENCE_THRESHOLD || maxNgram.lang != result.preferred)) return

      count(size, result.preferred)
    }

    private fun count(size: Int, lang: Language) {
      counter[lang] = counter.getOrDefault(lang, 0) + (size / SCORE_SIZE + 1)
    }

    fun clear() {
      counter.clear()
    }
  }
}
