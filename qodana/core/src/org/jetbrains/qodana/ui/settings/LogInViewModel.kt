package org.jetbrains.qodana.ui.settings

import com.intellij.openapi.project.Project
import com.intellij.util.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.qodana.cloud.QodanaCloudStateService
import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.cloudclient.v1.QDCloudSchema
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.stats.SourceUserState
import org.jetbrains.qodana.stats.StatsUserState

internal class LogInViewModel(private val scope: CoroutineScope,
                              private val project: Project,
                              private val logCollectorSourceUserState: SourceUserState) {
  val uiStateFlow: StateFlow<UiState?> = createUiStateFlow()

  private fun createUiStateFlow(): StateFlow<UiState?> {
    val uiStateFlow: MutableStateFlow<UiState?> = MutableStateFlow(null)
    scope.launch(QodanaDispatchers.Default) {
      QodanaCloudStateService.getInstance().userState.collectLatest { newState ->
        coroutineScope {
          uiStateFlow.value = getUiStateByUserState(this, newState)
          awaitCancellation()
        }
      }
    }
    return uiStateFlow
  }

  private suspend fun getUiStateByUserState(innerScope: CoroutineScope, userState: UserState): UiState {
    return when (userState) {
      is UserState.NotAuthorized -> UiState.NotAuthorized(
        userState.selfHostedFrontendUrl?.toExternalForm(),
        authorizeAction = {
          userState.authorize(it)
          logNewUserStateStats(StatsUserState.AUTHORIZING)
        }
      )
      is UserState.Authorizing -> UiState.Authorizing(
        userState.selfHostedFrontendUrl?.toExternalForm(),
        cancelAction = {
          userState.cancelAuthorization()
          logNewUserStateStats(StatsUserState.NOT_AUTHORIZED)
        },
        checkLicenseActon = {
          innerScope.launch {
            userState.checkLicenseStatus()
          }
        })
      is UserState.Authorized -> {
        val userDataProvider = userState.userDataProvider
        innerScope.launch {
          userDataProvider.refreshUserInfoLoop()
        }
        val authorizedInfoFlow = userState.userDataProvider.userInfo.map { propertyState ->
          AuthorizedInfo(
            propertyState.lastLoadedValue,
            propertyState.isRefreshing,
            refreshAction = {
              if (propertyState.isRefreshing) return@AuthorizedInfo
              innerScope.launch {
                userDataProvider.refreshUserInfo()
              }
            },
            logOutAction = {
              userState.logOut()
              logNewUserStateStats(StatsUserState.NOT_AUTHORIZED)
            }
          )
        }
        UiState.Authorized(userState.selfHostedFrontendUrl?.toExternalForm(), authorizedInfoFlow)
      }
    }
  }

  private fun logNewUserStateStats(newState: StatsUserState) {
    QodanaPluginStatsCounterCollector.UPDATE_CLOUD_USER_STATE.log(
      project,
      newState,
      logCollectorSourceUserState
    )
  }

  sealed interface UiState {
    class NotAuthorized(val serverName: String?, val authorizeAction: (Url?) -> Unit) : UiState

    class Authorizing(val serverName: String?, val cancelAction: () -> Unit, val checkLicenseActon: () -> Unit) : UiState

    class Authorized(val serverName: String?, val infoFlow: Flow<AuthorizedInfo>) : UiState
  }

  class AuthorizedInfo(
    val lastLoadedUserInfo: QDCloudResponse<QDCloudSchema.UserInfo>?,
    val isRefreshing: Boolean,
    val refreshAction: () -> Unit,
    val logOutAction: () -> Unit
  )
}