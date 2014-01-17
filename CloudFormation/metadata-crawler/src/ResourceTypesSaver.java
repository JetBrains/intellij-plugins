import com.intellij.aws.cloudformation.metadata.CloudFormationMetadata;
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceProperty;
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceType;
import com.intellij.aws.cloudformation.metadata.MetadataSerializer;
import javafx.util.Pair;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceTypesSaver {
  private static final Pattern RESOURCE_TYPE_PATTERN = Pattern.compile("<li><a href=\"([^\"]+)\">(AWS::[^<]+)</a></li>");

  public static void saveResourceTypes() throws Exception {
    final List<Pair<URL, String>> types = getResourceTypes();

    final CloudFormationMetadata metadata = new CloudFormationMetadata();

    metadata.predefinedParameters.addAll(getPredefinedParameters());

    for (Pair<URL, String> type : types) {
      final CloudFormationResourceType resourceType = getResourceType(type.getValue(), type.getKey());
      metadata.resourceTypes.add(resourceType);
    }

    try (OutputStream outputStream = new FileOutputStream(new File("generated/cloudformation-metadata.xml"))) {
      MetadataSerializer.toXML(metadata, outputStream);
    }
  }

  private static CloudFormationResourceType getResourceType(String name, URL url) throws IOException {
    System.out.println(name);

    final CloudFormationResourceType resourceType = new CloudFormationResourceType();
    resourceType.name = name;

    final Document doc = Jsoup.parse(url, 2000);

    Element vlist = doc.select("div.variablelist").first();

    if (vlist != null) {
      for (Element term : vlist.select("span.term")) {
        CloudFormationResourceProperty property = new CloudFormationResourceProperty();

        final Element descr = term.parent().nextElementSibling();
        //descr.children().
        final Elements descrElements = descr.select("p");

        property.name = term.text();
        property.description = "";

        String requiredValue = null;
        String typeValue = null;

        for (Element element : descrElements) {
          if (element.parent() != descr) {
            continue;
          }

          final String text = element.text();

          if (text.matches("[a-zA-Z]+:.*")) {
            String[] split = text.split(":", 2);
            assert split.length == 2 : text;

            final String fieldName = split[0].trim();
            final String fieldValue = split[1].trim().replaceFirst("\\.$", "");

            if ("Required".equals(fieldName)) {
              requiredValue = fieldValue;
            } else if ("Type".equals(fieldName)) {
              typeValue = fieldValue;
            }
          }
        }

        // property.description = descr.html();

        if (typeValue != null) {
          property.type = typeValue;
        } else {
          // TODO
          if (!resourceType.name.equals("AWS::Route53::RecordSet")) {
            throw new RuntimeException("Type is not found in property " + property.name + " in " + url);
          }
        }

        // TODO Handle "Required if NetBiosNameServers is specified; optional otherwise" in http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-resource-ec2-dhcp-options.html
        // TODO Handle "Yes, for VPC security groups" in http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-ec2-security-group.html
        // TODO Handle "Can be used instead of GroupId for EC2 security groups" in http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-ec2-security-group-ingress.html
        // TODO Handle "Yes, for VPC security groups; can be used instead of GroupName for EC2 security groups" in http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-ec2-security-group-ingress.html
        // TODO Handle "Yes, for ICMP and any protocol that uses ports" in http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-ec2-security-group-ingress.html
        // TODO http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-elasticache-cache-cluster.html: If your cache cluster isn't in a VPC, you must specify this property
        // TODO http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-elasticache-cache-cluster.html: If your cache cluster is in a VPC, you must specify this property

        if (requiredValue != null) {
          if (requiredValue.equals("Yes")) {
            property.required = true;
          } else if (requiredValue.startsWith("No") ||
                     requiredValue.startsWith("Conditional") ||
                     requiredValue.equals("Required if NetBiosNameServers is specified; optional otherwise") ||
                     requiredValue.equals("Yes, for VPC security groups") ||
                     requiredValue.equals("Can be used instead of GroupId for EC2 security groups") ||
                     requiredValue.equals("Yes, for VPC security groups; can be used instead of GroupName for EC2 security groups") ||
                     requiredValue.equals("Yes, for ICMP and any protocol that uses ports") ||
                     requiredValue.equals("If your cache cluster isn't in a VPC, you must specify this property") ||
                     requiredValue.equals("If your cache cluster is in a VPC, you must specify this property")) {
            property.required = false;
          } else {
            throw new RuntimeException("Unknown value for required in property " + property.name + " in " + url + ": " + requiredValue);
          }
        } else {
          // TODO
          if (!resourceType.name.equals("AWS::RDS::DBParameterGroup") && !resourceType.name.equals("AWS::Route53::RecordSet")) {
            throw new RuntimeException("Required is not found in property " + property.name + " in " + url);
          }
        }

        resourceType.properties.add(property);
      }
    }

    return resourceType;
  }

  private static List<Pair<URL, String>> getResourceTypes() throws IOException {
    URL url = new URL("http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-template-resource-type-ref.html");
    final String content = IOUtils.toString(url);

    List<Pair<URL, String>> result = new ArrayList<>();

    final Matcher matcher = RESOURCE_TYPE_PATTERN.matcher(content);
    while (matcher.find()) {
      final String href = matcher.group(1);
      final String name = matcher.group(2);

      // EXCEPTION Not a resource type
      if ("AWS::CloudFormation::Init".equals(name)) {
        continue;
      }

      result.add(new Pair<>(new URL(url, href), name));
    }

    Collections.sort(result, new Comparator<Pair<URL, String>>() {
      @Override
      public int compare(Pair<URL, String> o1, Pair<URL, String> o2) {
        return o1.getValue().compareTo(o2.getValue());
      }
    });

    return result;
  }

  private static List<String> getPredefinedParameters() throws IOException {
    URL url = new URL("http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/pseudo-parameter-reference.html");
    final Document doc = Jsoup.parse(url, 2000);

    List<String> result = new ArrayList<>();

    Element vlist = doc.select("div.variablelist").first();

    for (Element param : vlist.select("span.term")) {
      result.add(param.text());
    }

    Collections.sort(result);

    return result;
  }
}
