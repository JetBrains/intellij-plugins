package org.jetbrains.qodana.ui.ci

import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.platform.util.coroutines.childScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.ui.ProjectVcsDataProviderImpl
import javax.swing.JComponent

class SetupCIDialog(val project: Project) : DialogWrapper(project) {
  private val scope: CoroutineScope = project.qodanaProjectScope.childScope(ModalityState.nonModal().asContextElement())

  private val view = CombinedSetupCIView(
    scope,
    CombinedSetupCIViewModel(project, scope, ProjectVcsDataProviderImpl(project, scope)),
    CombinedSetupCIViewSpec()
  )

  init {
    title = QodanaBundle.message("qodana.add.to.ci.title")
    isModal = false
    init()
    scope.launch(QodanaDispatchers.Ui) {
      launch {
        view.viewModel.isFinishAvailableFlow.collect {
          isOKActionEnabled = it
        }
      }
      launch {
        view.viewModel.finishHappenedFlow.collect {
          applyFields()
          close(OK_EXIT_CODE)
        }
      }
      launch {
        view.viewModel.nextButtonTextFlow.filterNotNull().collect {
          buttonMap[okAction]?.text = it
        }
      }
    }
  }

  override fun doOKAction() {
    view.viewModel.finish()
  }

  override fun getStyle(): DialogStyle = DialogStyle.COMPACT

  override fun createCenterPanel(): JComponent = view.getView()

  override fun dispose() {
    scope.cancel()
    super.dispose()
  }
}