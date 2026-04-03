package org.jetbrains.qodana.jvm.java

import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.ControlFlowException
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.projectRoots.JavaSdkType
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.SdkType
import com.intellij.openapi.roots.ui.configuration.projectRoot.SdkDownloadTracker
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowExtension

private val LOG = logger<QodanaJavaConfigurator>()

class QodanaJavaConfigurator : QodanaWorkflowExtension {
  override suspend fun beforeProjectOpened(config: QodanaConfig) {
    removeInvalidJdks()
    service<QodanaConfigJdkService>().configureJdk(config)
  }

  private suspend fun removeInvalidJdks() {
    val jdkTable = ProjectJdkTable.getInstance()
    val invalidSdks = jdkTable.allJdks
      .filter { isInvalidSdk(it) }
      .filter { it.sdkType is JavaSdkType }
    if (invalidSdks.isEmpty()) return

    edtWriteAction {
      for (sdk in invalidSdks) {
        LOG.info("Removing invalid cached SDK '${sdk.name}' (homePath: ${sdk.homePath})")
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
      if (e is ControlFlowException) throw e
      LOG.warn("Failed to validate SDK '${sdk.name}': ${e.message}", e)
      false // match platform behavior: keep SDK if validation throws
    }
  }
}
