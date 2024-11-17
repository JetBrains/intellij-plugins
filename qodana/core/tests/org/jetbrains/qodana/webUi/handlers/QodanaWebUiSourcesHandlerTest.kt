package org.jetbrains.qodana.webUi.handlers

import com.intellij.ide.browsers.BrowserLauncher
import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.util.application
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.ide.BuiltInServerManager
import org.jetbrains.qodana.QodanaPluginLightTestBase
import org.jetbrains.qodana.run.QodanaConverterInput
import org.jetbrains.qodana.webUi.ActiveWebUi
import org.jetbrains.qodana.webUi.QodanaWebUiService
import org.jetbrains.qodana.webUi.TestBrowserLauncher
import org.jetbrains.qodana.webUi.withUnlimitedRestApiRequests
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*

class QodanaWebUiSourcesHandlerTest : QodanaPluginLightTestBase() {
  private val activeWebUi: ActiveWebUi by lazy {
    runBlocking {
      QodanaWebUiService.getInstance(project).requestOpenBrowserWebUi(
        id = "1",
        converterInput = QodanaConverterInput.SarifFileOnly(sarifTestReports.validForConverter)
      )!!
    }
  }

  override fun runInDispatchThread(): Boolean = false

  override fun setUp() {
    super.setUp()
    invokeAndWaitIfNeeded {
      reinstansiateService(application, BrowserLauncher::class.java, TestBrowserLauncher())
    }
    BuiltInServerManager.getInstance().waitForStart()
    activeWebUi
  }

  fun `test 200 with token in url`(): Unit = runBlocking {
    doRequest200NotEmptyBody(
      "/qodana.ide/index.html?_qdt=${activeWebUi.token}",
      emptyMap()
    )
  }

  fun `test 200 with token in referer`(): Unit = runBlocking {
    doRequest200NotEmptyBody(
      "/qodana.ide/index.html",
      mapOf(
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      )
    )
  }

  fun `test 404 without token`(): Unit = runBlocking {
    doRequest404(
      "/qodana.ide/index.html",
      emptyMap()
    )
  }

  fun `test 404 with token in url not local origin`(): Unit = runBlocking {
    doRequest404(
      "/qodana.ide/index.html?_qdt=${activeWebUi.token}",
      mapOf(
        "origin" to "https://not.local.com"
      )
    )
  }

  fun `test 404 with token in url not local referer`(): Unit = runBlocking {
    doRequest404(
      "/qodana.ide/index.html?_qdt=${activeWebUi.token}",
      mapOf(
        "referer" to "https://not.local.com"
      )
    )
  }

  fun `test 404 with token in referer not local referer`(): Unit = runBlocking {
    doRequest404(
      "/qodana.ide/index.html",
      mapOf(
        "referer" to "https://not.local.com?_qdt=${activeWebUi.token}"
      )
    )
  }

  fun `test 404 with token in referer not local origin`(): Unit = runBlocking {
    doRequest404(
      "/qodana.ide/index.html",
      mapOf(
        "origin" to "https://not.local.com",
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      )
    )
  }

  fun `test 404 with not valid token in url`(): Unit = runBlocking {
    doRequest404(
      "/qodana.ide/index.html?_qdt=${UUID.randomUUID()}",
      emptyMap()
    )
  }

  fun `test 404 with not valid token in referer`(): Unit = runBlocking {
    doRequest404(
      "/qodana.ide/index.html",
      mapOf(
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${UUID.randomUUID()}"
      )
    )
  }

  fun `test 404 with token of closed web ui in url`(): Unit = runBlocking {
    activeWebUi.close()
    doRequest404(
      "/qodana.ide/index.html?_qdt=${activeWebUi.token}",
      emptyMap()
    )
  }

  fun `test 404 with token of closed web ui in referer`(): Unit = runBlocking {
    activeWebUi.close()
    doRequest404(
      "/qodana.ide/index.html",
      mapOf(
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      )
    )
  }

  private fun doRequest200NotEmptyBody(urlPathWithParameters: String, headers: Map<String, String>) {
    doSourcesRequest(urlPathWithParameters, headers) {
      assertThat(it.statusCode()).isEqualTo(200)
      assertThat(it.body().readAllBytes()).isNotEmpty()
    }
  }

  private fun doRequest404(urlPathWithParameters: String, headers: Map<String, String>) {
    doSourcesRequest(urlPathWithParameters, headers) {
      assertThat(it.statusCode()).isEqualTo(404)
    }
  }

  private fun doSourcesRequest(
    urlPathWithParameters: String,
    headers: Map<String, String>,
    responseAssert: (HttpResponse<InputStream>) -> Unit
  ) {
    withUnlimitedRestApiRequests {
      val url = "http://localhost:${BuiltInServerManager.getInstance().port}$urlPathWithParameters"
      var requestBuilder = HttpRequest.newBuilder(URI(url)).GET()

      requestBuilder = if (headers.isNotEmpty()) {
        requestBuilder.headers(*headers.flatMap { listOf(it.key, it.value) }.toTypedArray())
      } else {
        requestBuilder
      }

      val client = HttpClient.newBuilder().build()
      val response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofInputStream())
      responseAssert.invoke(response)
    }
  }
}