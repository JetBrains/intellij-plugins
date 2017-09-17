import com.intellij.aws.cloudformation.metadata.CloudFormationLimits
import com.intellij.aws.cloudformation.metadata.CloudFormationMetadata
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceAttribute
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceProperty
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceType
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceTypeDescription
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceTypesDescription
import com.intellij.aws.cloudformation.metadata.MetadataSerializer
import org.apache.commons.io.IOUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.ArrayList
import java.util.regex.Pattern

object ResourceTypesSaver {
  private val RESOURCE_TYPE_PATTERN = Pattern.compile("<li><a href=\"([^\"]+)\">(AWS::[^<]+)</a></li>")
  val FETCH_TIMEOUT_MS = 10000

  fun saveResourceTypes() {
    val resourceAttributesMap = fetchResourceAttributes()

    val limits = fetchLimits()
    val resourceTypeLocations = fetchResourceTypeLocations()
    val predefinedParameters = fetchPredefinedParameters()

    for (orphanResourceTypeName in resourceAttributesMap.keys.minus(resourceTypeLocations.map { it.name })) {
      throw RuntimeException("ResourceType $orphanResourceTypeName from resources attributes list is not found in list of resource types")
    }

    val resourceTypes = resourceTypeLocations.map {
      it.name to fetchResourceType(it.name, it.location, resourceAttributesMap.getOrElse(it.name, { mapOf() }))
    }.toMap()

    val metadata = CloudFormationMetadata(
        resourceTypes = resourceTypes.mapValues { it.value.first },
        predefinedParameters = predefinedParameters,
        limits = limits
    )

    val descriptions = CloudFormationResourceTypesDescription(
        resourceTypes = resourceTypes.mapValues { it.value.second }
    )

    FileOutputStream(File("src/main/resources/cloudformation-metadata.xml")).use { outputStream -> MetadataSerializer.toXML(metadata, outputStream) }
    FileOutputStream(File("src/main/resources/cloudformation-descriptions.xml")).use { outputStream -> MetadataSerializer.toXML(descriptions, outputStream) }
  }

  private fun fetchResourceAttributes(): Map<String, Map<String, Pair<CloudFormationResourceAttribute, String>>> {
    val fnGetAttrDocUrl = URL("https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html")
    val doc = getDocumentFromUrl(fnGetAttrDocUrl)

    val attribBlock = doc.getElementById("intrinsic-function-reference-getatt-attrib")!!
    val tableElement = attribBlock.nextElementSibling().nextElementSibling()
    assert(tableElement.className() == "table")

    val table = parseTable(tableElement)

    val result: MutableMap<String, MutableMap<String, Pair<CloudFormationResourceAttribute, String>>> = hashMapOf()

    for (row in table) {
      if (row.size != 3) {
        continue
      }

      var resourceTypeName = row[0]
      val attribute = row[1]
      val description = row[2]

      if (resourceTypeName == "AWS::CloudFormation::Stack" && attribute == "Outputs.NestedStackOutputName") {
        // Not an attribute name
        continue
      }

      if (resourceTypeName == "AWS::Serverless::Function") {
        // Skip, this is a part of another spec
        // https://github.com/awslabs/serverless-application-model/tree/master/versions
        continue
      }

      if (resourceTypeName == "AWS::EC2::AWS::EC2::SubnetNetworkAclAssociation") {
        resourceTypeName = "AWS::EC2::SubnetNetworkAclAssociation"
      }

      if (resourceTypeName == "the section called “AWS::Config::ConfigRule”") {
        resourceTypeName = "AWS::Config::ConfigRule"
      }

      fun addAttribute(_resourceTypeName: String, _attribute: String, _description: String) {
        val attributesList = result.getOrPut(_resourceTypeName, { mutableMapOf() })
        attributesList[_attribute] = Pair(CloudFormationResourceAttribute(_attribute), _description)
      }

      if (resourceTypeName == "AWS::DirectoryService::MicrosoftAD and AWS::DirectoryService::SimpleAD") {
        addAttribute("AWS::DirectoryService::MicrosoftAD", attribute, description)
        addAttribute("AWS::DirectoryService::SimpleAD", attribute, description)
        continue
      }

      addAttribute(resourceTypeName, attribute, description)
    }

    return result
  }

