package org.jetbrains.qodana.extensions.ci

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

interface AzureCIConfigHandler {
  companion object {
    private val EP_NAME: ExtensionPointName<AzureCIConfigHandler> = ExtensionPointName.create("org.intellij.qodana.azureCiConfigUpdateHandler")

    suspend fun insertStepToAzurePipelinesBuild(project: Project, initialText: String, taskToAddText: String): String {
      for (e in EP_NAME.extensionList) {
        return e.insertStepToAzurePipelinesBuild(project, initialText, taskToAddText) ?: continue
      }
      return initialText
    }

    suspend fun isQodanaTaskPresent(project: Project, virtualFile: VirtualFile): Boolean {
      for (e in EP_NAME.extensionList) {
        return e.isQodanaTaskPresent(project, virtualFile)
      }
      return false
    }
  }

  suspend fun insertStepToAzurePipelinesBuild(project: Project, initialText: String, taskToAddText: String): String?

  suspend fun isQodanaTaskPresent(project: Project, virtualFile: VirtualFile): Boolean
}