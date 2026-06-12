package com.intellij.openRewrite.run

import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.externalSystem.service.ui.project.path.ExternalProject
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile

interface OpenRewriteExternalSystemBridge {
  companion object {
    internal val EP_NAME: ExtensionPointName<OpenRewriteExternalSystemBridge> =
      ExtensionPointName.create("com.intellij.openRewrite.externalSystemBridge")

    fun findDelegate(configuration: OpenRewriteRunConfiguration): RunConfiguration? {
      for (bridge in EP_NAME.extensionList) {
        return bridge.getDelegate(configuration) ?: continue
      }
      return null
    }
  }

  fun getDelegate(configuration: OpenRewriteRunConfiguration): RunConfiguration?

  suspend fun collectExternalProjects(project: Project): List<ExternalProject>

  fun hasBuildFile(directory: VirtualFile, project: Project): Boolean

  fun isBuildFile(module: Module, psiFile: PsiFile): Boolean

  fun adjustModule(module: Module): Module

  fun isAvailable(project: Project): Boolean

  fun installFile(project: Project, commandLine: String): Boolean
}