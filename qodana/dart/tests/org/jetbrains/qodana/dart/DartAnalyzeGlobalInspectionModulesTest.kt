package org.jetbrains.qodana.dart

import com.jetbrains.qodana.sarif.model.Level
import org.junit.Test
import java.nio.file.Path

internal class DartAnalyzeGlobalInspectionModulesTest : DartAnalyzeProjectQodanaTestCase() {
  override val projectSourcePath: Path
    get() = testData.resolve("dart-analyze/mini-monorepo")

  override val analyzeResponses: Map<String, RecordedDartAnalyzeResponse> = mapOf(
    "" to readDartAnalyzeTestData("monorepo-root.json"),
    "packages/core" to readDartAnalyzeTestData("single-core-uncategorized.json"),
  )

  @Test
  fun `analyzes the project root once`() {
    runAnalysis()

    assertEquals(
      listOf(
        "apps/app/lib/main.dart",
        "packages/core/lib/main.dart",
        "packages/core/nested_flutter_app/lib/main.dart",
        "packages/flutter_app/lib/main.dart",
      ),
      sarifResultPaths(),
    )
    assertTrue(manager.sarifRun.results.all { it.ruleId == DART_ANALYZE_ERROR_SHORT_NAME })
    assertTrue(manager.sarifRun.results.all { it.level == Level.ERROR })
    assertRecordedAnalysisDirectories("")
  }

  @Test
  fun `analyzes only directory once`() {
    updateQodanaConfig { it.copy(onlyDirectory = Path.of("packages/core")) }
    runAnalysis()

    assertEquals(
      listOf("packages/core/lib/main.dart", "packages/core/nested_flutter_app/lib/main.dart"),
      sarifResultPaths(),
    )
    assertRecordedAnalysisDirectories("packages/core")
  }
}
