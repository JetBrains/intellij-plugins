package org.jetbrains.qodana.extensions.ci

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.annotations.TestOnly

interface JenkinsConfigHandler {
  companion object {
    @TestOnly
    val EP_NAME: ExtensionPointName<JenkinsConfigHandler> = ExtensionPointName.create("org.intellij.qodana.jenkinsConfigHandler")

    suspend fun addStage(project: Project, text: String, stageToAddText: String): String {
      for (e in EP_NAME.extensionList) {
        return e.addStage(project, text, stageToAddText) ?: continue
      }
      return DummyJenkinsConfigHandler().addStage(text, stageToAddText) ?: text
    }

    suspend fun isQodanaStagePresent(project: Project, virtualFile: VirtualFile): Boolean {
      for (e in EP_NAME.extensionList) {
        return e.isQodanaStagePresent(project, virtualFile) ?: continue
      }
      return DummyJenkinsConfigHandler().isQodanaStagePresent(virtualFile)
    }
  }

  suspend fun addStage(project: Project, text: String, stageToAddText: String): String?

  suspend fun isQodanaStagePresent(project: Project, virtualFile: VirtualFile): Boolean?
}