package org.jetbrains.qodana.staticAnalysis.packageChecker

import com.intellij.testFramework.TestDataPath
import com.jetbrains.qodana.sarif.SarifUtil
import com.jetbrains.qodana.sarif.model.Notification
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.staticAnalysis.inspections.runner.FULL_SARIF_REPORT_NAME
import org.jetbrains.qodana.staticAnalysis.sarif.notifications.QodanaConfigureNotificationCollector
import org.jetbrains.qodana.staticAnalysis.sarif.qodanaKind
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaRunnerTestCase
import org.jetbrains.qodana.staticAnalysis.vulnerabilities.QodanaVulnerabilityServiceAvailability
import org.jetbrains.qodana.staticAnalysis.vulnerabilities.VULNERABILITY_SERVICE_DISABLED_NOTIFICATION
import org.jetbrains.qodana.staticAnalysis.vulnerabilities.VulnerabilityServiceState
import org.junit.Test
import java.nio.file.Path
import kotlin.io.path.div

@TestDataPath($$"$CONTENT_ROOT/test-data/PackageCheckerAvailabilityReporterTest")
class PackageCheckerAvailabilityReporterTest : QodanaRunnerTestCase() {
  override val testDataBasePath: Path
    get() = Path.of(javaClass.simpleName, if (name.contains("no package-checker")) "noPackageChecker" else "packageChecker")

  override fun tearDown() {
    try {
      QodanaVulnerabilityServiceAvailability.getInstance().setStateForTest(null)
    }
    catch (e: Throwable) {
      addSuppressedException(e)
    }
    finally {
      super.tearDown()
    }
  }

  @Test
  fun `unreachable service gives one problem per enabled package-checker inspection`() {
    QodanaVulnerabilityServiceAvailability.getInstance().setStateForTest(VulnerabilityServiceState.Unreachable("connect timed out"))

    runAnalysis()

    val results = manager.sarifRun.results.filter { it.message.text.contains("connect timed out") }
    assertThat(results.map { it.ruleId }).containsExactlyInAnyOrder("VulnerableLibrariesLocal", "MaliciousLibrariesLocal")
    assertThat(results.flatMap { it.locations.orEmpty() }).isEmpty()
    assertThat(vulnerabilityNotifications()).isEmpty()
  }

  @Test
  fun `air-gapped mode gives one notification and no problem`() {
    QodanaVulnerabilityServiceAvailability.getInstance().setStateForTest(VulnerabilityServiceState.DisabledByAirGapped)

    runAnalysis()

    assertThat(manager.sarifRun.results).isEmpty()
    assertThat(vulnerabilityNotifications()).hasSize(1)
  }

  @Test
  fun `no package-checker inspection gives no report`() {
    QodanaVulnerabilityServiceAvailability.getInstance().setStateForTest(VulnerabilityServiceState.Unreachable("connect timed out"))

    runAnalysis()

    assertThat(manager.sarifRun.results.filter { it.message.text.contains("connect timed out") }).isEmpty()
    assertThat(vulnerabilityNotifications()).isEmpty()
  }

  @Test
  fun `available service gives no report`() {
    QodanaVulnerabilityServiceAvailability.getInstance().setStateForTest(VulnerabilityServiceState.Available)

    runAnalysis()

    assertThat(vulnerabilityNotifications()).isEmpty()
  }

  override fun setUp() {
    super.setUp()
    runBeforeAnalysis { config, project -> QodanaConfigureNotificationCollector().configureForQodana(config, project) }
  }

  private fun vulnerabilityNotifications(): List<Notification> {
    return SarifUtil.readReport(qodanaConfig.outPath / FULL_SARIF_REPORT_NAME)
      .runs.orEmpty()
      .flatMap { it.invocations.orEmpty() }
      .flatMap { it.toolExecutionNotifications.orEmpty() }
      .filter { it.qodanaKind == VULNERABILITY_SERVICE_DISABLED_NOTIFICATION }
  }
}
