package org.jetbrains.qodana.cloud

import com.intellij.collaboration.auth.services.OAuthRequest
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.registry.Registry
import com.intellij.util.application
import kotlinx.coroutines.delay
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.*
import org.jetbrains.qodana.cloud.api.mockQDCloudHttpClient
import org.jetbrains.qodana.cloud.api.respond
import org.jetbrains.qodana.cloud.authorization.QodanaCloudOAuthRequest
import org.jetbrains.qodana.cloud.authorization.QodanaCloudOAuthService
import org.jetbrains.qodana.cloud.authorization.credentials.QodanaAccessToken
import org.jetbrains.qodana.cloud.authorization.credentials.QodanaCloudCredentials
import org.jetbrains.qodana.cloud.authorization.credentials.QodanaRefreshToken
import org.jetbrains.qodana.cloud.impl.AuthorizedImpl
import org.jetbrains.qodana.cloudclient.QDCloudException
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.cloudclient.asSuccess
import org.jetbrains.qodana.cloudclient.qodanaCloudResponse
import org.jetbrains.qodana.cloudclient.v1.QDCloudSchema
import org.jetbrains.qodana.registry.QodanaRegistry
import java.time.Instant
import java.util.concurrent.CompletableFuture
import kotlin.time.Duration.Companion.seconds

class QodanaCloudStateServiceTest : QodanaPluginLightTestBase() {
  private lateinit var oAuthServiceMock: QodanaCloudOAuthServiceMock

  private val cloudStateService get() = QodanaCloudStateService.getInstance()
  private val userStateValue: UserState get() = cloudStateService.userState.value

  override fun setUp() {
    super.setUp()
    runDispatchingOnUi {
      val registryValue = Registry.get(QodanaRegistry.CLOUD_INTEGRATION_ENABLE_KEY)
      registryValue.setValue(true)
      Disposer.register(testRootDisposable) {
        registryValue.resetToDefault()
      }

      oAuthServiceMock = QodanaCloudOAuthServiceMock()
      reinstansiateService<QodanaCloudOAuthService>(application, oAuthServiceMock)
      reinstansiateService(application, QodanaCloudStateService(scope))
      cloudStateService.authorizationExpirationTimeout = 2.seconds
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
    }
  }

  override fun runInDispatchThread(): Boolean = false

  fun `test 1 auth attempt success initially not accepted license then accept`(): Unit = runDispatchingOnUi {
    val userId = "id"
    mockLicenses(isAccepted = false)
    mockUserInfo(userId)

    val credentialsFromOAuth = oAuthServiceMock.mockAuthorizeFuture()
    val notAuthorized = userStateValue as UserState.NotAuthorized

    notAuthorized.authorize() as UserState.Authorizing
    dispatchAllTasksOnUi()

    val authorizing = userStateValue as UserState.Authorizing

    credentialsFromOAuth.complete(QodanaCloudCredentialsMock)
    authorizing.licenseAgreementAcceptedCallback(userId, isAccepted = true)
    dispatchAllTasksOnUi()

    assertTrue(userStateValue is UserState.Authorized)
    assertThat(oAuthServiceMock.timesAuthorizeWasCalled).isOne
  }

  fun `test 1 auth attempt success initially not accepted license then decline by handler`(): Unit = runDispatchingOnUi {
    val userId = "id"
    mockLicenses(isAccepted = false)
    mockUserInfo(userId)

    val credentialsFromOAuth = oAuthServiceMock.mockAuthorizeFuture()
    val notAuthorized = userStateValue as UserState.NotAuthorized

    notAuthorized.authorize() as UserState.Authorizing
    dispatchAllTasksOnUi()

    val authorizing = userStateValue as UserState.Authorizing

    credentialsFromOAuth.complete(QodanaCloudCredentialsMock)
    authorizing.licenseAgreementAcceptedCallback(userId, isAccepted = false)
    dispatchAllTasksOnUi()

    assertTrue(userStateValue is UserState.NotAuthorized)
    assertThat(oAuthServiceMock.timesAuthorizeWasCalled).isOne
  }

