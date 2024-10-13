package org.jetbrains.qodana.cloud.authorization

import com.intellij.collaboration.auth.services.OAuthService
import com.intellij.openapi.components.service
import org.jetbrains.qodana.cloud.authorization.credentials.QodanaCloudCredentials

interface QodanaCloudOAuthService : OAuthService<QodanaCloudCredentials> {
  companion object {
    fun getInstance(): QodanaCloudOAuthService = service()
  }

  fun currentOAuthRequest(): QodanaCloudOAuthRequest?

  fun cancelAuthorization(): Boolean
}