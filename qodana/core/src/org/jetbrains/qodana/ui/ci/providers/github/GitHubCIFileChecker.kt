package org.jetbrains.qodana.ui.ci.providers.github

import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findDirectory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.shareIn
import org.jetbrains.qodana.extensions.ci.GitHubCIConfigHandler
import org.jetbrains.qodana.ui.ci.CIFile
import org.jetbrains.qodana.ui.ci.providers.CIFileChecker
import org.jetbrains.qodana.ui.ci.providers.alreadyContainsQodana
import org.jetbrains.qodana.ui.ci.providers.createCIFileFlow
import kotlin.io.path.Path
import kotlin.io.path.pathString

@Service(Service.Level.PROJECT)
private class GitHubCIFileCheckerService(
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
      if (containsQodana) {
        return CIFile.ExistingWithQodana(config.path, this, config)
      }
    }
    val projectDir = project.guessProjectDir() ?: return CIFile.Empty
    val toCheck = projectDir.findDirectory(GITHUB_WORKFLOWS_DIR)
                  ?: return CIFile.NotExisting(Path(projectDir.path).resolve(GITHUB_WORKFLOWS_DIR).pathString)
    return if (githubWorkflowFiles.isEmpty()) {
      CIFile.NotExisting(toCheck.path)
    }
    else {
      CIFile.ExistingMultipleInstances(toCheck.path)
    }
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

  private val _ciFileFlow: Flow<CIFile> = createCIFileFlow(scope, ::refreshCIFile)

  override val ciFileFlow: Flow<CIFile> =
    _ciFileFlow.shareIn(scope, SharingStarted.WhileSubscribed(replayExpirationMillis = 0), 1)
}

/**
 * Wrapper around [GitHubCIFileCheckerService] to make it compatible with other [CIFileChecker] classes
 */
class GitHubCIFileChecker(private val project: Project): CIFileChecker {

  override val ciPart: String = project.service<GitHubCIFileCheckerService>().ciPart

  override suspend fun isQodanaPresent(virtualFile: VirtualFile): Boolean =
    project.service<GitHubCIFileCheckerService>().isQodanaPresent(virtualFile)

  override val ciFileFlow: Flow<CIFile> = project.service<GitHubCIFileCheckerService>().ciFileFlow
}