import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.io.File
import java.net.URI
import java.net.URLDecoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.nio.file.Files

object TerraformProvidersMetadataBuilder {

  private val objectMapper: ObjectMapper = ObjectMapper()

  private val httpClient: HttpClient = HttpClient.newBuilder().build()

  private fun getQuery(httpQuery: String): HttpResponse<String> {
    val httpRequest =
      HttpRequest.newBuilder().uri(
        URI(httpQuery)
      ).GET().build()
    return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())
  }

  private fun String.urlDecode(): String = URLDecoder.decode(this, StandardCharsets.UTF_8)

  private fun getProvidersDataFromPages(): Sequence<JsonNode> {
    val host = "https://registry.terraform.io"
    println("loading providers from $host ...")
    return sequence {
      var httpResponse = getQuery("${host}/v2/providers")
      while (true) {
        val jsonResponse = objectMapper.readTree(httpResponse.body())
        val page = jsonResponse.get("meta")?.get("pagination")?.get("current-page")
        val pageTotal = jsonResponse.get("meta")?.get("pagination")?.get("total-pages")
        println("Loaded page ${page} of ${pageTotal}  ...")
        val next = jsonResponse.get("links")?.get("next")?.takeIf { !it.isNull }?.asText()?.urlDecode()
        when (val responseData = jsonResponse["data"]) {
          is ObjectNode -> yield(responseData)
          is ArrayNode -> yieldAll(responseData.elements())
        }
        if (next == null) break
        httpResponse = getQuery("${host}${next}")
      }
    }
  }

  private val provComparator: Comparator<JsonNode> = compareBy<JsonNode> { providerData ->
    when (providerData["attributes"]["tier"].asText()) {
      "builtin" -> 3
      "official" -> 2
      "partner" -> 1
      else -> 0
    }
  }.thenComparing(Comparator.comparing { it["attributes"]["downloads"].asLong() })

  @JvmStatic
  fun main(args: Array<String>) {

    val allOut = File("allout.json")
    if (!allOut.exists())
      objectMapper.writerWithDefaultPrettyPrinter().writeValue(allOut,
                                                               getProvidersDataFromPages().asIterable())

    val allProvidersData = objectMapper.readTree(allOut)

    checkTotalProvidersAmount(allProvidersData)

    val buildInProvider = objectMapper.nodeFactory.let { nf ->
      nf.objectNode().set<JsonNode>("attributes",
                                    nf.objectNode()
                                      .put("full-name", "terraform.io/builtin/terraform")
                                      .put("name", "terraform")
                                      .put("namespace", "terraform")
                                      .put("tier", "builtin"))
    }

    val providerVendorsTier = setOf("partner", "official")
    val downloadsLimit = 10000
    val mostUsefulProviders = sequenceOf<JsonNode>(buildInProvider) +
                              allProvidersData.elements().asSequence()
                                .filter { providerData ->
                                  val attributes = providerData["attributes"]
                                  (providerVendorsTier.contains(attributes["tier"].asText()) || attributes["downloads"].asLong() >= downloadsLimit)
                                  && attributes["full-name"].asText().contains("aws")
                                }
    val selected = mostUsefulProviders
      .groupBy { it["attributes"]["name"] }
      .mapValues {
        val ambiguous = it.value
        val selected = ambiguous.maxWith(provComparator)
        if (ambiguous.size > 1) {
          println(
            "multiple providers for '${selected["attributes"]["name"].asText()}, '${selected["attributes"]["full-name"].asText()}' " +
            "is selected among ${ambiguous.map { it["attributes"]["full-name"] }}")
        }
        selected
      }.values

    println("Selected ${selected.size} most useful providers")

    //checkMandatoryHashicorpProviders(selected)

    val version = getTerraformVersion()
    println("terraform version: $version")

    val outputDir = File("plugins-meta").apply { mkdirs() }
    val generatedJsonFileNames = mutableListOf<String>()
    selected.asSequence().map {
      buildProviderMetadata(it, version, outputDir)
    }.forEach {file ->
      if (file.exists()) {
        println("Schema file generated: ${file.path}")
        generatedJsonFileNames.add(file.nameWithoutExtension)
      }
      else {
        println("Error generating schema for provider: ${file.nameWithoutExtension}")
      }
    }
    File(File(outputDir, "resources/model").apply { mkdirs() }, "providers.list").writeText(generatedJsonFileNames.sorted().joinToString("\n"))
  }

  private fun checkTotalProvidersAmount(allProvidersData: JsonNode) {
    val totalProviders = allProvidersData.elements().asSequence().count()
    println("Total providers downloaded: = $totalProviders")
    val minProviders = 4108
    assert(totalProviders >= minProviders, { "Terraform should provide at least ${minProviders}" })
  }

  private fun checkMandatoryHashicorpProviders(selected: Collection<JsonNode>) {
    val mandatoryProviders = selected.count { provider ->
      provider["attributes"]["namespace"].asText().contains("hashicorp")
    }
    val minMandatoryProviders = 33
    assert(mandatoryProviders >= minMandatoryProviders, { "We must have all mandatory providers from hashicorp. Selected ${mandatoryProviders} of ${minMandatoryProviders}" })
  }

  private fun buildProviderMetadata(provider: JsonNode,
                                    version: String,
                                    outputDir: File): File {
    return run {
      val fullName = provider["attributes"]["full-name"].asText()
      val name = provider["attributes"]["name"].asText().lowercase()
      println("provider: $fullName")

      val tfgendir = File("terraform-gen-dir/${name}")
      tfgendir.mkdirs()

      writeVersionsTfFile(tfgendir, version, fullName)

      val logFile = File(File(outputDir, "logs").apply { mkdirs() }, "$name.log")
      val initError = initTerraform(tfgendir, logFile)

      val schemaFile = File(File(outputDir, "resources/model/providers").apply { mkdirs() }, "$name.json")
      val schemaError: String = generateTerraformSchema(tfgendir, schemaFile)

      if (schemaFile.length() <= 0L) {
        schemaFile.delete()
        val failures = File(outputDir, "failed")
        val failureDir = File(failures, name).apply { deleteRecursively() }.also { it.mkdirs() }
        File(tfgendir, "versions.tf").copyTo(File(failureDir, "versions.tf"))
        File(failureDir, "init.err").writeText(initError)
        File(failureDir, "schema.err").writeText(schemaError)
      }
      deleteDirRecursively(tfgendir)
      schemaFile
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
        .forEach(File::delete)
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
    val schemaProcess = ProcessBuilder(listOf("terraform", "providers", "schema", "-json")).directory(tfgendir)
      .start()
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