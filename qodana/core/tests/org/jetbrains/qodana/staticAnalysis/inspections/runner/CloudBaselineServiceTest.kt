package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.openapi.application.PathManager
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
import org.jetbrains.qodana.cloudclient.QDCloudEnvironment
import org.jetbrains.qodana.cloudclient.QDCloudException
import org.jetbrains.qodana.cloudclient.QDCloudHttpClient
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.cloudclient.qodanaCloudResponse
import org.jetbrains.qodana.staticAnalysis.QodanaEnvEmpty
import org.jetbrains.qodana.staticAnalysis.QodanaTestCase
import org.jetbrains.qodana.staticAnalysis.addQodanaEnvMock
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.zip.GZIPOutputStream
import kotlin.io.path.deleteIfExists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.notExists
import kotlin.io.path.readText

/** The frontend that the fake Qodana Cloud of the tests answers for. */
private const val FRONTEND_URL = "https://tests-qodana.cloud"

/** Tests the baseline that the analysis downloads from Qodana Cloud. */
class CloudBaselineServiceTest : QodanaTestCase() {
  private val downloaded = mutableListOf<Path>()
  private var server: HttpServer? = null
  /** Lets a server that stopped in the middle of a body finish its answer. */
  private var stalledServer: CountDownLatch? = null
  private val cloudProvider = CountingCloudClientProvider()

  override fun setUp() {
    super.setUp()
    application.replaceService(IjQDCloudClientProvider::class.java, cloudProvider, testRootDisposable)
  }

  override fun tearDown() {
    RunAll.runAll(
      ThrowableRunnable<Throwable> { stalledServer?.countDown() },
      ThrowableRunnable<Throwable> { server?.stop(0) },
      ThrowableRunnable<Throwable> { downloaded.forEach { it.deleteIfExists() } },
      ThrowableRunnable<Throwable> { super.tearDown() },
    )
  }

  @Test
  fun `the cli turns the cloud baseline off`() = runTest {
    tokenOnly()
    System.setProperty(SKIP_CLOUD_BASELINE_PROPERTY, "true")
    try {
      assertNull(resolveCloudBaseline(baselineFile = null, frontendUrl = FRONTEND_URL))
    }
    finally {
      System.clearProperty(SKIP_CLOUD_BASELINE_PROPERTY)
    }
    assertEquals(0, cloudProvider.clients)
  }

  @Test
  fun `a baseline file gives no cloud baseline`() = runTest {
    tokenOnly()

    assertNull(resolveCloudBaseline(baselineFile = "given.sarif.json", frontendUrl = FRONTEND_URL))
  }

  @Test
  fun `without the variable and without a file the run gets a cloud baseline that downloads later`() = runTest {
    tokenOnly()

    assertNotNull(resolveCloudBaseline(baselineFile = null, frontendUrl = FRONTEND_URL))
    assertEquals(0, cloudProvider.clients)
  }

  @Test
  fun `without a token there is no cloud baseline`() = runTest {
    assertNull(resolveCloudBaseline(baselineFile = null, frontendUrl = FRONTEND_URL))
  }

  @Test
  fun `the cloud baseline downloads at the first use`() = runTest {
    tokenOnly()
    cloudProvider.lintersHost = serveBaseline(entry("a"))
    val cloud = resolveCloudBaseline(baselineFile = null, frontendUrl = FRONTEND_URL)!!

    val file = cloud.download()?.also(downloaded::add)

    assertNotNull(file)
    assertEquals(1, SarifUtil.readReport(file!!).runs.first().results.size)
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
  fun `a source that always throws gives no baseline after three attempts`() = runTest {
    var attempts = 0

    val file = downloadCloudBaseline(retryPause = Duration.ZERO) {
      attempts++
      throw IOException("the cloud is unreachable")
    }

    assertNull(file)
    assertEquals(3, attempts)
  }

  @Test
  fun `a failed attempt is tried again`() = runTest {
    var attempts = 0

    val file = downloadCloudBaseline(retryPause = Duration.ZERO) {
      attempts++
      if (attempts == 1) throw IOException("the cloud is unreachable")
      writeBaseline(responseWith(entry("a")).byteInputStream(), it)
    }?.also(downloaded::add)

    assertNotNull(file)
    assertEquals(2, attempts)
  }

  @Test
  fun `a failed discovery of the linters api is tried again`() = runTest {
    tokenOnly()
    cloudProvider.lintersHost = serveBaseline(entry("a"))
    cloudProvider.offlineAnswers = 1

    val file = cloudBaselineOf(FRONTEND_URL, retryPause = Duration.ZERO)!!.download()?.also(downloaded::add)

    assertNotNull(file)
    assertEquals(2, cloudProvider.clients)
  }

  @Test
  fun `a forbidden project gives no baseline`() {
    val host = serve { it.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, -1) }

    assertNull(httpBaseline(host, "the-token", qodanaProductCode()))
  }

  @Test
  fun `a community jvm linter asks for the shared jvm baseline`() {
    var query: String? = null
    val host = serve { exchange ->
      query = exchange.requestURI.query
      val body = responseWith(entry("a")).toByteArray()
      exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, body.size.toLong())
      exchange.responseBody.use { it.write(body) }
    }

    val file = httpBaseline(host, "the-token", "QDJVMC")?.also(downloaded::add)

