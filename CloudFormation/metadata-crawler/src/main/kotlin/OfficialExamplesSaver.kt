
import com.intellij.ReviseWhenPortedToJDK
import com.intellij.aws.cloudformation.tests.TestUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.Urls
import com.intellij.util.io.HttpRequests
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.jsoup.Jsoup
import java.io.File
import java.io.IOException
import java.net.URL
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

object OfficialExamplesSaver {
  fun save() {
    val url = Urls.newFromEncoded("https://s3.amazonaws.com/cloudformation-templates-us-east-1")

    val docText = HttpRequests.request(url).connect {
      it.readString()
    }

    val doc = Jsoup.parse(docText)
    for (key in doc.getElementsByTag("Key")) {
      val name = key.text()
      val size = Integer.parseInt(key.parent()?.getElementsByTag("Size")?.first()?.text())

      val fileUrl = url.resolve(name.replace(" ", "%20"))

      val localName = StringUtil.trimEnd(name.toLowerCase(), ".template") + "-" + DigestUtils.md5Hex(name).substring(0, 4) + ".template"
      val localFile = File(TestUtil.getTestDataFile("officialExamples/src"), localName)

      if (localFile.exists() && localFile.length() == size.toLong()) {
        continue
      }

      println("Downloading $fileUrl")
      FileUtils.copyURLToFile(URL(fileUrl.toExternalForm()), localFile)
    }
  }

  @ReviseWhenPortedToJDK("13")
  fun saveServerless() {
    val targetRoot = TestUtil.getTestDataFile("serverless-application-model/src")

    val tempFile = Files.createTempFile("serverless-application-model-master", ".zip")
    tempFile.toFile().deleteOnExit()

    val url = URL("https://github.com/awslabs/serverless-application-model/archive/master.zip")
    FileUtils.copyURLToFile(url, tempFile.toFile())

    val zipFs = FileSystems.newFileSystem(tempFile, null as ClassLoader?)

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
