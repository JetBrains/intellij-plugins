import org.jsoup.Jsoup
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystems
import java.nio.file.FileVisitResult
import java.nio.file.StandardCopyOption
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.security.MessageDigest

object OfficialExamplesSaver {
  private const val TEMPLATES_BUCKET_URL = "https://s3.amazonaws.com/cloudformation-templates-us-east-1"
  private const val SERVERLESS_EXAMPLES_ZIP = "https://github.com/awslabs/serverless-application-model/archive/master.zip"

  private val httpClient: HttpClient = HttpClient.newBuilder().build()

  fun save() {
    val targetRoot = CrawlerPaths.officialExamplesOutputDir.also { it.mkdirs() }
    val docText = getText(TEMPLATES_BUCKET_URL)

    val doc = Jsoup.parse(docText)
    for (key in doc.getElementsByTag("Key")) {
      val name = key.text()
      val size = Integer.parseInt(key.parent()?.getElementsByTag("Size")?.first()?.text())

      val fileUrl = URI("$TEMPLATES_BUCKET_URL/${encodePath(name)}").toURL()

      val localName = name.lowercase().removeSuffix(".template") + "-" + sha1Hex(name).substring(0, 4) + ".template"
      val localFile = File(targetRoot, localName)

      if (localFile.exists() && localFile.length() == size.toLong()) {
        continue
      }

      println("Downloading $fileUrl")
      fileUrl.openStream().use { input ->
        localFile.outputStream().use { output ->
          input.copyTo(output)
        }
      }
    }
  }

  fun saveServerless() {
    val targetRoot = CrawlerPaths.serverlessExamplesOutputDir

    val tempFile = Files.createTempFile("serverless-application-model-master", ".zip")
    try {
      URI(SERVERLESS_EXAMPLES_ZIP).toURL().openStream().use { input ->
        Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING)
      }

      FileSystems.newFileSystem(tempFile, null as ClassLoader?).use { zipFs ->
        val rootPath = zipFs.rootDirectories.toList().single()

        if (targetRoot.exists()) {
          targetRoot.deleteRecursively()
        }
        targetRoot.mkdirs()

        val pathInZip = rootPath.resolve("serverless-application-model-master").resolve("examples")
        Files.walkFileTree(pathInZip, object : SimpleFileVisitor<Path>() {
          @Throws(IOException::class)
          override fun visitFile(filePath: Path, attrs: BasicFileAttributes): FileVisitResult {
            val fileName = pathInZip.relativize(filePath).toString().replace("/", "_")

            if (!fileName.endsWith(".yaml")) return FileVisitResult.CONTINUE

            val targetPath = targetRoot.toPath().resolve(fileName)
            println(targetPath)

            Files.createDirectories(targetPath.parent)
            Files.copy(filePath, targetPath, StandardCopyOption.REPLACE_EXISTING)

            return FileVisitResult.CONTINUE
          }
        })
      }
    }
    finally {
      Files.deleteIfExists(tempFile)
    }
  }

  private fun getText(url: String): String {
    val request = HttpRequest.newBuilder().uri(URI(url)).GET().build()
    val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
  }

  private fun encodePath(path: String): String {
    return path
      .split("/")
      .joinToString("/") { URLEncoder.encode(it, StandardCharsets.UTF_8).replace("+", "%20") }
  }

  private fun sha1Hex(text: String): String {
    val digest = MessageDigest.getInstance("SHA-1").digest(text.toByteArray(StandardCharsets.UTF_8))
    return digest.joinToString("") { "%02x".format(it) }
  }
}
