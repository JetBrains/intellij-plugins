package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.testFramework.RunAll
import com.intellij.testFramework.replaceService
import com.intellij.util.ThrowableRunnable
import com.intellij.util.application
import com.jetbrains.qodana.sarif.SarifUtil
import com.jetbrains.qodana.sarif.model.SarifReport
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.cloud.api.IjQDCloudClientProvider
import org.jetbrains.qodana.cloud.api.IjQDCloudClientProviderTestImpl
import org.jetbrains.qodana.cloudclient.QDCloudClient
import org.jetbrains.qodana.cloudclient.QDCloudHttpClient
import org.jetbrains.qodana.staticAnalysis.QodanaEnvEmpty
import org.jetbrains.qodana.staticAnalysis.QodanaTestCase
import org.jetbrains.qodana.staticAnalysis.addQodanaEnvMock
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaBaselineSource
import org.junit.Test
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.nio.file.Path
import java.util.zip.GZIPOutputStream
import kotlin.io.path.deleteIfExists
import kotlin.io.path.readText

/** The frontend that the fake Qodana Cloud of the tests answers for. */
private const val FRONTEND_URL = "https://tests-qodana.cloud"

/** Tests the baseline that the analysis downloads from Qodana Cloud. */
class CloudBaselineServiceTest : QodanaTestCase() {
  private val downloaded = mutableListOf<Path>()
  private var server: HttpServer? = null
  private val cloudProvider = CountingCloudClientProvider()

  override fun setUp() {
    super.setUp()
    application.replaceService(IjQDCloudClientProvider::class.java, cloudProvider, testRootDisposable)
  }

  override fun tearDown() {
    RunAll.runAll(
      ThrowableRunnable<Throwable> { server?.stop(0) },
      ThrowableRunnable<Throwable> { downloaded.forEach { it.deleteIfExists() } },
      ThrowableRunnable<Throwable> { super.tearDown() },
    )
  }

  @Test
  fun `a baseline of the cli stops this run from asking the cloud`() = runTest {
    // qodana-cli sets the variable for all three outcomes, so any value means it already asked.
    cliResolved(QodanaBaselineSource.NO_BASELINE.name)

    val resolved = resolveBaseline(baselineFile = null, frontendUrl = FRONTEND_URL)

    assertEquals(0, cloudProvider.clients)
    assertNull(resolved.path)
    assertNull(resolved.temporaryFile)
    assertEquals(QodanaBaselineSource.NO_BASELINE, resolved.source)
  }

  @Test
  fun `an unknown value of the cli also stops this run from asking the cloud`() = runTest {
    cliResolved("SOMETHING_ELSE")

    val resolved = resolveBaseline(baselineFile = null, frontendUrl = FRONTEND_URL)

    assertEquals(0, cloudProvider.clients)
    assertEquals(QodanaBaselineSource.NO_BASELINE, resolved.source)
  }

  @Test
  fun `a baseline file of the cli keeps the source that the cli reported`() = runTest {
    cliResolved(QodanaBaselineSource.CLOUD_BASELINE.name)

    val resolved = resolveBaseline(baselineFile = "given.sarif.json", frontendUrl = FRONTEND_URL)

    assertEquals("given.sarif.json", resolved.path)
    assertEquals(QodanaBaselineSource.CLOUD_BASELINE, resolved.source)
    // The file belongs to qodana-cli, so this run must not delete it.
    assertNull(resolved.temporaryFile)
  }

  @Test
  fun `a baseline file without the variable is a local one`() = runTest {
    val resolved = resolveBaseline(baselineFile = "given.sarif.json", frontendUrl = FRONTEND_URL)

    assertEquals("given.sarif.json", resolved.path)
    assertEquals(QodanaBaselineSource.LOCAL_BASELINE, resolved.source)
    assertNull(resolved.temporaryFile)
  }

  @Test
  fun `without the variable and without a file the run asks the cloud`() = runTest {
    tokenOnly()

    resolveBaseline(baselineFile = null, frontendUrl = FRONTEND_URL).temporaryFile?.let(downloaded::add)

    // The fake cloud answers on a host that no test reaches, so only the attempt is observable here.
    assertEquals(1, cloudProvider.clients)
  }

  @Test
  fun `an empty body gives no baseline`() = runTest {
    assertNull(download(""))
  }

  @Test
  fun `an empty baseline array gives no baseline`() = runTest {
    assertNull(download("""{"baseline":[]}"""))
  }

  @Test
  fun `a body that cannot be read gives no baseline`() = runTest {
    assertNull(download("not json"))
  }

  @Test
  fun `baseline problems become a sarif report of one run`() = runTest {
    val file = download(responseWith(entry("a"), entry("b")))

    assertNotNull(file)
    val report = SarifUtil.readReport(file!!)
    assertEquals(SarifReport.Version._2_1_0, report.version)
    assertEquals(1, report.runs.size)
    val run = report.runs.first()
    // BaselineCalculation matches a baseline run to a report run by this name.
    assertEquals(qodanaProductCode(), run.tool.driver.name)
    assertEquals(listOf("a", "b"), run.results.map { it.partialFingerprints?.get("equalIndicator", 1) })
    assertEquals(listOf("short-a", "short-b"), run.results.map { it.partialFingerprints?.get("equalIndicator", 2) })
  }

