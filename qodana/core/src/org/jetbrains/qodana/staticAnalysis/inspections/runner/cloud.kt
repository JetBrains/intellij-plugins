package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.Urls
import org.jetbrains.qodana.cloud.QodanaCloudDefaultUrls
import org.jetbrains.qodana.cloud.api.IjQDCloudClient
import org.jetbrains.qodana.cloud.api.IjQDCloudHttpClient
import org.jetbrains.qodana.cloud.normalizeUrl
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.cloudclient.qodanaCloudResponse
import org.jetbrains.qodana.cloudclient.v1.QDCloudClientV1
import org.jetbrains.qodana.cloudclient.v1.QDCloudProjectApiV1
import org.jetbrains.qodana.staticAnalysis.qodanaEnv
import java.net.MalformedURLException

private val LOG = Logger.getInstance("org.jetbrains.qodana.staticAnalysis.inspections.runner.cloud")

interface QDCloudLinterProjectApi {
  val api: QDCloudProjectApiV1

  val frontendUrl: String
}

internal suspend fun obtainQodanaCloudProjectApi(): QDCloudResponse<QDCloudLinterProjectApi>? {
  val token = qodanaEnv().QODANA_TOKEN.value
  if (token == null) {
    LOG.info("${qodanaEnv().QODANA_TOKEN.key} environment variable is not defined")
    return null
  }

  val backendUrl = qodanaEnv().ENDPOINT.value

  val selfHostedFrontendUrl = try {
    val url = qodanaEnv().QODANA_ENDPOINT.value?.let { normalizeUrl(it) }
    url?.let { Urls.newFromEncoded(it) } // validation
    url
  }
  catch (e: MalformedURLException) {
    LOG.error("${qodanaEnv().QODANA_ENDPOINT.key} environment variable should be valid URL, value: ${qodanaEnv().QODANA_ENDPOINT.value}")
    throw e
  }

  val frontendUrl = selfHostedFrontendUrl ?: QodanaCloudDefaultUrls.websiteUrl

  val api = qodanaCloudResponse {
    val v1Client = when {
      // respect QODANA_ENDPOINT over ENDPOINT
      selfHostedFrontendUrl != null -> {
        IjQDCloudClient(selfHostedFrontendUrl).v1().value()
      }
      backendUrl != null -> {
        QDCloudClientV1(backendUrl, minorVersion = 0, IjQDCloudHttpClient())
      }
      else -> {
        IjQDCloudClient(frontendUrl).v1().value()
      }
    }

    val projectApi = v1Client.projectApi(token)

    object : QDCloudLinterProjectApi {
      override val api: QDCloudProjectApiV1
        get() = projectApi

      override val frontendUrl: String
        get() = frontendUrl
    }
  }
  return api
}

