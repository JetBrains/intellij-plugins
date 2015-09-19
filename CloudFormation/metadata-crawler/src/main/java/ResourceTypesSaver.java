import com.intellij.aws.cloudformation.metadata.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceTypesSaver {
  private static final Pattern RESOURCE_TYPE_PATTERN = Pattern.compile("<li><a href=\"([^\"]+)\">(AWS::[^<]+)</a></li>");
  public static final int FETCH_TIMEOUT_MS = 10000;

  public static void saveResourceTypes() throws Exception {
    final CloudFormationMetadata metadata = new CloudFormationMetadata();

    metadata.limits = getLimits();
    metadata.predefinedParameters.addAll(getPredefinedParameters());

    final List<Pair<URL, String>> types = getResourceTypes();
    for (Pair<URL, String> type : types) {
      final CloudFormationResourceType resourceType = getResourceType(type.getValue(), type.getKey());
      metadata.resourceTypes.add(resourceType);
    }

    fetchResourceAttributes(metadata);

    try (OutputStream outputStream = new FileOutputStream(new File("src/main/resources/cloudformation-metadata.xml"))) {
      MetadataSerializer.toXML(metadata, outputStream);
    }
  }

  private static void fetchResourceAttributes(CloudFormationMetadata metadata) throws IOException {
    URL fnGetAttrDocUrl = new URL("http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html");
    final Document doc = getDocumentFromUrl(fnGetAttrDocUrl);

    Element tableElement = doc.select("div.informaltable").first();
    assert tableElement != null;

    final List<List<String>> table = parseTable(tableElement);

    for (List<String> row : table) {
      if (row.size() != 3) {
        continue;
      }

      String resourceTypeName = row.get(0);
      final String attribute = row.get(1);
      // final String description = row.get(2);

      if (resourceTypeName.equals("AWS::CloudFormation::Stack") &&
          attribute.equals("Outputs.NestedStackOutputName")) {
        // Not an attribute name
        continue;
      }

      if (resourceTypeName.equals("AWS::EC2::AWS::EC2::SubnetNetworkAclAssociation")) {
        resourceTypeName = "AWS::EC2::SubnetNetworkAclAssociation";
      }

      CloudFormationResourceType resourceType = metadata.findResourceType(resourceTypeName);
      if (resourceType == null) {
        resourceType = new CloudFormationResourceType();
        resourceType.name = resourceTypeName;
        metadata.resourceTypes.add(resourceType);
      }

      resourceType.attributes.add(CloudFormationResourceAttribute.create(attribute, ""));
    }
  }

  private static Document getDocumentFromUrl(URL url) {
    System.out.println("Downloading " + url);
    for (int retry = 1; retry < 5; retry++) {
      try {
        return Jsoup.parse(url, FETCH_TIMEOUT_MS);
      }
      catch (IOException ignored) {
      }

      System.out.println("retry...");
    }

    throw new RuntimeException("Could not download from " + url);
  }

  private static CloudFormationResourceType getResourceType(String name, URL url) {
    System.out.println(name);

    final CloudFormationResourceType resourceType = new CloudFormationResourceType();
    resourceType.name = name;

    final Document doc = getDocumentFromUrl(url);

    Element description = doc.select(".section").first();
    resourceType.description = description.toString();

    Elements vlists = doc.select("div.variablelist");

    if (!vlists.isEmpty()) {
      for (Element vlist : vlists) {
        Elements titleElements = vlist.parent().select("h2.title");
        if (titleElements.isEmpty()) {
          continue;
        }

        String sectionTitle = titleElements.first().text();
        if (!"Properties".equals(sectionTitle) && !"Members".equals(sectionTitle)) {
          continue;
        }

        for (Element term : vlist.select("span.term")) {
          CloudFormationResourceProperty property = new CloudFormationResourceProperty();

          final Element descr = term.parent().nextElementSibling();
          //descr.children().
          final Elements descrElements = descr.select("p");

          property.name = term.text();

          String requiredValue = null;
          String typeValue = null;
          String descriptionValue = null;
          String updateValue = null;

          for (Element element : descrElements) {
            if (element.parent() != descr) {
              continue;
            }

            final String text = element.text();

            if (text.matches("[a-zA-Z ]+:.*")) {
              String[] split = text.split(":", 2);
              assert split.length == 2 : text;

              final String fieldName = split[0].trim();
              final String fieldValue = split[1].trim().replaceFirst("\\.$", "");

              if ("Required".equals(fieldName)) {
                requiredValue = fieldValue;
              } else if ("Type".equals(fieldName)) {
                typeValue = element.toString().replace("Type:", "");
              } else if ("Update requires".equals((fieldName))) {
                updateValue = element.toString().replace("Update requires:", "");
              }
            } else {
              descriptionValue = element.toString();
            }
          }

          property.description = descriptionValue;
          property.updateRequires = updateValue;
          if (typeValue != null) {
            property.type = typeValue;
          } else if (resourceType.name.equals("AWS::Redshift::Cluster") && property.name.equals("SnapshotClusterIdentifier")) {
            property.type = "String";
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
            if (!resourceType.name.equals("AWS::RDS::DBParameterGroup") &&
                    !resourceType.name.equals("AWS::Route53::RecordSet") &&
                    !resourceType.name.equals("AWS::RDS::DBSecurityGroupIngress")) {
              throw new RuntimeException("Required is not found in property " + property.name + " in " + url);
            }
          }

          resourceType.properties.add(property);
        }
      }
    } else {
      Element tableElement = doc.select("div.informaltable").first();
      if (tableElement != null) {
        final List<List<String>> table = parseTable(tableElement);

        for (List<String> row : table) {
          if (row.size() != 4) {
            continue;
          }

          final String property = row.get(0);
          final String type = row.get(1);
          final String required = row.get(2);
          // final String notes = row.get(3);

          CloudFormationResourceProperty resourceProperty = new CloudFormationResourceProperty();

          resourceProperty.name = property;
          resourceProperty.description = ""; // notes

          if (required.equalsIgnoreCase("yes")) {
            resourceProperty.required = true;
          } else if (required.equalsIgnoreCase("no")) {
            resourceProperty.required = false;
          } else {
            throw new RuntimeException("Unknown value for required in property " + property + " in " + url + ": " + required);
          }

          resourceProperty.type = type;

          resourceType.properties.add(resourceProperty);
        }
      } else {
        if (!name.equals("AWS::CloudFormation::WaitConditionHandle") &&
            !name.equals("AWS::SDB::Domain") &&
            !name.equals("AWS::ECS::Cluster")) {
          throw new RuntimeException("No properties found in " + url);
        }
      }
    }

    // De-facto changes not covered in documentation

    if (name.equals("AWS::ElasticBeanstalk::Application")) {
      // Not in official documentation yet, found in examples
      resourceType.properties.add(CloudFormationResourceProperty.create("ConfigurationTemplates", "", "Unknown", false, ""));
      resourceType.properties.add(CloudFormationResourceProperty.create("ApplicationVersions", "", "Unknown", false, ""));
    }

    if (name.equals("AWS::IAM::AccessKey")) {
      resourceType.findProperty("Status").required = false;
    }

    if (name.equals("AWS::RDS::DBInstance")) {
      resourceType.findProperty("AllocatedStorage").required = false;
    }

    // See #17, ToPort and FromPort are required with port-based ip protocols only, will implement special check later
    if (name.equals("AWS::EC2::SecurityGroupEgress") || name.equals("AWS::EC2::SecurityGroupIngress")) {
      resourceType.findProperty("ToPort").required = false;
      resourceType.findProperty("FromPort").required = false;
    }

    return resourceType;
  }

  private static List<List<String>> parseTable(Element table) {
    final Element tbody = table.getElementsByTag("tbody").first();
    if (tbody == null) {
      return new ArrayList<>();
    }

    List<List<String>> result = new ArrayList<>();
    for (Element tr : tbody.children()) {
      if (!tr.tagName().equals("tr")) {
        continue;
      }

      List<String> row = new ArrayList<>();
      for (Element td : tr.children()) {
        if (!td.tagName().equals("td")) {
          continue;
        }

        row.add(td.text());
      }

      result.add(row);
    }

    return result;
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

      result.add(Pair.of(new URL(url, href.trim()), name.trim()));
    }

    Collections.sort(result, new Comparator<Pair<URL, String>>() {
      @Override
      public int compare(@NotNull Pair<URL, String> o1, @NotNull Pair<URL, String> o2) {
        return o1.getValue().compareTo(o2.getValue());
      }
    });

    return result;
  }

  private static List<String> getPredefinedParameters() throws IOException {
    URL url = new URL("http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/pseudo-parameter-reference.html");
    final Document doc = getDocumentFromUrl(url);

    List<String> result = new ArrayList<>();

    Element vlist = doc.select("div.variablelist").first();

    for (Element param : vlist.select("span.term")) {
      result.add(param.text());
    }

    Collections.sort(result);

    return result;
  }

  private static CloudFormationLimits getLimits() throws IOException {
    CloudFormationLimits result = new CloudFormationLimits();

    URL fnGetAttrDocUrl = new URL("http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/cloudformation-limits.html");
    final Document doc = getDocumentFromUrl(fnGetAttrDocUrl);

    Element tableElement = doc.select("div.table-contents").first();
    assert tableElement != null;

    final List<List<String>> table = parseTable(tableElement);
    Map<String, String> limits = new HashMap<>();
    for (List<String> row : table) {
      limits.put(row.get(0), row.get(2));
    }

    result.maxOutputs = Integer.parseInt(limits.get("Outputs").replace(" outputs", ""));
    result.maxParameters = Integer.parseInt(limits.get("Parameters").replace(" parameters", ""));
    result.maxMappings = Integer.parseInt(limits.get("Mappings").replace(" mappings", ""));

    return result;
  }
}