  fun `test 1 auth attempt success initially not accepted license then accept by handler`(): Unit = runDispatchingOnUi {
    val userId = "id"
    mockLicenses(isAccepted = false)
    mockUserInfo(userId)

    val credentialsFromOAuth = oAuthServiceMock.mockAuthorizeFuture()
    val notAuthorized = userStateValue as UserState.NotAuthorized

    notAuthorized.authorize() as UserState.Authorizing
    credentialsFromOAuth.complete(QodanaCloudCredentialsMock)
    dispatchAllTasksOnUi()

    assertTrue(userStateValue is UserState.Authorizing)

    mockLicenses(isAccepted = true)
    delay(cloudStateService.authorizationExpirationTimeout / 2)
    dispatchAllTasksOnUi()

    assertTrue(userStateValue is UserState.Authorized)
    assertThat(oAuthServiceMock.timesAuthorizeWasCalled).isOne
  }

  fun `test 1 auth attempt success initially not accepted license then accept after manual check`(): Unit = runDispatchingOnUi {
    val userId = "id"
    mockLicenses(isAccepted = false)
    mockUserInfo(userId)

    val credentialsFromOAuth = oAuthServiceMock.mockAuthorizeFuture()
    val notAuthorized = userStateValue as UserState.NotAuthorized

    val authorizing = notAuthorized.authorize() as UserState.Authorizing
    credentialsFromOAuth.complete(QodanaCloudCredentialsMock)
    dispatchAllTasksOnUi()

    assertTrue(userStateValue is UserState.Authorizing)

    mockLicenses(isAccepted = true)
    authorizing.checkLicenseStatus()
    dispatchAllTasksOnUi()

    assertTrue(userStateValue is UserState.Authorized)
    assertThat(oAuthServiceMock.timesAuthorizeWasCalled).isOne
  }

  fun `test 1 auth attempt success initially not accepted license then decline`(): Unit = runDispatchingOnUi {
    val userId = "id"
    mockLicenses(isAccepted = false)
    mockUserInfo(userId)

    val credentialsFromOAuth = oAuthServiceMock.mockAuthorizeFuture()
    val notAuthorized = userStateValue as UserState.NotAuthorized

    notAuthorized.authorize() as UserState.Authorizing
    dispatchAllTasksOnUi()
    credentialsFromOAuth.complete(QodanaCloudCredentialsMock)
    dispatchAllTasksOnUi()
    delay(cloudStateService.authorizationExpirationTimeout)
    dispatchAllTasksOnUi()

    assertTrue(userStateValue is UserState.NotAuthorized)
    assertThat(oAuthServiceMock.timesAuthorizeWasCalled).isOne
  }

  fun `test 1 auth attempt success initially not accepted license then accept for different user`(): Unit = runDispatchingOnUi {
    val userId = "id"
    mockLicenses(isAccepted = false)
    mockUserInfo(userId)

    val credentialsFromOAuth = oAuthServiceMock.mockAuthorizeFuture()
    val notAuthorized = userStateValue as UserState.NotAuthorized

    notAuthorized.authorize() as UserState.Authorizing
    dispatchAllTasksOnUi()

    val authorizing = userStateValue as UserState.Authorizing

    credentialsFromOAuth.complete(QodanaCloudCredentialsMock)
    authorizing.licenseAgreementAcceptedCallback("other_user_id", isAccepted = true)
    dispatchAllTasksOnUi()

    assertTrue(userStateValue is UserState.Authorizing)
  }

  fun `test 1 auth attempt success initially accepted`(): Unit = runDispatchingOnUi {
    mockLicenses(isAccepted = true)

    val credentialsFromOAuth = oAuthServiceMock.mockAuthorizeFuture()
    val notAuthorized = userStateValue as UserState.NotAuthorized

    notAuthorized.authorize() as UserState.Authorizing
    dispatchAllTasksOnUi()

    userStateValue as UserState.Authorizing

    credentialsFromOAuth.complete(QodanaCloudCredentialsMock)
    dispatchAllTasksOnUi()

    assertTrue(userStateValue is UserState.Authorized)
    assertThat(oAuthServiceMock.timesAuthorizeWasCalled).isOne
  }

