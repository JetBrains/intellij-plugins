package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.net.PlatformHttpClient
import com.intellij.diagnostic.rethrowControlFlowException
import com.jetbrains.qodana.sarif.model.PropertyBag
import com.jetbrains.qodana.sarif.model.SarifReport
import kotlinx.coroutines.runInterruptible
import org.jetbrains.qodana.cloud.api.IjQDCloudClient
import org.jetbrains.qodana.cloudclient.asSuccess
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaBaselineSource
import org.jetbrains.qodana.staticAnalysis.qodanaEnv
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
import java.util.zip.GZIPInputStream
import kotlin.io.path.deleteIfExists

private val LOG = Logger.getInstance("org.jetbrains.qodana.staticAnalysis.inspections.runner.CloudBaselineService")
private const val DOWNLOAD_TIMEOUT_PROPERTY = "qodana.cloud.baseline.download.timeout.seconds"
private val DEFAULT_DOWNLOAD_TIMEOUT: Duration = Duration.ofSeconds(60)

/**
 * The time limit for the response of the cloud baseline download.
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

/** The run property that marks a baseline file as made from the baseline of Qodana Cloud. qodana-sarif reads it. */
private const val BASELINE_ORIGIN_PROPERTY = "baselineOrigin"
private const val CLOUD_BASELINE_ORIGIN = "CLOUD"

/** Writes the baseline of `toolName` to a temporary file. Gives null when Qodana Cloud keeps none. */
internal typealias QodanaCloudBaselineSource = (toolName: String) -> Path?

/** The baseline of a run: its file, where it came from, and the file that this run must delete. */
internal class ResolvedBaseline(
  val path: String?,
  val source: QodanaBaselineSource,
  val temporaryFile: Path?,
)

/**
 * Resolves the baseline of a headless run, and downloads it from Qodana Cloud when needed.
 *
 * qodana-cli sets `QODANA_BASELINE_SOURCE` once it has resolved the baseline, for all three
 * outcomes. A run that sees any value takes that answer and never asks Qodana Cloud, so the inner
 * CLI of a cdnet or clang image does not download the same baseline twice.
 *
 * A baseline file always wins, so the cloud is asked only when the command line gives none.
 */
internal suspend fun resolveBaseline(baselineFile: String?, frontendUrl: String?): ResolvedBaseline {
  val resolvedByCli = qodanaEnv().QODANA_BASELINE_SOURCE.value?.takeIf { it.isNotEmpty() }
  if (resolvedByCli != null && QodanaBaselineSource.entries.none { it.name == resolvedByCli }) {
    // An unknown value still means that a CLI resolved the baseline, so this run must not ask again.
    LOG.warn("qodana-cli reported an unknown baseline source '$resolvedByCli'")
  }

  if (baselineFile != null) {
    val source = when (resolvedByCli) {
      QodanaBaselineSource.CLOUD_BASELINE.name -> QodanaBaselineSource.CLOUD_BASELINE
      else -> QodanaBaselineSource.LOCAL_BASELINE
    }
    return ResolvedBaseline(baselineFile, source, temporaryFile = null)
  }

  if (resolvedByCli != null) {
    LOG.info("qodana-cli resolved the baseline of this run as $resolvedByCli")
    return ResolvedBaseline(path = null, source = QodanaBaselineSource.NO_BASELINE, temporaryFile = null)
  }

  val downloaded = frontendUrl?.let { cloudBaseline(it) }
    ?: return ResolvedBaseline(path = null, source = QodanaBaselineSource.NO_BASELINE, temporaryFile = null)
  return ResolvedBaseline(downloaded.toString(), QodanaBaselineSource.CLOUD_BASELINE, downloaded)
}

/**
 * Downloads the baseline of the Qodana Cloud project of `QODANA_TOKEN` from [frontendUrl].
 *
 * It gives null when the run has no token, and when Qodana Cloud keeps no baseline. The analysis then
 * runs without a baseline. The caller owns the file it returns.
 */
internal suspend fun cloudBaseline(frontendUrl: String): Path? {
  val token = qodanaEnv().QODANA_TOKEN.value?.takeIf { it.isNotEmpty() } ?: return null
  val source = cloudBaselineSource(frontendUrl, token) ?: return null
  return downloadCloudBaseline(source)
}

/** Builds the source that reads the baseline of the Qodana Cloud project of [qodanaToken]. */
private suspend fun cloudBaselineSource(frontendUrl: String, qodanaToken: String): QodanaCloudBaselineSource? {
  val client = IjQDCloudClient(frontendUrl)
  val host = client.environment.getApis().asSuccess()
    ?.linters
    ?.filter { it.majorVersion == 1 }
    ?.maxByOrNull { it.minorVersion }
    ?.host
  if (host == null) {
    LOG.info("Qodana Cloud tells no host for its v1 linters API")
    return null
  }
  return { toolName -> httpBaseline(host, qodanaToken, toolName) }
}

/** Reads the baseline of [toolName] from [host] and writes it to a temporary file. */
internal fun httpBaseline(host: String, qodanaToken: String, toolName: String): Path? {
  val uri = URI("${host.removeSuffix("/")}/linters/baseline?toolName=${URLEncoder.encode(toolName, StandardCharsets.UTF_8)}")
  val request = PlatformHttpClient.requestBuilder(uri)
    .header("Authorization", "Bearer $qodanaToken")
    .header("Accept-Encoding", "gzip")
    .timeout(DOWNLOAD_TIMEOUT)
    .build()

  // The client must stay open until the body is read to the end, so the whole copy runs in this block.
  return PlatformHttpClient.client().use { client ->
    val response = PlatformHttpClient.response(client, request, HttpResponse.BodyHandlers.ofInputStream())
    if (response.statusCode() == HttpURLConnection.HTTP_NO_CONTENT) {
      LOG.info("Qodana Cloud keeps no baseline for the project")
      return@use null
    }
    // Answer without gzip must work too.
    val gzip = response.headers().firstValue("Content-Encoding").orElse("").equals("gzip", ignoreCase = true)
    response.body().use { body ->
      val stream = if (gzip) GZIPInputStream(body) else body
      writeBaseline(stream, toolName)
    }
  }
}

/** Downloads the baseline of Qodana Cloud into a temporary file and returns that file if any. */
internal suspend fun downloadCloudBaseline(source: QodanaCloudBaselineSource): Path? {
  val productCode = qodanaProductCode()
  LOG.info("Fetching the baseline from Qodana Cloud for $productCode")
  return try {
    runInterruptible(StaticAnalysisDispatchers.IO) { source(productCode) }
  }
  catch (e: Throwable) {
    rethrowControlFlowException(e)
    // A cut body throws here rather than passing as a short baseline, which would un-baseline real problems.
    LOG.warn("Cannot use the baseline of Qodana Cloud", e)
    null
  }
}

/** Marks each run of the baseline [report] as a cloud baseline. */
internal fun markAsCloudBaseline(report: SarifReport) {
  for (run in report.runs.orEmpty()) {
    run.properties = (run.properties ?: PropertyBag()).also { it[BASELINE_ORIGIN_PROPERTY] = CLOUD_BASELINE_ORIGIN }
  }
}

/** Writes the baseline problems of [input] to a temporary file as a SARIF report of one run, marked as a cloud baseline. */
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
  writer.name("properties").beginObject().name(BASELINE_ORIGIN_PROPERTY).value(CLOUD_BASELINE_ORIGIN).endObject()
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
