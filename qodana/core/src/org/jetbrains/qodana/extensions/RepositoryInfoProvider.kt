package org.jetbrains.qodana.extensions

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

interface RepositoryInfoProvider {
  companion object {
    val EP_NAME: ExtensionPointName<RepositoryInfoProvider> =
      ExtensionPointName.create("org.intellij.qodana.repositoryInfoProvider")

    fun getBranch(project: Project): String? = EP_NAME.computeSafeIfAny { it.getBranch(project) }

    fun getProjectBranches(project: Project): List<String> = EP_NAME.computeSafeIfAny { it.getProjectBranches(project) } ?: emptyList()

    fun getProjectOriginUrl(project: Project): String? = EP_NAME.computeSafeIfAny { it.getProjectOriginUrl(project) }

    fun findContentRoot(project: Project, remoteUrl: String): VirtualFile? =
      EP_NAME.computeSafeIfAny { it.findContentRoot(project, remoteUrl) }
  }

  fun getBranch(project: Project): String?

  fun getProjectBranches(project: Project): List<String>?

  fun getProjectOriginUrl(project: Project): String?

  fun findContentRoot(project: Project, remoteUrl: String): VirtualFile?
}
