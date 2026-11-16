package org.jetbrains.qodana.staticAnalysis.packageChecker

import com.intellij.codeInspection.ex.JobDescriptor
import com.intellij.openapi.diagnostic.logger
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext
import org.jetbrains.qodana.staticAnalysis.inspections.runner.externalTools.ExternalToolsConfigurationProvider
import org.jetbrains.qodana.staticAnalysis.vulnerabilities.QodanaVulnerabilityServiceAvailability
import org.jetbrains.qodana.staticAnalysis.vulnerabilities.VulnerabilityServiceState
import org.jetbrains.qodana.staticAnalysis.vulnerabilities.enabledPackageCheckerInspections
import org.jetbrains.qodana.staticAnalysis.vulnerabilities.reportProjectLevelProblem
import org.jetbrains.qodana.staticAnalysis.vulnerabilities.reportVulnerabilityServiceState

private val LOG = logger<PackageCheckerAvailabilityReporter>()

/**
 * Tells the user that the package-checker inspections did not check the packages.
 *
 * When the service is unreachable, each enabled package-checker inspection gets one problem without a file.
 * So the profile, the baseline and the inspection filters treat the problem as a result of that inspection.
 */
@ApiStatus.Internal
class PackageCheckerAvailabilityReporter : ExternalToolsConfigurationProvider {
  override fun announceJobDescriptors(context: QodanaGlobalInspectionContext): Array<JobDescriptor> = emptyArray()

  override suspend fun performPreRunActivities(context: QodanaGlobalInspectionContext) {
    val inspectionIds = context.profile.enabledPackageCheckerInspections()
    if (inspectionIds.isEmpty()) return

    val state = QodanaVulnerabilityServiceAvailability.getInstance().await()
    if (state == VulnerabilityServiceState.NotRequested) {
      LOG.warn("Package-checker inspections $inspectionIds are enabled, but the vulnerability service check did not start")
    }
    reportVulnerabilityServiceState(context.project, state) { message ->
      inspectionIds.forEach { context.reportProjectLevelProblem(it, message) }
    }
  }

  override suspend fun performPostRunActivities(context: QodanaGlobalInspectionContext) {}
}
