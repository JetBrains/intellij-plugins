package org.jetbrains.qodana.staticAnalysis.inspections.coverage

import com.intellij.coverage.CoverageDataManager
import com.intellij.coverage.CoverageSuitesBundle
import com.intellij.coverage.view.CoverageViewManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.util.awaitCancellationAndInvoke
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.highlight.HighlightedReportState
import org.jetbrains.qodana.highlight.QodanaHighlightedReportService
import org.jetbrains.qodana.registry.QodanaRegistry.openCoverageReportEnabled

/*
 * Listener service responsible for spinning up coverage reports on the screen once they arrive from the cloud.
 */
@Service(Service.Level.PROJECT)
class CoverageListenerService(private val project: Project, scope: CoroutineScope) {
  companion object {
    fun getInstance(project: Project): CoverageListenerService = project.service()
  }

  init {
    scope.launch(QodanaDispatchers.Default) {
      val highlightingService = QodanaHighlightedReportService.getInstance(project)
      launch {
        highlightingService.highlightedReportState.collectLatest {
          if (!openCoverageReportEnabled) return@collectLatest
          if (it !is HighlightedReportState.Selected) return@collectLatest

          val metaData = it.highlightedReportData.reportMetadata
          val suite = CoverageCloudArtifactsProcessor.runCoverageProviders(metaData.map, project) ?: return@collectLatest
          openCoverageSuiteAndCloseOnCancellation(suite)
        }
      }
      launch {
        highlightingService
          .uiTabsActivateRequest
          .filter { it.activateCoverage }
          .collectLatest {
            if (!openCoverageReportEnabled) return@collectLatest
            openCoverageToolwindow()
          }
      }
    }
  }

  private suspend fun openCoverageSuiteAndCloseOnCancellation(suite: CoverageSuitesBundle) {
    val coverageDataManager = CoverageDataManager.getInstance(project)
    withContext(QodanaDispatchers.Ui) {
      coverageDataManager.chooseSuitesBundle(suite)
      awaitCancellationAndInvoke {
        if (!project.isDisposed) {
          coverageDataManager.closeSuitesBundle(suite)
        }
      }
    }
  }

  private suspend fun openCoverageToolwindow() {
    withContext(QodanaDispatchers.Ui) {
      val coverageToolwindow = ToolWindowManager.getInstance(project).getToolWindow(CoverageViewManager.TOOLWINDOW_ID) ?: return@withContext
      coverageToolwindow.activate(null)
    }
  }
}