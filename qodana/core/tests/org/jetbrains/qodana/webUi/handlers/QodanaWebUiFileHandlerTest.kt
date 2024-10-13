package org.jetbrains.qodana.webUi.handlers

import com.intellij.ide.browsers.BrowserLauncher
import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.openapi.project.guessProjectDir
import com.intellij.util.application
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.ide.BuiltInServerManager
import org.jetbrains.qodana.QodanaPluginHeavyTestBase
import org.jetbrains.qodana.run.QodanaConverterInput
import org.jetbrains.qodana.webUi.ActiveWebUi
import org.jetbrains.qodana.webUi.QodanaWebUiService
import org.jetbrains.qodana.webUi.TestBrowserLauncher
import org.jetbrains.qodana.webUi.withUnlimitedRestApiRequests
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.nio.file.Path
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.pathString
import kotlin.io.path.readBytes

class QodanaWebUiFileHandlerTest : QodanaPluginHeavyTestBase() {
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

  fun `test 200 yaml request token in url new yaml`(): Unit = runBlocking {
    doRequest200(
      "/api/qodana/file?path=qodana.yaml&_qdt=${activeWebUi.token}",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
        "origin" to "http://localhost:63343"
      ),
      "request-qodana.yaml",
      "qodana.yaml"
    )
  }

  fun `test 200 yaml request token in url rewrite existing yaml`(): Unit = runBlocking {
    doRequest200(
      "/api/qodana/file?path=qodana.yaml&_qdt=${activeWebUi.token}",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
      ),
      "request-qodana.yaml",
      "qodana.yaml"
    )
  }

  fun `test 200 yaml request token in url rewrite existing yml`(): Unit = runBlocking {
    doRequest200(
      "/api/qodana/file?path=qodana.yaml&_qdt=${activeWebUi.token}",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
      ),
      "request-qodana.yaml",
      "qodana.yml"
    )
  }

  fun `test 200 yaml request token in referer new yaml`(): Unit = runBlocking {
    doRequest200(
      "/api/qodana/file?path=qodana.yaml",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      ),
      "request-qodana.yaml",
      "qodana.yaml"
    )
  }

  fun `test 200 yaml request token in referer rewrite existing yaml`(): Unit = runBlocking {
    doRequest200(
      "/api/qodana/file?path=qodana.yaml",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      ),
      "request-qodana.yaml",
      "qodana.yaml"
    )
  }

  fun `test 200 yaml request token in referer rewrite existing yml`(): Unit = runBlocking {
    doRequest200(
      "/api/qodana/file?path=qodana.yaml",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      ),
      "request-qodana.yaml",
      "qodana.yml"
    )
  }

  fun `test 200 qodana sarif request token in url new sarif`(): Unit = runBlocking {
    doRequest200(
      "/api/qodana/file?path=qodana.sarif.json&_qdt=${activeWebUi.token}",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
      ),
      "request-qodana.sarif.json",
      "qodana.sarif.json"
    )
  }

  fun `test 200 qodana sarif request token in url overwrite sarif`(): Unit = runBlocking {
    doRequest200(
      "/api/qodana/file?path=qodana.sarif.json&_qdt=${activeWebUi.token}",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
      ),
      "request-qodana.sarif.json",
      "qodana.sarif.json"
    )
  }

  fun `test 200 qodana sarif request token in referer new sarif`(): Unit = runBlocking {
    doRequest200(
      "/api/qodana/file?path=qodana.sarif.json",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      ),
      "request-qodana.sarif.json",
      "qodana.sarif.json"
    )
  }

  fun `test 200 qodana sarif request token in referer overwrite sarif`(): Unit = runBlocking {
    doRequest200(
      "/api/qodana/file?path=qodana.sarif.json",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      ),
      "request-qodana.sarif.json",
      "qodana.sarif.json"
    )
  }

  fun `test 404 yaml not in root token in url`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/file?path=root%2Fqodana.yaml&_qdt=${activeWebUi.token}",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
      ),
      "request-qodana.yaml",
    )
  }

  fun `test 404 yaml not in root token in referer`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/file?path=root%2Fqodana.yaml",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      ),
      "request-qodana.yaml",
    )
  }

  fun `test 404 qodana sarif not in root token in url`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/file?path=root%2Fqodana.sarif.json&_qdt=${activeWebUi.token}",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
      ),
      "request-qodana.sarif.json",
    )
  }

  fun `test 404 qodana sarif not in root token in referer`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/file?path=root%2Fqodana.sarif.json",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      ),
      "request-qodana.sarif.json",
    )
  }

  fun `test 404 not qodana sarif and not yaml token in url`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/file?path=different-file.txt&_qdt=${activeWebUi.token}",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
      ),
      "request-different-file.txt",
    )
  }

  fun `test 404 not qodana sarif and not yaml token in referer`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/file?path=different-file.txt",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      ),
      "request-different-file.txt",
    )
  }

  fun `test 404 yaml no token`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/file?path=qodana.yaml",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
      ),
      "request-qodana.yaml",
    )
  }

  fun `test 404 yaml invalid token in url`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/file?path=qodana.yaml&_qdt=${UUID.randomUUID()}",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
      ),
      "request-qodana.yaml",
    )
  }

  fun `test 404 yaml invalid token in referer`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/file?path=qodana.yaml",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${UUID.randomUUID()}"
      ),
      "request-qodana.yaml",
    )
  }

  fun `test 404 yaml token in url not local origin`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/file?path=qodana.yaml&_qdt=${activeWebUi.token}",
      mapOf(
        "origin" to "https://not-local.com",
        "Sec-Fetch-Site" to "same-origin",
      ),
      "request-qodana.yaml",
    )
  }

  fun `test 404 yaml token in referer not local origin`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/file?path=qodana.yaml",
      mapOf(
        "origin" to "https://not-local.com",
        "Sec-Fetch-Site" to "same-origin",
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      ),
      "request-qodana.yaml",
    )
  }

  fun `test 404 yaml token in url not local referer`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/file?path=qodana.yaml&_qdt=${activeWebUi.token}",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
        "referer" to "http://not-local.com"
      ),
      "request-qodana.yaml",
    )
  }

  fun `test 404 yaml token in referer not local referer`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/file?path=qodana.yaml",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
        "referer" to "http://not-local.com&_qdt=${activeWebUi.token}"
      ),
      "request-qodana.yaml",
    )
  }

  fun `test 404 yaml token in url no sec fetch site`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/file?path=qodana.yaml&_qdt=${activeWebUi.token}",
      emptyMap(),
      "request-qodana.yaml",
    )
  }

  fun `test 404 yaml token in referer no sec fetch site`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/file?path=qodana.yaml",
      mapOf(
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      ),
      "request-qodana.yaml",
    )
  }

  fun `test 404 yaml token in url not same origin`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/file?path=qodana.yaml&_qdt=${activeWebUi.token}",
      mapOf(
        "Sec-Fetch-Site" to "cross-site",
      ),
      "request-qodana.yaml",
    )
  }

  fun `test 404 yaml token in referer not same origin`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/file?path=qodana.yaml",
      mapOf(
        "Sec-Fetch-Site" to "cross-site",
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      ),
      "request-qodana.yaml",
    )
  }

  fun `test 404 yaml token in url closed web ui`(): Unit = runBlocking {
    activeWebUi.close()
    doRequest404(
      "/api/qodana/file?path=qodana.yaml&_qdt=${activeWebUi.token}",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
      ),
      "request-qodana.yaml",
    )
  }

  fun `test 404 yaml token in referer closed web ui`(): Unit = runBlocking {
    activeWebUi.close()
    doRequest404(
      "/api/qodana/file?path=qodana.yaml",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      ),
      "request-qodana.yaml",
    )
  }

  fun `test 404 qodana sarif no token`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/file?path=qodana.sarif.json",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
      ),
      "request-qodana.sarif.json",
    )
  }

  fun `test 404 qodana sarif invalid token in url`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/file?path=qodana.sarif.json&_qdt=${UUID.randomUUID()}",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
      ),
      "request-qodana.sarif.json",
    )
  }
  fun `test 404 qodana sarif invalid token in referer`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/file?path=qodana.sarif.json",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${UUID.randomUUID()}"
      ),
      "request-qodana.sarif.json",
    )
  }

  fun `test 404 qodana sarif token in url not local origin`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/file?path=qodana.sarif.json&_qdt=${activeWebUi.token}",
      mapOf(
        "origin" to "http://not-local.com",
        "Sec-Fetch-Site" to "same-origin",
      ),
      "request-qodana.sarif.json",
    )
  }

  fun `test 404 qodana sarif token in referer not local origin`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/file?path=qodana.sarif.json",
      mapOf(
        "origin" to "http://not-local.com",
        "Sec-Fetch-Site" to "same-origin",
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      ),
      "request-qodana.sarif.json",
    )
  }

  fun `test 404 qodana sarif token in url not local referer`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/file?path=qodana.sarif.json&_qdt=${activeWebUi.token}",
      mapOf(
        "referer" to "http://not-local.com",
        "Sec-Fetch-Site" to "same-origin",
      ),
      "request-qodana.sarif.json",
    )
  }

  fun `test 404 qodana sarif token in referer not local referer`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/file?path=qodana.sarif.json",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
        "referer" to "http://not-local.com?_qdt=${activeWebUi.token}"
      ),
      "request-qodana.sarif.json",
    )
  }

  fun `test 404 qodana sarif token in url no sec fetch site`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/file?path=qodana.sarif.json&_qdt=${activeWebUi.token}",
      emptyMap(),
      "request-qodana.sarif.json",
    )
  }

  fun `test 404 qodana sarif token in referer no sec fetch site`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/file?path=qodana.sarif.json",
      mapOf(
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      ),
      "request-qodana.sarif.json",
    )
  }

  fun `test 404 qodana sarif token in url not same origin`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/file?path=qodana.sarif.json&_qdt=${activeWebUi.token}",
      mapOf(
        "Sec-Fetch-Site" to "cross-site",
      ),
      "request-qodana.sarif.json",
    )
  }

  fun `test 404 qodana sarif token in referer not same origin`(): Unit = runBlocking {
    doRequest404(
      "/api/qodana/file?path=qodana.sarif.json",
      mapOf(
        "Sec-Fetch-Site" to "cross-site",
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      ),
      "request-qodana.sarif.json",
    )
  }

  fun `test 404 qodana sarif token in url closed web ui`(): Unit = runBlocking {
    activeWebUi.close()
    doRequest404(
      "/api/qodana/file?path=qodana.sarif.json&_qdt=${activeWebUi.token}",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
      ),
      "request-qodana.sarif.json",
    )
  }

  fun `test 404 qodana sarif token in referer closed web ui`(): Unit = runBlocking {
    activeWebUi.close()
    doRequest404(
      "/api/qodana/file?path=qodana.sarif.json",
      mapOf(
        "Sec-Fetch-Site" to "same-origin",
        "referer" to "http://localhost:63343/qodana.ide/index.html?_qdt=${activeWebUi.token}"
      ),
      "request-qodana.sarif.json",
    )
  }

  private fun doRequest200(
    urlPathWithParameters: String,
    headers: Map<String, String>,
    requestFile: String,
    expectedNewFile: String
  ) {
    val projectNioPath = myFixture.project.guessProjectDir()!!.toNioPath()
    val filesBeforeRequest = projectNioPath.toFile().walkTopDown().toSet()
    val requestNioFile = projectNioPath.resolve(requestFile)

    doFileRequest(urlPathWithParameters, headers, requestNioFile) {
      assertThat(it.statusCode()).isEqualTo(200)
    }

    val newFile = projectNioPath.resolve(expectedNewFile).toFile()
    val filesAfterRequest = projectNioPath.toFile().walkTopDown().toSet()
    assertThat(filesBeforeRequest + listOf(newFile)).isEqualTo(filesAfterRequest)
    assertThat(newFile.readBytes()).isEqualTo(requestNioFile.readBytes())
  }

  private fun doRequest404(
    urlPathWithParameters: String,
    headers: Map<String, String>,
    filename: String,
  ) {
    val projectNioPath = myFixture.project.guessProjectDir()!!.toNioPath()
    val filesBeforeRequest = projectNioPath.toFile().walkTopDown().toSet()
    val requestFile = projectNioPath.resolve(filename)

    doFileRequest(urlPathWithParameters, headers, requestFile) {
      assertThat(it.statusCode()).isEqualTo(404)
    }

    val filesAfterRequest = projectNioPath.toFile().walkTopDown().toSet()
    assertThat(filesBeforeRequest).isEqualTo(filesAfterRequest)
  }

  private fun doFileRequest(
    urlPathWithParameters: String,
    headers: Map<String, String>,
    file: Path,
    responseAssert: (HttpResponse<InputStream>) -> Unit
  ) {
    withUnlimitedRestApiRequests {
      val url = "http://localhost:${BuiltInServerManager.getInstance().port}$urlPathWithParameters"
      var requestBuilder = HttpRequest.newBuilder(URI(url)).POST(BodyPublishers.ofFile(file))

      requestBuilder = if (headers.isNotEmpty()) {
        requestBuilder.headers(*headers.flatMap { listOf(it.key, it.value) }.toTypedArray())
      } else {
        requestBuilder
      }

      val client = HttpClient.newBuilder().build()
      val request = requestBuilder.build()
      val response = client.send(request, HttpResponse.BodyHandlers.ofInputStream())
      responseAssert.invoke(response)
    }
  }
}