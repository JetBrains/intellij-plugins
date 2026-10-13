package org.jetbrains.qodana.inspectionKts.mcp.impl

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.supervisorScope
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.OutputFormat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertContains
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.seconds

@TestApplication
internal class DynamicInspectionRunnerParallelTest {
  @Test
  fun `timeout cancels the active analysis`(@TempDir tempDirectory: Path): Unit = timeoutRunBlocking {
    val analysisStarted = CompletableDeferred<Unit>()
    val analysisCancelled = CompletableDeferred<Unit>()
    val runner = DynamicInspectionRunnerParallel.createForTest(this) { _, _ ->
      analysisStarted.complete(Unit)
      try {
        awaitCancellation()
      }
      finally {
        analysisCancelled.complete(Unit)
      }
    }
    val outputDirectory = tempDirectory.resolve("output")
    val config = QodanaConfig.fromYaml(
      projectPath = tempDirectory,
      outPath = outputDirectory,
      resultsStorage = outputDirectory.resolve("results"),
      outputFormat = OutputFormat.SARIF_AND_PROJECT_STRUCTURE,
    )

    supervisorScope {
      val result = async {
        assertFailsWith<InspectionAnalysisTimeoutException> {
          runner.runSingle(TestInspection(), config, outputDirectory, 1.seconds)
        }
      }
      analysisStarted.await()

      val failure = result.await()
      analysisCancelled.await()
      assertContains(failure.message.orEmpty(), "Time limit")
    }
  }

  private class TestInspection : LocalInspectionTool()
}
