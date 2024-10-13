package org.jetbrains.qodana.ui.protocol

import com.intellij.ide.ui.laf.darcula.ui.DarculaButtonUI
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.NlsSafe
import com.intellij.platform.util.coroutines.childScope
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.components.panels.Wrapper
import com.intellij.ui.dsl.builder.*
import com.intellij.util.IconUtil
import com.intellij.util.Urls
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.NamedColorUtil
import icons.QodanaIcons
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.cloud.QodanaCloudDefaultUrls
import org.jetbrains.qodana.cloud.api.name
import org.jetbrains.qodana.cloud.currentQodanaCloudFrontendUrl
import org.jetbrains.qodana.cloud.hostsEqual
import org.jetbrains.qodana.cloud.projectFrontendUrlForQodanaCloud
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.cloudclient.v1.QDCloudSchema
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import org.jetbrains.qodana.protocol.OpenInIdeCloudParameters
import org.jetbrains.qodana.stats.SourceUserState
import org.jetbrains.qodana.ui.setContentAndRepaint
import org.jetbrains.qodana.ui.settings.LogInViewModel
import java.awt.Dimension
import java.net.MalformedURLException
import javax.swing.Icon
import javax.swing.JComponent

@OptIn(ExperimentalCoroutinesApi::class)
class OpenInIdeLogInDialog(
  val openInIdeCloudParameters: OpenInIdeCloudParameters,
  project: Project
) : DialogWrapper(project) {
  private val scope = project.qodanaProjectScope.childScope(ModalityState.any().asContextElement())

  private val viewModel = LogInViewModel(scope, project, SourceUserState.OPEN_IN_IDE_DIALOG)

  private val isFinishAvailableFlow = viewModel.uiStateFlow.map {
    it is LogInViewModel.UiState.Authorized && hostsEqual(it.serverName, openInIdeCloudParameters.cloudHost)
  }

  private val viewWrapper = Wrapper()

  init {
    setOKButtonText(QodanaBundle.message("qodana.open.in.ide.log.in.dialog.continue.button"))
    isOKActionEnabled = false
    title = QodanaBundle.message("qodana.open.in.ide.log.in.dialog.title", ApplicationNamesInfo.getInstance().fullProductName)
    scope.launch(QodanaDispatchers.Ui, start = CoroutineStart.UNDISPATCHED) {
      launch(start = CoroutineStart.UNDISPATCHED) {
        viewModel.uiStateFlow
          .flatMapLatest { uiState ->
            when(uiState) {
              is LogInViewModel.UiState.NotAuthorized -> flowOf(notAuthorizedView(uiState))
              is LogInViewModel.UiState.Authorizing -> flowOf(authorizingView(uiState))
              is LogInViewModel.UiState.Authorized -> authorizedViewFlow(uiState)
              null -> flowOf(panel {})
            }
          }.collect {
            viewWrapper.setContentAndRepaint(it)
            updatePreferredFocusedComponent()
          }
      }
      launch {
        isFinishAvailableFlow.collect {
          isOKActionEnabled = it
        }
      }
    }
    init()
  }

  override fun getPreferredFocusedComponent(): JComponent? {
    if (isOKActionEnabled) {
      return getButton(okAction)
    }
    return (viewWrapper.targetComponent as? DialogPanel)?.preferredFocusedComponent
  }

  private fun updatePreferredFocusedComponent() {
    preferredFocusedComponent?.requestFocusInWindow()
  }

  override fun createCenterPanel(): JComponent {
    return panel {
      row {
        cell(viewWrapper)
          .align(Align.CENTER)
          .resizableColumn()
      }.resizableRow()
    }.apply {
      val size = Dimension(400, 400)
      minimumSize = size
      preferredSize = size
    }
  }

  override fun dispose() {
    scope.cancel()
    super.dispose()
  }

  private fun notAuthorizedView(notAuthorized: LogInViewModel.UiState.NotAuthorized): DialogPanel {
    return panel {
      row {
        icon(QodanaIcons.Images.Qodana)
          .align(AlignX.CENTER)
      }
      row {
        text(QodanaBundle.message("qodana.open.in.ide.log.in.dialog.log.in.description", openInIdeCloudParameters.htmlLinkElement()))
          .align(AlignX.CENTER)
          .applyToComponent {
            font = JBFont.label().biggerOn(1F)
          }
      }.bottomGap(BottomGap.SMALL)
      row {
        button(QodanaBundle.message("qodana.settings.panel.log.in.button")) {
          val serverUrl = try {
            openInIdeCloudParameters.cloudHost?.let { Urls.newFromEncoded(it) }
          } catch (e: MalformedURLException) {
            Urls.newFromEncoded(QodanaCloudDefaultUrls.websiteUrl)
          }
          notAuthorized.authorizeAction.invoke(serverUrl)
        }.applyToComponent {
          putClientProperty(DarculaButtonUI.DEFAULT_STYLE_KEY, true)
        }.align(AlignX.CENTER).focused()
      }
    }
  }

  private fun authorizingView(authorizing: LogInViewModel.UiState.Authorizing): DialogPanel {
    return panel {
      row {
        icon(progressIconScaled())
          .align(AlignX.CENTER)
      }.bottomGap(BottomGap.SMALL)
      row {
        label(QodanaBundle.message("qodana.open.in.ide.log.in.dialog.authorizing"))
          .align(AlignX.CENTER)
      }.bottomGap(BottomGap.MEDIUM)
      row {
        button(QodanaBundle.message("qodana.settings.panel.cancel.button")) {
          authorizing.cancelAction.invoke()
        }.applyToComponent {
          putClientProperty(DarculaButtonUI.DEFAULT_STYLE_KEY, true)
        }.align(AlignX.CENTER).focused()
          .comment(QodanaBundle.message("qodana.settings.panel.check.license")) {
            authorizing.checkLicenseActon.invoke()
          }
      }
    }
  }

  private fun authorizedViewFlow(authorized: LogInViewModel.UiState.Authorized): Flow<DialogPanel> {
    return authorized.infoFlow.map { authorizedInfo ->
      when {
        authorizedInfo.lastLoadedUserInfo is QDCloudResponse.Success -> {
          if (hostsEqual(authorized.serverName, openInIdeCloudParameters.cloudHost)) {
            authorizedWithUserInfoView(authorizedInfo.lastLoadedUserInfo.value, authorizedInfo.logOutAction)
          } else {
            authorizedDifferentCloudView(authorizedInfo.lastLoadedUserInfo.value, authorizedInfo.logOutAction)
          }
        }
        authorizedInfo.isRefreshing -> {
          fetchingUserDataView(authorizedInfo.logOutAction)
        }
        authorizedInfo.lastLoadedUserInfo is QDCloudResponse.Error -> {
          errorFetchingUserInfoView(authorizedInfo.lastLoadedUserInfo, authorizedInfo.refreshAction, authorizedInfo.logOutAction)
        }
        else -> panel { }
      }
    }
  }

  private fun authorizedWithUserInfoView(userInfo: QDCloudSchema.UserInfo, logOutAction: () -> Unit): DialogPanel {
    return panel {
      row {
        label(QodanaBundle.message("qodana.open.in.ide.log.in.dialog.logged.it"))
          .align(AlignX.CENTER)
          .applyToComponent {
            font = JBFont.h3()
          }
      }
      row {
        text(QodanaBundle.message("qodana.open.in.ide.log.in.dialog.logged.in.description", userInfo.name, openInIdeCloudParameters.htmlLinkElement()))
          .align(AlignX.CENTER)
          .applyToComponent {
            foreground = NamedColorUtil.getInactiveTextColor()
            font = JBFont.medium()
          }
      }
      row {
        button(QodanaBundle.message("qodana.settings.panel.log.out.button")) {
          logOutAction.invoke()
        }.align(AlignX.CENTER).focused()
      }
    }
  }

  private fun authorizedDifferentCloudView(userInfo: QDCloudSchema.UserInfo, logOutAction: () -> Unit): DialogPanel {
    return panel {
      row {
        label(QodanaBundle.message("qodana.open.in.ide.log.in.dialog.logged.in.another.account"))
          .align(AlignX.CENTER)
          .applyToComponent {
            font = JBFont.h3()
          }
      }
      row {
        text(QodanaBundle.message("qodana.open.in.ide.log.in.dialog.logged.in.another.account",
                                  userInfo.name, openInIdeCloudParameters.htmlLinkElement()))
          .align(AlignX.CENTER)
          .applyToComponent {
            foreground = NamedColorUtil.getInactiveTextColor()
            font = JBFont.medium()
          }
      }
      row {
        button(QodanaBundle.message("qodana.settings.panel.log.out.button")) {
          logOutAction.invoke()
        }.align(AlignX.CENTER).focused()
      }
    }
  }

  private fun fetchingUserDataView(cancelAction: () -> Unit): DialogPanel {
    return panel {
      row {
        icon(progressIconScaled())
          .align(AlignX.CENTER)
      }.bottomGap(BottomGap.SMALL)
      row {
        label(QodanaBundle.message("qodana.settings.panel.fetching.user.data"))
          .align(AlignX.CENTER)
      }.bottomGap(BottomGap.MEDIUM)
      row {
        button(QodanaBundle.message("qodana.settings.panel.cancel.button")) {
          cancelAction.invoke()
        }.align(AlignX.CENTER).focused()
      }
    }
  }

  private fun errorFetchingUserInfoView(
    error: QDCloudResponse.Error,
    refreshAction: () -> Unit,
    logOutAction: () -> Unit
  ): DialogPanel {
    return panel {
      row {
        label(QodanaBundle.message("qodana.open.in.ide.log.in.dialog.logged.it"))
          .align(AlignX.CENTER)
      }
      row {
        panel {
          row {
            label(getErrorMessage(error)).gap(RightGap.SMALL)
            link(QodanaBundle.message("qodana.settings.panel.refresh")) { refreshAction.invoke() }
          }
        }.align(AlignX.CENTER)
      }
      row {
        button(QodanaBundle.message("qodana.settings.panel.log.out.button")) {
          logOutAction.invoke()
        }.align(AlignX.CENTER)
      }
    }
  }

  private fun getErrorMessage(error: QDCloudResponse.Error): @NlsSafe String {
    return when(error) {
      is QDCloudResponse.Error.Offline -> QodanaBundle.message("qodana.settings.panel.offline")
      is QDCloudResponse.Error.ResponseFailure -> QodanaBundle.message("qodana.settings.panel.error", error.errorMessage)
    }
  }
}

private fun progressIconScaled(): Icon {
  return IconUtil.scale(AnimatedIcon.Default(), null, 2F)
}

private fun OpenInIdeCloudParameters.htmlLinkElement(): String {
  val projectName = this.projectName ?: "qodana.cloud"
  val projectLink = this.projectId?.let { projectFrontendUrlForQodanaCloud(projectId, reportId, cloudHost) } ?: currentQodanaCloudFrontendUrl()
  return "<a href='$projectLink'>$projectName</a>"
}