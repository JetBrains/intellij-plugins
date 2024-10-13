package org.jetbrains.qodana.extensions

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FilePath

interface VcsIgnoredFilesProvider {
  companion object {
    private val EP_NAME: ExtensionPointName<VcsIgnoredFilesProvider> = ExtensionPointName.create("org.intellij.qodana.vcsIgnoredFilesProvider")

    fun getVcsRepositoriesIgnoredFiles(project: Project): List<FilePath> {
      for (vcs in EP_NAME.extensionList) {
        return vcs.getRepositoriesIgnoredFiles(project)
      }
      return emptyList()
    }
  }

  fun getRepositoriesIgnoredFiles(project: Project): List<FilePath>
}