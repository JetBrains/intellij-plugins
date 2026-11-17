package org.jetbrains.qodana.staticAnalysis.packageChecker

import com.intellij.codeInspection.ex.JobDescriptor
import com.intellij.codeInspection.ex.Tools
import com.intellij.packageChecker.model.RemoteConfigService
import com.intellij.packageChecker.service.PackageChecker
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext
import org.jetbrains.qodana.staticAnalysis.inspections.runner.externalTools.ExternalToolsConfigurationProvider
import org.jetbrains.qodana.staticAnalysis.vulnerabilities.QodanaVulnerabilityServiceAvailability
import org.jetbrains.qodana.staticAnalysis.vulnerabilities.skipsLookups

private const val PACKAGE_CHECKER_PLUGIN_ID = "org.jetbrains.security.package-checker"

/**
 * Caches the package checker remote configuration before the local inspections run, when a package checker inspection is enabled.
 */
internal class QodanaCachePackageCheckerRemoteConfig : ExternalToolsConfigurationProvider {
  override fun announceJobDescriptors(context: QodanaGlobalInspectionContext): Array<JobDescriptor> = emptyArray()

  override suspend fun performPreRunActivities(context: QodanaGlobalInspectionContext) {
    // the package checker inspections wait for the package check only in headless mode
    if (!PackageChecker.isHeadlessMode()) return
    val packageCheckerInspectionEnabled = context.profile.effectiveProfile.tools.any { tools ->
      tools.isEnabled && tools.isFromPackageChecker()
    }
    if (!packageCheckerInspectionEnabled) return

    if (QodanaVulnerabilityServiceAvailability.getInstance().await().skipsLookups) return
    RemoteConfigService.getInstance().cacheRemoteConfiguration()
  }

  override suspend fun performPostRunActivities(context: QodanaGlobalInspectionContext) {}
}

private fun Tools.isFromPackageChecker(): Boolean =
  tool.extension?.pluginDescriptor?.pluginId?.idString == PACKAGE_CHECKER_PLUGIN_ID
