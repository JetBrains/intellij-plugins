package org.jetbrains.qodana.ui.link

import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.NlsSafe
import com.intellij.platform.util.coroutines.childScope
import com.intellij.ui.components.panels.Wrapper
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.cloud.QodanaCloudStateService
import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.cloud.api.name
import org.jetbrains.qodana.cloud.openBrowserWithCurrentQodanaCloudFrontend
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.stats.CreateProjectSource
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.Action
import javax.swing.JComponent

private val DIALOG_SIZE = JBUI.size(630, 650)

class LinkCloudProjectDialog(project: Project) : DialogWrapper(project, false) {
  private val scope: CoroutineScope = project.qodanaProjectScope.childScope(ModalityState.any().asContextElement())

  private val linkCloudProjectView =
    LinkCloudProjectView(scope, project, LinkCloudProjectViewModel(project, scope), afterProjectCreation = { close(OK_EXIT_CODE) })

  private val createAction = object : AbstractAction(QodanaBundle.message("qodana.link.project.dialog.create.project")) {
    override fun actionPerformed(e: ActionEvent?) {
      openBrowserWithCurrentQodanaCloudFrontend()
      QodanaPluginStatsCounterCollector.CREATE_PROJECT_PRESSED.log(CreateProjectSource.SOUTH_PANEL)
    }
  }

  private val wrapper = Wrapper()

  init {
    title = QodanaBundle.message("qodana.link.project.dialog.title")
    setOKButtonText(QodanaBundle.message("qodana.link.project.dialog.ok.button.text"))
    init()
    scope.launch(QodanaDispatchers.Ui) {
      linkCloudProjectView.viewModel.selectedProject.collect { selectedProject ->
        isOKActionEnabled = selectedProject != null
      }
    }
    scope.launch {
      linkCloudProjectView.emptyStateFlow.collectLatest { flow ->
        flow.collect {
          buttonMap[createAction]?.isVisible = !it
        }
      }
    }
    scope.launch {
      QodanaCloudStateService.getInstance().userState.collectLatest { state ->
        when (state) {
          is UserState.Authorized -> wrapper.setContent(createPanel(state))
          else -> wrapper.setContent(panel {})
        }
      }
    }
  }

  override fun dispose() {
    scope.cancel()
    super.dispose()
  }

  override fun doOKAction() {
    linkCloudProjectView.viewModel.finishAndLinkWithSelectedCloudProject()
    close(OK_EXIT_CODE)
  }

  override fun createCenterPanel(): JComponent {
    return wrapper.apply {
      minimumSize = DIALOG_SIZE
      preferredSize = DIALOG_SIZE
    }
  }

  override fun createLeftSideActions(): Array<Action> {
    return arrayOf(createAction)
  }

  private fun createPanel(userState: UserState.Authorized): JComponent {
    return panel {
      @NlsSafe val userName = when (val info = userState.userDataProvider.userInfo.value.lastLoadedValue) {
        is QDCloudResponse.Success -> info.value.name
        else -> null
      }
      if (userName != null) {
        row { label(userName) }
      }
      row {
        button(QodanaBundle.message("qodana.settings.panel.log.out.button")) {
          userState.logOut()
          close(OK_EXIT_CODE)
        }
      }
      separator()
      row {
        cell(linkCloudProjectView.getView())
          .align(Align.FILL)
      }.resizableRow()
    }
  }
}