  @Test
  fun `the cloud id and the unknown fields of a problem survive`() = runTest {
    val file = download(responseWith(entry("a")))

    assertNotNull(file)
    assertEquals("cloud-a", SarifUtil.readReport(file!!).runs.first().results.first().properties?.get("cloudId"))
    // The model of SARIF does not declare this field, so only a copy of the raw problem keeps it.
    assertTrue(file.readText().contains("fieldTheModelDoesNotKnow"))
  }

  @Test
  fun `a source that throws gives no baseline`() = runTest {
    assertNull(downloadCloudBaseline { throw IllegalStateException("no handler") })
  }

  @Test
  fun `the request carries the token and reads a gzip answer`() {
    var path: String? = null
    var query: String? = null
    var authorization: String? = null
    var acceptEncoding: String? = null
    val host = serve { exchange ->
      path = exchange.requestURI.path
      query = exchange.requestURI.query
      authorization = exchange.requestHeaders.getFirst("Authorization")
      acceptEncoding = exchange.requestHeaders.getFirst("Accept-Encoding")
      exchange.responseHeaders.add("Content-Encoding", "gzip")
      exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0)
      GZIPOutputStream(exchange.responseBody).use { it.write(responseWith(entry("a")).toByteArray()) }
    }

    val file = httpBaseline(host, "the-token", qodanaProductCode())?.also(downloaded::add)

    assertNotNull(file)
    assertEquals(1, SarifUtil.readReport(file!!).runs.first().results.size)
    assertEquals("/linters/baseline", path)
    assertEquals("toolName=${qodanaProductCode()}", query)
    assertEquals("Bearer the-token", authorization)
    assertEquals("gzip", acceptEncoding)
  }

  @Test
  fun `a plain answer without gzip is read too`() {
    val host = serve { exchange ->
      val body = responseWith(entry("a")).toByteArray()
      exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, body.size.toLong())
      exchange.responseBody.use { it.write(body) }
    }

    val file = httpBaseline(host, "the-token", qodanaProductCode())?.also(downloaded::add)

    assertNotNull(file)
    assertEquals(1, SarifUtil.readReport(file!!).runs.first().results.size)
  }

  @Test
  fun `no content gives no baseline`() {
    // Qodana Cloud answers 204 for a project that does not use the cloud baseline.
    val host = serve { it.sendResponseHeaders(HttpURLConnection.HTTP_NO_CONTENT, -1) }

    assertNull(httpBaseline(host, "the-token", qodanaProductCode()))
  }

  /**
   * Makes this run look like one that qodana-cli has already resolved the baseline for.
   *
   * It also gives a token, because without one the run skips Qodana Cloud anyway and the test would
   * pass for the wrong reason.
   */
  private fun cliResolved(source: String) {
    addQodanaEnvMock(testRootDisposable, object : QodanaEnvEmpty() {
      override val QODANA_TOKEN by value("token")
      override val QODANA_BASELINE_SOURCE by value(source)
    })
  }

  /** Gives a token and no resolved source, so the run is free to ask Qodana Cloud. */
  private fun tokenOnly() {
    addQodanaEnvMock(testRootDisposable, object : QodanaEnvEmpty() {
      override val QODANA_TOKEN by value("token")
    })
  }

  /** Counts how often the run built a Qodana Cloud client, which is how it reaches the baseline. */
  private class CountingCloudClientProvider : IjQDCloudClientProvider {
    private val delegate = IjQDCloudClientProviderTestImpl()
    var clients: Int = 0

    override val httpClient: QDCloudHttpClient get() = delegate.httpClient

    override fun getQDCloudClient(frontendUrl: String): QDCloudClient {
      clients++
      return delegate.getQDCloudClient(frontendUrl)
    }
  }

  /** Starts a server that answers every request with [handler], and returns its host. */
  private fun serve(handler: (HttpExchange) -> Unit): String {
    val started = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
    server = started
    started.createContext("/") { exchange -> exchange.use(handler) }
    started.start()
    return "http://127.0.0.1:${started.address.port}"
  }

  private suspend fun download(body: String): Path? =
    downloadCloudBaseline { writeBaseline(body.byteInputStream(), it) }.also { it?.let(downloaded::add) }

  private fun responseWith(vararg entries: String): String = """{"baseline":[${entries.joinToString(",")}]}"""

  /** One problem in the shape that Qodana Cloud answers, which carries two fingerprint versions. */
  @Language("JSON")
  private fun entry(fingerprint: String): String = """
    {
      "ruleId": "SomeInspection",
      "level": "warning",
      "message": { "text": "A problem" },
      "partialFingerprints": { "equalIndicator/v1": "$fingerprint", "equalIndicator/v2": "short-$fingerprint" },
      "properties": { "cloudId": "cloud-$fingerprint", "fieldTheModelDoesNotKnow": true }
    }
  """.trimIndent()
}
