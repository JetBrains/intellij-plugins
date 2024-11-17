package org.jetbrains.qodana.cloud.authorization

import com.intellij.collaboration.auth.services.OAuthServiceBase
import org.jetbrains.qodana.cloud.authorization.credentials.QodanaCloudCredentials

internal const val QODANA_HANDLER_PREFIX = "qodana"

class QodanaCloudOAuthServiceImpl : OAuthServiceBase<QodanaCloudCredentials>(), QodanaCloudOAuthService {
  override val name: String = "$QODANA_HANDLER_PREFIX/oauth"

  override fun currentOAuthRequest(): QodanaCloudOAuthRequest? {
    return currentRequest.get()?.request as? QodanaCloudOAuthRequest
  }

  override fun cancelAuthorization(): Boolean = currentRequest.get()?.result?.cancel(true) ?: false

  override fun revokeToken(token: String) = Unit
}
