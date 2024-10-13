package org.jetbrains.qodana.registry

import com.intellij.openapi.util.registry.Registry
import org.jetbrains.annotations.VisibleForTesting
import java.util.*

object QodanaRegistry {
  @VisibleForTesting
  const val CLOUD_INTEGRATION_ENABLE_KEY = "qd.cloud.integration.enabled"

  val isQodanaLicenseAgreementCallbackEnabled: Boolean
    get() = Registry.`is`("qd.license.agreement.callback.enabled", true)

  val isForceLocalRunEnabled: Boolean
    get() = Registry.`is`("qd.force.local.run.enabled", false)

  val isForceSetupCIEnabled: Boolean
    get() = Registry.`is`("qd.force.setup.ci.enabled", false)

  val isQodanaCloudIntegrationEnabled: Boolean
    get() = Registry.`is`(CLOUD_INTEGRATION_ENABLE_KEY, true)

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

  val cloudRequestRetriesCount: Int
    get() = Registry.intValue("qd.cloud.request.retries.count", 2)

  val cloudDownloadRetriesCount: Int
    get() = Registry.intValue("qd.cloud.download.retries.count", 2)

  val useAllDistributionForInspectionKtsDependencies: Boolean
    get() = Registry.`is`("qd.inspection.kts.all.distribution.for.dependencies", false)

  val limitedInspectionKtsDependencies: Boolean
    get() = Registry.`is`("qd.inspection.kts.limited.dependencies", true)

  object Cloud {
    val website: String
      get() = fromRegistryOrDefaultIfEmpty("qd.cloud.website", "https://qodana.cloud")

    val cloudApi: String
      get() = fromRegistryOrDefaultIfEmpty("qd.cloud.api", "https://api.qodana.cloud")

    val jbaOAuthUrl: String
      get() = fromRegistryOrDefaultIfEmpty("qd.cloud.jba.oauth.url","https://oauth.account.jetbrains.com/oauth2/auth")

    val jbaAuthReferrer: String
      get() = fromRegistryOrDefaultIfEmpty("qd.cloud.jba.auth.referrer", "https://account.jetbrains.com/")

    val jbaClientId: String
      get() = fromRegistryOrDefaultIfEmpty("qd.cloud.jba.auth.client.id", "qodana")
  }
}

private fun fromRegistryOrDefaultIfEmpty(key: String, default: String): String {
  try {
    return Registry.stringValue(key).ifEmpty { default }
  }
  catch(_ : MissingResourceException) {
    return default
  }
}
