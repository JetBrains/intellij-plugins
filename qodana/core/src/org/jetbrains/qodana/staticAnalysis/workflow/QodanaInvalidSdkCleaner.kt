package org.jetbrains.qodana.staticAnalysis.workflow

import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.SdkType
import com.intellij.openapi.roots.ui.configuration.projectRoot.SdkDownloadTracker
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig

private val LOG = logger<QodanaInvalidSdkCleaner>()

class QodanaInvalidSdkCleaner : QodanaWorkflowExtension {
  override suspend fun beforeProjectOpened(config: QodanaConfig) {
    val jdkTable = ProjectJdkTable.getInstance()
    val invalidSdks = jdkTable.allJdks.filter { isInvalidSdk(it) }
    if (invalidSdks.isEmpty()) return

    for (sdk in invalidSdks) {
      LOG.info("Removing invalid cached SDK '${sdk.name}' (homePath: ${sdk.homePath})")
    }

    edtWriteAction {
      for (sdk in invalidSdks) {
        jdkTable.removeJdk(sdk)
      }
    }
  }

  // Mirrors UnknownInvalidSdk.resolveInvalidSdk() which is private
  private fun isInvalidSdk(sdk: Sdk): Boolean {
    val sdkType = sdk.sdkType as? SdkType ?: return false
    if (SdkDownloadTracker.getInstance().isDownloading(sdk)) return false

    return try {
      val homePath = sdk.homePath
      homePath == null || !sdkType.isValidSdkHome(homePath)
    }
    catch (e: Exception) {
      LOG.warn("Failed to validate SDK '${sdk.name}': ${e.message}", e)
      false // match platform behavior: keep SDK if validation throws
    }
  }
}
