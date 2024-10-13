package org.jetbrains.qodana.extensions.ci

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

interface GitHubCIConfigHandler {
  companion object {
    private val EP_NAME: ExtensionPointName<GitHubCIConfigHandler> = ExtensionPointName.create("org.intellij.qodana.githubCiConfigHandler")

    suspend fun isQodanaJobPresent(project: Project, virtualFile: VirtualFile): Boolean {
      for (e in EP_NAME.extensionList) {
        return e.isQodanaJobPresent(project, virtualFile)
      }
      return false
    }
  }
  suspend fun isQodanaJobPresent(project: Project, virtualFile: VirtualFile): Boolean
}