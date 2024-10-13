@file:OptIn(ExperimentalCoroutinesApi::class)

package org.jetbrains.qodana.ui.settings

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.platform.util.coroutines.childScope
import com.intellij.ui.components.panels.Wrapper
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.DslComponentProperty
import com.intellij.ui.dsl.builder.RightGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import com.intellij.util.ui.AsyncProcessIcon
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.cloud.api.name
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.cloudclient.v1.QDCloudSchema
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.stats.SourceUserState
import org.jetbrains.qodana.ui.buildSettingsLogInPanel
import org.jetbrains.qodana.ui.link.LinkCloudProjectView
import org.jetbrains.qodana.ui.link.LinkCloudProjectViewModel
import org.jetbrains.qodana.ui.setContentAndRepaint
import javax.swing.JComponent
import javax.swing.JPanel

internal class QodanaCloudSettingsView(
  project: Project
) : Disposable {
  private val currentStateView = Wrapper().apply {
    border = JBUI.Borders.empty()
  }

  private val scope: CoroutineScope = project.qodanaProjectScope.childScope(ModalityState.any().asContextElement())

  val viewModel = QodanaCloudSettingsViewModel(scope, project)

  init {
    scope.launch(QodanaDispatchers.Ui, start = CoroutineStart.UNDISPATCHED) {
      viewModel.uiStateFlow.collectLatest { state ->
        when (state) {
          is QodanaCloudSettingsViewModel.UiState.NotAuthorized ->
            currentStateView.setContentAndRepaint(buildSettingsLogInPanel(this, state.logInUiState.serverName) { state.logInUiState.authorizeAction.invoke(it) })
          is QodanaCloudSettingsViewModel.UiState.Authorizing ->
            currentStateView.setContentAndRepaint(buildSettingConnectingPanel(
              state.logInUiState.serverName,
              { state.logInUiState.cancelAction.invoke() },
              { state.logInUiState.checkLicenseActon.invoke()})
            )
          is QodanaCloudSettingsViewModel.UiState.Authorized -> {
            coroutineScope {
              val listView = LinkCloudProjectView(this, project, state.linkViewModel)
              state.logInUiState.infoFlow.collect { info ->
                val infoPanel = getUserInfoPanel(listView, info)
                currentStateView.setContentAndRepaint(infoPanel)
              }
              awaitCancellation()
            }
          }
          null -> currentStateView.setContent(panel {})
        }
      }
    }
  }

  override fun dispose() {
    scope.cancel()
  }

  fun getView(): JPanel = currentStateView

  private fun getUserInfoPanel(listView: LinkCloudProjectView, state: LogInViewModel.AuthorizedInfo): JComponent {
    return when(state.lastLoadedUserInfo) {
      is QDCloudResponse.Success -> buildSettingLoggedInPanel(listView, state.lastLoadedUserInfo.value, state.logOutAction)
      is QDCloudResponse.Error -> {
        if (state.isRefreshing) {
          buildSettingsFetchingUserDataPanel(state.logOutAction)
        }
        else {
          buildSettingsErrorFetchingUserData(state.lastLoadedUserInfo, state.refreshAction, state.logOutAction)
        }
      }
      null -> buildSettingsFetchingUserDataPanel(state.logOutAction)
    }
  }

  private fun buildSettingConnectingPanel(serverName: String?, cancelAction: () -> Unit, checkLicenseAction: () -> Unit) = panel {
    row {
      label(QodanaBundle.message("qodana.settings.panel.connecting.label", serverName ?: "Qodana"))
      cell(AsyncProcessIcon("Connecting to Qodana"))
    }
    row {
      button(QodanaBundle.message("qodana.settings.panel.cancel.button")) { cancelAction.invoke() }
        .apply { component.putClientProperty(DslComponentProperty.VISUAL_PADDINGS, UnscaledGaps.EMPTY) }
        .comment(QodanaBundle.message("qodana.settings.panel.check.license")) { checkLicenseAction.invoke() }
    }
  }

  private fun buildSettingsFetchingUserDataPanel(cancelAction: () -> Unit) = panel {
    row {
      label(QodanaBundle.message("qodana.settings.panel.fetching.user.data"))
      cell(AsyncProcessIcon("Fetching data"))
    }
    row {
      button(QodanaBundle.message("qodana.settings.panel.cancel.button")) { cancelAction.invoke() }
        .apply { component.putClientProperty(DslComponentProperty.VISUAL_PADDINGS, UnscaledGaps.EMPTY) }
    }
  }

  private fun buildSettingLoggedInPanel(
    listView: LinkCloudProjectView,
    userInfo: QDCloudSchema.UserInfo,
    logOutAction: () -> Unit
  ) = panel {
    @NlsSafe val userName = userInfo.name
    row { label(userName) }
    row {
      button(QodanaBundle.message("qodana.settings.panel.log.out.button")) { logOutAction.invoke() }
        .apply { component.putClientProperty(DslComponentProperty.VISUAL_PADDINGS, UnscaledGaps.EMPTY) }
    }
    separator()
    row {
      cell(listView.getView())
        .align(Align.FILL)
    }.resizableRow()
  }

  private fun buildSettingsErrorFetchingUserData(
    error: QDCloudResponse.Error,
    refreshAction: () -> Unit,
    logOutAction: () -> Unit
  ) = panel {
    row {
      label(getErrorMessage(error)).gap(RightGap.SMALL)
      link(QodanaBundle.message("qodana.settings.panel.refresh")) { refreshAction.invoke() }
    }
    row {
      button(QodanaBundle.message("qodana.settings.panel.log.out.button")) { logOutAction.invoke() }
        .apply { component.putClientProperty(DslComponentProperty.VISUAL_PADDINGS, UnscaledGaps.EMPTY) }
    }
  }

  private fun getErrorMessage(error: QDCloudResponse.Error): @NlsSafe String {
    return when(error) {
      is QDCloudResponse.Error.Offline ->
        QodanaBundle.message("qodana.settings.panel.offline")
      is QDCloudResponse.Error.ResponseFailure ->
        QodanaBundle.message("qodana.settings.panel.error", error.errorMessage)
    }
  }
}

