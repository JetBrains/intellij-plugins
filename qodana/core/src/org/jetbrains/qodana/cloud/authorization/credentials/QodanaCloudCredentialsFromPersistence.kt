package org.jetbrains.qodana.cloud.authorization.credentials

import org.jetbrains.qodana.cloud.QodanaCloudRefreshTokenPersistence

internal class QodanaCloudCredentialsFromPersistence(
  private val refreshTokenPersistence: QodanaCloudRefreshTokenPersistence
) : QodanaCloudCredentials {
  override val accessToken: String
    get() = ""

  override fun acquireAccessTokenForRequest(): QodanaAccessToken? = null

  override suspend fun acquireRefreshTokenForRequest(): QodanaRefreshToken? {
    return refreshTokenPersistence.getToken()
  }

  // already persisted
  override fun seeRefreshTokenToPersist(): QodanaRefreshToken? = null
}