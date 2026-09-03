package org.jetbrains.qodana.extensions

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import git4idea.remote.hosting.GitHostingUrlUtil
import git4idea.repo.GitRepositoryManager

class GitRepositoryInfoProvider : RepositoryInfoProvider {
  override fun getBranch(project: Project): String? {
    val repo = GitRepositoryManager.getInstance(project).repositories.firstOrNull()
    return repo?.currentBranch?.name
  }

  override fun getProjectBranches(project: Project): List<String>? {
    val repo = GitRepositoryManager.getInstance(project).repositories.firstOrNull() ?: return null
    val localBranchesNames = repo.branches.localBranches.map { it.name }
    val remoteBranchesNames = repo.branches.remoteBranches.map { it.name }
    return localBranchesNames + remoteBranchesNames
  }

  override fun getProjectOriginUrl(project: Project): String? {
    val gitRepo = GitRepositoryManager.getInstance(project).repositories.firstOrNull()
    val origin = gitRepo?.remotes?.find { it.name == "origin" }
    return origin?.firstUrl
  }

  override fun findContentRoot(project: Project, remoteUrl: String): VirtualFile? {
    return GitRepositoryManager.getInstance(project).repositories
      .firstOrNull { repository ->
        repository.remotes.any { remote -> remote.urls.any { areSameRemoteUrls(remoteUrl, it) } }
      }
      ?.root
  }
}

internal fun areSameRemoteUrls(first: String, second: String): Boolean {
  val firstUri = GitHostingUrlUtil.getUriFromRemoteUrl(first) ?: return false
  val secondUri = GitHostingUrlUtil.getUriFromRemoteUrl(second) ?: return false

  return firstUri.host.equals(secondUri.host, ignoreCase = true) &&
         firstUri.port == secondUri.port &&
         firstUri.path.equals(secondUri.path, ignoreCase = true)
}
