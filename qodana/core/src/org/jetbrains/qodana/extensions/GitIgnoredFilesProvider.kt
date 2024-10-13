package org.jetbrains.qodana.extensions

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FilePath
import git4idea.repo.GitRepositoryManager

class GitIgnoredFilesProvider: VcsIgnoredFilesProvider {
  override fun getRepositoriesIgnoredFiles(project: Project): List<FilePath> {
    return GitRepositoryManager.getInstance(project).repositories
      .map { it.ignoredFilesHolder }
      .flatMap { it.ignoredFilePaths }
  }
}