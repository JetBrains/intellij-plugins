import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import java.net.URI
import java.net.URLDecoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import kotlin.Comparator

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

        val next = jsonResponse["links"]["next"]?.takeIf { !it.isNull }?.asText()?.urlDecode()
        yieldAll(jsonResponse["data"].elements())
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

    val buildInProvider = objectMapper.nodeFactory.let { nf ->
      nf.objectNode()
        .set<JsonNode>("attributes",
                       nf.objectNode()
                         .put("full-name", "terraform.io/builtin/terraform")
                         .put("name", "terraform")
                         .put("namespace", "terraform")
                         .put("tier", "builtin"))
    }

    val mostUsefulProviders = sequenceOf<JsonNode>(buildInProvider) +
                              allProvidersData.elements().asSequence()
                                .filter { providerData ->
                                  val attributes = providerData["attributes"]
                                  attributes["tier"].asText().let { it == "partner" || it == "official" } ||
                                  attributes["downloads"].asLong() >= 10000
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
      }
      .values

    println("count = ${selected.size}")

    val version = getTerraformVersion()
    println("terraform version: $version")

    val tfgendir = File("terrform-gen-dir")
    val outputDir = File("plugins-meta").apply { mkdirs() }
    val failures = File(outputDir, "failed")
    val generatedJsonFileNames = mutableListOf<String>()

    for (provider in selected) {
      deleteDirRecursively(tfgendir)
      tfgendir.mkdirs()

      val fullName = provider["attributes"]["full-name"].asText()
      println("provider: $fullName")
      writeVersionsTfFile(tfgendir, version, fullName)

      val initError = initTerraform(tfgendir)
      val name = provider["attributes"]["name"].asText().lowercase()

      val schemaFile = File(File(outputDir, "providers").apply { mkdirs() }, "$name.json")

      println("schemaFile = $schemaFile")
      val schemaError: String = generateTerraformSchema(tfgendir, schemaFile)
      if (schemaFile.length() > 0)
        generatedJsonFileNames.add(name)
      else {
        schemaFile.delete()
        val failureDir = File(failures, name).apply { mkdirs() }
        File(tfgendir, "versions.tf").copyTo(File(failureDir, "versions.tf"))
        File(failureDir, "init.err").writeText(initError)
        File(failureDir, "schema.err").writeText(schemaError)
      }

    }

    File(outputDir, "providers.list").writeText(generatedJsonFileNames.sorted().joinToString("\n"))
    println("Everything done!")
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
        .forEach(File::delete);
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

  private fun initTerraform(tfgendir: File): String {
    val initError: String
    ProcessBuilder(listOf("terraform", "init")).directory(tfgendir)
      .redirectOutput(ProcessBuilder.Redirect.INHERIT)
      .start().also {
        it.errorStream.use { initError = it.reader().readText() }
      }
      .waitFor()
    return initError
  }

}