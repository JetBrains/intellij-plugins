// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import com.bertramlabs.plugins.hcl4j.HCLParser
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URI
import java.net.URLDecoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.ZipInputStream

object TerraformProvidersMetadataBuilder {

  private val objectMapper: ObjectMapper = ObjectMapper()

  private val httpClient: HttpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()

  private val terraformRegistryHost = System.getenv("TERRAFORM_REGISTRY_HOST") ?: "https://registry.terraform.io"
  private val downloadsLimitForProvider = System.getenv("DOWNLOADS_LIMIT_FOR_PROVIDER")?.toInt() ?: 50_000
  private val cleanDownloadedData = System.getenv("CLEAN_DOWNLOADED_DATA")?.toBoolean() ?: true
  private val maxSchemaSize = System.getenv("MAX_SCHEMA_SIZE")?.toLong() ?: (20L * 1024 * 1024)  // 20 MB by default
  private val metadataRepoUrl = System.getenv("REPO_URL") ?: "https://packages.jetbrains.team/maven/p/ij/intellij-dependencies"
  private val metadataGroup = System.getenv("ARTIFACT_GROUP") ?: "org.intellij.plugins.hcl.terraform"
  private val previousMetadataVersion: String? = System.getenv("PREVIOUS_METADATA_VERSION")

  private const val MAX_ATTEMPTS = 5

  private val logger = LoggerFactory.getLogger(TerraformProvidersMetadataBuilder::class.java.simpleName)

  private val totalProviders = AtomicInteger(0)
  private val failedProviders = AtomicInteger(0)
  private val skippedProviders = AtomicInteger(0)

