package org.jetbrains.qodana.ui.ci.providers.azure

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.jetbrains.qodana.extensions.ci.AzureCIConfigHandler
import org.jetbrains.qodana.ui.ci.CIFile
import org.jetbrains.qodana.ui.ci.providers.CIFileChecker
import org.jetbrains.qodana.ui.ci.providers.createCIFileFlow
import org.jetbrains.qodana.ui.ci.providers.createRefreshSingleCIFile

class AzurePipelinesCIFileChecker(
  private val project: Project,
  scope: CoroutineScope
) : CIFileChecker {
  override val ciPart: String = "task"

  override suspend fun isQodanaPresent(virtualFile: VirtualFile): Boolean {
    return AzureCIConfigHandler.isQodanaTaskPresent(project, virtualFile)
  }

  override val ciFileFlow: Flow<CIFile?> = createCIFileFlow(scope, createRefreshSingleCIFile(project, AZURE_PIPELINES_FILE, this))
}