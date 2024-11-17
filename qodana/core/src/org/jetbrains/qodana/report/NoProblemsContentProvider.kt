package org.jetbrains.qodana.report

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.util.NlsContexts
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.ui.problemsView.viewModel.QodanaProblemsViewModel
import java.awt.Component
import java.awt.event.InputEvent

interface NoProblemsContentProvider {
  companion object {
    fun showBaselineNoProblemsContent(qodanaProblemsViewModel: QodanaProblemsViewModel, baselineCount: Int): NoProblemsContent {
      val showBaselineAction = ActionDescriptor(QodanaBundle.message("no.problems.content.baseline.action")) { _, _ ->
        qodanaProblemsViewModel.updateProblemsViewState { it.copy(showBaselineProblems = true) }
      }
      return NoProblemsContent(
        title = QodanaBundle.message("no.problems.content.baseline.title"),
        description = QodanaBundle.message("no.problems.content.baseline.description", baselineCount),
        actions = showBaselineAction to null
      )
    }

    fun openOtherReportAction(): ActionDescriptor {
      return ActionDescriptor(QodanaBundle.message("no.problems.content.action.open.another.report")) { content, inputEvent ->
        invokeAction("Qodana.ShowReportGroup", content, inputEvent)
      }
    }

    fun openOtherProjectAction(): ActionDescriptor {
      return ActionDescriptor(QodanaBundle.message("no.problems.content.action.choose.another.project")) { content, inputEvent ->
        invokeAction("ManageRecentProjects", content, inputEvent)
      }
    }
  }

  fun noProblems(qodanaProblemsViewModel: QodanaProblemsViewModel): NoProblemsContent

  fun noProblemsWithBaseline(qodanaProblemsViewModel: QodanaProblemsViewModel, baselineCount: Int): NoProblemsContent {
    return showBaselineNoProblemsContent(qodanaProblemsViewModel, baselineCount)
  }

  fun notMatchingProject(qodanaProblemsViewModel: QodanaProblemsViewModel, totalProblemsCount: Int): NoProblemsContent

  class NoProblemsContent(
    @NlsContexts.StatusText val title: String,
    @NlsContexts.StatusText val description: String?,
    val actions: Pair<ActionDescriptor, ActionDescriptor?>?
  )

  class ActionDescriptor(
    @NlsContexts.StatusText val text: String,
    val action: suspend (component: Component, inputEvent: InputEvent?) -> Unit
  )
}

private suspend fun invokeAction(actionId: String, component: Component, inputEvent: InputEvent?) {
  withContext(QodanaDispatchers.Ui) {
    ActionUtil.invokeAction(
      ActionManager.getInstance().getAction(actionId),
      component,
      ActionPlaces.TOOLWINDOW_CONTENT,
      inputEvent,
      null
    )
  }
}