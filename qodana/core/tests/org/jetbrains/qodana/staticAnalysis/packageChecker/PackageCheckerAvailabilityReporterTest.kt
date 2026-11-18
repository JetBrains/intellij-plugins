package org.jetbrains.qodana.staticAnalysis.packageChecker

import com.intellij.openapi.util.registry.Registry
import com.intellij.testFramework.TestDataPath
import com.jetbrains.qodana.sarif.SarifUtil
import com.jetbrains.qodana.sarif.model.Notification
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.registry.QodanaRegistry
import org.jetbrains.qodana.staticAnalysis.inspections.runner.FULL_SARIF_REPORT_NAME
import org.jetbrains.qodana.staticAnalysis.inspections.runner.PACKAGE_CHECKER_EXIT_CODE
import org.jetbrains.qodana.staticAnalysis.inspections.runner.RUNTIME_ERRORS_EXIT_CODE
import org.jetbrains.qodana.staticAnalysis.sarif.notifications.QodanaConfigureNotificationCollector
import org.jetbrains.qodana.staticAnalysis.sarif.notifications.ToolErrorInspectListener
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
  fun `unreachable service fails the run`() {
    QodanaVulnerabilityServiceAvailability.getInstance().setStateForTest(VulnerabilityServiceState.Unreachable("connect timed out"))

    runAnalysis()

    assertSarifExitCode(PACKAGE_CHECKER_EXIT_CODE)
    assertThat(manager.sarifRun.invocations.single().exitCodeDescription).contains("connect timed out")
  }

  @Test
  fun `unreachable service with cancellation off gives a tool error and does not fail the run`() {
    Registry.get(QodanaRegistry.PACKAGE_CHECKER_CANCEL_ON_FAILURE_KEY).setValue(false, testRootDisposable)
    QodanaVulnerabilityServiceAvailability.getInstance().setStateForTest(VulnerabilityServiceState.Unreachable("connect timed out"))

    runAnalysis()

    assertSarifExitCode(0)
    assertThat(toolErrorNotifications().map { it.message.text }).singleElement().asString().contains("connect timed out")
  }

  @Test
  fun `unreachable service with cancellation off fails the run with failOnErrorNotification`() {
    Registry.get(QodanaRegistry.PACKAGE_CHECKER_CANCEL_ON_FAILURE_KEY).setValue(false, testRootDisposable)
    updateQodanaConfig { it.copy(failOnErrorNotification = true) }
    QodanaVulnerabilityServiceAvailability.getInstance().setStateForTest(VulnerabilityServiceState.Unreachable("connect timed out"))

    runAnalysis()

    assertSarifExitCode(RUNTIME_ERRORS_EXIT_CODE)
    assertThat(toolErrorNotifications()).hasSize(1)
  }

  @Test
  fun `air-gapped mode gives one notification and no problem`() {
    QodanaVulnerabilityServiceAvailability.getInstance().setStateForTest(VulnerabilityServiceState.DisabledByAirGapped)

    runAnalysis()

    assertThat(manager.sarifRun.results).isEmpty()
    assertThat(vulnerabilityNotifications()).hasSize(1)
  }

  @Test
  fun `no package-checker inspection gives no report and does not fail the run`() {
    QodanaVulnerabilityServiceAvailability.getInstance().setStateForTest(VulnerabilityServiceState.Unreachable("connect timed out"))

    runAnalysis()

    assertSarifExitCode(0)
    assertThat(vulnerabilityNotifications()).isEmpty()
    assertThat(toolErrorNotifications()).isEmpty()
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

  private fun vulnerabilityNotifications(): List<Notification> = notifications(VULNERABILITY_SERVICE_DISABLED_NOTIFICATION)

  private fun toolErrorNotifications(): List<Notification> = notifications(ToolErrorInspectListener.TOOL_ERROR_NOTIFICATION)

  private fun notifications(kind: String): List<Notification> {
    return SarifUtil.readReport(qodanaConfig.outPath / FULL_SARIF_REPORT_NAME)
      .runs.orEmpty()
      .flatMap { it.invocations.orEmpty() }
      .flatMap { it.toolExecutionNotifications.orEmpty() }
      .filter { it.qodanaKind == kind }
  }
}
