package org.jetbrains.qodana.staticAnalysis.sarif

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.jetbrains.qodana.sarif.model.Run
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.getBaselineReport

data class DependencyLicenseAuditResult(
  val prohibited: Set<String>,
  val unknown: Set<String>,
)

interface DependencyLicenseAuditProvider {
  suspend fun computeAudit(project: Project, config: QodanaConfig): DependencyLicenseAuditResult?

  companion object {
    val EP_NAME: ExtensionPointName<DependencyLicenseAuditProvider> =
      ExtensionPointName.create("org.intellij.qodana.dependencyLicenseAuditProvider")
  }
}

/**
 * Evaluates the dependency license audit and stores its outcome into the SARIF run for
 * [org.jetbrains.qodana.staticAnalysis.inspections.runner.setInvocationExitStatus], which has no access to the
 * project or the dependency-analysis service. Two run properties are written:
 *  - [dependencyLicenseAudit]: the new-only fail decision (booleans);
 *  - [dependencyLicenseAuditDetails]: the full current offending dependency sets, so that a subsequent run
 *    using this report as a baseline can compute the newly introduced issues.
 *
 * In incremental mode (a baseline is configured) the fail flags reflect only dependencies whose prohibited /
 * unknown license is absent from the baseline; in full mode they reflect all offending dependencies.
 */
internal class DependencyLicenseAuditContributor : SarifReportContributor {
  override fun contribute(run: Run, project: Project, config: QodanaConfig) {
    val conditions = config.failureConditions.dependencyLicenses
    if (!conditions.failOnProhibited && !conditions.failOnUnknown) return

    val provider = DependencyLicenseAuditProvider.EP_NAME.extensionList.firstOrNull() ?: return

    runBlockingCancellable {
      val current = provider.computeAudit(project, config) ?: return@runBlockingCancellable

      // Persist the full current state so the next run (using this report as a baseline) can diff against it.
      run.dependencyLicenseAuditDetails = mapOf(
        DEPENDENCY_AUDIT_PROHIBITED to current.prohibited.sorted(),
        DEPENDENCY_AUDIT_UNKNOWN to current.unknown.sorted(),
      )

      // In incremental mode consider only dependencies not already present in the baseline.
      val baselineDetails = config.baseline
        ?.let { getBaselineReport(config, includeResults = false) }
        ?.runs?.firstOrNull()?.dependencyLicenseAuditDetails

      run.dependencyLicenseAudit = newDependencyAuditFlags(current, baselineDetails)
    }
  }
}

/**
 * Computes the new-only fail flags for the dependency license audit: in full mode (no [baselineDetails]) every
 * offending dependency counts; in incremental mode only dependencies absent from the baseline do. Keyed by
 * [DEPENDENCY_AUDIT_PROHIBITED] / [DEPENDENCY_AUDIT_UNKNOWN].
 */
internal fun newDependencyAuditFlags(
  current: DependencyLicenseAuditResult,
  baselineDetails: Map<String, List<String>>?,
): Map<String, Boolean> {
  val newProhibited = baselineDetails?.let { current.prohibited - it[DEPENDENCY_AUDIT_PROHIBITED].orEmpty().toSet() }
                      ?: current.prohibited
  val newUnknown = baselineDetails?.let { current.unknown - it[DEPENDENCY_AUDIT_UNKNOWN].orEmpty().toSet() }
                   ?: current.unknown
  return mapOf(
    DEPENDENCY_AUDIT_PROHIBITED to newProhibited.isNotEmpty(),
    DEPENDENCY_AUDIT_UNKNOWN to newUnknown.isNotEmpty(),
  )
}
