package org.jetbrains.qodana.extensions

import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitLineHandler
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryManager
import git4idea.test.createRepository
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunnerWithVcsTestCase
import org.junit.Test
import java.io.File

class GitRepositoryInfoProviderTest : QodanaRunnerWithVcsTestCase() {
  @Test
  fun findsContentRootByRemoteUrl() {
    val repository = GitRepositoryManager.getInstance(project).repositories.single()
    val nestedRepository = createRepository(project, File(repository.root.path, "nested").path)
    addOrigin(repository, "git@github.com:JetBrains/qodana.git")
    addOrigin(nestedRepository, "ssh://git@github.com:2222/JetBrains/other.git")

    val provider = GitRepositoryInfoProvider()

    assertEquals(repository.root, provider.findContentRoot(project, "https://github.com/jetbrains/qodana.git"))
    assertEquals(nestedRepository.root, provider.findContentRoot(project, "git://github.com:2222/jetbrains/other.git"))
    assertNull(provider.findContentRoot(project, "https://github.com/jetbrains/unknown.git"))
  }

  private fun addOrigin(repository: GitRepository, url: String) {
    Git.getInstance().runCommand(
      GitLineHandler(project, repository.root, GitCommand.REMOTE).apply {
        addParameters("add", "origin", url)
      }
    ).getOutputOrThrow()
    repository.update()
  }
}
