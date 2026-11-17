package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.io.HttpRequests
import com.intellij.util.net.PlatformHttpClient
import com.intellij.diagnostic.rethrowControlFlowException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runInterruptible
import org.jetbrains.qodana.cloud.api.IjQDCloudClient
import org.jetbrains.qodana.cloud.api.message
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.qodanaEnv
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import kotlin.io.path.deleteIfExists
import kotlin.time.toKotlinDuration

private val LOG = Logger.getInstance("org.jetbrains.qodana.staticAnalysis.inspections.runner.CloudBaselineService")
private const val DOWNLOAD_TIMEOUT_PROPERTY = "qodana.cloud.baseline.download.timeout.seconds"
private val DEFAULT_DOWNLOAD_TIMEOUT: Duration = Duration.ofSeconds(60)

/** qodana-cli sets this property in stage 1 of the scoped script, because only stage 2 compares with the baseline. */
internal const val SKIP_CLOUD_BASELINE_PROPERTY = "qodana.skip.cloud.baseline"
private const val DOWNLOAD_ATTEMPTS = 3
private val RETRY_PAUSE: Duration = Duration.ofSeconds(30)
private const val GZIP_BUFFER_SIZE = 64 * 1024
private val NO_BASELINE_STATUSES = setOf(HttpURLConnection.HTTP_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, HttpURLConnection.HTTP_NOT_FOUND)

/**
 * The time limit for the response of the cloud baseline download, and for each read of its body.
 * The download has no total time limit, so a large baseline on a slow network can still arrive.
 * Set `-Dqodana.cloud.baseline.download.timeout.seconds=<seconds>` for a large baseline or a slow network.
 */
private val DOWNLOAD_TIMEOUT: Duration = downloadTimeout(System.getProperty(DOWNLOAD_TIMEOUT_PROPERTY))

/** Parses [value] as a positive number of seconds. Gives [DEFAULT_DOWNLOAD_TIMEOUT] for a missing or invalid value. */
internal fun downloadTimeout(value: String?): Duration {
  if (value == null) return DEFAULT_DOWNLOAD_TIMEOUT
  val seconds = value.trim().toLongOrNull()?.takeIf { it > 0 }
  if (seconds == null) {
    LOG.warn("Invalid value '$value' of the '$DOWNLOAD_TIMEOUT_PROPERTY' property. Use a positive number of seconds. Using the default ${DEFAULT_DOWNLOAD_TIMEOUT.seconds} s")
    return DEFAULT_DOWNLOAD_TIMEOUT
  }
  return Duration.ofSeconds(seconds)
}

/** Writes the baseline of `toolName` to a temporary file. Gives null when Qodana Cloud keeps none. */
internal typealias QodanaCloudBaselineSource = suspend (toolName: String) -> Path?

/**
 * The baseline of a run in Qodana Cloud.
 *
 * [download] writes the baseline to a temporary file, or gives null when Qodana Cloud gives no baseline.
 * The caller deletes the file.
 */
class CloudBaseline internal constructor(internal val download: suspend () -> Path?)

/**
 * Gives the cloud baseline of a run. It downloads nothing.
 *
 * A baseline file always wins, so a run with [baselineFile] gets no cloud baseline.
 * A run with [SKIP_CLOUD_BASELINE_PROPERTY] or without [frontendUrl] gets none too, see also [cloudBaselineOf].
 */
internal fun resolveCloudBaseline(baselineFile: String?, frontendUrl: String?): CloudBaseline? {
  if (baselineFile != null) return null
  if (System.getProperty(SKIP_CLOUD_BASELINE_PROPERTY).toBoolean()) {
    LOG.info("qodana-cli turned the cloud baseline of this run off")
    return null
  }
  return frontendUrl?.let { cloudBaselineOf(it) }
}

/**
 * Gives the baseline of the Qodana Cloud project of `QODANA_TOKEN` at [frontendUrl]. It downloads nothing.
 * A run without a token gets no cloud baseline.
 */
