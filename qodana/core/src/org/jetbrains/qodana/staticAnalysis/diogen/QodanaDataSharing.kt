package org.jetbrains.qodana.staticAnalysis.diogen

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.intellij.openapi.diagnostic.fileLogger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.qodana.cloud.QodanaCloudDefaultUrls
import org.jetbrains.qodana.cloud.api.IjQDCloudClient
import org.jetbrains.qodana.cloud.normalizeUrl
import org.jetbrains.qodana.cloudclient.QDCloudRequest
import org.jetbrains.qodana.cloudclient.asSuccess
import org.jetbrains.qodana.staticAnalysis.qodanaEnv
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal const val DATA_SHARING_ALLOWED_PROPERTY = "qodana.data.sharing.allowed"

internal val DATA_SHARING_REQUEST_TIMEOUT: Duration = 10.seconds

private val LOG = fileLogger()

/**
 * Tells whether Qodana Cloud allows data sharing for the organization.
 *
 * The `qodana.data.sharing.allowed` system property wins and causes no request.
 * Without the property, this function requests `linters/license-key` with `QODANA_TOKEN`.
 * It returns `false` and logs the cause when the token is missing, the request fails, or the field is absent.
 */
internal suspend fun isDataSharingAllowed(): Boolean {
  System.getProperty(DATA_SHARING_ALLOWED_PROPERTY)?.let { return it.toBoolean() }

  val token = qodanaEnv().QODANA_TOKEN.value
  if (token.isNullOrEmpty()) {
    LOG.info("Data sharing is not allowed: ${qodanaEnv().QODANA_TOKEN.key} environment variable is not defined")
    return false
  }
  return try {
    withTimeoutOrNull(DATA_SHARING_REQUEST_TIMEOUT) { requestDataSharingAllowed(token) }
    ?: false.also { LOG.warn("Data sharing is not allowed: the license request timed out") }
  }
  catch (e: CancellationException) {
    throw e
  }
  catch (e: Exception) {
    LOG.warn("Data sharing is not allowed: the license request failed", e)
    false
  }
}

private suspend fun requestDataSharingAllowed(token: String): Boolean {
  val frontendUrl = qodanaEnv().QODANA_ENDPOINT.value?.let { normalizeUrl(it) } ?: QodanaCloudDefaultUrls.websiteUrl
  val client = IjQDCloudClient(frontendUrl)
  val lintersHost = client.environment.getApis().asSuccess()?.linters
                      ?.filter { it.majorVersion == 1 }
                      ?.maxByOrNull { it.minorVersion }
                      ?.host
                    ?: error("$frontendUrl advertises no v1 linters API")
  val body = client.httpClient.doRequest(lintersHost, QDCloudRequest("linters/license-key", QDCloudRequest.GET), token).asSuccess()
             ?: error("the license request to $lintersHost failed")
  return jacksonObjectMapper().readTree(body).path("dataSharingAllowed").asBoolean(false)
}
