package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.openapi.progress.ProcessCanceledException
import com.jetbrains.qodana.sarif.SarifUtil
import kotlinx.coroutines.runBlocking
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaBaseline
import org.jetbrains.qodana.staticAnalysis.sarif.getOrAssignProperties
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaRunnerTestCase
import org.junit.Test
import org.junit.jupiter.api.assertThrows
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
  fun `a report that was compared with a cloud baseline gives the local source as the next baseline`() {
    // A report carries the source and not the origin, so a run that uses it as the baseline does not chain CLOUD.
    useCloudComparedReportAsBaseline()

    runAnalysis()

    assertEquals("LOCAL", reportedBaselineSource())
  }

  @Test
  fun `a run without a baseline gives the none source`() {
    updateQodanaConfig { it.copy(baseline = QodanaBaseline.None) }

    runAnalysis()

    assertEquals("NONE", reportedBaselineSource())
  }

  @Test
  fun `a missing baseline file gives the none source`() {
    updateQodanaConfig { it.copy(baseline = QodanaBaseline.Local(Path.of("missing.sarif.json"))) }

    runAnalysis()

    assertEquals("NONE", reportedBaselineSource())
  }

  @Test
  fun `a cloud baseline downloads once, gives the cloud source and is deleted after the comparison`() {
    val cloudFile = Path.of(project.basePath!!).resolve("cloud-baseline.sarif.json")
    var downloads = 0
    val cloud = QodanaBaseline.Cloud {
      downloads++
      Files.copy(getTestDataPath("baseline-results.sarif.json"), cloudFile)
    }
    updateQodanaConfig { it.copy(baseline = cloud) }

    runAnalysis()

    assertEquals("CLOUD", reportedBaselineSource())
    assertEquals(1, downloads)
    assertFalse(Files.exists(cloudFile))
  }

  @Test
  fun `a failed analysis does not download the cloud baseline`() {
    var downloads = 0
    val cloud = QodanaBaseline.Cloud {
      downloads++
      null
    }
    updateQodanaConfig { it.copy(baseline = cloud, stopThreshold = 1) }

    assertThrows<ProcessCanceledException> { runAnalysis() }

    assertEquals(0, downloads)
  }

  @Test
  fun `reading the rules of the baseline does not download the cloud baseline`() {
    var downloads = 0
    val cloud = QodanaBaseline.Cloud {
      downloads++
      null
    }
    updateQodanaConfig { it.copy(baseline = cloud) }

    // The rule names of the summary and the license audit read the baseline without results.
    assertNull(runBlocking { getBaselineReport(qodanaConfig, includeResults = false) })

    assertEquals(0, downloads)
  }

  @Test
  fun `a cloud baseline without a file gives the none source`() {
    updateQodanaConfig { it.copy(baseline = QodanaBaseline.Cloud { null }) }

    runAnalysis()

    assertEquals("NONE", reportedBaselineSource())
  }

  private fun reportedBaselineSource(): Any? = manager.sarifRun.properties?.get("baselineSource")

  /** Makes the run use a copy of the baseline of the test data, marked like a report that was compared with a cloud baseline. */
  private fun useCloudComparedReportAsBaseline() {
    val report = SarifUtil.readReport(getTestDataPath("baseline-results.sarif.json"))
    for (run in report.runs) {
      run.getOrAssignProperties()["baselineSource"] = "CLOUD"
    }
    val file = Path.of(project.basePath!!).resolve("baseline.sarif.json")
    Files.newBufferedWriter(file).use { SarifUtil.writeReport(it, report) }
    updateQodanaConfig { it.copy(baseline = QodanaBaseline.Local(file)) }
  }
}
