package org.jetbrains.qodana.staticAnalysis.vcs.git

import com.intellij.externalProcessAuthHelper.AuthenticationMode
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.vcsUtil.VcsUtil
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitLineHandler
import git4idea.config.GitVersionSpecialty
import git4idea.repo.GitRepositoryManager
import kotlinx.coroutines.runInterruptible
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException

internal data class GitStatusEntry(val status: String, val path: String, val filePath: FilePath)

internal suspend fun getStatus(project: Project): List<GitStatusEntry> {
  val repository = GitRepositoryManager.getInstance(project).repositories.firstOrNull()
                   ?: throw QodanaException("Repository root is not detected")
  val root = repository.root
  val handler = GitLineHandler(project, root, GitCommand.STATUS)
    .apply {
      addParameters("--no-renames", "--porcelain=1", "--ignore-submodules")
      ignoreAuthenticationMode = AuthenticationMode.SILENT
      setWithMediator(false)
    }
  val output = runInterruptible(StaticAnalysisDispatchers.IO) {
    Git.getInstance().runCommand(handler).getOutputOrThrow()
  }
  val lines = output.split("\n")
  return lines.map {
    val elements = it.split(" ")
    val path = git4idea.GitUtil.unescapePath(elements.last())
    val file = VcsUtil.getFilePath(root.url + "/" + path)
    GitStatusEntry(elements.first(), path, file)
  }
}

internal suspend fun restoreTrackedFiles(project: Project) {
  val repository = GitRepositoryManager.getInstance(project).repositories.firstOrNull()
                   ?: throw QodanaException("Repository root is not detected")

  val root = repository.root
  if (GitVersionSpecialty.RESTORE_SUPPORTED.existsIn(project)) {
    val handler = GitLineHandler(project, root, GitCommand.RESTORE)
      .apply {
        addParameters("--staged", "--worktree", "--source=HEAD")
        endOptions()
        addParameters(".")
        ignoreAuthenticationMode = AuthenticationMode.SILENT
        setWithMediator(false)
      }

    runInterruptible(StaticAnalysisDispatchers.IO) {
      Git.getInstance().runCommand(handler).getOutputOrThrow()
    }
  } else {
    resetHardLocal(project, root)
  }
}

internal suspend fun resetHardLocal(project: Project, root: VirtualFile) {
  val handler = GitLineHandler(project, root, GitCommand.RESET)
  handler.addParameters("--hard")
  handler.endOptions()
  runInterruptible(StaticAnalysisDispatchers.IO) {
    Git.getInstance().runCommand(handler).getOutputOrThrow()
  }
}