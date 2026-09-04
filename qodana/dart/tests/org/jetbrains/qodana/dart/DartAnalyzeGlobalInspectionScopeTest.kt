package org.jetbrains.qodana.dart

import org.jetbrains.qodana.staticAnalysis.inspections.config.InspectScope
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.junit.Test

internal class DartAnalyzeGlobalInspectionScopeTest : DartAnalyzeProjectQodanaTestCase() {
  override val analyzeResponses: Map<String, RecordedDartAnalyzeResponse> = mapOf(
    "test-module" to readDartAnalyzeTestData("two-uncategorized.json"),
  )

  @Test
  fun `reports diagnostics only from included files`() {
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig.named("qodana.single:$DART_PROJECT_SETUP_SHORT_NAME"),
        include = listOf(InspectScope(DART_ANALYZE_UNCATEGORIZED_SHORT_NAME, paths = listOf("test-module/lib/included.dart"))),
      )
    }
    runAnalysis()

    assertEquals(listOf("test-module/lib/included.dart"), sarifResultPaths())
  }

  @Test
  fun `includes an inspection everywhere when no paths are configured`() {
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig.named("qodana.single:$DART_PROJECT_SETUP_SHORT_NAME"),
        include = listOf(InspectScope(DART_ANALYZE_UNCATEGORIZED_SHORT_NAME)),
      )
    }
    runAnalysis()

    assertEquals(
      listOf("test-module/lib/included.dart", "test-module/lib/main.dart"),
      sarifResultPaths(),
    )
  }

  @Test
  fun `keeps an enabled inspection active outside included paths`() {
    updateQodanaConfig {
      it.copy(
        profile = dartAnalyzeProfile(),
        include = listOf(InspectScope(DART_ANALYZE_UNCATEGORIZED_SHORT_NAME, paths = listOf("test-module/lib/included.dart"))),
      )
    }
    runAnalysis()

    assertEquals(
      listOf("test-module/lib/included.dart", "test-module/lib/main.dart"),
      sarifResultPaths(),
    )
  }

  @Test
  fun `excludes an inspection only from configured files`() {
    updateQodanaConfig {
      it.copy(
        profile = dartAnalyzeProfile(),
        exclude = listOf(InspectScope(DART_ANALYZE_UNCATEGORIZED_SHORT_NAME, paths = listOf("test-module/lib/main.dart"))),
      )
    }
    runAnalysis()

    assertEquals(listOf("test-module/lib/included.dart"), sarifResultPaths())
  }

  @Test
  fun `excludes an inspection everywhere when no paths are configured`() {
    updateQodanaConfig {
      it.copy(
        profile = dartAnalyzeProfile(),
        exclude = listOf(InspectScope(DART_ANALYZE_UNCATEGORIZED_SHORT_NAME)),
      )
    }
    runAnalysis()

    assertEmptySarifResults()
  }

  @Test
  fun `file include overrides directory exclude`() {
    updateQodanaConfig {
      it.copy(
        profile = QodanaProfileConfig.named("qodana.single:$DART_PROJECT_SETUP_SHORT_NAME"),
        include = listOf(InspectScope(DART_ANALYZE_UNCATEGORIZED_SHORT_NAME, paths = listOf("test-module/lib/included.dart"))),
        exclude = listOf(InspectScope(DART_ANALYZE_UNCATEGORIZED_SHORT_NAME, paths = listOf("test-module/lib"))),
      )
    }
    runAnalysis()

    assertEquals(listOf("test-module/lib/included.dart"), sarifResultPaths())
  }
}
