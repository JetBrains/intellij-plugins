package org.jetbrains.qodana.ui.ci.providers.gitlab

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.jetbrains.qodana.extensions.ci.GitLabCIConfigHandler
import org.jetbrains.qodana.ui.ci.CIFile
import org.jetbrains.qodana.ui.ci.providers.CIFileChecker
import org.jetbrains.qodana.ui.ci.providers.createCIFileFlow
import org.jetbrains.qodana.ui.ci.providers.createRefreshSingleCIFile

class GitLabCIFileChecker(
  private val project: Project,
  scope: CoroutineScope
) : CIFileChecker {
  override val ciPart: String = "pipeline"

  override suspend fun isQodanaPresent(virtualFile: VirtualFile): Boolean {
    return GitLabCIConfigHandler.isQodanaPipelinePresent(project, virtualFile)
  }

  override val ciFileFlow: Flow<CIFile?> = createCIFileFlow(scope, createRefreshSingleCIFile(project, GITLAB_CI_FILE, this))
}