internal fun cloudBaselineOf(frontendUrl: String, retryPause: Duration = RETRY_PAUSE): CloudBaseline? {
  val token = qodanaEnv().QODANA_TOKEN.value?.takeIf { it.isNotEmpty() } ?: return null
  return CloudBaseline {
    downloadCloudBaseline(retryPause) { toolName ->
      val host = lintersHost(frontendUrl) ?: return@downloadCloudBaseline null
      runInterruptible(StaticAnalysisDispatchers.IO) { httpBaseline(host, token, toolName) }
    }
  }
}

/** Gives the host of the v1 linters API of Qodana Cloud. It throws when Qodana Cloud does not answer, so that the caller can try again. */
private suspend fun lintersHost(frontendUrl: String): String? {
  val apis = when (val response = IjQDCloudClient(frontendUrl).environment.getApis()) {
    is QDCloudResponse.Success -> response.value
    is QDCloudResponse.Error -> throw IOException("Qodana Cloud tells no APIs: ${response.message}", response.exception)
  }
  val host = apis.linters.filter { it.majorVersion == 1 }.maxByOrNull { it.minorVersion }?.host
  if (host == null) LOG.info("Qodana Cloud tells no host for its v1 linters API")
  return host
}

/**
 * Gives the name that Qodana Cloud keeps the baseline of [productCode] under.
 * All JVM linters share the baseline of `QDJVM`, as in `CloudBaselineToolName` of qodana-cli.
 */
internal fun cloudBaselineToolName(productCode: String): String = when (productCode) {
  "QDJVMC", "QDAND", "QDANDC" -> "QDJVM"
  else -> productCode
}

/**
 * Reads the baseline of [toolName] from [host] and writes it to a temporary file.
 *
 * It gives null when Qodana Cloud keeps no baseline, does not serve baselines, or does not accept the token.
 * A new attempt does not change these answers.
 * It throws for each other answer that is not a success, and for a body that stops arriving for [timeout],
 * so that the caller can try again.
 */
internal fun httpBaseline(host: String, qodanaToken: String, toolName: String, timeout: Duration = DOWNLOAD_TIMEOUT): Path? {
  val query = URLEncoder.encode(cloudBaselineToolName(toolName), StandardCharsets.UTF_8)
  val uri = URI("${host.removeSuffix("/")}/linters/baseline?toolName=$query")
  val request = PlatformHttpClient.requestBuilder(uri)
    .header("Authorization", "Bearer $qodanaToken")
    .header("Accept-Encoding", "gzip")
    .timeout(timeout)
    .build()

  // The client must stay open until the body is read to the end, so the whole copy runs in this block.
  return PlatformHttpClient.client().use { client ->
    val response = try {
      PlatformHttpClient.response(client, request, HttpResponse.BodyHandlers.ofInputStream())
    }
    catch (e: HttpRequests.HttpStatusException) {
      if (e.statusCode !in NO_BASELINE_STATUSES) throw e
      LOG.warn("Qodana Cloud gives no baseline: HTTP ${e.statusCode}")
      return@use null
    }
    if (response.statusCode() == HttpURLConnection.HTTP_NO_CONTENT) {
      LOG.info("Qodana Cloud keeps no baseline for the project")
      return@use null
    }
    // Answer without gzip must work too.
    val gzip = response.headers().firstValue("Content-Encoding").orElse("").equals("gzip", ignoreCase = true)
    response.body().use { body ->
      val guarded = StallGuardedStream(body, timeout)
      // Each read of the guard sets a timer, so gzip reads large blocks, as InputStreamReader does for a plain body.
      val stream = if (gzip) GZIPInputStream(guarded, GZIP_BUFFER_SIZE) else guarded
      writeBaseline(stream, toolName)
    }
  }
}

/**
 * Fails a read of [body] that waits longer than [timeout], as `stallGuard` of qodana-cli does.
 * Without it, a server that sends the headers and then stops would stop the run after the analysis.
 */
private class StallGuardedStream(private val body: InputStream, private val timeout: Duration) : InputStream() {
  override fun read(): Int = guarded { body.read() }
  override fun read(b: ByteArray, off: Int, len: Int): Int = guarded { body.read(b, off, len) }
  // GZIPInputStream finds a second gzip member by this value.
  override fun available(): Int = body.available()
  override fun close() = body.close()