  fun `test 2 auth attempts success 1 call to oauth service`() = runDispatchingOnUi {
    mockLicenses(isAccepted = true)

    val credentialsFromOAuth = oAuthServiceMock.mockAuthorizeFuture()
    val notAuthorized = userStateValue as UserState.NotAuthorized

    notAuthorized.authorize() as UserState.Authorizing
    dispatchAllTasksOnUi()
    assertThat(notAuthorized.authorize()).isNull()

    assertTrue(userStateValue is UserState.Authorizing)

    credentialsFromOAuth.complete(QodanaCloudCredentialsMock)
    dispatchAllTasksOnUi()

    assertTrue(userStateValue is UserState.Authorized)
    assertThat(oAuthServiceMock.timesAuthorizeWasCalled).isOne
  }

  fun `test 1 auth attempt fail`() = runDispatchingOnUi{
    val credentialsFromOAuth = oAuthServiceMock.mockAuthorizeFuture()
    val notAuthorized = userStateValue as UserState.NotAuthorized

    notAuthorized.authorize() as UserState.Authorizing
    dispatchAllTasksOnUi()

    assertTrue(userStateValue is UserState.Authorizing)

    val handledExceptions = allowExceptions {
      credentialsFromOAuth.completeExceptionally(Exception("Expected exception for fail of auth in tests"))
      dispatchAllTasksOnUi()
    }

    assertThat(handledExceptions.size).isEqualTo(1)
    assertThat(handledExceptions[0].message).isEqualTo("Expected exception for fail of auth in tests")

    assertTrue(userStateValue is UserState.NotAuthorized)
    assertThat(oAuthServiceMock.timesAuthorizeWasCalled).isOne
  }

  fun `test 2 auth attempts fail 1 call to oauth service`() = runDispatchingOnUi{
    val credentialsFromOAuth = oAuthServiceMock.mockAuthorizeFuture()
    val notAuthorized = userStateValue as UserState.NotAuthorized

    notAuthorized.authorize() as UserState.Authorizing
    dispatchAllTasksOnUi()

    assertThat(notAuthorized.authorize()).isNull()

    assertTrue(userStateValue is UserState.Authorizing)

    val handledExceptions = allowExceptions {
      credentialsFromOAuth.completeExceptionally(Exception("Expected exception for fail of auth in tests"))
      dispatchAllTasksOnUi()
    }

    assertThat(handledExceptions.size).isEqualTo(1)
    assertThat(handledExceptions[0].message).isEqualTo("Expected exception for fail of auth in tests")


    assertTrue(userStateValue is UserState.NotAuthorized)
    assertThat(oAuthServiceMock.timesAuthorizeWasCalled).isOne
  }

  fun `test first auth attempt fail second success`() = runDispatchingOnUi{
    mockLicenses(isAccepted = true)

    val credentialsFromOAuth = oAuthServiceMock.mockAuthorizeFuture()
    val notAuthorized = userStateValue as UserState.NotAuthorized

    notAuthorized.authorize() as UserState.Authorizing
    dispatchAllTasksOnUi()

    assertTrue(userStateValue is UserState.Authorizing)

    val handledExceptions = allowExceptions {
      credentialsFromOAuth.completeExceptionally(Exception("Expected exception for fail of auth in tests"))
      dispatchAllTasksOnUi()
    }

    assertThat(handledExceptions.size).isEqualTo(1)
    assertThat(handledExceptions[0].message).isEqualTo("Expected exception for fail of auth in tests")

    assertTrue(userStateValue is UserState.NotAuthorized)
    assertThat(oAuthServiceMock.timesAuthorizeWasCalled).isOne

    val credentialsFromOAuthToSucceed = oAuthServiceMock.mockAuthorizeFuture()
    val notAuthorizedNew = userStateValue as UserState.NotAuthorized

    notAuthorizedNew.authorize() as UserState.Authorizing
    dispatchAllTasksOnUi()

    assertTrue(userStateValue is UserState.Authorizing)
    credentialsFromOAuthToSucceed.complete(QodanaCloudCredentialsMock)
    dispatchAllTasksOnUi()

    assertTrue(userStateValue is UserState.Authorized)
    assertThat(oAuthServiceMock.timesAuthorizeWasCalled).isEqualTo(2)
  }

