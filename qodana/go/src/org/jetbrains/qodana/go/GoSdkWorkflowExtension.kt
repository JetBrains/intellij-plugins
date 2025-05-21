package org.jetbrains.qodana.go

import com.goide.sdk.GoSdkService
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.project.Project
import com.intellij.util.PlatformUtils
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowExtension

internal class GoSdkWorkflowExtension : QodanaWorkflowExtension {
  override suspend fun beforeProjectClose(project: Project) {
    val toRestore = project.getUserData(GoSdkService.CLI_ORIGINAL_SDK) ?: return
    val service = project.serviceAsync<GoSdkService>()

    edtWriteAction { service.setSdk(toRestore, false) }
  }

  override suspend fun afterConfiguration(config: QodanaConfig, project: Project) {
    if (!PlatformUtils.isGoIde()) return
    val service = project.serviceAsync<GoSdkService>()
    val sdk = service.getSdk(null)
    if (sdk.isValid) return

    throw QodanaException("Go SDK is not found")
  }
}
