package org.jetbrains.qodana.ui.run.wizard

import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.project.Project
import com.intellij.platform.util.coroutines.childScope
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.ui.ProjectVcsDataProviderImpl
import org.jetbrains.qodana.ui.run.LocalRunQodanaViewModel
import org.jetbrains.qodana.ui.run.QodanaYamlViewModelImpl
import org.jetbrains.qodana.ui.wizard.QodanaWizardDialog
import org.jetbrains.qodana.ui.wizard.QodanaWizardViewModel
import java.awt.Dimension

const val QODANA_RUN_WIZARD_DIALOG_WIDTH = 770
const val QODANA_RUN_WIZARD_DIALOG_HEIGHT = 700

class RunQodanaWizard(project: Project, scope: CoroutineScope, viewModel: QodanaWizardViewModel) :
  QodanaWizardDialog(project, scope, viewModel, Dimension(QODANA_RUN_WIZARD_DIALOG_WIDTH, QODANA_RUN_WIZARD_DIALOG_HEIGHT), isModal = false) {
  companion object {

    const val WIZARD_ID = "RunQodana"

    fun create(project: Project): RunQodanaWizard {
      val dialogScope = project.qodanaProjectScope.childScope(ModalityState.nonModal().asContextElement())

      val projectVcsDataProvider = ProjectVcsDataProviderImpl(project, dialogScope)

      val qodanaYamlViewModel = QodanaYamlViewModelImpl(project, dialogScope)
      val editYamlAndRunQodanaStep = EditYamlAndRunQodanaStep(
        (EditYamlAndRunStepViewModel(project, LocalRunQodanaViewModel(project, dialogScope, qodanaYamlViewModel, projectVcsDataProvider)))
      )
      val steps = listOf(
        editYamlAndRunQodanaStep
      )
      val wizardViewModel = QodanaWizardViewModel(WIZARD_ID, project, dialogScope, steps, editYamlAndRunQodanaStep)
      return RunQodanaWizard(project, dialogScope, wizardViewModel)
    }
  }
}