  fun `test 1 auth attempt cancelled`() = runDispatchingOnUi{
    oAuthServiceMock.mockAuthorizeFuture()
    val notAuthorized = userStateValue as UserState.NotAuthorized

    notAuthorized.authorize() as UserState.Authorizing
    dispatchAllTasksOnUi()

    val authorizing = userStateValue as UserState.Authorizing
    authorizing.cancelAuthorization()
    dispatchAllTasksOnUi()

    assertTrue(userStateValue is UserState.NotAuthorized)
    assertThat(oAuthServiceMock.timesAuthorizeWasCalled).isOne
  }

  fun `test acquire access token success`() = runDispatchingOnUi {
    val refreshMock = QodanaCloudRefreshApiMock {
      CredentialsData(
        "access",
        "refresh",
        Instant.MAX
      )
    }
    mockQDCloudHttpClient.respond("idea/auth/refresh/") {
      refreshMock.credentials()
    }

    val authorized = doInitialTransitionToAuthorized(QodanaCloudCredentialsMock)

    assertThat(authorized.acquireAccessToken().asSuccess()).isEqualTo(QodanaCloudCredentialsMock.accessToken)
    assertThat(refreshMock.timesRefreshWasCalled).isZero
  }

  fun `test authorize without access token do success refresh`() = runDispatchingOnUi {
    val accessTokenFromRefresh = "REFRESHED"
    val refreshMock = QodanaCloudRefreshApiMock {
      CredentialsData(
        access = accessTokenFromRefresh,
        refresh = "refresh",
        Instant.MAX
      )
    }
    mockQDCloudHttpClient.respond("idea/auth/refresh/") {
      refreshMock.credentials()
    }

    val credentialsWithoutAccessToken = credentialsWithoutAccessTokenWhenAuthorized()
    val authorized = doInitialTransitionToAuthorized(credentialsWithoutAccessToken)

    assertThat(authorized.acquireAccessToken().asSuccess()).isEqualTo(accessTokenFromRefresh)
    assertThat(refreshMock.timesRefreshWasCalled).isOne
  }

  fun `test first refresh token is expired next is not stay authorized`() = runDispatchingOnUi {
    var accessTokenCount = -1
    val refreshMock = QodanaCloudRefreshApiMock {
      accessTokenCount++
      CredentialsData(
        "ACCESS $accessTokenCount",
        "REFRESH $accessTokenCount",
        expiresAt = if (accessTokenCount == 0) Instant.MIN else Instant.MAX
      )
    }
    mockQDCloudHttpClient.respond("idea/auth/refresh/") {
      refreshMock.credentials()
    }

    val credentialsWithoutAccessToken = credentialsWithoutAccessTokenWhenAuthorized()
    val authorized = doInitialTransitionToAuthorized(credentialsWithoutAccessToken)

    assertTrue(userStateValue is UserState.Authorized)
    assertThat(authorized.acquireAccessToken().asSuccess()).isEqualTo("ACCESS 1")
    assertThat(refreshMock.timesRefreshWasCalled).isEqualTo(2)
  }

  fun `test refresh user info on authorization`() = runDispatchingOnUi{
    val userInfo = QDCloudSchema.UserInfo("id", "user_in_tests", null)
    mockUserInfo("id", "user_in_tests")
    val authorized = doInitialTransitionToAuthorized(QodanaCloudCredentialsMock)

    assertThat(authorized.userDataProvider.userInfo.value)
      .isEqualTo(RefreshableProperty.PropertyState(QDCloudResponse.Success(userInfo), isRefreshing = false))
  }

  fun `test fail refresh token because of response error transition to unauthorized state`() = runDispatchingOnUi {
    val refreshMock = QodanaCloudRefreshApiMock {
      throw QDCloudException.Error("failed refresh", 404)
    }
    mockQDCloudHttpClient.respond("idea/auth/refresh/") {
      refreshMock.credentials()
    }

    mockLicenses(isAccepted = true)

    val credentialsWithoutAccessToken = credentialsWithoutAccessTokenWhenAuthorized()
    val credentialsFuture = oAuthServiceMock.mockAuthorizeFuture()
    (userStateValue as UserState.NotAuthorized).authorize()
    credentialsFuture.complete(credentialsWithoutAccessToken)
    dispatchAllTasksOnUi() // here we do requests for user name and etc – so the refresh token will be requested

    assertTrue(userStateValue is UserState.NotAuthorized)
    assertThat(refreshMock.timesRefreshWasCalled).isOne()
  }

