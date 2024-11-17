package org.jetbrains.qodana.webUi.handlers

import com.intellij.ide.browsers.BrowserLauncher
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.util.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.ide.BuiltInServerManager
import org.jetbrains.qodana.QodanaPluginHeavyTestBase
import org.jetbrains.qodana.registerDialogInterceptor
import org.jetbrains.qodana.run.QodanaConverterInput
import org.jetbrains.qodana.ui.ci.EditYamlAndSetupCIWizardDialog
import org.jetbrains.qodana.ui.ci.SetupCIDialog
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
import kotlin.io.path.Path
import kotlin.io.path.pathString
import kotlin.time.Duration.Companion.seconds

class QodanaWebUiSetupCiHandlerTest : QodanaPluginHeavyTestBase() {
  private val activeWebUi: ActiveWebUi by lazy {
    runBlocking {
      QodanaWebUiService.getInstance(project).requestOpenBrowserWebUi(
        id = "1",
        converterInput = QodanaConverterInput.SarifFileOnly(sarifTestReports.validForConverter)
      )!!
    }
  }

  override fun getBasePath(): String = Path(super.getBasePath(), this::class.simpleName!!).pathString

  override fun runInDispatchThread(): Boolean = false

  override fun setUp() {
    super.setUp()
    setUpProject()
    invokeAndWaitIfNeeded {
      reinstansiateService(application, BrowserLauncher::class.java, TestBrowserLauncher())
    }
    BuiltInServerManager.getInstance().waitForStart()
    activeWebUi
  }

  private fun setUpProject() {
    invokeAndWaitIfNeeded {
      copyProjectTestData(getTestName(true).trim())
    }
  }

  fun `test 200 with token in url no yaml in project`(): Unit = runBlocking {
    doRequest200<EditYamlAndSetupCIWizardDialog>(
      "/api/qodana/setupCi?_qdt=${activeWebUi.token}",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
      )
    )
  }

  fun `test 200 with token in referer no yaml in project`(): Unit = runBlocking {
    doRequest200<EditYamlAndSetupCIWizardDialog>(
      "/api/qodana/setupCi",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      )
    )
  }

  fun `test 200 with token in url with yaml in project`(): Unit = runBlocking {
    doRequest200<SetupCIDialog>(
      "/api/qodana/setupCi?_qdt=${activeWebUi.token}",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
      )
    )
  }

  fun `test 200 with token in url with yml in project`(): Unit = runBlocking {
    doRequest200<SetupCIDialog>(
      "/api/qodana/setupCi?_qdt=${activeWebUi.token}",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
      )
    )
  }

  fun `test 200 with token in referer with yaml in project`(): Unit = runBlocking {
    doRequest200<SetupCIDialog>(
      "/api/qodana/setupCi",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      )
    )
  }

  fun `test 200 with token in referer with yml in project`(): Unit = runBlocking {
    doRequest200<SetupCIDialog>(
      "/api/qodana/setupCi",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      )
    )
  }

  fun `test 404 without token`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/setupCi",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
      ),
    )
  }

  fun `test 404 with invalid token in url`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/setupCi?_qdt=${UUID.randomUUID()}",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
      )
    )
  }

  fun `test 404 with invalid token in referer`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/setupCi",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${UUID.randomUUID()}"
      )
    )
  }

  fun `test 404 token in url not local origin`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/setupCi?_qdt=${activeWebUi.token}",
      mapOf(
        "origin" to "https://not-local.com",
        "Sec-Fetch-Site" to "same-origin",
      ),
    )
  }

  fun `test 404 token in referer not local origin`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/setupCi",
      mapOf(
        "origin" to "https://not-local.com",
        "Sec-Fetch-Site" to "same-origin",
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      ),
    )
  }

  fun `test 404 token in url not local referer`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/setupCi?_qdt=${activeWebUi.token}",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
        "referer" to "https://not-local.com",
      ),
    )
  }

  fun `test 404 token in referer not local referer`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/setupCi",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
        "referer" to "https://not-local.com?_qdt=${activeWebUi.token}"
      ),
    )
  }

  fun `test 404 token in url no sec fetch site`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/setupCi?_qdt=${activeWebUi.token}",
      emptyMap(),
    )
  }

  fun `test 404 token in referer no sec fetch site`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/setupCi",
      mapOf(
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      ),
    )
  }

  fun `test 404 token in url not same origin`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/setupCi?_qdt=${activeWebUi.token}",
      mapOf(
        "Sec-Fetch-Site" to "cross-site",
      )
    )
  }

  fun `test 404 token in referer not same origin`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/setupCi",
      mapOf(
        "Sec-Fetch-Site" to "cross-site",
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      ),
    )
  }

  fun `test 404 token in url closed web ui`(): Unit = runBlocking {
    activeWebUi.close()
    doRequest404(
      "/api/qodana/setupCi?_qdt=${activeWebUi.token}",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
      )
    )
  }

  fun `test 404 token in referer closed web ui`(): Unit = runBlocking {
    activeWebUi.close()
    doRequest404(
      "/api/qodana/setupCi",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      )
    )
  }

  private suspend inline fun <reified T : DialogWrapper> doRequest200(urlPathWithParameters: String, headers: Map<String, String>) {
    val dialog = registerDialogInterceptor<T>()
    doRequest(urlPathWithParameters, headers) {
      assertThat(it.statusCode()).isEqualTo(200)
    }
    withContext(Dispatchers.EDT) {
      withTimeout(15.seconds) {
        dialog.await().close(0)
      }
    }
  }

  private fun doRequest404(urlPathWithParameters: String, headers: Map<String, String>) {
    doRequest(urlPathWithParameters, headers) {
      assertThat(it.statusCode()).isEqualTo(404)
    }
  }

  private fun doRequest(
    urlPathWithParameters: String,
    headers: Map<String, String>,
    responseAssert: (HttpResponse<InputStream>) -> Unit
  ) {
    withUnlimitedRestApiRequests {
      val url = "http://localhost:${BuiltInServerManager.getInstance().port}$urlPathWithParameters"
      var requestBuilder = HttpRequest.newBuilder(URI(url)).POST(HttpRequest.BodyPublishers.noBody())

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