  private fun downloadDocument(url: URL): Document {
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

  private fun getDocumentFromUrl(url: URL): Document {
    val doc = downloadDocument(url)

    // Fix all links to be absolute URLs, this helps IDEA to navigate to them (opening external browser)
    val select = doc.select("a")
    for (e in select) {
      val absUrl = e.absUrl("href")
      e.attr("href", absUrl)
    }

    return doc
  }

  private fun fetchResourceType(resourceTypeName: String, docLocation: URL, resourceAttributes: Map<String, Pair<CloudFormationResourceAttribute, String>>): Pair<CloudFormationResourceType, CloudFormationResourceTypeDescription> {
    println(resourceTypeName)

    val doc = getDocumentFromUrl(docLocation)

    val descriptionElement = doc.select("div").single { it.attr("id") == "main-col-body" }
    descriptionElement.getElementsByAttributeValueMatching("id", "language-filter").forEach { it.remove() }
    descriptionElement.getElementsByAttributeValueMatching("summary", "Breadcrumbs").forEach { it.remove() }
    val description = cleanupHtml(descriptionElement.toString())

    val vlists = doc.select("div.variablelist")

    val properties: MutableMap<String, Pair<CloudFormationResourceProperty, String>> = mutableMapOf()

    if (!vlists.isEmpty()) {
      for (vlist in vlists) {
        var cur = vlist
        while (cur != null) {
          if (cur.tagName() == "h2") {
            break
          }
          cur = cur.previousElementSibling()
        }

        if (cur == null) continue
        assert(cur.tagName() == "h2")

        val sectionTitle = cur.text()
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

          val href = term.previousElementSibling()
          assert(href.tagName() == "a")
          assert(href.hasAttr("id"))
          val propertyId = href.attr("id")
          assert(propertyId.contains(name, ignoreCase = true)) {
            "Property anchor id ($propertyId) should have a propery name ($name) as substring in $docLocation"
          }
          val docUrl = doc.baseUri() + "#" + propertyId

          var requiredValue: String? = null
          var typeValue: String? = null
          var descriptionValue = ""
          var updateValue = ""

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

          var type = ""

          if (typeValue != null) {
            type = typeValue
          } else if (resourceTypeName == "AWS::Redshift::Cluster" && name == "SnapshotClusterIdentifier") {
            type = "String"
          } else if (resourceTypeName == "AWS::Cognito::IdentityPool" && name == "SupportedLoginProviders") {
            type = "String"
          } else if (resourceTypeName == "AWS::SNS::TopicPolicy" && name == "PolicyDocument") {
            type = "JSON or YAML"
          } else {
            // TODO
            if (resourceTypeName != "AWS::Route53::RecordSet" && !(name == "ScheduleExpression" && resourceTypeName == "AWS::Events::Rule")) {
              throw RuntimeException("Type is not found in property $name in $docLocation")
            }
          }

          // TODO Handle "Required if NetBiosNameServers is specified; optional otherwise" in https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-resource-ec2-dhcp-options.html
          // TODO Handle "Yes, for VPC security groups" in https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-ec2-security-group.html
          // TODO Handle "Can be used instead of GroupId for EC2 security groups" in https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-ec2-security-group-ingress.html
          // TODO Handle "Yes, for VPC security groups; can be used instead of GroupName for EC2 security groups" in https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-ec2-security-group-ingress.html
          // TODO Handle "Yes, for ICMP and any protocol that uses ports" in https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-ec2-security-group-ingress.html
          // TODO https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-elasticache-cache-cluster.html: If your cache cluster isn't in a VPC, you must specify this property
          // TODO https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-elasticache-cache-cluster.html: If your cache cluster is in a VPC, you must specify this property

          var required = false

          if (requiredValue != null) {
            if (requiredValue.equals("Yes", ignoreCase = true)) {
              required = true
            } else if (
                requiredValue.startsWith("No", ignoreCase = true) ||
                requiredValue.startsWith("Conditional") ||
                requiredValue == "Required if NetBiosNameServers is specified; optional otherwise" ||
                requiredValue == "Yes, for VPC security groups" ||
                requiredValue == "Yes. The IamInstanceProfile and ServiceRole options are required" ||
                requiredValue == "Can be used instead of GroupId for EC2 security groups" ||
                requiredValue == "Yes, for VPC security groups; can be used instead of GroupName for EC2 security groups" ||
                requiredValue == "Yes, for ICMP and any protocol that uses ports" ||
                requiredValue == "If your cache cluster isn't in a VPC, you must specify this property" ||
                requiredValue == "If your cache cluster is in a VPC, you must specify this property" ||
                requiredValue == "Yes. If you specify the AuthorizerId property, specify CUSTOM for this property" ||
                requiredValue == "Yes, for VPC security groups without a default VPC") {
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

          if (resourceTypeName == "AWS::AutoScaling::AutoScalingGroup" && name == "NotificationConfigurations") {
            val additionalProperty = CloudFormationResourceProperty("NotificationConfiguration", type, required, docUrl, updateValue)
            properties[additionalProperty.name] = Pair(additionalProperty, descriptionValue)
          }

          // Most likely a documentation bugs, it contradicts examples in the same article
          // see https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-resource-batch-jobdefinition.html
          if (resourceTypeName == "AWS::Batch::JobDefinition" && name == "Parameters") {
            required = false
          }

          properties[name] = Pair(CloudFormationResourceProperty(name, type, required, docUrl, updateValue), descriptionValue)
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

          properties[name] = Pair(CloudFormationResourceProperty(name, type, required, "", ""), "")
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
      val value = properties[name] ?: error("Property $name is not found in resource type $resourceTypeName")
      properties[name] = Pair(converter(value.first), "")
    }

    if (resourceTypeName == "AWS::ElasticBeanstalk::Application") {
      // Not in official documentation yet, found in examples
      properties["ConfigurationTemplates"] = Pair(CloudFormationResourceProperty("ConfigurationTemplates", "Unknown", false, "", ""), "")
      properties["ApplicationVersions"] = Pair(CloudFormationResourceProperty("ApplicationVersions", "Unknown", false, "", ""), "")
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

    return Pair(
        CloudFormationResourceType(
            resourceTypeName,
            doc.baseUri(),
            properties.mapValues { it.value.first },
            resourceAttributes.mapValues { it.value.first }
        ),

        CloudFormationResourceTypeDescription(
            description,
            properties = properties.mapValues { it.value.second },
            attributes = resourceAttributes.mapValues { it.value.second }
        )
    )
  }

  private val cleanElementIdPattern = Regex(" ?id=\\\"[0-9a-f]{8}\\\"")
  private val cleanElementNamePattern = Regex(" name=\\\"[0-9a-f]{8}\\\"")

  private fun cleanupHtml(s: String): String {
    return s.replace(cleanElementIdPattern, "").replace(cleanElementNamePattern, "")
  }

  private fun parseTable(table: Element): List<List<String>> {
    val tbody = table.getElementsByTag("tbody").first() ?: return emptyList()

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
    val url = URL("https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-template-resource-type-ref.html")
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
    val url = URL("https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/pseudo-parameter-reference.html")
    val doc = getDocumentFromUrl(url)
    return doc.select("h2").filter { it.attr("id").startsWith("cfn-pseudo-param") }.map { it.text() }.sorted().toList()
  }

  private fun fetchLimits(): CloudFormationLimits {
    val fnGetAttrDocUrl = URL("https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/cloudformation-limits.html")
    val doc = getDocumentFromUrl(fnGetAttrDocUrl)

    val tableElement = doc.select("div.table-contents").first()!!

    val table = parseTable(tableElement)

    val limits = table.filter { it.size == 4 }.map { it[0] to it[2] }.toMap()

    return CloudFormationLimits(
        maxMappings = Integer.parseInt(limits["Mappings"]!!.replace(" mappings", "")),
        maxParameters = Integer.parseInt(limits["Parameters"]!!.replace(" parameters", "")),
        maxOutputs = Integer.parseInt(limits["Outputs"]!!.replace(" outputs", ""))
    )
  }
}
