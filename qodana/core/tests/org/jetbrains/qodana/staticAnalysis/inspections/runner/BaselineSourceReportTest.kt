package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.jetbrains.qodana.sarif.SarifUtil
import com.jetbrains.qodana.sarif.model.PropertyBag
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaBaselineSource
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaRunnerTestCase
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path

/** Tests the baseline source that qodana-sarif writes to the report when it compares the report with the baseline. */
class BaselineSourceReportTest : QodanaRunnerTestCase() {
  // Any run with a baseline file fits, so all tests share the data of one.
  override val testDataBasePath: Path get() = Path.of("QodanaRunnerTest", "baseline, new results")

  @Test
  fun `a baseline file without the origin gives the local source`() {
    runAnalysis()

    assertEquals("LOCAL", reportedBaselineSource())
  }

  @Test
  fun `a baseline file with the cloud origin gives the cloud source`() {
    useBaselineWith("baselineOrigin" to "CLOUD")

    runAnalysis()

    assertEquals("CLOUD", reportedBaselineSource())
  }

  @Test
  fun `a baseline file of qodana-cli without the origin gives the cloud source`() {
    updateQodanaConfig { it.copy(baselineSource = QodanaBaselineSource.CLOUD_BASELINE) }

    runAnalysis()

    assertEquals("CLOUD", reportedBaselineSource())
  }

  @Test
  fun `a report that was compared with a cloud baseline gives the local source as the next baseline`() {
    // A report carries the source and not the origin, so a run that uses it as the baseline does not chain CLOUD.
    useBaselineWith("baselineSource" to "CLOUD")

    runAnalysis()

    assertEquals("LOCAL", reportedBaselineSource())
  }

  @Test
  fun `a run without a baseline gives no source`() {
    updateQodanaConfig { it.copy(baseline = null) }

    runAnalysis()

    assertNull(reportedBaselineSource())
  }

  private fun reportedBaselineSource(): Any? = manager.sarifRun.properties?.get("baselineSource")

  /** Makes the run use a copy of the baseline of the test data, with the run property [property]. */
  private fun useBaselineWith(property: Pair<String, String>) {
    val report = SarifUtil.readReport(getTestDataPath("baseline-results.sarif.json"))
    for (run in report.runs) {
      run.properties = (run.properties ?: PropertyBag()).also { it[property.first] = property.second }
    }
    val file = Path.of(project.basePath!!).resolve("baseline.sarif.json")
    Files.newBufferedWriter(file).use { SarifUtil.writeReport(it, report) }
    updateQodanaConfig { it.copy(baseline = file.toString()) }
  }
}
