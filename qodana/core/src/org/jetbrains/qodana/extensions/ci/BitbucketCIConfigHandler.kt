package org.jetbrains.qodana.extensions.ci

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

interface BitbucketCIConfigHandler {
  companion object {
    private val EP_NAME: ExtensionPointName<BitbucketCIConfigHandler> = ExtensionPointName.create("org.intellij.qodana.bitbucketCiConfigUpdateHandler")

    suspend fun addQodanaStepToBranches(project: Project, initialText: String, stepText: String, branches: List<String>): String {
      for (e in EP_NAME.extensionList) {
        return e.addQodanaStepToBranches(project, initialText, stepText, branches) ?: continue
      }
      return initialText
    }

    suspend fun addCachesSection(project: Project, initialText: String): String {
      for (e in EP_NAME.extensionList) {
        return e.addCachesSection(project, initialText) ?: continue
      }
      return initialText
    }

    suspend fun isQodanaStepPresent(project: Project, virtualFile: VirtualFile): Boolean {
      for (e in EP_NAME.extensionList) {
        return e.isQodanaStepPresent(project, virtualFile)
      }
      return false
    }
  }

  suspend fun addQodanaStepToBranches(project: Project, initialText: String, stepText: String, branches: List<String>): String?

  suspend fun addCachesSection(project: Project, initialText: String): String?

  suspend fun isQodanaStepPresent(project: Project, virtualFile: VirtualFile): Boolean
}