internal class QodanaCloudSettingsViewModel(scope: CoroutineScope, project: Project) {
  private val logInViewModel = LogInViewModel(scope, project, SourceUserState.QODANA_SETTINGS_PANEL)

  val uiStateFlow: StateFlow<UiState?> = logInViewModel.uiStateFlow
    .transformLatest { uiState ->
      when (uiState) {
        is LogInViewModel.UiState.NotAuthorized -> {
          emit(UiState.NotAuthorized(uiState))
        }
        is LogInViewModel.UiState.Authorizing -> {
          emit(UiState.Authorizing(uiState))
        }
        is LogInViewModel.UiState.Authorized -> {
          coroutineScope {
            val linkViewModel = LinkCloudProjectViewModel(project, this)
            emit(UiState.Authorized(linkViewModel, uiState))
            awaitCancellation()
          }
        }
        else -> {
          emit(null)
        }
      }
    }
    .flowOn(QodanaDispatchers.Default)
    .stateIn(scope, SharingStarted.Lazily, null)

  val areSettingsModified: Boolean
    get() {
      val authorizedUiState = uiStateFlow.value as? UiState.Authorized ?: return false
      return authorizedUiState.linkViewModel.isModified()
    }

  fun finish() {
    val authorizedUiState = uiStateFlow.value as? UiState.Authorized ?: return
    authorizedUiState.linkViewModel.finishAndLinkWithSelectedCloudProject()
  }

  internal sealed interface UiState {
    class NotAuthorized(val logInUiState: LogInViewModel.UiState.NotAuthorized) : UiState

    class Authorizing(val logInUiState: LogInViewModel.UiState.Authorizing) : UiState

    class Authorized(val linkViewModel: LinkCloudProjectViewModel, val logInUiState: LogInViewModel.UiState.Authorized) : UiState
  }
}