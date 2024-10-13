package org.jetbrains.qodana.cloud.authorization.credentials

import com.intellij.collaboration.auth.credentials.Credentials
import java.util.concurrent.atomic.AtomicBoolean

internal typealias QodanaAccessToken = String
internal typealias QodanaRefreshToken = String

interface QodanaCloudCredentials : Credentials {
  fun acquireAccessTokenForRequest(): QodanaAccessToken?

  /**
   * Acquires refresh token for request.
   * I.e. for example, if token is single-use, on the next calls [acquireRefreshTokenForRequest] and [seeRefreshTokenToPersist] must return `null`
   */
  suspend fun acquireRefreshTokenForRequest(): QodanaRefreshToken?

  /** Returns refresh token to persist (do not use for refresh request, use [acquireRefreshTokenForRequest] for that). */
  fun seeRefreshTokenToPersist(): QodanaRefreshToken?
}

abstract class QodanaCloudCredentialsSingleUseRefreshToken(private val refreshToken: QodanaRefreshToken): QodanaCloudCredentials {
  private val refreshTokenWasUsed = AtomicBoolean(false)

  override suspend fun acquireRefreshTokenForRequest(): QodanaRefreshToken? {
    if (!refreshTokenWasUsed.compareAndSet(false, true)) return null

    return refreshToken
  }

  override fun seeRefreshTokenToPersist(): QodanaRefreshToken? {
    if (refreshTokenWasUsed.get()) return null

    return refreshToken
  }
}