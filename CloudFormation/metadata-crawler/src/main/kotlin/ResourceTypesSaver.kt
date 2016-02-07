import com.intellij.aws.cloudformation.metadata.*
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.tuple.Pair
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.*
import java.util.regex.Pattern

object ResourceTypesSaver {
  private val RESOURCE_TYPE_PATTERN = Pattern.compile("<li><a href=\"([^\"]+)\">(AWS::[^<]+)</a></li>")
  val FETCH_TIMEOUT_MS = 10000

  @Throws(Exception::class)
  fun saveResourceTypes() {
    val metadata = CloudFormationMetadata()

    metadata.limits = limits
    metadata.predefinedParameters.addAll(predefinedParameters)

    val types = resourceTypes
    for (type in types) {
      val resourceType = getResourceType(type.value, type.key)
      metadata.resourceTypes.add(resourceType)
    }

    fetchResourceAttributes(metadata)

    FileOutputStream(File("src/main/resources/cloudformation-metadata.xml")).use { outputStream -> MetadataSerializer.toXML(metadata, outputStream) }
  }

  @Throws(IOException::class)
  private fun fetchResourceAttributes(metadata: CloudFormationMetadata) {
    val fnGetAttrDocUrl = URL("http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html")
    val doc = getDocumentFromUrl(fnGetAttrDocUrl)

    val tableElement = doc.select("div.informaltable").first()!!

    val table = parseTable(tableElement)

    for (row in table) {
      if (row.size != 3) {
        continue
      }

      var resourceTypeName = row[0]
      val attribute = row[1]
      // final String description = row.get(2);

      if (resourceTypeName == "AWS::CloudFormation::Stack" && attribute == "Outputs.NestedStackOutputName") {
        // Not an attribute name
        continue
      }

      if (resourceTypeName == "AWS::EC2::AWS::EC2::SubnetNetworkAclAssociation") {
        resourceTypeName = "AWS::EC2::SubnetNetworkAclAssociation"
      }

      var resourceType: CloudFormationResourceType? = metadata.findResourceType(resourceTypeName)
      if (resourceType == null) {
        resourceType = CloudFormationResourceType()
        resourceType.name = resourceTypeName
        metadata.resourceTypes.add(resourceType)
      }

      resourceType.attributes.add(CloudFormationResourceAttribute.create(attribute, ""))
    }
  }

  private fun getDocumentFromUrl(url: URL): Document {
    println("Downloading " + url)
    for (retry in 1..4) {
      try {
        return Jsoup.parse(url, FETCH_TIMEOUT_MS)
      } catch (ignored: IOException) {
      }

      println("retry...")
    }

    throw RuntimeException("Could not download from " + url)
  }

