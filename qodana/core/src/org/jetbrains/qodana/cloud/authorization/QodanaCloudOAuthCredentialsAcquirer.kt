package org.jetbrains.qodana.cloud.authorization

import com.intellij.collaboration.auth.services.OAuthCredentialsAcquirer
import com.intellij.openapi.progress.runBlockingCancellable
import org.jetbrains.qodana.cloud.authorization.credentials.QodanaCloudCredentials
import org.jetbrains.qodana.cloud.authorization.credentials.QodanaCloudCredentialsImpl
import org.jetbrains.qodana.cloudclient.QDCloudClient
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.cloudclient.qodanaCloudResponse

class QodanaCloudOAuthCredentialsAcquirer(
  private val cloudClient: QDCloudClient,
  private val codeVerifier: String
) : OAuthCredentialsAcquirer<QodanaCloudCredentials> {
  override fun acquireCredentials(code: String): OAuthCredentialsAcquirer.AcquireCredentialsResult<QodanaCloudCredentials> {
    return runBlockingCancellable {
      val response = qodanaCloudResponse {
        val credentials = cloudClient.v1().value()
          .notAuthorizedApi().getCredentialsFromOAuthCode(code, codeVerifier).value()
        QodanaCloudCredentialsImpl(
          accessToken = credentials.access,
          expirationMoment = credentials.expiresAt,
          refreshToken = credentials.refresh
        )
      }
      return@runBlockingCancellable when(response) {
        is QDCloudResponse.Success -> {
          OAuthCredentialsAcquirer.AcquireCredentialsResult.Success(response.value)
        }
        is QDCloudResponse.Error -> {
          OAuthCredentialsAcquirer.AcquireCredentialsResult.Error(response.exception.toString())
        }
      }
    }
  }
}