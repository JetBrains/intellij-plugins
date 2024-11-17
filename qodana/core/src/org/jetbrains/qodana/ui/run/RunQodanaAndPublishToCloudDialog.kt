package org.jetbrains.qodana.ui.run

import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.platform.util.coroutines.childScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.cloud.api.getErrorNotification
import org.jetbrains.qodana.cloud.project.LinkState
import org.jetbrains.qodana.cloud.project.LinkedCloudReportDescriptor
import org.jetbrains.qodana.cloud.userApi
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.cloudclient.qodanaCloudResponse
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.highlight.QodanaHighlightedReportService
import org.jetbrains.qodana.report.ReportDescriptor
import org.jetbrains.qodana.report.openReportFromFileAndHighlight
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.stats.SourceHighlight
import org.jetbrains.qodana.stats.toStatsReportType
import org.jetbrains.qodana.ui.ProjectVcsDataProviderImpl
import java.nio.file.Path
import javax.swing.JComponent

private val NOT_INITIALIZED_TOKEN_PLACEHOLDER = "not_initialized"

class RunQodanaAndPublishToCloudDialog(project: Project, linked: LinkState.Linked) : DialogWrapper(project) {
  private val scope: CoroutineScope = project.qodanaProjectScope.childScope(ModalityState.nonModal().asContextElement())

  private val viewModel = RunQodanaAndPublishToCloudViewModel(project, scope, linked)

  init {
    isModal = false
    title = QodanaBundle.message("qodana.run.action")
    scope.launch(QodanaDispatchers.Ui) {
      viewModel.localRunQodanaViewModel.analysisWasLaunchedFlow.collect {
        applyFields()
        close(OK_EXIT_CODE)
      }
    }
    init()
  }

  override fun createCenterPanel(): JComponent = localRunQodanaMainView(scope, viewModel.localRunQodanaViewModel, showCloudTokenField = false)

  override fun doOKAction() {
    viewModel.launchAnalysisPublishAndHighlight()
  }

  override fun dispose() {
    scope.cancel()
    super.dispose()
  }
}

private class RunQodanaAndPublishToCloudViewModel(
  private val project: Project,
  scope: CoroutineScope,
  private val linked: LinkState.Linked
) {
  val localRunQodanaViewModel = LocalRunQodanaViewModel(
    project,
    scope,
    QodanaYamlViewModelImpl(project, scope),
    ProjectVcsDataProviderImpl(project, scope)
  )

  init {
    localRunQodanaViewModel.setPublishToCloud(true)
    localRunQodanaViewModel.setCloudToken(NOT_INITIALIZED_TOKEN_PLACEHOLDER)
    scope.launch(QodanaDispatchers.Default) {
      val tokenResponse = qodanaCloudResponse {
        linked.authorized.userApi().value()
          .getProjectToken(linked.projectDataProvider.projectPrimaryData.id).value().token
      }
      when(tokenResponse) {
        is QDCloudResponse.Success -> {
          localRunQodanaViewModel.setCloudToken(tokenResponse.value)
        }
        is QDCloudResponse.Error.ResponseFailure -> {
          thisLogger().warn("Failed loading token, trying to generate", tokenResponse.exception)

          val generatedTokenResponse = qodanaCloudResponse {
            linked.authorized.userApi().value()
              .generateProjectToken(linked.projectDataProvider.projectPrimaryData.id).value().token
          }
          when (generatedTokenResponse) {
            is QDCloudResponse.Success -> {
              localRunQodanaViewModel.setCloudToken(generatedTokenResponse.value)
            }
            is QDCloudResponse.Error -> {
              thisLogger().warn("Failed generating token", generatedTokenResponse.exception)
              generatedTokenResponse.getErrorNotification(QodanaBundle.message("qodana.cloud.failed.loading.token")).notify(project)
            }
          }
        }
        is QDCloudResponse.Error.Offline -> {
          thisLogger().warn("Failed loading token", tokenResponse.exception)
          tokenResponse.getErrorNotification(QodanaBundle.message("qodana.cloud.failed.loading.token")).notify(project)
        }
      }
    }
  }

  fun launchAnalysisPublishAndHighlight() {
    project.qodanaProjectScope.launch(QodanaDispatchers.Default) {
      waitForToken()
      val launchResult = localRunQodanaViewModel.launchAnalysis().await() ?: return@launch
      val reportDescriptor = when(launchResult) {
        is LocalRunQodanaViewModel.LaunchResult.NoPublishToCloud -> {
          openReportFromFileAndHighlight(project, launchResult.qodanaInIdeOutput.sarifPath)
        }
        is LocalRunQodanaViewModel.LaunchResult.PublishToCloud -> {
          highlightPublishedReport(project, launchResult.qodanaInIdeOutput.sarifPath)
        }
      } ?: return@launch
      logHighlightReportStats(reportDescriptor)
    }
  }

  private suspend fun waitForToken() {
    localRunQodanaViewModel.cloudTokenStateFlow.filter { it != NOT_INITIALIZED_TOKEN_PLACEHOLDER }.first()
  }

  private suspend fun highlightPublishedReport(project: Project, sarifPath: Path): LinkedCloudReportDescriptor? {
    val reportDescriptor = linked.cloudReportDescriptorBuilder.createPublishedReportDescriptor(sarifPath) ?: return null
    QodanaHighlightedReportService.getInstance(project).highlightReport(reportDescriptor)
    return reportDescriptor
  }

  private fun logHighlightReportStats(reportDescriptor: ReportDescriptor) {
    QodanaPluginStatsCounterCollector.UPDATE_HIGHLIGHTED_REPORT.log(
      project,
      true,
      reportDescriptor.toStatsReportType(),
      SourceHighlight.RUN_QODANA_DIALOG
    )
  }
}