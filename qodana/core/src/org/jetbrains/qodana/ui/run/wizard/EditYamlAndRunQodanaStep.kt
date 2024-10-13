package org.jetbrains.qodana.ui.run.wizard

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.highlight.QodanaHighlightedReportService
import org.jetbrains.qodana.report.FromFileReportDescriptorBuilder
import org.jetbrains.qodana.report.QodanaLocalReportsService
import org.jetbrains.qodana.report.ReportDescriptor
import org.jetbrains.qodana.run.LocalRunNotPublishedReportDescriptor
import org.jetbrains.qodana.run.LocalRunPublishedReportDescriptor
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.stats.SourceHighlight
import org.jetbrains.qodana.stats.toStatsReportType
import org.jetbrains.qodana.ui.run.LocalRunQodanaViewModel
import org.jetbrains.qodana.ui.run.localRunQodanaMainView
import org.jetbrains.qodana.ui.wizard.QodanaWizardStep
import org.jetbrains.qodana.ui.wizard.QodanaWizardStepViewModel
import org.jetbrains.qodana.ui.wizard.QodanaWizardStepViewProvider
import org.jetbrains.qodana.ui.wizard.QodanaWizardTransition
import javax.swing.JComponent

class EditYamlAndRunQodanaStep(override val viewModel: EditYamlAndRunStepViewModel) : QodanaWizardStep {
  companion object {
    const val ID = "YamlAndRunQodana"
  }

  override val id: String get() = ID

  override val viewProvider: QodanaWizardStepViewProvider = EditYamlAndRunStepViewProvider(viewModel)
}

class EditYamlAndRunStepViewModel(private val project: Project, val localRunQodanaViewModel: LocalRunQodanaViewModel) : QodanaWizardStepViewModel {
  private val transitionBackFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

  override val stepTransitionFlow: Flow<Pair<QodanaWizardTransition, String?>> = merge(
    localRunQodanaViewModel.analysisWasLaunchedFlow.map { QodanaWizardTransition.NEXT to null },
    transitionBackFlow.map { QodanaWizardTransition.PREVIOUS to WelcomeRunQodanaStep.ID }
  )

  val finishAvailableFlow: Flow<Boolean> = localRunQodanaViewModel.isFinishAvailableFlow

  fun back() {
    transitionBackFlow.tryEmit(Unit)
  }

  fun finish() {
    project.qodanaProjectScope.launch(QodanaDispatchers.Default) {
      val launchResult = localRunQodanaViewModel.launchAnalysis().await() ?: return@launch
      val fileReportDescriptor = FromFileReportDescriptorBuilder(launchResult.qodanaInIdeOutput.sarifPath, project).createReportDescriptor() ?: return@launch
      val reportDescriptor = when(launchResult) {
        is LocalRunQodanaViewModel.LaunchResult.NoPublishToCloud -> LocalRunNotPublishedReportDescriptor(fileReportDescriptor)
        is LocalRunQodanaViewModel.LaunchResult.PublishToCloud -> LocalRunPublishedReportDescriptor(fileReportDescriptor, launchResult.publishedReportLink)
      }
      QodanaLocalReportsService.getInstance(project).addReport(reportDescriptor)
      QodanaHighlightedReportService.getInstance(project).highlightReport(reportDescriptor)
      logHighlightReportStats(reportDescriptor)
    }
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

private class EditYamlAndRunStepViewProvider(val viewModel: EditYamlAndRunStepViewModel) : QodanaWizardStepViewProvider {
  override val titleFlow: Flow<String> = flowOf(QodanaBundle.message("qodana.run.wizard.title"))

  override val mainViewFlow: Flow<JComponent> = createMainViewFlow()

  override val nextButtonDescriptorFlow: Flow<QodanaWizardStepViewProvider.ButtonDescriptor?> = createNextButtonDescriptorFlow()

  override val previousButtonDescriptorFlow: Flow<QodanaWizardStepViewProvider.ButtonDescriptor?> = flowOf(null)

  fun createMainViewFlow(): Flow<DialogPanel> {
    return flow {
      coroutineScope {
        emit(localRunQodanaMainView(this, viewModel.localRunQodanaViewModel))
        awaitCancellation()
      }
    }
  }

  fun createNextButtonDescriptorFlow(): Flow<QodanaWizardStepViewProvider.ButtonDescriptor> {
    return viewModel.finishAvailableFlow.map { isEnabled ->
      QodanaWizardStepViewProvider.ButtonDescriptor(isEnabled, QodanaBundle.message("qodana.run.wizard.finish.button")) {
        viewModel.finish()
      }
    }
  }
}