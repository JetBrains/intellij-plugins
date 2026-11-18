package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.intellij.diagnostic.rethrowControlFlowException
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.io.HttpRequests
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.job
import kotlinx.coroutines.runInterruptible
import org.jetbrains.qodana.cloud.api.IjQDCloudClient
import org.jetbrains.qodana.cloud.api.message
import org.jetbrains.qodana.cloudclient.QDCloudResponse
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.qodanaEnv
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private val LOG = Logger.getInstance("org.jetbrains.qodana.staticAnalysis.inspections.runner.CloudBaselineService")
private const val DOWNLOAD_TIMEOUT_PROPERTY = "qodana.cloud.baseline.download.timeout.seconds"
private val DEFAULT_DOWNLOAD_TIMEOUT: Duration = 60.seconds

/** qodana-cli sets this property in stage 1 of the scoped script, because only stage 2 compares with the baseline. */
internal const val SKIP_CLOUD_BASELINE_PROPERTY = "qodana.skip.cloud.baseline"
private const val DOWNLOAD_ATTEMPTS = 3
private val RETRY_PAUSE: Duration = 30.seconds
private val NO_BASELINE_STATUSES = setOf(HttpURLConnection.HTTP_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN, HttpURLConnection.HTTP_NOT_FOUND)

/**
 * The time limit for each read of the cloud baseline download: the wait for the response, and each read of its body.
 * The download has no total time limit, so a large baseline on a slow network can still arrive.
 * Set `-Dqodana.cloud.baseline.download.timeout.seconds=<seconds>` for a slow server or a slow network.
 */
private val DOWNLOAD_TIMEOUT: Duration = downloadTimeout(System.getProperty(DOWNLOAD_TIMEOUT_PROPERTY))

/** Parses [value] as a positive number of seconds. Gives [DEFAULT_DOWNLOAD_TIMEOUT] for a missing or invalid value. */
internal fun downloadTimeout(value: String?): Duration {
  if (value == null) return DEFAULT_DOWNLOAD_TIMEOUT
  val seconds = value.trim().toLongOrNull()?.takeIf { it > 0 }
  if (seconds == null) {
    LOG.warn("Invalid value '$value' of the '$DOWNLOAD_TIMEOUT_PROPERTY' property. Use a positive number of seconds. Using the default ${DEFAULT_DOWNLOAD_TIMEOUT.inWholeSeconds} s")
    return DEFAULT_DOWNLOAD_TIMEOUT
  }
  return seconds.seconds
}

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
 * A run without a token gets no cloud baseline. The tests pass a short [retryPause] and [readTimeout].
 */
internal fun cloudBaselineOf(
  frontendUrl: String,
  retryPause: Duration = RETRY_PAUSE,
  readTimeout: Duration = DOWNLOAD_TIMEOUT,
): CloudBaseline? {
  val token = qodanaEnv().QODANA_TOKEN.value?.takeIf { it.isNotEmpty() } ?: return null
  return CloudBaseline { downloadCloudBaseline(frontendUrl, token, retryPause, readTimeout) }
}

/**
 * Downloads the baseline of Qodana Cloud into a temporary file and returns that file if any.
 *
 * It makes [DOWNLOAD_ATTEMPTS] attempts with [retryPause] between them, as qodana-cli does.
 * An answer of [NO_BASELINE_STATUSES] gives no baseline at once, because a new attempt does not change it.
 * After the last failed attempt the analysis runs without a baseline.
 */
private suspend fun downloadCloudBaseline(frontendUrl: String, token: String, retryPause: Duration, readTimeout: Duration): Path? {
  val productCode = qodanaProductCode()
  for (attempt in 1..DOWNLOAD_ATTEMPTS) {
    LOG.info("Fetching the baseline from Qodana Cloud for $productCode, attempt $attempt of $DOWNLOAD_ATTEMPTS")
    try {
      val host = lintersHost(frontendUrl) ?: return null
      return httpBaseline(host, token, productCode, readTimeout)
    }
    catch (e: Throwable) {
      rethrowControlFlowException(e)
      if (e is HttpRequests.HttpStatusException && e.statusCode in NO_BASELINE_STATUSES) {
        LOG.warn("Qodana Cloud gives no baseline: HTTP ${e.statusCode}")
        return null
      }
      // A cut body throws here rather than passing as a short baseline, which would un-baseline real problems.
      LOG.warn("Attempt $attempt of $DOWNLOAD_ATTEMPTS to fetch the baseline of Qodana Cloud failed", e)
    }
    if (attempt < DOWNLOAD_ATTEMPTS) delay(retryPause)
  }
  LOG.warn("Cannot use the baseline of Qodana Cloud, the analysis runs without a baseline")
  return null
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
 * It gives null when Qodana Cloud keeps no baseline for the project.
 *
 * It throws [HttpRequests.HttpStatusException] for an answer that is not a success,
 * and an [IOException] for a read that waits longer than [readTimeout].
 */
private suspend fun httpBaseline(host: String, qodanaToken: String, toolName: String, readTimeout: Duration): Path? {
  val query = URLEncoder.encode(cloudBaselineToolName(toolName), StandardCharsets.UTF_8)
  val url = "${host.removeSuffix("/")}/linters/baseline?toolName=$query"
  val job = currentCoroutineContext().job
  return runInterruptible(StaticAnalysisDispatchers.IO) {
    HttpRequests.request(url)
      .tuner { it.setRequestProperty("Authorization", "Bearer $qodanaToken") }
      .readTimeout(readTimeout.inWholeMilliseconds.toInt())
      .connect { request ->
        if ((request.connection as HttpURLConnection).responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
          LOG.info("Qodana Cloud keeps no baseline for the project")
          return@connect null
        }
        // HttpRequests asks for gzip and decodes a gzip answer itself.
        writeBaseline(CancellableStream(request.inputStream, job), toolName)
      }
  }
}

/**
 * Stops the download at the next read after [job] is cancelled.
 * An interrupt does not end a read of [HttpURLConnection], so `runInterruptible` alone cannot stop the download.
 */
private class CancellableStream(body: InputStream, private val job: Job) : FilterInputStream(body) {
  override fun read(): Int {
    job.ensureActive()
    return super.read()
  }

  override fun read(b: ByteArray, off: Int, len: Int): Int {
    job.ensureActive()
    return super.read(b, off, len)
  }
}

/** Writes the baseline problems of [input] to a temporary file as a SARIF report of one run. */
private fun writeBaseline(input: InputStream, toolName: String): Path? {
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
