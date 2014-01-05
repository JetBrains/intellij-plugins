import com.intellij.aws.cloudformation.metadata.CloudFormationMetadata;
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceType;
import com.intellij.aws.cloudformation.metadata.MetadataSerializer;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceTypesSaver {
  private static final Pattern RESOURCE_TYPE_PATTERN = Pattern.compile("<li><a href=\"([^\"]+)\">(AWS::[^<]+)</a></li>");

  public static void saveResourceTypes() throws Exception {
    final List<String> types = getResourceTypes();
    Collections.sort(types);

    final CloudFormationMetadata metadata = new CloudFormationMetadata();

    for (String type : types) {
      final CloudFormationResourceType resourceType = new CloudFormationResourceType();
      resourceType.name = type;
      metadata.resourceTypes.add(resourceType);
    }

    final XStream xstream = new XStream(new StaxDriver());
    xstream.alias("Metadata", CloudFormationMetadata.class);
    xstream.alias("ResourceType", CloudFormationResourceType.class);

    try (OutputStream outputStream = new FileOutputStream(new File("generated/cloudformation-metadata.xml"))) {
      MetadataSerializer.toXML(metadata, outputStream);
    }
  }

  private static List<String> getResourceTypes() throws IOException {
    URL url = new URL("http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-template-resource-type-ref.html");
    final String content = IOUtils.toString(url);

    List<String> result = new ArrayList<>();

    final Matcher matcher = RESOURCE_TYPE_PATTERN.matcher(content);
    while (matcher.find()) {
      // final String href = matcher.group(1);
      final String name = matcher.group(2);

      result.add(name);
    }

    // EXCEPTION Not a resource type
    result.remove("AWS::CloudFormation::Init");

    return result;
  }
}
