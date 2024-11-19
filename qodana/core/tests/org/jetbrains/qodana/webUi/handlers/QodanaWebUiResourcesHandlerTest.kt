package org.jetbrains.qodana.webUi.handlers

import com.intellij.ide.browsers.BrowserLauncher
import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.util.application
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
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
import java.nio.file.Files
import java.util.*
import kotlin.io.path.exists
import kotlin.io.path.pathString

class QodanaWebUiResourcesHandlerTest : QodanaPluginLightTestBase() {
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
      reinstansiateService(project, QodanaWebUiService::class.java, QodanaWebUiService(project, scope))
      reinstansiateService(application, BrowserLauncher::class.java, TestBrowserLauncher())
    }
    BuiltInServerManager.getInstance().waitForStart()
    activeWebUi
  }

  fun `test 200 with token in url`() = runBlocking {
    doRequest200NotEmptyBody(
      "/api/qodana/resources?file=qodana.sarif.json&_qdt=${activeWebUi.token}",
      mapOf(
        "Sec-Fetch-Site" to "same-origin"
      )
    )
  }

  fun `test 200 with token in referer`() = runBlocking {
    doRequest200NotEmptyBody(
      "/api/qodana/resources?file=qodana.sarif.json",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      )
    )
  }

  fun `test 404 without token`() = runBlocking {
    doRequest404(
      "/api/qodana/resources?file=qodana.sarif.json",
      mapOf(
        "Sec-Fetch-Site" to "same-origin"
      )
    )
  }

  fun `test 404 invalid token in url`() = runBlocking {
    doRequest404(
      "/api/qodana/resources?file=qodana.sarif.json&_qdt=${UUID.randomUUID()}",
      mapOf(
        "Sec-Fetch-Site" to "same-origin"
      )
    )
  }

  fun `test 404 invalid token in referer`() = runBlocking {
    doRequest404(
      "/api/qodana/resources?file=qodana.sarif.json",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${UUID.randomUUID()}"
      )
    )
  }

  fun `test 404 not present file`() = runBlocking {
    doRequest404(
      "/api/qodana/resources?file=not.present.sarif.json&_qdt=${activeWebUi.token}",
      mapOf(
        "Sec-Fetch-Site" to "same-origin"
      )
    )
  }

  fun `test 404 with token in url not local origin`() = runBlocking {
    doRequest404(
      "/api/qodana/resources?file=qodana.sarif.json&_qdt=${activeWebUi.token}",
      mapOf(
        "origin" to "https://not.local.com",
        "Sec-Fetch-Site" to "same-origin"
      )
    )
  }

  fun `test 404 with token in url not local referer`() = runBlocking {
    doRequest404(
      "/api/qodana/resources?file=qodana.sarif.json&_qdt=${activeWebUi.token}",
      mapOf(
        "referer" to "https://not.local.com",
        "Sec-Fetch-Site" to "same-origin"
      )
    )
  }

  fun `test 404 with token in url no sec fetch site`() = runBlocking {
    doRequest404(
      "/api/qodana/resources?file=qodana.sarif.json&_qdt=${activeWebUi.token}",
      emptyMap()
    )
  }

  fun `test 404 with token in url not same origin`() = runBlocking {
    doRequest404(
      "/api/qodana/resources?file=qodana.sarif.json&_qdt=${activeWebUi.token}",
      mapOf(
        "referer" to "https://not.local.com",
        "Sec-Fetch-Site" to "cross-site"
      )
    )
  }

  fun `test 404 with token in url web ui closed`(): Unit = runBlocking {
    activeWebUi.close()
    doRequest404(
      "/api/qodana/resources?file=qodana.sarif.json&_qdt=${activeWebUi.token}",
      mapOf(
        "referer" to "https://not.local.com",
        "Sec-Fetch-Site" to "cross-site"
      )
    )
  }

  fun `test 404 with token in referer not local origin`() = runBlocking {
    doRequest404(
      "/api/qodana/resources?file=qodana.sarif.json",
      mapOf(
        "origin" to "https://not.local.com",
        "Sec-Fetch-Site" to "same-origin",
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      )
    )
  }

  fun `test 404 with token in referer not local referer`() = runBlocking {
    doRequest404(
      "/api/qodana/resources?file=qodana.sarif.json",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
        "referer" to "http://not.local.com/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      )
    )
  }

  fun `test 404 with token in referer no sec fetch site`() = runBlocking {
    doRequest404(
      "/api/qodana/resources?file=qodana.sarif.json",
      mapOf(
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      )
    )
  }

  fun `test 404 with token in referer not same origin`() = runBlocking {
    doRequest404(
      "/api/qodana/resources?file=qodana.sarif.json",
      mapOf(
        "Sec-Fetch-Site" to "cross-site",
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      )
    )
  }

  fun `test 404 with token in referer web ui closed`(): Unit = runBlocking {
    activeWebUi.close()
    doRequest404(
      "/api/qodana/resources?file=qodana.sarif.json",
      mapOf(
        "Sec-Fetch-Site" to "cross-site",
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      )
    )
  }

  fun `test 404 file is outside results dir`() = runBlocking {
    val fileOutsideResultsDir = Files.createTempFile("file-outside-results-dir", ".txt")
    Assertions.assertThat(fileOutsideResultsDir.exists()).isTrue()

    val relativePath = activeWebUi.qodanaConverterResults.path.relativize(fileOutsideResultsDir)
    doRequest404(
      "/api/qodana/resources?file=${relativePath.pathString}",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      )
    )
  }

  private fun doRequest200NotEmptyBody(urlPathWithParameters: String, headers: Map<String, String>) {
    doResourcesRequest(urlPathWithParameters, headers) {
      Assertions.assertThat(it.statusCode()).isEqualTo(200)
      Assertions.assertThat(it.body().readAllBytes()).isNotEmpty()
    }
  }

  private fun doRequest404(urlPathWithParameters: String, headers: Map<String, String>) {
    doResourcesRequest(urlPathWithParameters, headers) {
      Assertions.assertThat(it.statusCode()).isEqualTo(404)
    }
  }

  private fun doResourcesRequest(
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