  private inline fun guarded(read: () -> Int): Int {
    val watch = ReadWatch(Thread.currentThread())
    val timer = AppExecutorUtil.getAppScheduledExecutorService().schedule(watch::stall, timeout.toMillis(), TimeUnit.MILLISECONDS)
    try {
      return read()
    }
    catch (e: IOException) {
      if (!watch.stalled) throw e
      throw IOException("The baseline of Qodana Cloud stopped arriving for ${timeout.seconds} s", e)
    }
    finally {
      timer.cancel(false)
      watch.finish()
    }
  }
}

/** Interrupts the [reader] of one read when the read stalls. The interrupt ends a read that waits for the body. */
private class ReadWatch(private val reader: Thread) {
  private var finished = false

  @Volatile
  var stalled = false
    private set

  @Synchronized
  fun stall() {
    if (finished) return
    stalled = true
    reader.interrupt()
  }

  /** After this call, the watch interrupts nothing, and the [reader] carries no interrupt of the watch. */
  @Synchronized
  fun finish() {
    finished = true
    if (stalled) Thread.interrupted()
  }
}

/**
 * Downloads the baseline of Qodana Cloud into a temporary file and returns that file if any.
 *
 * It makes [DOWNLOAD_ATTEMPTS] attempts with [retryPause] between them, as qodana-cli does.
 * After the last failed attempt the analysis runs without a baseline.
 */
internal suspend fun downloadCloudBaseline(retryPause: Duration, source: QodanaCloudBaselineSource): Path? {
  val productCode = qodanaProductCode()
  for (attempt in 1..DOWNLOAD_ATTEMPTS) {
    LOG.info("Fetching the baseline from Qodana Cloud for $productCode, attempt $attempt of $DOWNLOAD_ATTEMPTS")
    try {
      return source(productCode)
    }
    catch (e: Throwable) {
      rethrowControlFlowException(e)
      // A cut body throws here rather than passing as a short baseline, which would un-baseline real problems.
      LOG.warn("Attempt $attempt of $DOWNLOAD_ATTEMPTS to fetch the baseline of Qodana Cloud failed", e)
    }
    if (attempt < DOWNLOAD_ATTEMPTS) delay(retryPause.toKotlinDuration())
  }
  LOG.warn("Cannot use the baseline of Qodana Cloud, the analysis runs without a baseline")
  return null
}

/** Writes the baseline problems of [input] to a temporary file as a SARIF report of one run. */
internal fun writeBaseline(input: InputStream, toolName: String): Path? {
  val directory = Files.createDirectories(PathManager.getTempDir())
  val file = Files.createTempFile(directory, "qodana-cloud-baseline", ".sarif.json")
  val count = try {
    JsonReader(InputStreamReader(input, StandardCharsets.UTF_8)).use { reader ->
      JsonWriter(Files.newBufferedWriter(file)).use { writer -> copyBaseline(reader, writer, toolName) }
    }
  }
  catch (e: Throwable) {
    file.deleteIfExists()
    throw e
  }

  if (count == 0) {
    file.deleteIfExists()
    LOG.info("Qodana Cloud keeps no baseline problem that the analysis can use")
    return null
  }
  LOG.info("Using the baseline of Qodana Cloud with $count problems, file $file")
  return file
}

/** Copies the problems of `{"baseline":[...]}`, where each entry is one SARIF result, and counts them. */
private fun copyBaseline(reader: JsonReader, writer: JsonWriter, toolName: String): Int {
  val gson = Gson()
  var count = 0

  writer.beginObject()
  writer.name("version").value("2.1.0")
  writer.name("runs").beginArray().beginObject()
  writer.name("tool").beginObject().name("driver").beginObject().name("name").value(toolName).endObject().endObject()
  writer.name("results").beginArray()

  reader.beginObject()
  while (reader.hasNext()) {
    if (reader.nextName() != "baseline") {
      reader.skipValue()
      continue
    }
    reader.beginArray()
    while (reader.hasNext()) {
      gson.toJson(JsonParser.parseReader(reader), writer)
      count++
    }
    reader.endArray()
  }
  reader.endObject()

  writer.endArray().endObject().endArray().endObject()
  return count
}
