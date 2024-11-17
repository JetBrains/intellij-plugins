package org.jetbrains.qodana.ui.ci.providers.bitbucket

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.jetbrains.qodana.extensions.ci.BitbucketCIConfigHandler
import org.jetbrains.qodana.ui.ci.CIFile
import org.jetbrains.qodana.ui.ci.providers.CIFileChecker
import org.jetbrains.qodana.ui.ci.providers.createCIFileFlow
import org.jetbrains.qodana.ui.ci.providers.createRefreshSingleCIFile

class BitbucketCIFIleChecker(
  private val project: Project,
  scope: CoroutineScope
) : CIFileChecker {
  override val ciPart: String = "pipeline"

  override suspend fun isQodanaPresent(virtualFile: VirtualFile): Boolean {
    return BitbucketCIConfigHandler.isQodanaStepPresent(project, virtualFile)
  }

  override val ciFileFlow: Flow<CIFile?> = createCIFileFlow(scope, createRefreshSingleCIFile(project, BITBUCKET_CI_FILE, this))
}