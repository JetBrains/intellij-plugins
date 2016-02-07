import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import java.io.File
import java.net.URL

object OfficialExamplesSaver {
  @Throws(Exception::class)
  fun save() {
    val url = URL("http://s3.amazonaws.com/cloudformation-templates-us-east-1/")

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

      println("Downloading " + fileUrl)
      FileUtils.copyURLToFile(fileUrl, localFile)
    }
  }
}
