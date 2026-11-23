package org.jetbrains.qodana.registry

import com.intellij.openapi.util.registry.Registry
import org.jetbrains.annotations.VisibleForTesting
import java.util.MissingResourceException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object QodanaRegistry {
  @VisibleForTesting
  const val CLOUD_INTEGRATION_ENABLE_KEY: String = "qd.cloud.integration.enabled"
  @VisibleForTesting
  const val SCOPE_EXTENDING_ENABLE_KEY: String = "qd.scope.extending.enabled"
  @VisibleForTesting
  const val PACKAGE_CHECKER_CANCEL_ON_FAILURE_KEY = "qd.package-checker.cancel.on.failure"
  @VisibleForTesting
  const val VULNERABILITY_SERVICE_PROBE_TIMEOUT_KEY: String = "qd.vulnerability.service.probe.timeout.seconds"
  @VisibleForTesting
  const val REDUCED_SCOPE_INCLUDE_REMOVED_FILES_ENABLE_KEY: String = "qd.reduced.scope.include.removed.files.enabled"
  @VisibleForTesting
  const val INSPECTION_VETO_ENABLE_KEY: String = "qd.inspection.veto.enabled"
  @VisibleForTesting
  const val REVERSE_SCOPED_INCLUDE_UNCHANGED_ENABLE_KEY: String = "qd.reverse.scoped.include.unchanged.enabled"
  @VisibleForTesting
  const val XML_SEMANTIC_IDENTITY_ENABLE_KEY: String = "qd.xml.semantic.identity.enabled"

  val isQodanaLicenseAgreementCallbackEnabled: Boolean
    get() = Registry.`is`("qd.license.agreement.callback.enabled", true)

  val isForceLocalRunEnabled: Boolean
    get() = Registry.`is`("qd.force.local.run.enabled", false)

  val isQodanaCloudIntegrationEnabled: Boolean
    get() = Registry.`is`(CLOUD_INTEGRATION_ENABLE_KEY, true)

  val isScopeExtendingEnabled: Boolean
    get() = Registry.`is`(SCOPE_EXTENDING_ENABLE_KEY, false)

  val isPackageCheckerCancelOnFailureEnabled: Boolean
    get() = Registry.`is`(PACKAGE_CHECKER_CANCEL_ON_FAILURE_KEY, true)

  val vulnerabilityServiceProbeTimeout: Duration
    get() = Registry.intValue(VULNERABILITY_SERVICE_PROBE_TIMEOUT_KEY, 5).seconds

  val isReducedScopeIncludeRemovedFilesEnabled: Boolean
    get() = Registry.`is`(REDUCED_SCOPE_INCLUDE_REMOVED_FILES_ENABLE_KEY, false)

  val isInspectionVetoEnabled: Boolean
    get() = Registry.`is`(INSPECTION_VETO_ENABLE_KEY, false)

  val isReverseScopedIncludeUnchangedEnabled: Boolean
    get() = Registry.`is`(REVERSE_SCOPED_INCLUDE_UNCHANGED_ENABLE_KEY, false)

  /** The XML semantic identity of a problem in a plain XML file replaces its location in the baseline fingerprints. */
  val isXmlSemanticIdentityEnabled: Boolean
    get() = Registry.`is`(XML_SEMANTIC_IDENTITY_ENABLE_KEY, false)

  val openSarifInEditor: Boolean
    get() = Registry.`is`("qd.open.sarif.in.editor", false)

  val openCoverageReportEnabled: Boolean
    get() = Registry.`is`("qd.open.coverage.enabled", true)

  val openCoverageSmartFilteringEnabled: Boolean
    get() = Registry.`is`("qd.open.coverage.smart.filter.enabled", true)

  val openCoveragePackageLength: Int
    get() = Registry.intValue("qd.open.coverage.common.package.length", 3)

  val webUiSourcesPath: String
    get() = Registry.stringValue("qd.web.ui.sources.url")

  val vcsRevisionPageSize: Int
    get() = Registry.intValue("qd.vcs.revision.page.size", 100)

  val vcsRevisionMaxPages: Int
    get() = Registry.intValue("qd.vcs.revision.max.pages", 10)

  val cloudDownloadRetriesCount: Int
    get() = Registry.intValue("qd.cloud.download.retries.count", 2)

  val useAllDistributionForInspectionKtsDependencies: Boolean
    get() = Registry.`is`("qd.inspection.kts.all.distribution.for.dependencies", false)

  val limitedInspectionKtsDependencies: Boolean
    get() = Registry.`is`("qd.inspection.kts.limited.dependencies", true)

  object Cloud {
    val website: String
      get() = fromRegistryOrDefaultIfEmpty("qd.cloud.website", "https://qodana.cloud")
  }
}

private fun fromRegistryOrDefaultIfEmpty(key: String, default: String): String {
  try {
    return Registry.stringValue(key).ifEmpty { default }
  } catch (_: MissingResourceException) {
    return default
  }
}
