// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import com.bertramlabs.plugins.hcl4j.HCLParser
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.URI
import java.net.URLDecoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.concurrent.atomic.AtomicInteger

object TerraformProvidersMetadataBuilder {

  private val objectMapper: ObjectMapper = ObjectMapper()

  private val httpClient: HttpClient = HttpClient.newBuilder().build()

  private val terraformRegistryHost = System.getenv("TERRAFORM_REGISTRY_HOST") ?: "https://registry.terraform.io"
  private val downloadsLimitForProvider = System.getenv("DOWNLOADS_LIMIT_FOR_PROVIDER")?.toInt() ?: 20000
  private val cleanDownloadedData = System.getenv("CLEAN_DOWNLOADED_DATA")?.toBoolean() ?: true

  private val logger = LoggerFactory.getLogger(TerraformProvidersMetadataBuilder::class.java.simpleName)

  private fun getQuery(httpQuery: String): HttpResponse<String> {
    val httpRequest =
      HttpRequest.newBuilder().uri(
        URI(httpQuery)
      ).GET().build()
    return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())
  }

  private fun String.urlDecode(): String = URLDecoder.decode(this, StandardCharsets.UTF_8)

  private fun getProvidersDataFromPages(): Sequence<JsonNode> {
    logger.info("Loading providers from $terraformRegistryHost ...")
    return sequence {
      var httpResponse = getQuery("${terraformRegistryHost}/v2/providers?page[size]=100")
      do {
        val jsonResponse = objectMapper.readTree(httpResponse.body())
        val page = jsonResponse.get("meta")?.get("pagination")?.get("current-page")?.asLong() ?: 0
        val pageTotal = jsonResponse.get("meta")?.get("pagination")?.get("total-pages")?.asLong()
        logger.info("Loaded page $page of $pageTotal  ...")
        when (val responseData = jsonResponse["data"]) {
          is ObjectNode -> yield(responseData)
          is ArrayNode -> yieldAll(responseData.elements())
        }
        val next = jsonResponse.get("links")?.get("next")?.takeIf { !it.isNull }?.asText()?.urlDecode()
        if (next != null) httpResponse = getQuery("${terraformRegistryHost}${next}")
      }
      while (next != null)
    }
  }

  @JvmStatic
  fun main(args: Array<String>) {
    val outputDir = File("plugins-meta").apply { mkdirs() }
    val allOut = File(outputDir, "allout.json")
    val officialProviders = loadOfficialProvidersList()
    if (!allOut.exists()) {
      objectMapper.writerWithDefaultPrettyPrinter().writeValue(allOut, getProvidersDataFromPages().asIterable())
    }
    logger.info("Providers from $terraformRegistryHost are loaded to $allOut")
    val buildInProvider = objectMapper.nodeFactory.let { nf ->
      nf.objectNode().set<JsonNode>("attributes",
                                    nf.objectNode()
                                      .put("full-name", "terraform.io/builtin/terraform")
                                      .put("type", "providers")
                                      .put("name", "terraform")
                                      .put("namespace", "terraform")
                                      .put("tier", "builtin"))
    }
    val providerVendorsTier = setOf("partner", "official")
    val mostUsefulProviders = sequenceOf<JsonNode>(buildInProvider) +
                              objectMapper.readTree(allOut).elements().asSequence()
                                .filter { providerData ->
                                  val attributes = providerData["attributes"]
                                  val unlisted = attributes["unlisted"].asBoolean()
                                  !unlisted && (providerVendorsTier.contains(attributes["tier"].asText())
                                                || attributes["downloads"].asLong() >= downloadsLimitForProvider)
                                }
    val version = getTerraformVersion()
    logger.info("Terraform version: $version")

    val generatedJsonFileNames = File(File(outputDir, "resources/model").apply { mkdirs() }, "providers.list")
    val totalProviders = AtomicInteger(0)
    val errors = AtomicInteger(0)
    mostUsefulProviders.forEach { data ->
      totalProviders.incrementAndGet()
      val name = data["attributes"]["full-name"].asText()
      logger.info("Processing: $name")
      officialProviders.remove(name)
      val file = buildProviderMetadata(data, version, outputDir)
      if (file.exists()) {
        generatedJsonFileNames.appendText("$name\n")
      }
      else {
        errors.incrementAndGet()
      }
    }
    if (officialProviders.isNotEmpty()) {
      throw IllegalStateException("Not all official providers are loaded. Missing providers: ${officialProviders.joinToString()}")
    }
    if (cleanDownloadedData) {
      logger.info("Deleting data about providers from file: $allOut")
      allOut.deleteOnExit()
    }
    logger.info("Providing processing finished, processed $totalProviders providers, errors: $errors")
  }

  private fun loadOfficialProvidersList(): MutableSet<String> {
    val resource = TerraformProvidersMetadataBuilder::class.java.getResourceAsStream("/official-providers.list")
    return resource?.use { inputStream ->
      BufferedReader(InputStreamReader(inputStream)).readLines()
    }?.toMutableSet() ?: mutableSetOf()
  }

  private fun buildProviderMetadata(data: JsonNode,
                                    version: String,
                                    outputDir: File): File {
    val name = data["attributes"]["full-name"].asText()
    logger.info("Provider: $name")
    val dir = name.substringBeforeLast("/")
    val file = name.substringAfterLast("/")


    val tfgendir = File("terraform-gen-dir/${dir}").apply { mkdirs() }

    writeVersionsTfFile(tfgendir, version, name)

    val logFile = File(File(outputDir, "logs/$dir").apply { mkdirs() }, "$file.log")
    val parentDir = File(outputDir, "resources/model/providers/$dir").apply { mkdirs() }
    val schemaFile = File(parentDir, "$file.json")

    val initError = initTerraform(tfgendir, logFile)
    val schemaError: String = generateTerraformSchema(tfgendir, schemaFile)

    if (schemaFile.length() <= 0L) {
      schemaFile.delete()
      parentDir.delete()
      val failures = File(outputDir, "failed/$dir")
      val failureDir = File(failures, file).apply { deleteRecursively() }.also { it.mkdirs() }
      File(tfgendir, "versions.tf").copyTo(File(failureDir, "versions.tf"))
      File(failureDir, "init.err").writeText(initError)
      File(failureDir, "schema.err").writeText(schemaError)
      logger.error("Metadata build failure for provider $name. \n Error: $initError \n Schema Generation Error: $schemaError")
    }
    else {
      storeRegistryData(data, tfgendir, outputDir, dir, file)
      logger.info("Schema file generated: $file")
    }
    deleteDirRecursively(tfgendir)
    return schemaFile
  }

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

  private fun deleteDirRecursively(tfgendir: File) {
    if (tfgendir.exists())
      Files.walk(tfgendir.toPath())
        .sorted(Comparator.reverseOrder())
        .map { it.toFile() }
        .forEach(File::deleteOnExit)
  }

  private fun writeVersionsTfFile(tfgendir: File, version: String?, fullName: String?) {
    File(tfgendir, "versions.tf").writeText("""
        terraform {
          required_version = "$version"
          required_providers {
            googleworkspace = {
              source = "$fullName"
            }
          }
        }
      """.trimIndent())
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