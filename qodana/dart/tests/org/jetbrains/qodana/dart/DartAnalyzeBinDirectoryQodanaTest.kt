package org.jetbrains.qodana.dart

import org.junit.Test
import java.nio.file.Path

internal class DartAnalyzeBinDirectoryQodanaTest : DartAnalyzeProjectQodanaTestCase() {
  override val testDataBasePath: Path = Path.of(javaClass.simpleName)

  override val projectSourcePath: Path
    get() = testData.resolve("dart-analyze/bin-package")

  override val analyzeResponses: Map<String, RecordedDartAnalyzeResponse>
    get() = mapOf(
      "" to readDartAnalyzeTestData("bin-package.json"),
    )

  @Test
  fun `reports a Dart diagnostic from the bin directory of a Dart package`() {
    runAnalysis()

    assertEquals(listOf("bin/main.dart", "lib/lib.dart"), sarifResultPaths())
  }
}