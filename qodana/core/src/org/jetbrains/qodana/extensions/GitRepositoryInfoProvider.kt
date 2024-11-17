package org.jetbrains.qodana.extensions

import com.intellij.openapi.project.Project
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
}