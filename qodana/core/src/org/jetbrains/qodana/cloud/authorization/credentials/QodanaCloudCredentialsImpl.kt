package org.jetbrains.qodana.cloud.authorization.credentials

import java.time.DateTimeException
import java.time.Instant

class QodanaCloudCredentialsImpl(
  override val accessToken: QodanaAccessToken,
  expirationMoment: Instant,
  refreshToken: QodanaRefreshToken,
) : QodanaCloudCredentialsSingleUseRefreshToken(refreshToken) {
  private val safeExpirationMoment: Instant = computeExpirationMomentWithDelay(expirationMoment, delaySeconds = 60L)

  private fun isAccessTokenExpired(): Boolean {
    return Instant.now().isAfter(safeExpirationMoment)
  }

  override fun acquireAccessTokenForRequest(): QodanaAccessToken? {
    return if (isAccessTokenExpired()) null else accessToken
  }

  private fun computeExpirationMomentWithDelay(baseMoment: Instant, delaySeconds: Long): Instant {
    return try {
      baseMoment.minusSeconds(delaySeconds)
    }
    catch (e : DateTimeException) {
      baseMoment
    }
  }
}