import com.intellij.aws.cloudformation.metadata.*
import org.apache.commons.io.IOUtils
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
    val resourceAttributesMap = fetchResourceAttributes()

    val limits = fetchLimits()
    val resourceTypeLocations = fetchResourceTypeLocations()
    val predefinedParameters = fetchPredefinedParameters()

    for (orphanResourceTypeName in resourceAttributesMap.keys.minus(resourceTypeLocations.map { it.name })) {
      throw RuntimeException("ResourceType $orphanResourceTypeName from resources attributes list is not found in list of resource types")
    }

    val resourceTypes = resourceTypeLocations.map {
      fetchResourceType(it.name, it.location, resourceAttributesMap.getOrElse(it.name, { arrayListOf() }))
    }

    val metadata = CloudFormationMetadata(
        resourceTypes = resourceTypes,
        predefinedParameters = predefinedParameters,
        limits = limits
    )

    FileOutputStream(File("src/main/resources/cloudformation-metadata.xml")).use { outputStream -> MetadataSerializer.toXML(metadata, outputStream) }
  }

  private fun fetchResourceAttributes(): Map<String, List<CloudFormationResourceAttribute>> {
    val fnGetAttrDocUrl = URL("http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html")
    val doc = getDocumentFromUrl(fnGetAttrDocUrl)

    val tableElement = doc.select("div.informaltable").first()!!

    val table = parseTable(tableElement)

    val result: MutableMap<String, MutableList<CloudFormationResourceAttribute>> = hashMapOf()

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

      if (resourceTypeName == "the section called “AWS::Config::ConfigRule”") {
        resourceTypeName = "AWS::Config::ConfigRule"
      }

      fun addAttribute(resourceTypeName: String, attribute: String) {
        val attributesList = result.getOrPut(resourceTypeName, { arrayListOf() })
        attributesList.add(CloudFormationResourceAttribute(attribute, ""))
      }

      if (resourceTypeName == "AWS::DirectoryService::MicrosoftAD and AWS::DirectoryService::SimpleAD") {
        addAttribute("AWS::DirectoryService::MicrosoftAD", attribute)
        addAttribute("AWS::DirectoryService::SimpleAD", attribute)
        continue
      }

      addAttribute(resourceTypeName, attribute)
    }

    return result
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

  private fun fetchResourceType(resourceTypeName: String, docLocation: URL, resourceAttributes: List<CloudFormationResourceAttribute>): CloudFormationResourceType {
    println(resourceTypeName)

    val doc = getDocumentFromUrl(docLocation)

    val descriptionElement = doc.select(".section").first()
    val description = cleanupHtml(descriptionElement.toString())

    val vlists = doc.select("div.variablelist")

    val properties: MutableList<CloudFormationResourceProperty> = arrayListOf()

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

          val descr = term.parent().nextElementSibling()
          //descr.children().
          val descrElements = descr.select("p")

          val name = term.text()

          var requiredValue: String? = null
          var typeValue: String? = null
          var descriptionValue: String = ""
          var updateValue: String = ""

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

          var type: String = ""

          if (typeValue != null) {
            type = typeValue
          } else if (resourceTypeName == "AWS::Redshift::Cluster" && name == "SnapshotClusterIdentifier") {
            type = "String"
          } else {
            // TODO
            if (resourceTypeName != "AWS::Route53::RecordSet") {
              throw RuntimeException("Type is not found in property $name in $docLocation")
            }
          }

          // TODO Handle "Required if NetBiosNameServers is specified; optional otherwise" in http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-resource-ec2-dhcp-options.html
          // TODO Handle "Yes, for VPC security groups" in http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-ec2-security-group.html
          // TODO Handle "Can be used instead of GroupId for EC2 security groups" in http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-ec2-security-group-ingress.html
          // TODO Handle "Yes, for VPC security groups; can be used instead of GroupName for EC2 security groups" in http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-ec2-security-group-ingress.html
          // TODO Handle "Yes, for ICMP and any protocol that uses ports" in http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-ec2-security-group-ingress.html
          // TODO http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-elasticache-cache-cluster.html: If your cache cluster isn't in a VPC, you must specify this property
          // TODO http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-elasticache-cache-cluster.html: If your cache cluster is in a VPC, you must specify this property

          var required = false

          if (requiredValue != null) {
            if (requiredValue == "Yes") {
              required = true
            } else if (requiredValue.startsWith("No") ||
                requiredValue.startsWith("Conditional") ||
                requiredValue == "Required if NetBiosNameServers is specified; optional otherwise" ||
                requiredValue == "Yes, for VPC security groups" ||
                requiredValue == "Can be used instead of GroupId for EC2 security groups" ||
                requiredValue == "Yes, for VPC security groups; can be used instead of GroupName for EC2 security groups" ||
                requiredValue == "Yes, for ICMP and any protocol that uses ports" ||
                requiredValue == "If your cache cluster isn't in a VPC, you must specify this property" ||
                requiredValue == "If your cache cluster is in a VPC, you must specify this property") {
              required = false
            } else {
              throw RuntimeException("Unknown value for required in property $name in $docLocation: $requiredValue")
            }
          } else {
            // TODO
            if (resourceTypeName != "AWS::RDS::DBParameterGroup" &&
                resourceTypeName != "AWS::Route53::RecordSet" &&
                resourceTypeName != "AWS::RDS::DBSecurityGroupIngress") {
              throw RuntimeException("Required is not found in property $name in $docLocation")
            }
          }

          properties.add(CloudFormationResourceProperty(name, descriptionValue, type, required, updateValue))
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

          val name = row[0]
          val type = row[1]
          val requiredString = row[2]
          // final String notes = row.get(3);

          val required: Boolean

          if (requiredString.equals("yes", ignoreCase = true)) {
            required = true
          } else if (requiredString.equals("no", ignoreCase = true)) {
            required = false
          } else {
            throw RuntimeException("Unknown value for required in property $name in $docLocation: $requiredString")
          }

          properties.add(CloudFormationResourceProperty(name, "", type, required, ""))
        }
      } else {
        if (resourceTypeName != "AWS::CloudFormation::WaitConditionHandle" &&
            resourceTypeName != "AWS::SDB::Domain" &&
            resourceTypeName != "AWS::CodeDeploy::Application" &&
            resourceTypeName != "AWS::ECS::Cluster") {
          throw RuntimeException("No properties found in $docLocation")
        }
      }
    }

    // De-facto changes not covered in documentation

    fun changeProperty(name: String, converter: (CloudFormationResourceProperty) -> CloudFormationResourceProperty) {
      val statusIndex = properties.indexOfFirst { it.name == name }
      assert(statusIndex >= 0, { "Property $name is not found in resource type $resourceTypeName"})

      properties[statusIndex] = converter(properties[statusIndex])
    }

    if (resourceTypeName == "AWS::ElasticBeanstalk::Application") {
      // Not in official documentation yet, found in examples
      properties.add(CloudFormationResourceProperty("ConfigurationTemplates", "", "Unknown", false, ""))
      properties.add(CloudFormationResourceProperty("ApplicationVersions", "", "Unknown", false, ""))
    }

    if (resourceTypeName == "AWS::IAM::AccessKey") {
      changeProperty("Status", { it.copy(required = false) })
    }

    if (resourceTypeName == "AWS::RDS::DBInstance") {
      changeProperty("AllocatedStorage", { it.copy(required = false) })
    }

    // See #17, ToPort and FromPort are required with port-based ip protocols only, will implement special check later
    if (resourceTypeName == "AWS::EC2::SecurityGroupEgress" || resourceTypeName == "AWS::EC2::SecurityGroupIngress") {
      changeProperty("ToPort", { it.copy(required = false) })
      changeProperty("FromPort", { it.copy(required = false) })
    }

    return CloudFormationResourceType(resourceTypeName, description, properties, resourceAttributes)
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

  private data class ResourceTypeLocation(val name: String, val location: URL)

  private fun fetchResourceTypeLocations(): List<ResourceTypeLocation> {
    val url = URL("http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-template-resource-type-ref.html")
    val content = IOUtils.toString(url)

    val result: MutableList<ResourceTypeLocation> = ArrayList()

    val matcher = RESOURCE_TYPE_PATTERN.matcher(content)
    while (matcher.find()) {
      val href = matcher.group(1)
      val name = matcher.group(2)
      if ("AWS::CloudFormation::Init" == name) {
        continue
      }

      result.add(ResourceTypeLocation(name.trim(), URL(url, href.trim())))
    }

    return result.sortedBy { it.name }
  }

  private fun fetchPredefinedParameters(): List<String> {
    val url = URL("http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/pseudo-parameter-reference.html")
    val doc = getDocumentFromUrl(url)

    val vlist = doc.select("div.variablelist").first()
    return vlist.select("span.term").map { it.text() }.sorted().toList()
  }

  private fun fetchLimits(): CloudFormationLimits {
    val fnGetAttrDocUrl = URL("http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/cloudformation-limits.html")
    val doc = getDocumentFromUrl(fnGetAttrDocUrl)

    val tableElement = doc.select("div.table-contents").first()!!

    val table = parseTable(tableElement)
    val limits = HashMap<String, String>()
    for (row in table) {
      limits.put(row[0], row[2])
    }

    return CloudFormationLimits(
        maxMappings = Integer.parseInt(limits["Mappings"]?.replace(" mappings", "")),
        maxParameters = Integer.parseInt(limits["Parameters"]?.replace(" parameters", "")),
        maxOutputs = Integer.parseInt(limits["Outputs"]?.replace(" outputs", ""))
    )
  }
}
