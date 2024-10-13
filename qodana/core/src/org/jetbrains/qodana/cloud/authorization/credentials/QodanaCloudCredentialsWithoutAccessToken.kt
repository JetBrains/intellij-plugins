package org.jetbrains.qodana.cloud.authorization.credentials

/**
 * Credentials containing only refresh token, used in "offline mode":
 *
 * - When the refresh of access token failed because of offline mode, we still save the refresh token, but have no more access token
 * - Used for initialization, since we need to persist only refresh token
 */
class QodanaCloudCredentialsWithoutAccessToken(refreshToken: QodanaRefreshToken) : QodanaCloudCredentialsSingleUseRefreshToken(refreshToken) {
  override val accessToken: String = ""
  override fun acquireAccessTokenForRequest(): QodanaAccessToken? = null
}