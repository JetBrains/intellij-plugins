package org.jetbrains.qodana.cloud.impl

import com.intellij.notification.NotificationType
import com.intellij.openapi.components.service
import com.intellij.util.Url
import com.intellij.util.Urls
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withTimeout
import org.jetbrains.ide.BuiltInServerManager
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.cloud.*
import org.jetbrains.qodana.cloud.api.*
import org.jetbrains.qodana.cloud.authorization.QodanaCloudOAuthRequest
import org.jetbrains.qodana.cloud.authorization.QodanaCloudOAuthService
import org.jetbrains.qodana.cloud.authorization.credentials.QodanaCloudCredentials
import org.jetbrains.qodana.cloudclient.QDCloudClient
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.cloudclient.qodanaCloudResponse
import org.jetbrains.qodana.cloudclient.v1.QDCloudUserApiV1
import org.jetbrains.qodana.notifications.QodanaNotifications
import org.jetbrains.qodana.registry.QodanaRegistry
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.stats.SourceUserState
import org.jetbrains.qodana.stats.StatsUserState
import java.net.MalformedURLException
import kotlin.time.Duration.Companion.seconds

internal class AuthorizingImpl(
  private val stateManager: StateManager<UserState>,
  override val selfHostedFrontendUrl: Url?
) : UserState.Authorizing {
  private val licenseAgreementAcceptedCallbackFlow = MutableSharedFlow<Pair<String, Boolean>>(replay = 1)

  private val updateFlow: MutableSharedFlow<Unit> = MutableSharedFlow(replay = 1)

  suspend fun startOAuth() {
    val frontendUrlString: String = frontendUrl.toExternalForm()
    var finalState: UserState = NotAuthorizedImpl(stateManager, selfHostedFrontendUrl)
    try {
      val cloudClient = IjQDCloudClient(frontendUrlString)
      val port = BuiltInServerManager.getInstance().port
      val oauthData = qodanaCloudResponse {
        cloudClient.v1().value()
          .notAuthorizedApi().getOAuthProviderData().value()
      }
      when(oauthData) {
        is QDCloudResponse.Error -> {
          oauthData.getErrorNotification(QodanaBundle.message("notification.title.cloud.failed.to.authorize")).notify(null)
          return
        }
        is QDCloudResponse.Success -> {}
      }
      val oauthUrl = try {
        Urls.newFromEncoded(oauthData.value.oauthUrl)
      } catch (e: MalformedURLException) {
        QodanaNotifications.General.notification(
          QodanaBundle.message("notification.title.cloud.failed.to.authorize"),
          QodanaBundle.message("notification.content.cloud.incorrect.oauth.url", frontendUrlString, oauthData.value.oauthUrl),
          NotificationType.ERROR,
          withQodanaIcon = true
        ).notify(null)
        return
      }
      val oauthRequest = QodanaCloudOAuthRequest(
        port,
        frontendUrl = frontendUrlString,
        cloudClient,
        authUrl = oauthUrl
      )

      val credentials = QodanaCloudOAuthService.getInstance().authorize(oauthRequest).await()
      finalState = checkUserLicenseAndWaitForCallbackIfNeeded(credentials, cloudClient) ?: return
      logSuccessfullyAuthorizedStats()
    }
    finally {
      stateManager.changeState(this, finalState)
    }
  }

  private suspend fun checkUserLicenseAndWaitForCallbackIfNeeded(
    credentials: QodanaCloudCredentials,
    cloudClient: QDCloudClient,
  ): UserState.Authorized? {
    val credentialsProvider = suspend {
      qodanaCloudResponse {
        requireNotNull(credentials.acquireAccessTokenForRequest())
      }
    }
    val userApi: QDCloudResponse<QDCloudUserApiV1> = qodanaCloudResponse {
      cloudClient.v1().value().userApi(credentialsProvider)
    }
    val userInfoResponse = qodanaCloudResponse {
      userApi.value().getUserInfo().value()
    }
    val userInfo = when (userInfoResponse) {
      is QDCloudResponse.Success -> {
        userInfoResponse.value
      }
      is QDCloudResponse.Error -> {
        userInfoResponse
          .getErrorNotification(QodanaBundle.message("notification.title.failed.to.obtain.user.info"))
          .notify(null)
        return null
      }
    }

    val authorized = AuthorizedImpl(
      stateManager,
      QodanaCloudRefreshTokenPersistence(),
      cloudClient,
      credentials,
      userInfo,
      selfHostedFrontendUrl
    )
    if (!QodanaRegistry.isQodanaLicenseAgreementCallbackEnabled) {
      return authorized
    }
    return try {
      withTimeout(QodanaCloudStateService.getInstance().authorizationExpirationTimeout) {
        merge(
          poolingAndManualCheckFlow(userApi),
          licenceAgreementAcceptedCallbackFlow(userInfo.id)
        ).map { accepted ->
          if (accepted) authorized else null
        }.first()
      }
    } catch (e: TimeoutCancellationException) {
      return null
    }
  }

  private suspend fun licenceAgreementAcceptedCallbackFlow(userId: String): Flow<Boolean> {
    return flow {
      emitAll(licenseAgreementAcceptedCallbackFlow.filter { it.first == userId }.map { it.second })
    }
  }

  private fun poolingAndManualCheckFlow(userApi: QDCloudResponse<QDCloudUserApiV1>): Flow<Boolean> {
    return merge(poolingPeriodFlow(), updateFlow).map {
      val isLicenseAlreadyAcceptedResponse = qodanaCloudResponse {
        userApi.value().getUserLicenses().value().missing.isEmpty()
      }
      val isLicenseAlreadyAccepted = when (isLicenseAlreadyAcceptedResponse) {
        is QDCloudResponse.Success -> {
          isLicenseAlreadyAcceptedResponse.value
        }
        is QDCloudResponse.Error -> {
          isLicenseAlreadyAcceptedResponse
            .getErrorNotification(QodanaBundle.message("notification.title.can.t.obtain.user.license"))
            .notify(null)
          return@map false
        }
      }
      if (isLicenseAlreadyAccepted) {
        return@map true
      }
      null
    }.filterNotNull()
  }

  private fun poolingPeriodFlow(): Flow<Unit> {
    var refreshDelay = 1.seconds
    var timePassed = 0.seconds
    return flow {
      while (true) {
        emit(Unit)
        delay(refreshDelay)
        timePassed += refreshDelay
        refreshDelay *= timePassed.inWholeMinutes.toInt() + 1
      }
    }
  }

  override fun checkLicenseStatus() {
    updateFlow.tryEmit(Unit)
  }

  private fun logSuccessfullyAuthorizedStats() {
    QodanaPluginStatsCounterCollector.UPDATE_CLOUD_USER_STATE.log(
      StatsUserState.AUTHORIZED,
      SourceUserState.OAUTH_SUCCEEDED
    )
  }

  override suspend fun licenseAgreementAcceptedCallback(userId: String, isAccepted: Boolean) {
    licenseAgreementAcceptedCallbackFlow.emit(userId to isAccepted)
  }

  override fun cancelAuthorization(): UserState.NotAuthorized? {
    val notAuthorized = stateManager.changeState(this, NotAuthorizedImpl(stateManager, selfHostedFrontendUrl))
    if (notAuthorized != null) {
      service<QodanaCloudOAuthService>().cancelAuthorization()
    }
    return notAuthorized
  }


}