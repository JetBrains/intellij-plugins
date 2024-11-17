package org.jetbrains.qodana.extensions

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import git4idea.repo.GitRepositoryManager

class GitRepositoryRevisionProvider: RepositoryRevisionProvider {
  override fun getRevision(project: Project, virtualFile: VirtualFile?): String? {
    val repository = GitRepositoryManager.getInstance(project).getRepositoryForFileQuick(virtualFile)
    return repository?.currentRevision
  }
}