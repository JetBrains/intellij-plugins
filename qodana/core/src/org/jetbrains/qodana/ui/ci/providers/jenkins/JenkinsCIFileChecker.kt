package org.jetbrains.qodana.ui.ci.providers.jenkins

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.jetbrains.qodana.extensions.ci.JenkinsConfigHandler
import org.jetbrains.qodana.ui.ci.CIFile
import org.jetbrains.qodana.ui.ci.providers.CIFileChecker
import org.jetbrains.qodana.ui.ci.providers.createCIFileFlow
import org.jetbrains.qodana.ui.ci.providers.createRefreshSingleCIFile

class JenkinsCIFileChecker(
  private val project: Project,
  scope: CoroutineScope
) : CIFileChecker {
  override val ciPart: String = "pipeline"

  override suspend fun isQodanaPresent(virtualFile: VirtualFile): Boolean {
    return JenkinsConfigHandler.isQodanaStagePresent(project, virtualFile)
  }

  override val ciFileFlow: Flow<CIFile?> = createCIFileFlow(scope, createRefreshSingleCIFile(project, JENKINS_FILE, this))
}