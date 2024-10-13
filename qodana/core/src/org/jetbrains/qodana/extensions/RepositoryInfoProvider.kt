package org.jetbrains.qodana.extensions

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project

interface RepositoryInfoProvider {
  companion object {
    val EP_NAME: ExtensionPointName<RepositoryInfoProvider> =
      ExtensionPointName.create("org.intellij.qodana.repositoryInfoProvider")

    fun getBranch(project: Project): String? {
      for (vcs in EP_NAME.extensionList) {
        return vcs.getBranch(project) ?: continue
      }
      return null
    }

    fun getProjectBranches(project: Project): List<String> {
      for (vcs in EP_NAME.extensionList) {
        return vcs.getProjectBranches(project) ?: continue
      }
      return emptyList()
    }

    fun getProjectOriginUrl(project: Project): String? {
      for (vcs in EP_NAME.extensionList) {
        return vcs.getProjectOriginUrl(project) ?: continue
      }
      return null
    }
  }

  fun getBranch(project: Project): String?

  fun getProjectBranches(project: Project): List<String>?

  fun getProjectOriginUrl(project: Project): String?
}