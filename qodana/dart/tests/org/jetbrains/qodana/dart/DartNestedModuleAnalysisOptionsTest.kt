package org.jetbrains.qodana.dart

import com.intellij.testFramework.TestDataPath
import org.junit.Test
import java.nio.file.Path

@TestDataPath($$"$CONTENT_ROOT/../test-data/DartNestedModuleAnalysisOptionsTest")
internal class DartNestedModuleAnalysisOptionsTest : DartAnalyzeQodanaTestCase() {
  override val testDataBasePath: Path = Path.of(javaClass.simpleName)
  override val analyzeResponses: Map<String, RecordedDartAnalyzeResponse> = mapOf(
    "test-module" to readDartAnalyzeTestData("nested-root.json"),
    "test-module/core-lints" to readDartAnalyzeTestData("nested-core-lints.json"),
    "test-module/no-lints" to readDartAnalyzeTestData("no-diagnostics.json"),
    "test-module/submodule" to readDartAnalyzeTestData("nested-submodule.json"),
  )

  @Test
  fun `reports diagnostics from root and nested Dart modules`() {
    runAnalysis()
    assertSarifResultCodes(
      listOf(
        "test-module/core-lints/lib/await_only_futures.dart:$DART_ANALYZE_INFO_SHORT_NAME",
        "test-module/root.dart:$DART_ANALYZE_INFO_SHORT_NAME",
        "test-module/root.dart:$DART_ANALYZE_INFO_SHORT_NAME",
        "test-module/lib/included.dart:$DART_ANALYZE_INFO_SHORT_NAME",
        "test-module/submodule/submodule.dart:$DART_ANALYZE_INFO_SHORT_NAME",
        "test-module/submodule/submodule.dart:$DART_ANALYZE_INFO_SHORT_NAME",
      ),
    )
  }

  private fun assertSarifResultCodes(expected: List<String>) {
    val actual = manager.sarifRun.results.map { result ->
      val location = result.singleLocation().physicalLocation.artifactLocation.uri
      "$location:${result.ruleId}"
    }.sorted()
    assertEquals(expected.sorted(), actual)
  }

}
