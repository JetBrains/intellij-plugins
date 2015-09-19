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

      if (name.equalsIgnoreCase("AutoScalingRollingUpdates.template") ||
          name.equalsIgnoreCase("VPC_RDS_DB_Instance.template") ||
          name.equalsIgnoreCase("RDS_MySQL_With_Read_Replica.template") ||
          name.equalsIgnoreCase("RDSDatabaseWithOptionalReadReplica.template") ||
          name.equalsIgnoreCase("auto_scaling_with_instance_profile.template") ||
          name.equalsIgnoreCase("DynamoDB_Table.template") ||
          name.equalsIgnoreCase("AutoScalingMultiAZSample-1.0.0.template")) {
        // Known to be broken
        continue;
      }

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
