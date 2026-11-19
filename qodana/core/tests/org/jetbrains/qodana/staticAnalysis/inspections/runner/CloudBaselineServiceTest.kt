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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
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
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaBaseline
import org.junit.Test
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.nio.file.Path
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.GZIPOutputStream
import kotlin.io.path.deleteIfExists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.notExists
import kotlin.io.path.readText
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/** The frontend that the fake Qodana Cloud of the tests answers for. */
private const val FRONTEND_URL = "https://tests-qodana.cloud"

/** Tests the baseline that the analysis downloads from Qodana Cloud, through a fake linters API. */
class CloudBaselineServiceTest : QodanaTestCase() {
  private val downloaded = mutableListOf<Path>()
  private var server: HttpServer? = null
  /** A second server, for the target of a redirect. */
  private var otherServer: HttpServer? = null
  /** Runs the handlers, so that a handler that waits does not block the next attempt. */
  private val handlers: ExecutorService = Executors.newCachedThreadPool()
  /** The number of requests that the fake linters API got. */
  private val requests = AtomicInteger()
  /** Lets a server that stopped in the middle of a body finish its answer. */
  private val stalledServer = CountDownLatch(1)
  private val cloudProvider = CountingCloudClientProvider()

  override fun setUp() {
    super.setUp()
    application.replaceService(IjQDCloudClientProvider::class.java, cloudProvider, testRootDisposable)
  }

  override fun tearDown() {
    RunAll.runAll(
      ThrowableRunnable<Throwable> { stalledServer.countDown() },
      ThrowableRunnable<Throwable> { server?.stop(0) },
      ThrowableRunnable<Throwable> { otherServer?.stop(0) },
      ThrowableRunnable<Throwable> { handlers.shutdownNow() },
      ThrowableRunnable<Throwable> { downloaded.forEach { it.deleteIfExists() } },
      ThrowableRunnable<Throwable> { super.tearDown() },
    )
  }

  @Test
  fun `the cli turns the cloud baseline off`() = runTest {
    tokenOnly()
    System.setProperty(SKIP_CLOUD_BASELINE_PROPERTY, "true")
    try {
      assertNull(resolveBaseline(baselineFile = null, frontendUrl = FRONTEND_URL))
    }
    finally {
      System.clearProperty(SKIP_CLOUD_BASELINE_PROPERTY)
    }
    assertEquals(0, cloudProvider.clients)
  }

  @Test
  fun `a baseline file wins over the cloud baseline`() = runTest {
    tokenOnly()

    val baseline = resolveBaseline(baselineFile = Path.of("given.sarif.json"), frontendUrl = FRONTEND_URL)

    assertEquals(Path.of("given.sarif.json"), (baseline as QodanaBaseline.Local).path)
    assertEquals(0, cloudProvider.clients)
  }

  @Test
  fun `without the variable and without a file the run gets a cloud baseline that downloads later`() = runTest {
    tokenOnly()

    assertInstanceOf(resolveBaseline(baselineFile = null, frontendUrl = FRONTEND_URL), QodanaBaseline.Cloud::class.java)
    assertEquals(0, cloudProvider.clients)
  }

  @Test
  fun `without a token there is no cloud baseline`() = runTest {
    assertNull(resolveBaseline(baselineFile = null, frontendUrl = FRONTEND_URL))
  }

  @Test
  fun `the cloud baseline downloads at the first use`() = runTest {
    serveBaseline(entry("a"))
    val cloud = resolveBaseline(baselineFile = null, frontendUrl = FRONTEND_URL) as QodanaBaseline.Cloud

    val file = cloud.download()?.also(downloaded::add)

    assertNotNull(file)
    assertEquals(1, SarifUtil.readReport(file!!).runs.first().results.size)
    assertEquals(1, cloudProvider.clients)
  }

  @Test
  fun `an empty body gives no baseline`() = runTest {
    serveBody("")

    assertNull(download())
  }

  @Test
  fun `an empty baseline array gives no baseline`() = runTest {
    serveBody("""{"baseline":[]}""")

    assertNull(download())
  }

  @Test
  fun `a body that cannot be read gives no baseline`() = runTest {
    serveBody("not json")

    assertNull(download())
  }

  @Test
  fun `baseline problems become a sarif report of one run`() = runTest {
    serveBaseline(entry("a"), entry("b"))

    val file = download()

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
    serveBaseline(entry("a"))

    val file = download()

    assertNotNull(file)
    assertEquals("cloud-a", SarifUtil.readReport(file!!).runs.first().results.first().properties?.get("cloudId"))
    // The model of SARIF does not declare this field, so only a copy of the raw problem keeps it.
    assertTrue(file.readText().contains("fieldTheModelDoesNotKnow"))
  }

