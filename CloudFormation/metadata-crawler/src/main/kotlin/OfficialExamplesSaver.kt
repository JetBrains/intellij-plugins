import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.io.IOException
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.SimpleFileVisitor
import java.nio.file.FileSystems
import java.nio.file.FileVisitResult
import java.nio.file.Path

object OfficialExamplesSaver {
  fun save() {
    val url = URL("https://s3.amazonaws.com/cloudformation-templates-us-east-1/")

    val doc = Jsoup.parse(url, 2000)
    for (key in doc.getElementsByTag("Key")) {
      val name = key.text()
      val size = Integer.parseInt(key.parent().getElementsByTag("Size").first().text())

      val fileUrl = URL(url, name.replace(" ", "%20"))

      val localName = StringUtils.removeEnd(name.toLowerCase(), ".template") + "-" + DigestUtils.md5Hex(name).substring(0, 4) + ".template"
      val localFile = File("testData/officialExamples/src", localName)

      if (localFile.exists() && localFile.length() == size.toLong()) {
        continue
      }

      println("Downloading $fileUrl")
      FileUtils.copyURLToFile(fileUrl, localFile)
    }
  }

  fun saveServerless() {
    val targetRoot = File("testData/serverless-application-model/src")

    val tempFile = Files.createTempFile("serverless-application-model-master", ".zip")
    tempFile.toFile().deleteOnExit()

    val url = URL("https://github.com/awslabs/serverless-application-model/archive/master.zip")
    FileUtils.copyURLToFile(url, tempFile.toFile())

    val zipFs = FileSystems.newFileSystem(tempFile, null)

    val rootPath = zipFs.rootDirectories.toList().single()

    if (targetRoot.exists()) {
      FileUtils.deleteDirectory(targetRoot)
    }

    val pathInZip = rootPath.resolve("serverless-application-model-master").resolve("examples")
    Files.walkFileTree(pathInZip, object : SimpleFileVisitor<Path>() {
      @Throws(IOException::class)
      override fun visitFile(filePath: Path, attrs: BasicFileAttributes): FileVisitResult {
        val fileName = pathInZip.relativize(filePath).toString().replace("/", "_")

        if (!fileName.endsWith(".yaml")) return FileVisitResult.CONTINUE

        val targetPath = targetRoot.toPath().resolve(fileName)
        println(targetPath)

        Files.createDirectories(targetPath.parent)
        Files.copy(filePath, targetPath)

        return FileVisitResult.CONTINUE
      }
    })
  }
}
