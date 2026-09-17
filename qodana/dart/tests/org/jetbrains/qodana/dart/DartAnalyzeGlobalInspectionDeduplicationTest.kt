package org.jetbrains.qodana.dart

import com.jetbrains.qodana.sarif.model.Level
import org.junit.Test

internal class DartAnalyzeGlobalInspectionDeduplicationTest : DartAnalyzeProjectQodanaTestCase() {
  override val analyzeResponses: Map<String, RecordedDartAnalyzeResponse> = mapOf(
    "test-module" to readDartAnalyzeTestData("dedup-root.json"),
    "test-module/example" to readDartAnalyzeTestData("dedup-example.json"),
  )

  @Test
  fun `deduplicates diagnostics reported by a parent and an example subproject`() {
    runAnalysis()

    val result = singleSarifResult()
    assertEquals(
      "[undefined_identifier] Undefined name 'missing'. Try correcting the name to one that is defined, or defining the name.",
      result.message.text,
    )
    val location = result.singleLocation().physicalLocation
    assertEquals("test-module/example/lib/main.dart", location.artifactLocation.uri)
    assertEquals(DART_ANALYZE_ERROR_SHORT_NAME, result.ruleId)
    assertEquals(Level.ERROR, result.level)
    assertEquals(1, location.region.startLine)
    assertEquals(15, location.region.startColumn)
  }
}
