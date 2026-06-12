package com.intellij.openRewrite.run.editor

import com.intellij.execution.ExecutionBundle
import com.intellij.openRewrite.run.OpenRewriteExternalSystemBridge
import com.intellij.openapi.externalSystem.service.ui.project.path.ExternalProject
import com.intellij.openapi.externalSystem.service.ui.project.path.WorkingDirectoryInfo
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

internal class OpenRewriteWorkingDirectoryInfo(private val project: Project) : WorkingDirectoryInfo {
  override val emptyFieldError: String
    get() = ExecutionBundle.message("run.configuration.working.directory.empty.error")

  override val fileChooserDescriptor: FileChooserDescriptor
    get() = OpenRewriteFileChooserDescriptor(project).withTitle(ExecutionBundle.message("select.working.directory.message"))

  override val editorLabel: String
    get() = ExecutionBundle.message("run.configuration.working.directory.label")

  override val settingsName: String
    get() = ExecutionBundle.message("run.configuration.working.directory.name")

  override suspend fun collectExternalProjects(): List<ExternalProject> {
    val result = ArrayList<ExternalProject>()
    for (bridge in OpenRewriteExternalSystemBridge.EP_NAME.extensionList) {
      result.addAll(bridge.collectExternalProjects(project))
    }
    return result
  }

  private class OpenRewriteFileChooserDescriptor(private val project: Project) :
    FileChooserDescriptor(false, true, false, false, false, false) {

    override fun isFileSelectable(file: VirtualFile?): Boolean {
      if (!super.isFileSelectable(file)) return false
      return OpenRewriteExternalSystemBridge.EP_NAME.extensionList.any { it.hasBuildFile(file!!, project) }
    }
  }
}
