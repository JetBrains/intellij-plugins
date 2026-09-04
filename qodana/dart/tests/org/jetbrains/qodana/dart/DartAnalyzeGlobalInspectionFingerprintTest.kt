package org.jetbrains.qodana.dart

import com.jetbrains.qodana.sarif.baseline.BaselineCalculation
import com.jetbrains.qodana.sarif.model.Result
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileYamlConfig
import org.junit.Assert.assertNotEquals
import org.junit.Test

internal class DartAnalyzeGlobalInspectionFingerprintTest : DartAnalyzeProjectQodanaTestCase() {
  override val analyzeResponses: Map<String, RecordedDartAnalyzeResponse> = mapOf(
    "test-module" to readDartAnalyzeTestData("fingerprints.json"),
  )

  @Test
  fun `keeps fingerprints stable on retrigger and separates inspections`() {
    updateQodanaConfig {
      it.copy(
        profile = dartAnalyzeProfile().copy(
          inspections = listOf(QodanaProfileYamlConfig.InspectionConfig("DartAnalyze.await_only_futures", enabled = true)),
        ),
      )
    }
    runAnalysis()
    val initialResults = resultsByRuleId()
    val initialSignatures = initialResults.mapValues { resultSignature(it.value) }
    val initialFingerprints = initialResults.mapValues { fingerprints(it.value) }

    runAnalysis()
    val retriggeredResults = resultsByRuleId()
    assertEquals(initialSignatures, retriggeredResults.mapValues { resultSignature(it.value) })
    assertEquals(initialFingerprints, retriggeredResults.mapValues { fingerprints(it.value) })

    val uncategorized = requireNotNull(initialFingerprints[DART_ANALYZE_UNCATEGORIZED_SHORT_NAME])
    val categorized = requireNotNull(initialFingerprints["DartAnalyze.await_only_futures"])
    assertNotEquals(uncategorized.first, categorized.first)
    assertNotEquals(uncategorized.second, categorized.second)
  }

  private fun resultsByRuleId(): Map<String, Result> {
    val sarifResults = manager.sarifRun.results
    assertEquals("Expected exactly one SARIF result per inspection", 2, sarifResults.size)
    val results = sarifResults.associateBy { it.ruleId }
    assertEquals(
      setOf(DART_ANALYZE_UNCATEGORIZED_SHORT_NAME, "DartAnalyze.await_only_futures"),
      results.keys,
    )
    return results
  }

  private fun fingerprints(result: Result): Pair<String, String> {
    val partialFingerprints = requireNotNull(result.partialFingerprints)
    return requireNotNull(partialFingerprints.get(BaselineCalculation.EQUAL_INDICATOR, 1)) to
           requireNotNull(partialFingerprints.get(BaselineCalculation.EQUAL_INDICATOR, 2))
  }

  private fun resultSignature(result: Result): List<Any?> {
    val location = result.singleLocation().physicalLocation
    val region = location.region
    return listOf(
      result.ruleId,
      result.level,
      result.message.text,
      location.artifactLocation.uri,
      region.startLine,
      region.startColumn,
      region.charLength,
    )
  }
}
