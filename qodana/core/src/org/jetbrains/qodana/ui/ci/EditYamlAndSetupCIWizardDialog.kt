package org.jetbrains.qodana.ui.ci

import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.project.Project
import com.intellij.platform.util.coroutines.childScope
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.ui.ProjectVcsDataProviderImpl
import org.jetbrains.qodana.ui.run.QodanaYamlViewModelImpl
import org.jetbrains.qodana.ui.run.wizard.*
import org.jetbrains.qodana.ui.wizard.QodanaWizardDialog
import org.jetbrains.qodana.ui.wizard.QodanaWizardViewModel
import java.awt.Dimension

class EditYamlAndSetupCIWizardDialog(
  project: Project,
  scope: CoroutineScope,
  viewModel: QodanaWizardViewModel,
) : QodanaWizardDialog(project, scope, viewModel, Dimension(QODANA_RUN_WIZARD_DIALOG_WIDTH, QODANA_RUN_WIZARD_DIALOG_HEIGHT), isModal = false) {
  companion object {

    const val WIZARD_ID = "YamlAndCI"

    fun create(project: Project): EditYamlAndSetupCIWizardDialog {
      val dialogScope = project.qodanaProjectScope.childScope(ModalityState.nonModal().asContextElement())

      val projectVcsDataProvider = ProjectVcsDataProviderImpl(project, dialogScope)
      val qodanaYamlViewModel = QodanaYamlViewModelImpl(project, dialogScope)
      val editYamlBeforeSetupCIStep = EditYamlBeforeSetupCIStep(EditYamlBeforeSetupCIStepViewModel(previousStepId = null, dialogScope, qodanaYamlViewModel))
      val steps = listOf(
        editYamlBeforeSetupCIStep,
        SetupCIStep(SetupCIStepViewModel(qodanaYamlViewModel, CombinedSetupCIViewModel(project, dialogScope, projectVcsDataProvider)))
      )
      val wizardViewModel = QodanaWizardViewModel(WIZARD_ID, project, dialogScope, steps, editYamlBeforeSetupCIStep)
      return EditYamlAndSetupCIWizardDialog(project, dialogScope, wizardViewModel)
    }
  }
}