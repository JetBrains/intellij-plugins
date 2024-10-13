package org.jetbrains.qodana.ui.run.wizard

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.ui.ci.CombinedSetupCIView
import org.jetbrains.qodana.ui.ci.CombinedSetupCIViewModel
import org.jetbrains.qodana.ui.ci.CombinedSetupCIViewSpec
import org.jetbrains.qodana.ui.run.QodanaYamlViewModel
import org.jetbrains.qodana.ui.wizard.QodanaWizardStep
import org.jetbrains.qodana.ui.wizard.QodanaWizardStepViewModel
import org.jetbrains.qodana.ui.wizard.QodanaWizardStepViewProvider
import org.jetbrains.qodana.ui.wizard.QodanaWizardTransition
import javax.swing.JComponent
import javax.swing.JPanel

class SetupCIStep(override val viewModel: SetupCIStepViewModel) : QodanaWizardStep {
  companion object {
    const val ID = "SetupCI"
  }

  override val id: String get() = ID

  override val viewProvider: QodanaWizardStepViewProvider = SetupCIStepViewProvider(viewModel)
}

class SetupCIStepViewModel(
  private val qodanaYamlViewModel: QodanaYamlViewModel,
  val combinedSetupCIViewModel: CombinedSetupCIViewModel
) : QodanaWizardStepViewModel {
  private val previousStepFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

  override val stepTransitionFlow: Flow<Pair<QodanaWizardTransition, String?>> = createStepTransitionFlow()

  val isFinishAvailableFlow: Flow<Boolean> = combinedSetupCIViewModel.isFinishAvailableFlow

  private fun createStepTransitionFlow(): Flow<Pair<QodanaWizardTransition, String?>> {
    return merge(
      previousStepFlow.map { QodanaWizardTransition.PREVIOUS to EditYamlBeforeSetupCIStep.ID },
      combinedSetupCIViewModel.finishHappenedFlow.map { QodanaWizardTransition.NEXT to null }
    )
  }

  fun finish() {
    qodanaYamlViewModel.writeQodanaYamlIfNeeded()
    combinedSetupCIViewModel.finish()
  }

  fun back() {
    previousStepFlow.tryEmit(Unit)
  }
}

private class SetupCIStepViewProvider(val viewModel: SetupCIStepViewModel) : QodanaWizardStepViewProvider {
  override val titleFlow: Flow<String> = flowOf(QodanaBundle.message("qodana.add.to.ci.title"))

  override val mainViewFlow: Flow<JComponent> = createMainViewFlow()

  override val nextButtonDescriptorFlow: Flow<QodanaWizardStepViewProvider.ButtonDescriptor?> = createNextButtonDescriptorFlow()

  override val previousButtonDescriptorFlow: Flow<QodanaWizardStepViewProvider.ButtonDescriptor?> = createPreviousButtonDescriptorFlow()

  private fun createMainViewFlow(): Flow<JPanel> {
    return flow {
      coroutineScope {
        val viewSpec = CombinedSetupCIViewSpec()
        val newMainViewSpec = viewSpec.mainViewSpec.copy(
          borderTop = 0,
          borderRight = 0,
          borderBottom = 0,
        )
        val newViewSpec = viewSpec.copy(mainViewSpec = newMainViewSpec)
        emit(CombinedSetupCIView(this, viewModel.combinedSetupCIViewModel, newViewSpec).getView())
        awaitCancellation()
      }
    }
  }
  private fun createNextButtonDescriptorFlow(): Flow<QodanaWizardStepViewProvider.ButtonDescriptor?> {
    return viewModel.isFinishAvailableFlow.map { isEnabled ->
      val text = viewModel.combinedSetupCIViewModel.nextButtonTextFlow.firstOrNull() ?: return@map null
      QodanaWizardStepViewProvider.ButtonDescriptor(isEnabled, text) {
        viewModel.finish()
      }
    }
  }

  private fun createPreviousButtonDescriptorFlow(): Flow<QodanaWizardStepViewProvider.ButtonDescriptor> {
    return flowOf(
      QodanaWizardStepViewProvider.ButtonDescriptor(isEnabled = true, QodanaBundle.message("qodana.run.wizard.previous.button")) {
        viewModel.back()
      }
    )
  }
}