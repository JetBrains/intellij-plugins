package org.jetbrains.qodana.cloud.project

import com.intellij.collaboration.auth.services.OAuthRequest
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.util.Url
import com.intellij.util.application
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.QodanaPluginTest
import org.jetbrains.qodana.cloud.QodanaCloudStateService
import org.jetbrains.qodana.cloud.UserState
import org.jetbrains.qodana.cloud.api.mockQDCloudHttpClient
import org.jetbrains.qodana.cloud.api.respond
import org.jetbrains.qodana.cloud.authorization.QodanaCloudOAuthRequest
import org.jetbrains.qodana.cloud.authorization.QodanaCloudOAuthService
import org.jetbrains.qodana.cloud.authorization.credentials.QodanaCloudCredentials
import org.jetbrains.qodana.cloudclient.qodanaCloudResponse
import org.jetbrains.qodana.dispatchAllTasksOnUi
import org.jetbrains.qodana.notImplemented
import org.jetbrains.qodana.reinstansiateService
import java.util.concurrent.CompletableFuture

fun QodanaPluginTest.doInitialTransitionToAuthorized(testRootDisposable: Disposable): UserState.Authorized = invokeAndWaitIfNeeded {
  reinstansiateService<QodanaCloudOAuthService>(application, QodanaCloudOAuthServiceMock)
  reinstansiateService(application, QodanaCloudStateService(scope))
  doTransitionToAuthorized()
}

fun doTransitionToAuthorized(frontendUrl: Url? = null): UserState.Authorized = invokeAndWaitIfNeeded {
  val qodanaCloudStateService = QodanaCloudStateService.getInstance()
  mockQDCloudHttpClient.apply {
    respond("users/me") {
      qodanaCloudResponse {
        @Language("JSON")
        val response = """
        {
          "id": "user_id",
          "fullName": "full_name",
          "username": "user_name"
        }
      """.trimIndent()
        response
      }
    }
    respond("users/me/licenses") {
      qodanaCloudResponse {
        @Language("JSON")
        val response = """
        {
          "missing": []
        }
      """.trimIndent()
        response
      }
    }
    respond("oauth/configurations") {
      qodanaCloudResponse {
        @Language("JSON")
        val response = """
        {
          "oauthUrl": "https://valid.link",
          "providerName": "provider_name"
        }
      """.trimIndent()
        response
      }
    }
  }
  val currentState = qodanaCloudStateService.userState.value
  if (currentState is UserState.Authorized) return@invokeAndWaitIfNeeded currentState

  val notAuthorized = qodanaCloudStateService.userState.value as UserState.NotAuthorized
  notAuthorized.authorize(frontendUrl)
  dispatchAllTasksOnUi()

  qodanaCloudStateService.userState.value as UserState.Authorized
}

object QodanaCloudOAuthServiceMock : QodanaCloudOAuthService {
  override val name: String = "qodana_oauth_mock"

  override fun currentOAuthRequest(): QodanaCloudOAuthRequest = notImplemented()

  override fun cancelAuthorization() = notImplemented()

  override fun authorize(request: OAuthRequest<QodanaCloudCredentials>): CompletableFuture<QodanaCloudCredentials> {
    return CompletableFuture.completedFuture(QodanaCloudCredentialsMock)
  }

  override fun revokeToken(token: String) = notImplemented()

  override fun handleOAuthServerCallback(path: String, parameters: Map<String, List<String>>)= notImplemented()
}

private object QodanaCloudCredentialsMock : QodanaCloudCredentials {
  override fun acquireAccessTokenForRequest() = accessToken

  override suspend fun acquireRefreshTokenForRequest() = notImplemented()

  override fun seeRefreshTokenToPersist() = null

  override val accessToken: String = "access token"
}