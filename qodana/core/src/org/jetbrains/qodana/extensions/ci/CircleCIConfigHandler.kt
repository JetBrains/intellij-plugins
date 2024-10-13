package org.jetbrains.qodana.extensions.ci

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

interface CircleCIConfigHandler {
  companion object {
    private val EP_NAME: ExtensionPointName<CircleCIConfigHandler> = ExtensionPointName.create("org.intellij.qodana.circleCiConfigUpdateHandler")

    suspend fun addOrb(project: Project, text: String, orbName: String, orbValue: String): String {
      for (e in EP_NAME.extensionList) {
        return e.addOrb(project, text, orbName, orbValue) ?: continue
      }
      return text
    }

    suspend fun addJob(project: Project, text: String, jobText: String): String {
      for (e in EP_NAME.extensionList) {
        return e.addJob(project, text, jobText) ?: continue
      }
      return text
    }

    suspend fun addWorkflowJob(project: Project, text: String, jobName: String, jobText: String): String {
      for (e in EP_NAME.extensionList) {
        return e.addWorkflowJob(project, text, jobName, jobText) ?: continue
      }
      return text
    }

    suspend fun isQodanaStepPresent(project: Project, virtualFile: VirtualFile): Boolean {
      for (e in EP_NAME.extensionList) {
        return e.isQodanaStepPresent(project, virtualFile)
      }
      return false
    }
  }

  suspend fun addOrb(project: Project, text: String, orbName: String, orbValue: String): String?

  suspend fun addJob(project: Project, text: String, jobText: String): String?

  suspend fun addWorkflowJob(project: Project, text: String, jobName: String, jobText: String): String?

  suspend fun isQodanaStepPresent(project: Project, virtualFile: VirtualFile): Boolean
}