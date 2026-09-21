package org.jetbrains.qodana.dart

import com.jetbrains.qodana.sarif.baseline.BaselineCalculation
import com.jetbrains.qodana.sarif.model.Result
import org.junit.Assert.assertNotEquals
import org.junit.Test

internal class DartAnalyzeGlobalInspectionFingerprintTest : DartAnalyzeProjectQodanaTestCase() {
  override val analyzeResponses: Map<String, RecordedDartAnalyzeResponse> = mapOf(
    "" to readDartAnalyzeTestData("fingerprints.json"),
  )

  @Test
  fun `keeps fingerprints stable on retrigger and separates diagnostics`() {
    runAnalysis()
    val initialResults = resultsByRuleId()
    val initialSignatures = initialResults.mapValues { resultSignature(it.value) }
    val initialFingerprints = initialResults.mapValues { fingerprints(it.value) }

    runAnalysis()
    val retriggeredResults = resultsByRuleId()
    assertEquals(initialSignatures, retriggeredResults.mapValues { resultSignature(it.value) })
    assertEquals(initialFingerprints, retriggeredResults.mapValues { fingerprints(it.value) })

    val errors = requireNotNull(initialFingerprints[DART_ANALYZE_ERROR_SHORT_NAME])
    val information = requireNotNull(initialFingerprints[DART_ANALYZE_INFO_SHORT_NAME])
    assertNotEquals(errors.first, information.first)
    assertNotEquals(errors.second, information.second)
  }

  private fun resultsByRuleId(): Map<String, Result> {
    val sarifResults = manager.sarifRun.results
    assertEquals("Expected exactly one SARIF result per inspection", 2, sarifResults.size)
    val results = sarifResults.associateBy { it.ruleId }
    assertEquals(
      setOf(DART_ANALYZE_ERROR_SHORT_NAME, DART_ANALYZE_INFO_SHORT_NAME),
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
