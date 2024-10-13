package org.jetbrains.qodana.cloud.authorization

import com.intellij.collaboration.auth.OAuthCallbackHandlerBase
import com.intellij.collaboration.auth.services.OAuthService
import com.intellij.util.Urls
import io.netty.handler.codec.http.HttpRequest
import org.jetbrains.qodana.registry.QodanaRegistry

class QodanaCloudOAuthCallbackHandler : OAuthCallbackHandlerBase() {
  override fun oauthService(): QodanaCloudOAuthService = QodanaCloudOAuthService.getInstance()

  override fun handleOAuthResult(oAuthResult: OAuthService.OAuthResult<*>): AcceptCodeHandleResult {
    val currentOAuthRequest = oAuthResult.request as? QodanaCloudOAuthRequest ?: throw RuntimeException("No auth in process")

    val frontendUrl = Urls.newFromEncoded(currentOAuthRequest.frontendUrl)
    val redirectUrl = if (QodanaRegistry.isQodanaLicenseAgreementCallbackEnabled) {
      frontendUrl.resolve("ideauth").addParameters(mapOf("port" to currentOAuthRequest.port.toString()))
    } else {
      frontendUrl
    }
    return AcceptCodeHandleResult.Redirect(redirectUrl)
  }

  // TODO fix for on-premise
  override fun isOriginAllowed(request: HttpRequest): OriginCheckResult {
    oauthService().currentOAuthRequest() ?: return OriginCheckResult.FORBID
    return OriginCheckResult.ALLOW
  }
}
