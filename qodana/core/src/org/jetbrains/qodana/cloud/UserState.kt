package org.jetbrains.qodana.cloud

import com.intellij.util.Url
import com.intellij.util.Urls
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.cloudclient.qodanaCloudResponse
import org.jetbrains.qodana.cloudclient.v1.QDCloudUserApiV1

/**
 * User authorized state in Qodana Cloud
 *
 * See [QodanaCloudStateService]
 */
sealed interface UserState {
  val selfHostedFrontendUrl: Url?

  interface Authorized : UserState {
    val userDataProvider: QodanaCloudUserDataProvider

    fun logOut(): NotAuthorized?

    suspend fun cloudClient(): QDCloudResponse<IjQDCloudClientV1>
  }

  interface Authorizing : UserState {
    suspend fun licenseAgreementAcceptedCallback(userId: String, isAccepted: Boolean)

    fun checkLicenseStatus()

    fun cancelAuthorization(): NotAuthorized?
  }

  interface NotAuthorized : UserState {
    fun authorize(selfHostedUrl: Url? = null): Authorizing?
  }
}

val UserState.frontendUrl: Url
  get() = selfHostedFrontendUrl ?: Urls.newFromEncoded(QodanaCloudDefaultUrls.websiteUrl)

suspend fun UserState.Authorized.userApi(): QDCloudResponse<QDCloudUserApiV1> {
  return qodanaCloudResponse {
    cloudClient().value().userApi()
  }
}