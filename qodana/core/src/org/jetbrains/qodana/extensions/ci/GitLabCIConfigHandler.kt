package org.jetbrains.qodana.extensions.ci

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

interface GitLabCIConfigHandler {
  companion object {
    private val EP_NAME: ExtensionPointName<GitLabCIConfigHandler> = ExtensionPointName.create("org.intellij.qodana.gitlabCiConfigHandler")

    suspend fun isQodanaPipelinePresent(project: Project, virtualFile: VirtualFile): Boolean {
      for (e in EP_NAME.extensionList) {
        return e.isQodanaPipelinePresent(project, virtualFile)
      }
      return false
    }
  }
  suspend fun isQodanaPipelinePresent(project: Project, virtualFile: VirtualFile): Boolean
}