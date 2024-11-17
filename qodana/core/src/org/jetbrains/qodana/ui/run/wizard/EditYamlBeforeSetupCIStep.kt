package org.jetbrains.qodana.ui.run.wizard

import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.RightGap
import com.intellij.ui.dsl.builder.panel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.ui.ci.providers.withBottomInsetBeforeComment
import org.jetbrains.qodana.ui.run.QodanaYamlViewModel
import org.jetbrains.qodana.ui.run.qodanaYamlView
import org.jetbrains.qodana.ui.wizard.QodanaWizardStep
import org.jetbrains.qodana.ui.wizard.QodanaWizardStepViewModel
import org.jetbrains.qodana.ui.wizard.QodanaWizardStepViewProvider
import org.jetbrains.qodana.ui.wizard.QodanaWizardTransition
import javax.swing.JComponent

class EditYamlBeforeSetupCIStep(override val viewModel: EditYamlBeforeSetupCIStepViewModel) : QodanaWizardStep {
  companion object {
    const val ID = "EditYamlThenSetupCI"
  }

  override val id: String get() = ID

  override val viewProvider: QodanaWizardStepViewProvider = EditYamlBeforeSetupCIStepViewProvider(viewModel)
}

class EditYamlBeforeSetupCIStepViewModel(
  val previousStepId: String?,
  val scope: CoroutineScope,
  val qodanaYamlViewModel: QodanaYamlViewModel
) : QodanaWizardStepViewModel {
  private val transitionFlow = MutableSharedFlow<QodanaWizardTransition>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

  override val stepTransitionFlow: Flow<Pair<QodanaWizardTransition, String?>> = transitionFlow.map {
    val stepId = if (it == QodanaWizardTransition.PREVIOUS) previousStepId else SetupCIStep.ID
    it to stepId
  }

  fun forward() {
    scope.launch(QodanaDispatchers.Default) {
      val parseResult = qodanaYamlViewModel.parseQodanaYaml().await()
      if (parseResult !is QodanaYamlViewModel.ParseResult.Valid) return@launch

      transitionFlow.emit(QodanaWizardTransition.NEXT)
    }
  }

  fun back() {
    transitionFlow.tryEmit(QodanaWizardTransition.PREVIOUS)
  }
}

private class EditYamlBeforeSetupCIStepViewProvider(val viewModel: EditYamlBeforeSetupCIStepViewModel) : QodanaWizardStepViewProvider {
  override val titleFlow: Flow<String> = createTitleFlow()

  override val mainViewFlow: Flow<JComponent> = createMainViewFlow()

  override val nextButtonDescriptorFlow: Flow<QodanaWizardStepViewProvider.ButtonDescriptor?> = createNextButtonDescriptorFlow()

  override val previousButtonDescriptorFlow: Flow<QodanaWizardStepViewProvider.ButtonDescriptor?> = createPreviousButtonDescriptorFlow()

  fun createTitleFlow(): Flow<String> {
    return viewModel.qodanaYamlViewModel.yamlStateFlow.map {
      val isPhysical = it?.isPhysical == true
      if (isPhysical) {
        QodanaBundle.message("qodana.run.wizard.step.edit.qodana.yam.title")
      } else {
        QodanaBundle.message("qodana.run.wizard.step.add.qodana.yam.title")
      }
    }
  }

  fun createMainViewFlow(): Flow<DialogPanel> {
    return flow {
      coroutineScope {
        emit(
          panel {
            row {
              cell(qodanaYamlView(this@coroutineScope, viewModel.qodanaYamlViewModel).withBottomInsetBeforeComment())
                .align(Align.FILL)
                .resizableColumn()
                .comment(QodanaBundle.message("qodana.run.wizard.step.add.qodana.yam.about"))
                .gap(RightGap.COLUMNS)
            }.resizableRow().bottomGap(BottomGap.SMALL)
          }
        )
        awaitCancellation()
      }
    }
  }

  fun createNextButtonDescriptorFlow(): Flow<QodanaWizardStepViewProvider.ButtonDescriptor> {
    return flowOf(
      QodanaWizardStepViewProvider.ButtonDescriptor(isEnabled = true, QodanaBundle.message("qodana.run.wizard.next.button")) {
        viewModel.forward()
      }
    )
  }

  fun createPreviousButtonDescriptorFlow(): Flow<QodanaWizardStepViewProvider.ButtonDescriptor?> {
    if (viewModel.previousStepId ==  null) return flowOf(null)
    return flowOf(
      QodanaWizardStepViewProvider.ButtonDescriptor(isEnabled = true, QodanaBundle.message("qodana.run.wizard.previous.button")) {
        viewModel.back()
      }
    )
  }
}