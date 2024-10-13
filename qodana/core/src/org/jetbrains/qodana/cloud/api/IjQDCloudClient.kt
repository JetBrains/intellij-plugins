@file:Suppress("FunctionName")

package org.jetbrains.qodana.cloud.api

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.annotations.TestOnly
import org.jetbrains.qodana.cloudclient.*
import java.net.http.HttpClient

fun IjQDCloudClient(frontendUrl: String): QDCloudClient {
  return service<IjQDCloudClientProvider>().getQDCloudClient(frontendUrl)
}

fun IjQDCloudHttpClient(): QDCloudHttpClient {
  return service<IjQDCloudClientProvider>().httpClient
}

val mockQDCloudHttpClient: MockQDCloudHttpClient
  @TestOnly
  get() {
    return (service<IjQDCloudClientProvider>() as IjQDCloudClientProviderTestImpl).httpClient
  }

interface IjQDCloudClientProvider {
  val httpClient: QDCloudHttpClient

  fun getQDCloudClient(frontendUrl: String): QDCloudClient
}

private class IjQDCloudClientProviderImpl(private val scope: CoroutineScope) : IjQDCloudClientProvider {
  override val httpClient: QDCloudHttpClient = LoggingQDCloudHttpClient(QDCloudHttpClient(HttpClient.newHttpClient()))

  override fun getQDCloudClient(frontendUrl: String): QDCloudClient {
    val environment = QDCloudEnvironment(frontendUrl, httpClient).requestOn(scope)
    return QDCloudClient(httpClient, environment)
  }

  private class LoggingQDCloudHttpClient(private val base: QDCloudHttpClient) : QDCloudHttpClient {
    override suspend fun doRequest(host: String, request: QDCloudRequest, token: String?): QDCloudResponse<String> {
      val result = base.doRequest(host, request, token)
      if (result is QDCloudResponse.Error) {
        logger<LoggingQDCloudHttpClient>().warn("Failed request, $host, ${request.path} : ${result.message}", result.exception)
      }
      return result
    }
  }
}

@TestOnly
class IjQDCloudClientProviderTestImpl : IjQDCloudClientProvider {
  override val httpClient = MockQDCloudHttpClient.empty()

  override fun getQDCloudClient(frontendUrl: String): QDCloudClient {
    val environment = object : QDCloudEnvironment {
      override suspend fun getApis(): QDCloudResponse<QDCloudEnvironment.Apis> {
        return qodanaCloudResponse {
          QDCloudEnvironment.Apis(
            api = listOf(QDCloudEnvironment.Apis.Api("https://tests-qodana.cloud", majorVersion = 1, minorVersion = 0)),
            linters = emptyList()
          )
        }
      }
    }
    return QDCloudClient(httpClient, environment)
  }
}

@TestOnly
fun MockQDCloudHttpClient.respond(
  pathPattern: String,
  handler: suspend (request: QDCloudRequest) -> QDCloudResponse<String>?
) {
  val fullHandler: MockQDCloudHttpRequestHandler = { requestHost: String, request: QDCloudRequest, token: String? ->
    if (isPatternMatchingPath(requestPath = request.path, pathPattern)) handler.invoke(request) else null
  }
  respond(fullHandler)
}

private fun isPatternMatchingPath(requestPath: String, patternPath: String): Boolean {
  val requestPathParts = requestPath.split("/")
  val patternPathParts = patternPath.split("/")
  if (requestPathParts.size != patternPathParts.size) return false

  requestPathParts.zip(patternPathParts) { requestPart, patternPart ->
    if (patternPart == "*") return@zip
    if (requestPart != patternPart) return false
  }
  return true
}