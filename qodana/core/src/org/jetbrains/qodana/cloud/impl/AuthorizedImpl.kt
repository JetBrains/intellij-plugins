package org.jetbrains.qodana.cloud.impl

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.util.Url
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.jetbrains.qodana.cloud.*
import org.jetbrains.qodana.cloud.authorization.credentials.QodanaAccessToken
import org.jetbrains.qodana.cloud.authorization.credentials.QodanaCloudCredentials
import org.jetbrains.qodana.cloud.authorization.credentials.QodanaCloudCredentialsImpl
import org.jetbrains.qodana.cloud.authorization.credentials.QodanaCloudCredentialsWithoutAccessToken
import org.jetbrains.qodana.cloudclient.QDCloudClient
import org.jetbrains.qodana.cloudclient.QDCloudException
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.cloudclient.qodanaCloudResponse
import org.jetbrains.qodana.cloudclient.v1.*
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.stats.QodanaPluginStatsCounterCollector
import org.jetbrains.qodana.stats.SourceUserState
import org.jetbrains.qodana.stats.StatsUserState

internal class AuthorizedImpl(
  private val stateManager: StateManager<UserState>,
  private val refreshTokenPersistence: QodanaCloudRefreshTokenPersistence,
  private val cloudClient: QDCloudClient,
  private val initialAuthorizedCredentials: QodanaCloudCredentials,
  userInfo: QDCloudSchema.UserInfo,
  override val selfHostedFrontendUrl: Url?
) : UserState.Authorized {
  override val userDataProvider = QodanaCloudUserDataProvider(this, userInfo)

  private val accessTokenRequests = Channel<CompletableDeferred<AccessTokenResult>>()

  private sealed interface AccessTokenResult {
    class Success(val accessToken: QodanaAccessToken) : AccessTokenResult

    object Offline : AccessTokenResult

    object FailedAndLoggedOut : AccessTokenResult
  }

  suspend fun startProcessingAccessTokenRequests() {
    var currentCredentials = initialAuthorizedCredentials
    saveRefreshToken(currentCredentials)
    try {
      for (accessTokenResultDeferred in accessTokenRequests) {
        val accessToken = currentCredentials.acquireAccessTokenForRequest()
        if (accessToken != null) {
          accessTokenResultDeferred.complete(AccessTokenResult.Success(accessToken))
          continue
        }
        val refreshedCredentials = refreshCredentials(currentCredentials)
        if (refreshedCredentials == null) {
          accessTokenResultDeferred.complete(AccessTokenResult.FailedAndLoggedOut)
          logTokenExpiredUnauthorizedStats()
          accessTokenRequests.cancel()
          logOut()
          continue
        }
        saveRefreshToken(refreshedCredentials)
        currentCredentials = refreshedCredentials

        val accessTokenFromRefreshedCredentials = refreshedCredentials.acquireAccessTokenForRequest()
        if (accessTokenFromRefreshedCredentials == null) {
          accessTokenResultDeferred.complete(AccessTokenResult.Offline)
          continue
        }
        accessTokenResultDeferred.complete(AccessTokenResult.Success(accessTokenFromRefreshedCredentials))
      }
    }
    finally {
      accessTokenRequests.cancel()
    }
  }

  suspend fun clearCurrentRefreshToken() {
    withContext(QodanaDispatchers.IO) {
      refreshTokenPersistence.clear()
    }
  }

  private suspend fun saveRefreshToken(credentials: QodanaCloudCredentials) {
    val refreshToken = credentials.seeRefreshTokenToPersist() ?: return
    refreshTokenPersistence.saveToken(refreshToken)
  }

  override fun logOut(): UserState.NotAuthorized? {
    return stateManager.changeState(this, NotAuthorizedImpl(stateManager, selfHostedFrontendUrl))
  }

  /**
   * Return s valid access token, refreshes token if needed.
   *
   * - If refresh is failed with response error, the user state is transitioned to [UserState.NotAuthorized] and current coroutine is cancelled
   */
  suspend fun acquireAccessToken(): QDCloudResponse<QDCloudUserToken> {
    val accessTokenResultDeferred = CompletableDeferred<AccessTokenResult>()
    accessTokenRequests.send(accessTokenResultDeferred)

    return when(val result = accessTokenResultDeferred.await()) {
      AccessTokenResult.FailedAndLoggedOut -> {
        coroutineScope {
          cancel()
          awaitCancellation()
        }
      }
      AccessTokenResult.Offline -> {
        QDCloudResponse.Error.Offline(QDCloudException.Offline())
      }
      is AccessTokenResult.Success -> {
        QDCloudResponse.Success(result.accessToken)
      }
    }
  }

  override suspend fun cloudClient(): QDCloudResponse<IjQDCloudClientV1> {
    return qodanaCloudResponse {
      val v1Client = cloudClient.v1().value()

      object : IjQDCloudClientV1 {
        override fun userApi(): QDCloudUserApiV1 {
          return v1Client.userApi {
            acquireAccessToken()
          }
        }

        override fun projectApi(projectToken: String): QDCloudProjectApiV1 {
          return v1Client.projectApi(projectToken)
        }

        override fun notAuthorizedApi(): QDCloudNotAuthorizedApiV1 {
          return v1Client.notAuthorizedApi()
        }
      }
    }
  }

  private suspend fun refreshCredentials(currentCredentials: QodanaCloudCredentials): QodanaCloudCredentials? {
    val refreshToken = currentCredentials.acquireRefreshTokenForRequest()
    clearCurrentRefreshToken()
    if (refreshToken == null) {
      thisLogger().warn("No Qodana refresh token")
      return null
    }

    val credentialsResponse = qodanaCloudResponse {
      val credentials = cloudClient.v1().value().notAuthorizedApi()
        .getNewCredentialsFromRefreshCode(refreshToken).value()
      QodanaCloudCredentialsImpl(
        accessToken = credentials.access,
        expirationMoment = credentials.expiresAt,
        refreshToken = credentials.refresh
      )
    }
    val refreshedCredentials = when(credentialsResponse) {
      is QDCloudResponse.Success -> {
        credentialsResponse.value
      }
      is QDCloudResponse.Error.Offline -> {
        QodanaCloudCredentialsWithoutAccessToken(refreshToken)
      }
      is QDCloudResponse.Error.ResponseFailure -> {
        thisLogger().warn("Failed to refresh credentials", credentialsResponse.exception)
        null
      }
    }

    return refreshedCredentials
  }

  private fun logTokenExpiredUnauthorizedStats() {
    QodanaPluginStatsCounterCollector.UPDATE_CLOUD_USER_STATE.log(
      StatsUserState.NOT_AUTHORIZED,
      SourceUserState.REFRESH_TOKEN_EXPIRED
    )
  }
}