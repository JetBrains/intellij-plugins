import net.htmlparser.jericho.Source
import java.io.File
import java.net.URL

object ChangeLogSaver {
  fun saveChangeLog() {
    val url = URL("http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/DocumentHistory.html")

    val source = Source(url)
    val renderedText = source.renderer.toString()

    File("testData/CloudFormation-ChangeLog.txt").writeText(renderedText)
  }
}