  /** Repeats the request after a 429 or 5xx response, with a longer pause after each attempt. */
  private fun getQuery(httpQuery: String): HttpResponse<String> {
    val httpRequest =
      HttpRequest.newBuilder().uri(
        URI(httpQuery)
      ).GET().build()
    var attempt = 1
    while (true) {
      val response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())
      val status = response.statusCode()
      if (status != 429 && status < 500 || attempt == MAX_ATTEMPTS) return response
      logger.warn("Status $status for $httpQuery, attempt $attempt of $MAX_ATTEMPTS")
      Thread.sleep(attempt * 10_000L)
      attempt++
    }
  }

  private fun String.urlDecode(): String = URLDecoder.decode(this, StandardCharsets.UTF_8)

  /**
   * Loads the providers of all tiers, because the registry can add a tier.
   * The page order can change during the load. Then a provider can come twice, and the function drops the copy.
   */
  private fun getProvidersDataFromPages(): List<JsonNode> = sequence {
    logger.info("Loading providers ...")
    var httpResponse = getQuery("${terraformRegistryHost}/v2/providers?sort=-downloads&page[size]=100")
    do {
      check(httpResponse.statusCode() == 200) { "Failed to load providers from ${httpResponse.uri()}, status=${httpResponse.statusCode()}" }
      val jsonResponse = objectMapper.readTree(httpResponse.body())
      val page = jsonResponse.get("meta")?.get("pagination")?.get("current-page")?.asLong() ?: 0
      val pageTotal = jsonResponse.get("meta")?.get("pagination")?.get("total-pages")?.asLong()
      logger.info("Loaded page $page of $pageTotal")
      when (val responseData = jsonResponse["data"]) {
        is ObjectNode -> yield(responseData)
        is ArrayNode -> yieldAll(responseData.elements())
      }
      val next = jsonResponse.get("links")?.get("next")?.takeIf { !it.isNull }?.asText()?.urlDecode()
      if (next != null) httpResponse = getQuery("${terraformRegistryHost}${next}")
    }
    while (next != null)
  }.distinctBy { it.fullName() }.toList()

  @JvmStatic
  fun main(args: Array<String>) {
    val version = getTerraformVersion()
    logger.info("Terraform version: $version")

    generateFunctionsMetadata()

    val outputDir = File("plugins-meta").apply { mkdirs() }
    val allOut = File(outputDir, "allout.json")
    val droppedProviders = loadResourceList("dropped-providers.list")
    val requiredProviders = loadResourceList("official-providers.list").apply {
      addAll(loadPreviousProviders())
      removeAll(droppedProviders)
    }
    // The crawler tries a dropped provider as a required one, but its failure does not stop the run.
    val wantedProviders = requiredProviders + droppedProviders
    if (!allOut.exists()) {
      objectMapper.writerWithDefaultPrettyPrinter().writeValue(allOut, getProvidersDataFromPages())
    }
    logger.info("Providers from $terraformRegistryHost are loaded to $allOut")
    val buildInProvider = objectMapper.nodeFactory.let { nf ->
      nf.objectNode().set<JsonNode>("attributes",
                                    nf.objectNode()
                                      .put("full-name", "terraform.io/builtin/terraform")
                                      .put("type", "providers")
                                      .put("name", "terraform")
                                      .put("namespace", "terraform")
                                      .put("tier", "${Tier.BUILTIN}"))
    }
    val listedProviders = objectMapper.readTree(allOut).elements().asSequence().toList()
    val listedNames = listedProviders.mapTo(HashSet()) { it.fullName() } + buildInProvider.fullName()
    val notListedProviders = wantedProviders.filter { it !in listedNames }.mapNotNull { name ->
      val data = lookUpProvider(name)
      if (data == null) {
        logger.warn("Provider $name is not in the registry anymore, dropping it")
        requiredProviders.remove(name)
      }
      data
    }
    val mostUsefulProviders = sequenceOf<JsonNode>(buildInProvider) +
                              listedProviders.asSequence()
                                .filter { providerData ->
                                  val attributes = providerData["attributes"]
                                  val unlisted = attributes["unlisted"].asBoolean()
                                  providerData.fullName() in wantedProviders ||
                                  !unlisted && (attributes["tier"].asText() != "${Tier.COMMUNITY}"
                                                || attributes["downloads"].asLong() >= downloadsLimitForProvider)
                                } +
                              notListedProviders

    val generatedJsonFileNames = File(File(outputDir, "resources/model").apply { mkdirs() }, "providers.list")
    mostUsefulProviders.forEach { data ->
      totalProviders.incrementAndGet()

      val name = data["attributes"]["full-name"].asText()
      logger.info("Processing: $name")
      val file = buildProviderMetadata(data, version, outputDir, required = data.fullName() in wantedProviders)
      if (file.exists()) {
        generatedJsonFileNames.appendText("$name\n")
        requiredProviders.remove(data.fullName())
        if (data.fullName() in droppedProviders) {
          logger.warn("Dropped provider $name builds again")
          println("##teamcity[message text='Dropped provider $name builds again, remove it from dropped-providers.list' status='WARNING']")
        }
      }
    }
    if (requiredProviders.isNotEmpty()) {
      throw IllegalStateException("Not all required providers are built, see plugins-meta/failed. Missing providers: ${requiredProviders.joinToString()}")
    }
    if (cleanDownloadedData) {
      logger.info("Deleting data about providers from file: $allOut")
      allOut.deleteOnExit()
    }
    logger.info("Providing processing finished, processed $totalProviders providers, failed: $failedProviders, skipped: $skippedProviders")
  }

  private fun loadResourceList(name: String): MutableSet<String> {
    val inputStream = TerraformProvidersMetadataBuilder::class.java.getResourceAsStream("/$name")
                      ?: throw IllegalStateException("Resource $name is not found")
    return inputStream.use { readProvidersList(it) }
  }

  /** Returns the providers of the metadata jar [previousMetadataVersion], or of the last published jar when it is not set. */
  private fun loadPreviousProviders(): Set<String> {
    val artifactUrl = "$metadataRepoUrl/${metadataGroup.replace('.', '/')}/terraform-metadata"
    val version = previousMetadataVersion ?: run {
      val response = getQuery("$artifactUrl/maven-metadata.xml")
      check(response.statusCode() == 200) { "Failed to load $artifactUrl/maven-metadata.xml, status=${response.statusCode()}" }
      Regex("<release>([^<]+)</release>").find(response.body())?.groupValues?.get(1)
      ?: throw IllegalStateException("$artifactUrl/maven-metadata.xml has no release")
    }
    val jarUrl = "$artifactUrl/$version/terraform-metadata-$version.jar"
    logger.info("Loading the required providers from $jarUrl")
    val response = httpClient.send(HttpRequest.newBuilder(URI(jarUrl)).GET().build(), HttpResponse.BodyHandlers.ofInputStream())
    check(response.statusCode() == 200) { "Failed to load $jarUrl, status=${response.statusCode()}" }
    return ZipInputStream(response.body()).use { zip ->
      generateSequence { zip.nextEntry }.firstOrNull { it.name == "terraform/model/providers.list" }
      ?: throw IllegalStateException("$jarUrl has no terraform/model/providers.list")
      readProvidersList(zip)
    }
  }

  /** Returns the lowercase provider names of the [input]. */
  private fun readProvidersList(input: InputStream): MutableSet<String> =
    BufferedReader(InputStreamReader(input)).readLines().filter { it.isNotBlank() }.mapTo(HashSet()) { it.trim().lowercase() }

  private fun JsonNode.fullName(): String = this["attributes"]["full-name"].asText().lowercase()

  /**
   * Returns the registry data of a provider that the provider list does not show,
   * or null when the registry does not know the provider.
   */
  private fun lookUpProvider(name: String): JsonNode? {
    val response = getQuery("$terraformRegistryHost/v2/providers/$name")
    return when (response.statusCode()) {
      200 -> objectMapper.readTree(response.body())["data"]
      404 -> null
      else -> throw IllegalStateException("Failed to look up provider $name, status=${response.statusCode()}")
    }
  }

  /** Skips a schema over [maxSchemaSize], unless the provider is [required]. */
  private fun buildProviderMetadata(data: JsonNode,
                                    version: String,
                                    outputDir: File,
                                    required: Boolean): File {
    val fullName = data["attributes"]["full-name"].asText()
    logger.info("Provider: $fullName")
    val namespace = fullName.substringBeforeLast("/")
    val provider = fullName.substringAfterLast("/")

    val tfgendir = File("terraform-gen-dir/${namespace}").apply { mkdirs() }

    writeVersionsTfFile(tfgendir, version, namespace, provider)

    val logFile = File(File(outputDir, "logs/$namespace").apply { mkdirs() }, "$provider.log")
    val parentDir = File(outputDir, "resources/model/providers/$namespace").apply { mkdirs() }
    val schemaFile = File(parentDir, "$provider.json")

    val initError = initTerraform(tfgendir, logFile)
    val schemaError: String = generateTerraformSchema(tfgendir, schemaFile)
    val schemaSize = schemaFile.length()
    if (schemaSize <= 0L) {
      cleanupSchema(schemaFile, parentDir)
      val failures = File(outputDir, "failed/$namespace")
      val failureDir = File(failures, provider).apply { deleteRecursively() }.also { it.mkdirs() }
      File(tfgendir, "versions.tf").copyTo(File(failureDir, "versions.tf"))
      File(failureDir, "init.err").writeText(initError)
      File(failureDir, "schema.err").writeText(schemaError)
      failedProviders.incrementAndGet()
      logger.error("Metadata build failure for provider $fullName. \n Error: $initError \n Schema Generation Error: $schemaError")
    }
    else if (schemaSize > maxSchemaSize && !required) {
      cleanupSchema(schemaFile, parentDir)
      skippedProviders.incrementAndGet()
      logger.warn("Skipping provider $fullName because schema is too large: ${formatMb(schemaSize)} MB")
    }
    else {
      storeRegistryData(data, tfgendir, outputDir, namespace, provider)
      logger.info("Schema file generated: $provider")
    }
    deleteDirRecursively(tfgendir)
    return schemaFile
  }

  private fun cleanupSchema(schemaFile: File, parentDir: File) {
    schemaFile.delete()
    parentDir.delete()
  }

  private fun formatMb(bytes: Long): String = "%.2f".format(bytes / 1024.0 / 1024.0)

  private fun storeRegistryData(data: JsonNode, tfgendir: File, outputDir: File, dir: String, file: String) {
    val lockFile = File(tfgendir, ".terraform.lock.hcl")
    if (lockFile.exists()) {
      val lockData = HCLParser().parse(lockFile)
      val providerMap = lockData["provider"] as? Map<*, *>
      val firstValue = providerMap?.values?.firstOrNull() as? Map<*, *>
      val version = firstValue?.get("version")
      version?.let {
        (data["attributes"] as? ObjectNode)?.put("version", it.toString())
      }
    }
    val metadataFile = File(File(outputDir, "resources/model/providers/$dir").apply { mkdirs() }, "$file.json.metadata")
    val mapper = ObjectMapper()
    val jsonNode = mapper.createObjectNode()
    val fullName = ((data["attributes"] as? ObjectNode)?.get("full-name")?.asText() ?: "${dir}/${file}").lowercase()
    jsonNode.putIfAbsent(fullName, data)
    jsonNode.toString().byteInputStream().use { inputStream ->
      metadataFile.outputStream().use { outputStream ->
        inputStream.copyTo(outputStream)
      }
    }
  }

  private fun getTerraformVersion(): String = ProcessBuilder(listOf("terraform", "-v", "--json"))
    .redirectError(ProcessBuilder.Redirect.INHERIT)
    .start().let { versionProcess ->
      versionProcess.inputStream.use { objectMapper.readTree(it) }["terraform_version"].asText().also {
        versionProcess.waitFor()
      }
    }

  private fun generateFunctionsMetadata() {
    val functionsFile = Path.of("resources", "model", "functions.json")
    try {
      val process = ProcessBuilder(listOf("terraform", "metadata", "functions", "-json")).start()
      val output = process.inputStream.use { it.readBytes() }
      val error = process.errorStream.use { it.reader().readText() }
      process.waitFor()

      val json = runCatching { objectMapper.readTree(output) }.getOrNull()
      val signatures = json?.get("function_signatures")
      if (json == null || signatures == null || !signatures.isObject) {
        logger.error("Failed to generate functions metadata, keeping existing '$functionsFile'. Error: $error")
        return
      }

      Files.newOutputStream(functionsFile).use { out ->
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(out, json)
      }
      logger.info("Regenerated $functionsFile from 'terraform metadata functions -json' (${signatures.size()} functions)")
    }
    catch (e: Exception) {
      logger.error("Error running 'terraform metadata functions -json', keeping existing '$functionsFile'", e)
    }
  }

  private fun deleteDirRecursively(tfgendir: File) {
    if (tfgendir.exists())
      Files.walk(tfgendir.toPath()).use { paths ->
        paths.sorted(Comparator.reverseOrder())
          .map { it.toFile() }
          .forEach(File::delete)
      }
  }

  private fun writeVersionsTfFile(tfGenDir: File, version: String, namespace: String, provider: String) {
    val providerVersion = getLatestProviderVersion(namespace, provider)

    File(tfGenDir, "versions.tf").writeText("""
      terraform {
        required_version = "$version"
        required_providers {
          googleworkspace = {
            source = "$namespace/$provider"
            ${if (!providerVersion.isNullOrEmpty()) "version = \"$providerVersion\"" else ""}
          }
        }
      }
      """.trimIndent()
    )
  }

  private fun getLatestProviderVersion(namespace: String, provider: String): String? {
    val url = "$terraformRegistryHost/v1/providers/$namespace/$provider"
    val fullName = "$namespace/$provider"
    return try {
      val response = getQuery(url)
      if (response.statusCode() != 200) {
        logger.warn("Failed to get latest version for $fullName, status=${response.statusCode()}")
        null
      }
      else {
        val json = objectMapper.readTree(response.body())
        val version = json["version"]?.asText() ?: return null

        logger.info("Latest version for $fullName = $version")
        version
      }
    }
    catch (e: Exception) {
      logger.error("Error fetching latest version for $fullName", e)
      null
    }
  }

  private fun generateTerraformSchema(tfgendir: File, schemaFile: File): String {
    val schemaProcess = ProcessBuilder(listOf("terraform", "providers", "schema", "-json")).directory(tfgendir).start()
    schemaFile.outputStream().buffered().use { out ->
      schemaProcess.inputStream.use { input -> input.transferTo(out) }
    }
    val schemaError: String = schemaProcess.errorStream.use { it.reader().readText() }
    schemaProcess.waitFor()
    return schemaError
  }

  private fun initTerraform(tfgendir: File, logFile: File): String {
    val initError: String
    ProcessBuilder(listOf("terraform", "init")).directory(tfgendir)
      .redirectOutput(logFile)
      .start().also {
        it.errorStream.use { error -> initError = error.reader().readText() }
      }
      .waitFor()
    return initError
  }
}

internal enum class Tier(private val tierName: String) {
  COMMUNITY("community"),
  BUILTIN("builtin");

  override fun toString(): String = tierName
}