  fun `test log out from authorized`() = runDispatchingOnUi {
    val authorized = doInitialTransitionToAuthorized(QodanaCloudCredentialsMock)
    authorized.logOut()

    assertTrue(userStateValue is UserState.NotAuthorized)
  }

  private fun doInitialTransitionToAuthorized(credentialsToAuthorizeWith: QodanaCloudCredentials): AuthorizedImpl {
    mockLicenses(isAccepted = true)

    val credentialsFuture = oAuthServiceMock.mockAuthorizeFuture()
    (userStateValue as UserState.NotAuthorized).authorize()
    credentialsFuture.complete(credentialsToAuthorizeWith)
    dispatchAllTasksOnUi()

    return userStateValue as AuthorizedImpl
  }

  private fun mockUserInfo(id: String, fullName: String = "name") {
    mockQDCloudHttpClient.respond("users/me") {
      qodanaCloudResponse {
        @Language("JSON")
        val response = """
          {
            "id": "$id",
            "fullName": "$fullName"
          }
        """.trimIndent()
        response
      }
    }
  }

  private fun mockLicenses(isAccepted: Boolean) {
    @Language("JSON")
    val notAcceptedResponse = """
      {
        "missing": [
          {
            "id": "not-accepted"
          }
        ]
      }
    """.trimIndent()
    @Language("JSON")
    val acceptedResponse = """
      {
        "missing": []
      }
    """.trimIndent()
    mockQDCloudHttpClient.respond("users/me/licenses") {
      qodanaCloudResponse {
        if (isAccepted) acceptedResponse else notAcceptedResponse
      }
    }
  }
}

private class CredentialsData(
  val access: String,
  val refresh: String,
  val expiresAt: Instant
)

private class QodanaCloudRefreshApiMock(
  private val refreshCredentialsProvider: suspend () -> CredentialsData
) {
  var timesRefreshWasCalled = 0

  suspend fun credentials(): QDCloudResponse<String> {
    timesRefreshWasCalled += 1
    return qodanaCloudResponse {
      val credentials = refreshCredentialsProvider.invoke()

      @Language("JSON")
      val response = """
        {
          "access": "${credentials.access}",
          "refresh": "${credentials.refresh}",
          "expires_at": "${credentials.expiresAt}"
        }
      """.trimIndent()

      response
    }
  }
}

private fun credentialsWithoutAccessTokenWhenAuthorized(): QodanaCloudCredentials {
  return object : QodanaCloudCredentials by QodanaCloudCredentialsMock {
    override fun acquireAccessTokenForRequest(): QodanaAccessToken? {
      return if (QodanaCloudStateService.getInstance().userState.value is UserState.Authorized) null else "token"
    }
  }
}

private class QodanaCloudOAuthServiceMock : QodanaCloudOAuthService {
  var timesAuthorizeWasCalled = 0
  private lateinit var authorizeFuture: CompletableFuture<QodanaCloudCredentials>

  override val name: String = "qodana_oauth_mock"

  fun mockAuthorizeFuture() = CompletableFuture<QodanaCloudCredentials>().also { authorizeFuture = it }

  override fun currentOAuthRequest(): QodanaCloudOAuthRequest? = notImplemented()

  override fun cancelAuthorization(): Boolean = authorizeFuture.cancel(true)

  override fun authorize(request: OAuthRequest<QodanaCloudCredentials>): CompletableFuture<QodanaCloudCredentials> {
    timesAuthorizeWasCalled += 1
    return authorizeFuture
  }

  override fun revokeToken(token: String) = notImplemented()

  override fun handleOAuthServerCallback(path: String, parameters: Map<String, List<String>>) = notImplemented()
}

private object QodanaCloudCredentialsMock : QodanaCloudCredentials {
  override fun acquireAccessTokenForRequest(): QodanaAccessToken = ""

  override suspend fun acquireRefreshTokenForRequest(): QodanaRefreshToken = ""

  override fun seeRefreshTokenToPersist(): QodanaRefreshToken = ""

  override val accessToken: String = ""
}