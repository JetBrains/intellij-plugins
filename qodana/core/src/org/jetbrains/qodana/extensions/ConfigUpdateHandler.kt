package org.jetbrains.qodana.extensions

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

interface ConfigUpdateHandler {
  companion object {
    private val EP_NAME: ExtensionPointName<ConfigUpdateHandler> = ExtensionPointName.create("org.intellij.qodana.configUpdateHandler")

    fun excludeFromConfig(project: Project, virtualFile: VirtualFile, inspectionId: String?, path: String?) {
      for (e in EP_NAME.extensionList) {
        e.excludeFromConfig(project, virtualFile, inspectionId, path)
        return
      }
    }
  }

  fun excludeFromConfig(project: Project, virtualFile: VirtualFile, inspectionId: String?, path: String?)
}