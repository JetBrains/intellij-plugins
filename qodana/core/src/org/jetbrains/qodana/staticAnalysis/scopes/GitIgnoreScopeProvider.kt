package org.jetbrains.qodana.staticAnalysis.scopes

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.scope.packageSet.CustomScopesProviderEx
import com.intellij.psi.search.scope.packageSet.FilteredPackageSet
import com.intellij.psi.search.scope.packageSet.NamedScope
import com.intellij.vcsUtil.VcsUtil
import git4idea.repo.GitRepositoryManager

private const val SCOPE_NAME = "gitignore"

class GitIgnoreScopeProvider(private val project: Project): CustomScopesProviderEx() {
  override fun getCustomScopes(): List<NamedScope> {
    return emptyList()
  }

  override fun getCustomScope(name: String): NamedScope? {
    return if (name == SCOPE_NAME) constructGitIgnoreScope() else null
  }

  private fun constructGitIgnoreScope(): NamedScope {
    val ignoredFilesHolders = GitRepositoryManager.getInstance(project).repositories.map { it.ignoredFilesHolder }
    return NamedScope(SCOPE_NAME, object :FilteredPackageSet(SCOPE_NAME) {
      override fun contains(file: VirtualFile, project: Project): Boolean {
        return ignoredFilesHolders.any { it.containsFile(VcsUtil.getFilePath(file)) }
      }
    })
  }
}