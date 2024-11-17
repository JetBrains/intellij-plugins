package org.jetbrains.qodana.cloud

import com.intellij.openapi.components.*
import com.intellij.util.Urls
import com.intellij.util.xmlb.annotations.Attribute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.VisibleForTesting
import org.jetbrains.qodana.cloud.api.IjQDCloudClient
import org.jetbrains.qodana.cloud.authorization.credentials.QodanaCloudCredentialsFromPersistence
import org.jetbrains.qodana.cloud.impl.AuthorizedImpl
import org.jetbrains.qodana.cloud.impl.AuthorizingImpl
import org.jetbrains.qodana.cloud.impl.NotAuthorizedImpl
import org.jetbrains.qodana.cloudclient.asSuccess
import org.jetbrains.qodana.cloudclient.v1.QDCloudSchema
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.stats.StatsUserState
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.minutes

private const val QODANA_CLOUD_STORAGE = "QodanaCloud.xml"

/**
 * Main service for managing user's state in cloud.
 *
 * Use [userState] flow to retrieve and observe [UserState]
 */
@ApiStatus.Internal
@State(name = "QodanaCloudUserState", storages = [Storage(value = QODANA_CLOUD_STORAGE, roamingType = RoamingType.DISABLED)])
@Service(Service.Level.APP)
class QodanaCloudStateService(private val scope: CoroutineScope) : PersistentStateComponent<QodanaCloudStateService.ServiceState> {
  companion object {
    fun getInstance(): QodanaCloudStateService = service()

    private val _userStateUpdates = MutableSharedFlow<UserState>()
    val userStateUpdates = _userStateUpdates.asSharedFlow()
  }

  @VisibleForTesting
  var authorizationExpirationTimeout = 10.minutes

  @Suppress("RemoveExplicitTypeArguments")
  private val stateManager = StateManager<UserState> { NotAuthorizedImpl(it, null) }
  val userState: StateFlow<UserState>
    get() {
      val userStateFlow = stateManager.state
      // why launch coroutines only from here: QD-6818
      startStatesUpdatesSubscription(userStateFlow)
      return userStateFlow
    }

  private val statesUpdatesSubscriptionStarted = AtomicBoolean(false)

  private fun startStatesUpdatesSubscription(userStateFlow: StateFlow<UserState>) {
    if (!statesUpdatesSubscriptionStarted.compareAndSet(false, true)) return

    scope.launch(QodanaDispatchers.Default) {
      launch {
        userStateFlow.drop(1).collect {
          _userStateUpdates.emit(it)
        }
      }
      launch {
        userStateFlow.collectLatest { userState ->
          supervisorScope {
            when(userState) {
              is AuthorizedImpl -> {
                launch {
                  userState.userDataProvider.startComputeRequestsProcessing()
                }
                launch {
                  userState.startProcessingAccessTokenRequests()
                }
                userState.userDataProvider.refreshUserInfo()
              }
              is AuthorizingImpl -> {
                launch {
                  userState.startOAuth()
                }
              }
              else -> {}
            }
          }
        }
      }
    }
  }

  @Deprecated("use only for statistics, doesn't launch any state-observing coroutines, QD-6818")
  internal fun getUserStateStatsState(): StatsUserState {
    return when(stateManager.state.value) {
      is UserState.Authorized -> StatsUserState.AUTHORIZED
      is UserState.Authorizing -> StatsUserState.AUTHORIZING
      is UserState.NotAuthorized -> StatsUserState.NOT_AUTHORIZED
    }
  }

  override fun getState(): ServiceState {
    // value from state manager to not trigger coroutines QD-6818
    val authorized = stateManager.state.value as? AuthorizedImpl

    val lastLoadedUserInfo = authorized?.userDataProvider?.userInfo?.value?.lastLoadedValue?.asSuccess()
    return ServiceState().apply {
      isAuthorized = authorized != null
      userName = lastLoadedUserInfo?.username
      fullName = lastLoadedUserInfo?.fullName
      userId = lastLoadedUserInfo?.id
      frontendUrl = authorized?.selfHostedFrontendUrl?.toString()
    }
  }

  override fun loadState(state: ServiceState) {
    val userState = getUserStateFromPersisted(state)
    stateManager.changeState(this@QodanaCloudStateService.userState.value, userState)
  }

  private fun getUserStateFromPersisted(persistentState: ServiceState): UserState {
    val notAuthorized = NotAuthorizedImpl(stateManager, null)
    if (!persistentState.isAuthorized) return notAuthorized

    val userId = persistentState.userId
    val userName = persistentState.userName
    val fullName = persistentState.fullName
    val userInfoResponse = userId?.let { userName?.let { QDCloudSchema.UserInfo(userId, fullName, userName) } } ?: return notAuthorized
    val refreshTokenPersistence = QodanaCloudRefreshTokenPersistence()
    val selfHostedUrl = persistentState.frontendUrl?.let { Urls.newFromEncoded(it) }
    val frontendUrl = persistentState.frontendUrl ?: QodanaCloudDefaultUrls.websiteUrl

    return AuthorizedImpl(
      stateManager,
      refreshTokenPersistence,
      IjQDCloudClient(frontendUrl),
      QodanaCloudCredentialsFromPersistence(refreshTokenPersistence),
      userInfoResponse,
      selfHostedUrl
    )
  }

  class ServiceState {
    @get:Attribute
    var isAuthorized: Boolean = false

    @get:Attribute
    var userName: String? = null

    @get:Attribute
    var fullName: String? = null

    @get:Attribute
    var userId: String? = null

    @get:Attribute
    var frontendUrl: String? = null
  }
}