    assertEquals("toolName=QDJVM", query)
    // The report of the run carries the product code, so the baseline run keeps it too.
    assertEquals("QDJVMC", SarifUtil.readReport(file!!).runs.first().tool.driver.name)
  }

  @Test
  fun `the cloud tool name is shared by the jvm linters only`() {
    listOf("QDJVM", "QDJVMC", "QDAND", "QDANDC").forEach { assertEquals("QDJVM", cloudBaselineToolName(it)) }
    listOf("QDPY", "QDJS", "QDNET").forEach { assertEquals(it, cloudBaselineToolName(it)) }
  }

  @Test
  fun `an error answer throws so that the download is tried again`() {
    val host = serve { it.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, -1) }

    assertThrows<IOException> { httpBaseline(host, "the-token", qodanaProductCode()) }
  }

  @Test
  fun `a token that the cloud does not accept gives no baseline`() {
    val host = serve { it.sendResponseHeaders(HttpURLConnection.HTTP_UNAUTHORIZED, -1) }

    assertNull(httpBaseline(host, "the-token", qodanaProductCode()))
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
  fun `a body that stops arriving fails the download and leaves no file`() {
    val release = CountDownLatch(1)
    stalledServer = release
    val host = serve { exchange ->
      exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0)
      exchange.responseBody.write("""{"baseline":[""".toByteArray())
      exchange.responseBody.flush()
      release.await()
    }
    val before = cloudBaselineFiles()

    val e = assertThrows<IOException> { httpBaseline(host, "the-token", qodanaProductCode(), timeout = Duration.ofSeconds(1)) }

    assertTrue(e.message.orEmpty().contains("stopped arriving"))
    assertEquals(before, cloudBaselineFiles())
    assertFalse("the guard must not leave its interrupt on the thread", Thread.currentThread().isInterrupted)
  }

  @Test
  fun `no content gives no baseline`() {
    // Qodana Cloud answers 204 for a project that does not use the cloud baseline.
    val host = serve { it.sendResponseHeaders(HttpURLConnection.HTTP_NO_CONTENT, -1) }

    assertNull(httpBaseline(host, "the-token", qodanaProductCode()))
  }

  @Test
  fun `the download timeout takes a positive number of seconds and falls back to the default`() {
    assertEquals(60L, downloadTimeout(null).seconds)
    assertEquals(120L, downloadTimeout("120").seconds)
    assertEquals(120L, downloadTimeout(" 120 ").seconds)
    listOf("0", "-5", "abc", "", "5m").forEach { assertEquals(60L, downloadTimeout(it).seconds) }
  }

  /** Gives a token, so the run is free to ask Qodana Cloud. */
  private fun tokenOnly() {
    addQodanaEnvMock(testRootDisposable, object : QodanaEnvEmpty() {
      override val QODANA_TOKEN by value("token")
    })
  }

  /**
   * Counts how often the run built a Qodana Cloud client, which is how it reaches the baseline.
   *
   * With [lintersHost] set, the client tells that host for the linters API, so the run can download a baseline from it.
   */
  private class CountingCloudClientProvider : IjQDCloudClientProvider {
    private val delegate = IjQDCloudClientProviderTestImpl()
    var clients: Int = 0
    var lintersHost: String? = null
    /** The number of the first requests that find Qodana Cloud offline. */
    var offlineAnswers: Int = 0

    override val httpClient: QDCloudHttpClient get() = delegate.httpClient

    override fun getQDCloudClient(frontendUrl: String): QDCloudClient {
      clients++
      val host = lintersHost ?: return delegate.getQDCloudClient(frontendUrl)
      val environment = object : QDCloudEnvironment {
        override suspend fun getApis(): QDCloudResponse<QDCloudEnvironment.Apis> {
          if (offlineAnswers > 0) {
            offlineAnswers--
            return QDCloudResponse.Error.Offline(QDCloudException.Offline())
          }
          return qodanaCloudResponse {
            QDCloudEnvironment.Apis(
              api = listOf(QDCloudEnvironment.Apis.Api(frontendUrl, majorVersion = 1, minorVersion = 0)),
              linters = listOf(QDCloudEnvironment.Apis.Api(host, majorVersion = 1, minorVersion = 0)),
            )
          }
        }
      }
      return QDCloudClient(httpClient, environment)
    }
  }

  /** Starts a server that answers every request with a baseline of [entries], and returns its host. */
  private fun serveBaseline(vararg entries: String): String = serve { exchange ->
    val body = responseWith(*entries).toByteArray()
    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, body.size.toLong())
    exchange.responseBody.use { it.write(body) }
  }

  /** Starts a server that answers every request with [handler], and returns its host. */
  private fun serve(handler: (HttpExchange) -> Unit): String {
    val started = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
    server = started
    started.createContext("/") { exchange -> exchange.use(handler) }
    started.start()
    return "http://127.0.0.1:${started.address.port}"
  }

  private fun cloudBaselineFiles(): Set<String> {
    val directory = PathManager.getTempDir()
    if (directory.notExists()) return emptySet()
    return directory.listDirectoryEntries("qodana-cloud-baseline*").map { it.name }.toSet()
  }

  private suspend fun download(body: String): Path? =
    downloadCloudBaseline(retryPause = Duration.ZERO) { writeBaseline(body.byteInputStream(), it) }.also { it?.let(downloaded::add) }

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
