package org.jetbrains.qodana.staticAnalysis.sarif

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.jetbrains.qodana.sarif.model.Run
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig

data class DependencyLicenseAuditResult(
  val hasProhibited: Boolean,
  val hasUnknown: Boolean,
)

interface DependencyLicenseAuditProvider {
  suspend fun computeAudit(project: Project, config: QodanaConfig): DependencyLicenseAuditResult?

  companion object {
    val EP_NAME: ExtensionPointName<DependencyLicenseAuditProvider> =
      ExtensionPointName.create("org.intellij.qodana.dependencyLicenseAuditProvider")
  }
}

/**
 * Writes the dependency license audit presence flags (see [dependencyLicenseAudit]) into the SARIF
 * run so that [org.jetbrains.qodana.staticAnalysis.inspections.runner.setInvocationExitStatus] can
 * evaluate the dependency failure conditions, which have no access to the project or the
 * dependency-analysis service.
 */
internal class DependencyLicenseAuditContributor : SarifReportContributor {
  override fun contribute(run: Run, project: Project, config: QodanaConfig) {
    val conditions = config.failureConditions.dependencyLicenses
    if (!conditions.failOnProhibited && !conditions.failOnUnknown) return

    val provider = DependencyLicenseAuditProvider.EP_NAME.extensionList.firstOrNull() ?: return
    val result = runBlockingCancellable { provider.computeAudit(project, config) } ?: return

    run.dependencyLicenseAudit = mapOf(
      DEPENDENCY_AUDIT_PROHIBITED to result.hasProhibited,
      DEPENDENCY_AUDIT_UNKNOWN to result.hasUnknown,
    )
  }
}
