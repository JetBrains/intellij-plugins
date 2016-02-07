import net.htmlparser.jericho.Source
import org.apache.commons.io.FileUtils

import java.io.File
import java.net.URL

object ChangeLogSaver {
  @Throws(Exception::class)
  fun saveChangeLog() {
    val url = URL("http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/DocumentHistory.html")

    val source = Source(url)
    val renderedText = source.renderer.toString()

    FileUtils.write(File("testData/CloudFormation-ChangeLog.txt"), renderedText)
  }
}