  private fun getResourceType(name: String, url: URL): CloudFormationResourceType {
    println(name)

    val resourceType = CloudFormationResourceType()
    resourceType.name = name

    val doc = getDocumentFromUrl(url)

    val description = doc.select(".section").first()
    resourceType.description = cleanupHtml(description.toString())

    val vlists = doc.select("div.variablelist")

    if (!vlists.isEmpty()) {
      for (vlist in vlists) {
        val titleElements = vlist.parent().select("h2.title")
        if (titleElements.isEmpty()) {
          continue
        }

        val sectionTitle = titleElements.first().text()
        if ("Properties" != sectionTitle && "Members" != sectionTitle) {
          continue
        }

        for (term in vlist.select("span.term")) {
          if (term.parent().parent().parent() !== vlist) {
            continue
          }

          val property = CloudFormationResourceProperty()

          val descr = term.parent().nextElementSibling()
          //descr.children().
          val descrElements = descr.select("p")

          property.name = term.text()

          var requiredValue: String? = null
          var typeValue: String? = null
          var descriptionValue: String? = null
          var updateValue: String? = null

          for (element in descrElements) {
            if (element.parent() !== descr) {
              continue
            }

            val text = element.text()

            if (text.matches("[a-zA-Z ]+:.*".toRegex())) {
              val split = text.split(":".toRegex(), 2).toTypedArray()
              assert(split.size == 2) { text }

              val fieldName = split[0].trim { it <= ' ' }
              val fieldValue = split[1].trim { it <= ' ' }.replaceFirst("\\.$".toRegex(), "")

              if ("Required" == fieldName) {
                requiredValue = fieldValue
              } else if ("Type" == fieldName) {
                typeValue = element.toString().replace("Type:", "")
              } else if ("Update requires" == fieldName) {
                updateValue = element.toString().replace("Update requires:", "")
              }
            } else {
              descriptionValue = element.toString()
            }
          }

          property.description = descriptionValue
          property.updateRequires = updateValue
          if (typeValue != null) {
            property.type = typeValue
          } else if (resourceType.name == "AWS::Redshift::Cluster" && property.name == "SnapshotClusterIdentifier") {
            property.type = "String"
          } else {
            // TODO
            if (resourceType.name != "AWS::Route53::RecordSet") {
              throw RuntimeException("Type is not found in property " + property.name + " in " + url)
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
            if (requiredValue == "Yes") {
              property.required = true
            } else if (requiredValue.startsWith("No") ||
                requiredValue.startsWith("Conditional") ||
                requiredValue == "Required if NetBiosNameServers is specified; optional otherwise" ||
                requiredValue == "Yes, for VPC security groups" ||
                requiredValue == "Can be used instead of GroupId for EC2 security groups" ||
                requiredValue == "Yes, for VPC security groups; can be used instead of GroupName for EC2 security groups" ||
                requiredValue == "Yes, for ICMP and any protocol that uses ports" ||
                requiredValue == "If your cache cluster isn't in a VPC, you must specify this property" ||
                requiredValue == "If your cache cluster is in a VPC, you must specify this property") {
              property.required = false
            } else {
              throw RuntimeException("Unknown value for required in property " + property.name + " in " + url + ": " + requiredValue)
            }
          } else {
            // TODO
            if (resourceType.name != "AWS::RDS::DBParameterGroup" &&
                resourceType.name != "AWS::Route53::RecordSet" &&
                resourceType.name != "AWS::RDS::DBSecurityGroupIngress") {
              throw RuntimeException("Required is not found in property " + property.name + " in " + url)
            }
          }

          resourceType.properties.add(property)
        }
      }
    } else {
      val tableElement = doc.select("div.informaltable").first()
      if (tableElement != null) {
        val table = parseTable(tableElement)

        for (row in table) {
          if (row.size != 4) {
            continue
          }

          val property = row[0]
          val type = row[1]
          val required = row[2]
          // final String notes = row.get(3);

          val resourceProperty = CloudFormationResourceProperty()

          resourceProperty.name = property
          resourceProperty.description = "" // notes

          if (required.equals("yes", ignoreCase = true)) {
            resourceProperty.required = true
          } else if (required.equals("no", ignoreCase = true)) {
            resourceProperty.required = false
          } else {
            throw RuntimeException("Unknown value for required in property $property in $url: $required")
          }

          resourceProperty.type = type

          resourceType.properties.add(resourceProperty)
        }
      } else {
        if (name != "AWS::CloudFormation::WaitConditionHandle" &&
            name != "AWS::SDB::Domain" &&
            name != "AWS::CodeDeploy::Application" &&
            name != "AWS::ECS::Cluster") {
          throw RuntimeException("No properties found in " + url)
        }
      }
    }

    // De-facto changes not covered in documentation

    if (name == "AWS::ElasticBeanstalk::Application") {
      // Not in official documentation yet, found in examples
      resourceType.properties.add(CloudFormationResourceProperty.create("ConfigurationTemplates", "", "Unknown", false, ""))
      resourceType.properties.add(CloudFormationResourceProperty.create("ApplicationVersions", "", "Unknown", false, ""))
    }

    if (name == "AWS::IAM::AccessKey") {
      resourceType.findProperty("Status").required = false
    }

    if (name == "AWS::RDS::DBInstance") {
      resourceType.findProperty("AllocatedStorage").required = false
    }

    // See #17, ToPort and FromPort are required with port-based ip protocols only, will implement special check later
    if (name == "AWS::EC2::SecurityGroupEgress" || name == "AWS::EC2::SecurityGroupIngress") {
      resourceType.findProperty("ToPort").required = false
      resourceType.findProperty("FromPort").required = false
    }

    return resourceType
  }

  private val cleanElementIdPattern = Regex(" ?id=\\\"[0-9a-f]{8}\\\"")
  private val cleanElementNamePattern = Regex(" name=\\\"[0-9a-f]{8}\\\"")

  private fun cleanupHtml(s: String): String {
    return s.replace(cleanElementIdPattern, "").replace(cleanElementNamePattern, "")
  }

  private fun parseTable(table: Element): List<List<String>> {
    val tbody = table.getElementsByTag("tbody").first() ?: return ArrayList()

    val result = ArrayList<List<String>>()
    for (tr in tbody.children()) {
      if (tr.tagName() != "tr") {
        continue
      }

      val row = ArrayList<String>()
      for (td in tr.children()) {
        if (td.tagName() != "td") {
          continue
        }

        row.add(td.text())
      }

      result.add(row)
    }

    return result
  }

  private // EXCEPTION Not a resource type
  val resourceTypes: List<Pair<URL, String>>
    @Throws(IOException::class)
    get() {
      val url = URL("http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-template-resource-type-ref.html")
      val content = IOUtils.toString(url)

      val result = ArrayList<Pair<URL, String>>()

      val matcher = RESOURCE_TYPE_PATTERN.matcher(content)
      while (matcher.find()) {
        val href = matcher.group(1)
        val name = matcher.group(2)
        if ("AWS::CloudFormation::Init" == name) {
          continue
        }

        result.add(Pair.of(URL(url, href.trim { it <= ' ' }), name.trim { it <= ' ' }))
      }

      Collections.sort(result) { o1, o2 -> o1.value.compareTo(o2.value) }

      return result
    }

  private val predefinedParameters: List<String>
    @Throws(IOException::class)
    get() {
      val url = URL("http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/pseudo-parameter-reference.html")
      val doc = getDocumentFromUrl(url)

      val result = ArrayList<String>()

      val vlist = doc.select("div.variablelist").first()

      for (param in vlist.select("span.term")) {
        result.add(param.text())
      }

      Collections.sort(result)

      return result
    }

  private val limits: CloudFormationLimits
    @Throws(IOException::class)
    get() {
      val result = CloudFormationLimits()

      val fnGetAttrDocUrl = URL("http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/cloudformation-limits.html")
      val doc = getDocumentFromUrl(fnGetAttrDocUrl)

      val tableElement = doc.select("div.table-contents").first()!!

      val table = parseTable(tableElement)
      val limits = HashMap<String, String>()
      for (row in table) {
        limits.put(row[0], row[2])
      }

      result.maxOutputs = Integer.parseInt(limits["Outputs"]?.replace(" outputs", ""))
      result.maxParameters = Integer.parseInt(limits["Parameters"]?.replace(" parameters", ""))
      result.maxMappings = Integer.parseInt(limits["Mappings"]?.replace(" mappings", ""))

      return result
    }
}
