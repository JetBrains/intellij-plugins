package org.jetbrains.qodana.cloud.authorization

import com.intellij.collaboration.auth.services.OAuthCredentialsAcquirer
import com.intellij.collaboration.auth.services.OAuthRequest
import com.intellij.collaboration.auth.services.PkceUtils
import com.intellij.openapi.components.service
import com.intellij.util.Url
import com.intellij.util.Urls
import com.intellij.util.io.DigestUtil
import org.jetbrains.ide.RestService
import org.jetbrains.qodana.cloud.authorization.credentials.QodanaCloudCredentials
import org.jetbrains.qodana.cloudclient.QDCloudClient
import java.util.*

private const val CODE_CHALLENGE_METHOD_SHA256 = "SHA256"


class QodanaCloudOAuthRequest(
  val port: Int,
  val frontendUrl: String,
  private val cloudClient: QDCloudClient,
  authUrl: Url
) : OAuthRequest<QodanaCloudCredentials> {

  private val codeVerifier: String = PkceUtils.generateCodeVerifier()

  private val codeChallenge: String = PkceUtils.generateShaCodeChallenge(codeVerifier, Base64.getEncoder())

  private val stateWithEmbeddedPortAndChallenge = "idea-$port-${DigestUtil.randomToken()}"

  override val authUrlWithParameters: Url = authUrl.addParameters(mapOf(
    "state" to stateWithEmbeddedPortAndChallenge,
  ))

  override val authorizationCodeUrl: Url
    get() = Urls.newFromEncoded("http://localhost:$port/${RestService.PREFIX}/${service<QodanaCloudOAuthService>().name}/authorization_code/")

  override val credentialsAcquirer: OAuthCredentialsAcquirer<QodanaCloudCredentials>
    get() = QodanaCloudOAuthCredentialsAcquirer(cloudClient, codeVerifier)
}