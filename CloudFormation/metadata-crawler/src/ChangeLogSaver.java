import net.htmlparser.jericho.Source;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;

public class ChangeLogSaver {
  public static void saveChangeLog() throws Exception {
    URL url = new URL("http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/DocumentHistory.html");

    Source source = new Source(url);
    String renderedText = source.getRenderer().toString();

    FileUtils.write(new File("testData/CloudFormation-ChangeLog.txt"), renderedText);
  }
}
