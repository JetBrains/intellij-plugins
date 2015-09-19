import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.net.URL;

public class OfficialExamplesSaver {
  public static void save() throws Exception {
    URL url = new URL("http://s3.amazonaws.com/cloudformation-templates-us-east-1/");

    Document doc = Jsoup.parse(url, 2000);
    for (Element key : doc.getElementsByTag("Key")) {
      String name = key.text();
      int size = Integer.parseInt(key.parent().getElementsByTag("Size").first().text());

      URL fileUrl = new URL(url, name.replace(" ", "%20"));

      String localName = StringUtils.removeEnd(name.toLowerCase(), ".template") + "-" + DigestUtils.md5Hex(name).substring(0, 4) + ".template";
      File localFile = new File("testData/officialExamples/src", localName);

      if (localFile.exists() && localFile.length() == size) {
        continue;
      }

      System.out.println("Downloading " + fileUrl);
      FileUtils.copyURLToFile(fileUrl, localFile);
    }
  }
}
