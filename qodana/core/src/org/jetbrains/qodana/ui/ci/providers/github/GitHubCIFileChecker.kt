package org.jetbrains.qodana.ui.ci.providers.github

import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findDirectory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import org.jetbrains.qodana.extensions.ci.GitHubCIConfigHandler
import org.jetbrains.qodana.ui.ci.CIFile
import org.jetbrains.qodana.ui.ci.providers.CIFileChecker
import org.jetbrains.qodana.ui.ci.providers.alreadyContainsQodana
import org.jetbrains.qodana.ui.ci.providers.createCIFileFlow
import kotlin.io.path.Path
import kotlin.io.path.pathString

class GitHubCIFileChecker(
  val project: Project,
  scope: CoroutineScope
) : CIFileChecker {
  override val ciPart: String = "job"

  override suspend fun isQodanaPresent(virtualFile: VirtualFile): Boolean {
    return GitHubCIConfigHandler.isQodanaJobPresent(project, virtualFile)
  }

  private suspend fun refreshCIFile(): CIFile {
    val githubWorkflowFiles = getGithubWorkflowFiles()
    val config = firstConfigWithQodana(githubWorkflowFiles)
    if (config != null) {
      val containsQodana = isQodanaPresent(config)
      return if (containsQodana) {
        CIFile.ExistingWithQodana(config.path, this, config)
      } else {
        CIFile.Existing(config.path, this, config)
      }
    }
    val projectDir = project.guessProjectDir() ?: return CIFile.Empty
    val toCheck = projectDir.findDirectory(GITHUB_WORKFLOWS_DIR)
                  ?: return CIFile.NotExisting(Path(projectDir.path).resolve(GITHUB_WORKFLOWS_DIR).pathString)
    return CIFile.NotExisting(toCheck.path)
  }

  private suspend fun getGithubWorkflowFiles(): List<VirtualFile> {
    val workflowDir = readAction { project.guessProjectDir()?.findDirectory(GITHUB_WORKFLOWS_DIR) } ?: return emptyList()
    return workflowDir.children.filter { it.extension in GITHUB_WORKFLOWS_EXTENSIONS }
  }

  private suspend fun firstConfigWithQodana(configFiles: List<VirtualFile>): VirtualFile? {
    return configFiles.map { virtualFile ->
      flow {
        if (virtualFile.alreadyContainsQodana()) emit(virtualFile)
      }
    }.merge().firstOrNull()
  }

  override val ciFileFlow: Flow<CIFile?> = createCIFileFlow(scope, ::refreshCIFile)
}