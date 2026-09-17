package org.jetbrains.qodana.dart

import com.jetbrains.qodana.sarif.baseline.BaselineCalculation
import com.jetbrains.qodana.sarif.model.Level
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.junit.Test

internal class DartAnalyzeGlobalInspectionQodanaTest : DartAnalyzeProjectQodanaTestCase() {
  override val analyzeResponses: Map<String, RecordedDartAnalyzeResponse> = mapOf(
    "test-module" to readDartAnalyzeTestData("single-uncategorized.json"),
  )

  @Test
  fun `reports a Dart diagnostic through SARIF`() {
    runAnalysis()

    val result = singleSarifResult()
    assertEquals(DART_ANALYZE_ERROR_SHORT_NAME, result.ruleId)
    assertEquals(Level.ERROR, result.level)
    assertNotNull(result.partialFingerprints?.get(BaselineCalculation.EQUAL_INDICATOR, 1))
    assertNotNull(result.partialFingerprints?.get(BaselineCalculation.EQUAL_INDICATOR, 2))
    assertEquals(
      "[undefined_identifier] Undefined name 'missing'. Try correcting the name to one that is defined, or defining the name.",
      result.message.text,
    )
    val location = result.singleLocation().physicalLocation
    assertEquals("test-module/lib/main.dart", location.artifactLocation.uri)
    assertEquals(1, location.region.startLine)
    assertEquals(15, location.region.startColumn)
  }

  @Test
  fun `does not configure the project before analysis`() {
    runAnalysis()

    assertRecordedDartExecutions("analyze" to "test-module")
  }

  @Test
  fun `does not report diagnostics when the inspection is disabled`() {
    updateQodanaConfig { it.copy(profile = QodanaProfileConfig.named("qodana.single:$DART_PROJECT_SETUP_SHORT_NAME")) }
    runAnalysis()

    assertEmptySarifResults()
  }
}
