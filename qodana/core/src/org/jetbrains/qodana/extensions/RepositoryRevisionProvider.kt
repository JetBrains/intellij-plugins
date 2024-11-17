package org.jetbrains.qodana.extensions

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

interface RepositoryRevisionProvider {
  companion object {
    private val EP_NAME: ExtensionPointName<RepositoryRevisionProvider> = ExtensionPointName.create(
      "org.intellij.qodana.repositoryRevisionProvider")

    fun getRepositoryRevision(project: Project, virtualFile: VirtualFile?): String? {
      for (vcs in EP_NAME.extensionList) {
        return vcs.getRevision(project, virtualFile) ?: continue
      }
      return null
    }
  }

  fun getRevision(project: Project, virtualFile: VirtualFile?): String?
}