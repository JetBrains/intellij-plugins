package tanvd.grazi.ide.fus

import com.intellij.internal.statistic.beans.*
import com.intellij.internal.statistic.eventLog.FeatureUsageData
import com.intellij.internal.statistic.service.fus.collectors.ApplicationUsagesCollector
import tanvd.grazi.GraziConfig

@Suppress("MissingRecentApi")
class GraziFUStateCollector : ApplicationUsagesCollector() {
    override fun getGroupId(): String = "grazi.state"
    override fun getVersion(): Int = 1

    override fun getMetrics(): Set<MetricEvent> {
        val metrics = HashSet<MetricEvent>()

        val state = GraziConfig.get()
        val default = GraziConfig.State()

        state.enabledLanguages.forEach { metrics.add(newMetric("enabled.language", it.shortCode)) }

        addIfDiffers(metrics, state, default, { s -> s.nativeLanguage.shortCode }, "native.language")
        addBoolIfDiffers(metrics, state, default, { s -> s.enabledSpellcheck }, "enabled.spellcheck")

        state.userEnabledRules.forEach { metrics.add(newMetric("rule", FeatureUsageData().addData("id", it).addData("enabled", true))) }

        state.userDisabledRules.forEach { metrics.add(newMetric("rule", FeatureUsageData().addData("id", it).addData("enabled", false))) }

        return metrics
    }
}
