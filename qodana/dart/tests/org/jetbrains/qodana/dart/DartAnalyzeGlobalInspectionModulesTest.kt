package org.jetbrains.qodana.dart

import com.jetbrains.qodana.sarif.model.Level
import org.jetbrains.qodana.staticAnalysis.inspections.config.InspectScope
import org.junit.Test
import java.nio.file.Path

internal class DartAnalyzeGlobalInspectionModulesTest : DartAnalyzeProjectQodanaTestCase() {
  override val projectSourcePath: Path
    get() = testData.resolve("dart-analyze/mini-monorepo")

  override val analyzeResponses: Map<String, RecordedDartAnalyzeResponse> = mapOf(
    "apps/app" to readDartAnalyzeTestData("single-app-uncategorized.json"),
    "packages/core" to readDartAnalyzeTestData("single-core-uncategorized.json"),
    "packages/core/nested_flutter_app" to readDartAnalyzeTestData("single-nested-flutter-uncategorized.json"),
    "packages/flutter_app" to readDartAnalyzeTestData("single-flutter-uncategorized.json"),
  )

  @Test
  fun `analyzes all modules regardless of file exclusions`() {
    updateQodanaConfig {
      it.copy(
        exclude = listOf(InspectScope(DART_ANALYZE_SHORT_NAME, paths = listOf("packages/core/lib/main.dart"))),
      )
    }
    runAnalysis()

    assertEquals(
      listOf(
        "apps/app/lib/main.dart",
        "packages/core/nested_flutter_app/lib/main.dart",
        "packages/flutter_app/lib/main.dart",
      ),
      sarifResultPaths(),
    )
    val results = manager.sarifRun.results
    assertTrue(results.all { it.ruleId == DART_ANALYZE_ERROR_SHORT_NAME })
    assertTrue(results.all { it.level == Level.ERROR })
    assertRecordedAnalyzedModules(
      "apps/app",
      "packages/core",
      "packages/core/nested_flutter_app",
      "packages/flutter_app",
    )
  }

  @Test
  fun `analyzes modules intersecting only directory scopes`() {
    updateQodanaConfig { it.copy(onlyDirectory = Path.of("packages/core")) }
    runAnalysis()

    assertEquals(
      listOf("packages/core/lib/main.dart", "packages/core/nested_flutter_app/lib/main.dart"),
      sarifResultPaths(),
    )
    assertRecordedAnalyzedModules("packages/core", "packages/core/nested_flutter_app")
  }

  @Test
  fun `analyzes a module when only directory selects its source directory`() {
    updateQodanaConfig { it.copy(onlyDirectory = Path.of("packages/core/lib")) }
    runAnalysis()

    assertEquals(listOf("packages/core/lib/main.dart"), sarifResultPaths())
    assertRecordedAnalyzedModules("packages/core")
  }

  @Test
  fun `analyzes a module when only directory selects its source file`() {
    updateQodanaConfig { it.copy(onlyDirectory = Path.of("packages/core/lib/main.dart")) }
    runAnalysis()

    assertEquals(listOf("packages/core/lib/main.dart"), sarifResultPaths())
    assertRecordedAnalyzedModules("packages/core")
  }

  @Test
  fun `does not analyze parent modules when only directory selects a nested Flutter module`() {
    updateQodanaConfig {
      it.copy(
        onlyDirectory = Path.of("packages/core/nested_flutter_app"),
      )
    }
    runAnalysis()

    assertEquals(listOf("packages/core/nested_flutter_app/lib/main.dart"), sarifResultPaths())
    assertRecordedAnalyzedModules("packages/core/nested_flutter_app")
  }
}
