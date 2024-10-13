package org.jetbrains.qodana.ui.ci.providers.circleci

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.jetbrains.qodana.extensions.ci.CircleCIConfigHandler
import org.jetbrains.qodana.ui.ci.CIFile
import org.jetbrains.qodana.ui.ci.providers.CIFileChecker
import org.jetbrains.qodana.ui.ci.providers.createCIFileFlow
import org.jetbrains.qodana.ui.ci.providers.createRefreshSingleCIFile

class CircleCIFileChecker(
  private val project: Project,
  scope: CoroutineScope
) : CIFileChecker {
  override val ciPart: String = "job"

  override suspend fun isQodanaPresent(virtualFile: VirtualFile): Boolean {
    return CircleCIConfigHandler.isQodanaStepPresent(project, virtualFile)
  }

  override val ciFileFlow: Flow<CIFile?> = createCIFileFlow(scope, createRefreshSingleCIFile(project, CIRCLE_CI_FILE, this))
}