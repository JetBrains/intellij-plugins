import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class ChangeLogSaver {
  public static void saveChangeLog() throws Exception {
    URL url = new URL("http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/DocumentHistory.html");
    try (InputStream input = url.openStream();
         OutputStream output = new FileOutputStream("testData/CloudFormation-ChangeLog.html")) {
      CrawlerUtils.copyStream(input, output);
    }
  }
}
