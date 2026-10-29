package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.jetbrains.qodana.sarif.SarifUtil
import com.jetbrains.qodana.sarif.model.PropertyBag
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaBaselineSource
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaRunnerTestCase
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path

/** Tests the baseline source that the run writes to the report after it compares the report with the baseline. */
class BaselineSourceReportTest : QodanaRunnerTestCase() {
  // Any run with a baseline file fits, so all tests share the data of one.
  override val testDataBasePath: Path get() = Path.of("QodanaRunnerTest", "baseline, new results")

  @Test
  fun `a local baseline file gives the local source`() {
    runAnalysis()

    assertEquals("LOCAL", reportedBaselineSource())
  }

  @Test
  fun `a cloud baseline gives the cloud source`() {
    // The run downloaded the baseline itself, or qodana-cli reported a cloud baseline for the file.
    updateQodanaConfig { it.copy(baselineSource = QodanaBaselineSource.CLOUD_BASELINE) }

    runAnalysis()

    assertEquals("CLOUD", reportedBaselineSource())
  }

  @Test
  fun `a report that was compared with a cloud baseline gives the local source as the next baseline`() {
    // A report carries the source and not the origin, so a run that uses it as the baseline does not chain CLOUD.
    useCloudComparedReportAsBaseline()

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

  /** Makes the run use a copy of the baseline of the test data, marked like a report that was compared with a cloud baseline. */
  private fun useCloudComparedReportAsBaseline() {
    val report = SarifUtil.readReport(getTestDataPath("baseline-results.sarif.json"))
    for (run in report.runs) {
      run.properties = (run.properties ?: PropertyBag()).also { it["baselineSource"] = "CLOUD" }
    }
    val file = Path.of(project.basePath!!).resolve("baseline.sarif.json")
    Files.newBufferedWriter(file).use { SarifUtil.writeReport(it, report) }
    updateQodanaConfig { it.copy(baseline = file.toString()) }
  }
}
