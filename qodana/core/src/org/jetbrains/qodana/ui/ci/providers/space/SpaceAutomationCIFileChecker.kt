package org.jetbrains.qodana.ui.ci.providers.space

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.jetbrains.qodana.ui.ci.CIFile
import org.jetbrains.qodana.ui.ci.providers.CIFileChecker
import org.jetbrains.qodana.ui.ci.providers.createCIFileFlow
import org.jetbrains.qodana.ui.ci.providers.createRefreshSingleCIFile

class SpaceAutomationCIFileChecker(
  project: Project,
  scope: CoroutineScope
) : CIFileChecker{
  override val ciPart: String = "job"

  override suspend fun isQodanaPresent(virtualFile: VirtualFile): Boolean {
    return virtualFile.readText().contains("jetbrains/qodana")
  }

  override val ciFileFlow: Flow<CIFile?> = createCIFileFlow(scope, createRefreshSingleCIFile(project, SPACE_AUTOMATION_CI_FILE, this))
}