  @Test
  fun `an error answer on each attempt gives no baseline after three attempts`() = runTest {
    tokenOnly()
    cloudProvider.lintersHost = serve { it.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, -1) }

    assertNull(download())
    assertEquals(3, requests.get())
  }

  @Test
  fun `a failed attempt is tried again`() = runTest {
    tokenOnly()
    val body = responseWith(entry("a")).toByteArray()
    cloudProvider.lintersHost = serve { exchange ->
      if (requests.get() == 1) {
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, -1)
        return@serve
      }
      exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, body.size.toLong())
      exchange.responseBody.use { it.write(body) }
    }

    assertNotNull(download())
    assertEquals(2, requests.get())
  }

  @Test
  fun `a failed discovery of the linters api is tried again`() = runTest {
    serveBaseline(entry("a"))
    cloudProvider.offlineAnswers = 1

    assertNotNull(download())
    assertEquals(2, cloudProvider.clients)
  }

  @Test
  fun `an answer that a new attempt does not change gives no baseline at once`() = runTest {
    tokenOnly()
    for (status in listOf(HttpURLConnection.HTTP_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, HttpURLConnection.HTTP_NOT_FOUND)) {
      server?.stop(0)
      requests.set(0)
      cloudProvider.lintersHost = serve { it.sendResponseHeaders(status, -1) }

      assertNull("HTTP $status", download())
      assertEquals("HTTP $status", 1, requests.get())
    }
  }

  @Test
  fun `no content gives no baseline`() = runTest {
    tokenOnly()
    // Qodana Cloud answers 204 for a project that does not use the cloud baseline.
    cloudProvider.lintersHost = serve { it.sendResponseHeaders(HttpURLConnection.HTTP_NO_CONTENT, -1) }

    assertNull(download())
    assertEquals(1, requests.get())
  }

  @Test
  fun `the cloud tool name is shared by the jvm linters only`() {
    listOf("QDJVM", "QDJVMC", "QDAND", "QDANDC").forEach { assertEquals("QDJVM", cloudBaselineToolName(it)) }
    listOf("QDPY", "QDJS", "QDNET").forEach { assertEquals(it, cloudBaselineToolName(it)) }
  }

  @Test
  fun `the request carries the token and reads a gzip answer`() = runTest {
    tokenOnly()
    var path: String? = null
    var query: String? = null
    var authorization: String? = null
    var acceptEncoding: String? = null
    cloudProvider.lintersHost = serve { exchange ->
      path = exchange.requestURI.path
      query = exchange.requestURI.query
      authorization = exchange.requestHeaders.getFirst("Authorization")
      acceptEncoding = exchange.requestHeaders.getFirst("Accept-Encoding")
      exchange.responseHeaders.add("Content-Encoding", "gzip")
      exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0)
      GZIPOutputStream(exchange.responseBody).use { it.write(responseWith(entry("a")).toByteArray()) }
    }

    val file = download()

    assertNotNull(file)
    assertEquals(1, SarifUtil.readReport(file!!).runs.first().results.size)
    assertEquals("/linters/baseline", path)
    assertEquals("toolName=${cloudBaselineToolName(qodanaProductCode())}", query)
    assertEquals("Bearer token", authorization)
    assertEquals("gzip", acceptEncoding)
  }

  @Test
  fun `a redirect to another host does not get the token`() = runTest {
    tokenOnly()
    var redirectedAuthorization: String? = "not asked"
    val target = HttpServer.create(InetSocketAddress(0), 0)
    otherServer = target
    target.executor = handlers
    target.createContext("/") { exchange ->
      exchange.use {
        redirectedAuthorization = it.requestHeaders.getFirst("Authorization")
        val body = responseWith(entry("a")).toByteArray()
        it.sendResponseHeaders(HttpURLConnection.HTTP_OK, body.size.toLong())
        it.responseBody.use { out -> out.write(body) }
      }
    }
    target.start()
    // The API host is 127.0.0.1, so localhost is another host for the client.
    val location = "http://localhost:${target.address.port}/storage/baseline.json"
    var apiAuthorization: String? = null
    cloudProvider.lintersHost = serve { exchange ->
      apiAuthorization = exchange.requestHeaders.getFirst("Authorization")
      exchange.responseHeaders.add("Location", location)
      exchange.sendResponseHeaders(HttpURLConnection.HTTP_MOVED_TEMP, -1)
    }

    val file = download()

    assertNotNull(file)
    assertEquals("Bearer token", apiAuthorization)
    assertNull(redirectedAuthorization)
  }

  @Test
  fun `a read timeout beyond the range of a socket timeout still downloads`() = runTest {
    serveBaseline(entry("a"))

    assertNotNull(download(readTimeout = Duration.INFINITE))
  }

  @Test
  fun `a plain answer without gzip is read too`() = runTest {
    serveBaseline(entry("a"))

    val file = download()

    assertNotNull(file)
    assertEquals(1, SarifUtil.readReport(file!!).runs.first().results.size)
  }

  @Test
  fun `a body that stops arriving fails each attempt and leaves no file`() = runTest {
    tokenOnly()
    cloudProvider.lintersHost = serve { exchange ->
      exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0)
      exchange.responseBody.write("""{"baseline":[""".toByteArray())
      exchange.responseBody.flush()
      stalledServer.await()
    }
    val before = cloudBaselineFiles()

    assertNull(download(readTimeout = 1.seconds))

    assertEquals(3, requests.get())
    assertEquals(before, cloudBaselineFiles())
  }

  @Test
  fun `a cancelled download stops while the body still arrives and leaves no file`() = runTest {
    tokenOnly()
    val sending = CompletableDeferred<Unit>()
    cloudProvider.lintersHost = serve { exchange ->
      exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0)
      val body = exchange.responseBody
      body.write("""{"baseline":[""".toByteArray())
      // A body of 10 s, much longer than the wait for the cancellation.
      val end = TimeSource.Monotonic.markNow() + 10.seconds
      while (stalledServer.count > 0 && end.hasNotPassedNow()) {
        body.write((entry("a") + ",").toByteArray())
        body.flush()
        sending.complete(Unit)
        Thread.sleep(20)
      }
    }
    val before = cloudBaselineFiles()
    val cloud = cloudBaselineOf(FRONTEND_URL, retryPause = Duration.ZERO)!!

    withTimeout(30.seconds) {
      val download = launch { cloud.download()?.also(downloaded::add) }
      sending.await()
      delay(200.milliseconds)
      val cancel = TimeSource.Monotonic.markNow()
      download.cancelAndJoin()
      assertTrue("the download must stop at the next read", cancel.elapsedNow() < 5.seconds)
    }
    assertEquals(before, cloudBaselineFiles())
  }

  @Test
  fun `the download timeout takes a positive number of seconds and falls back to the default`() {
    assertEquals(60L, downloadTimeout(null).inWholeSeconds)
    assertEquals(120L, downloadTimeout("120").inWholeSeconds)
    assertEquals(120L, downloadTimeout(" 120 ").inWholeSeconds)
    listOf("0", "-5", "abc", "", "5m").forEach { assertEquals(60L, downloadTimeout(it).inWholeSeconds) }
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

  /** Downloads the cloud baseline of the test project, with no pause between the attempts. */
  private suspend fun download(readTimeout: Duration = 10.seconds): Path? =
    cloudBaselineOf(FRONTEND_URL, retryPause = Duration.ZERO, readTimeout = readTimeout)!!.download()?.also(downloaded::add)

  /** Gives a token and a linters API that answers every request with a baseline of [entries]. */
  private fun serveBaseline(vararg entries: String) = serveBody(responseWith(*entries))

  /** Gives a token and a linters API that answers every request with [body]. */
  private fun serveBody(body: String) {
    tokenOnly()
    val bytes = body.toByteArray()
    cloudProvider.lintersHost = serve { exchange ->
      exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, if (bytes.isEmpty()) -1 else bytes.size.toLong())
      exchange.responseBody.use { it.write(bytes) }
    }
  }

  /** Starts a server that counts the requests and answers each with [handler], and returns its host. */
  private fun serve(handler: (HttpExchange) -> Unit): String {
    val started = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
    server = started
    started.executor = handlers
    started.createContext("/") { exchange ->
      requests.incrementAndGet()
      exchange.use(handler)
    }
    started.start()
    return "http://127.0.0.1:${started.address.port}"
  }

  private fun cloudBaselineFiles(): Set<String> {
    val directory = PathManager.getTempDir()
    if (directory.notExists()) return emptySet()
    return directory.listDirectoryEntries("qodana-cloud-baseline*").map { it.name }.toSet()
  }

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
