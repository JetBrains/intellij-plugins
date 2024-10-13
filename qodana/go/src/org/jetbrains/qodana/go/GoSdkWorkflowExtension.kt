package org.jetbrains.qodana.go

import com.goide.sdk.GoSdkService
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.project.Project
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowExtension

internal class GoSdkWorkflowExtension : QodanaWorkflowExtension {

  override suspend fun beforeProjectClose(project: Project) {
    val toRestore = project.getUserData(GoSdkService.CLI_ORIGINAL_SDK) ?: return
    val service = project.serviceAsync<GoSdkService>()

    writeAction { service.setSdk(toRestore, false) }
  }
}
