package tanvd.grazi.ide.fus

import com.intellij.internal.statistic.eventLog.FeatureUsageData
import com.intellij.internal.statistic.service.fus.collectors.FUCounterUsageLogger
import tanvd.grazi.grammar.Typo
import tanvd.grazi.language.Lang
import tanvd.grazi.utils.isSpellingTypo

object GraziFUCounterCollector {
    fun languageDetected(lang: Lang?) = log("language.detected") {
        addData("language", lang?.shortCode ?: "")
    }

    fun typoFound(typo: Typo) = log("typo.found") {
        addData("id", typo.info.rule.id)
        addData("fixes", typo.fixes.size)
        addData("spellcheck", typo.isSpellingTypo)
    }

    fun quickfixApplied(ruleId: String, cancelled: Boolean, isSpellcheck: Boolean) = log("quickfix.applied") {
        addData("id", ruleId)
        addData("cancelled", cancelled)
        addData("spellcheck", isSpellcheck)
    }

    private fun log(eventId: String, body: FeatureUsageData.() -> Unit) = FUCounterUsageLogger.getInstance().logEvent("grazi.count", eventId, FeatureUsageData().apply(body))
}
