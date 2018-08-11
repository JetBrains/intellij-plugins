@file:Suppress("LoopToCallChain", "Destructure")

import com.intellij.aws.cloudformation.CloudFormationConstants
import com.intellij.aws.cloudformation.metadata.CloudFormationLimits
import com.intellij.aws.cloudformation.metadata.CloudFormationManualResourceType
import com.intellij.aws.cloudformation.metadata.CloudFormationMetadata
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceTypesDescription
import com.intellij.aws.cloudformation.metadata.MetadataSerializer
import com.intellij.aws.cloudformation.metadata.awsServerless20161031ResourceTypes
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
  private val FETCH_TIMEOUT_MS = 10000

  fun CloudFormationManualResourceType.toResourceTypeBuilder(): ResourceTypeBuilder {
    val builder = ResourceTypeBuilder(name, url)
    builder.description = description
    builder.transform = CloudFormationConstants.awsServerless20161031TransformName

    attributes.forEach { attribute ->
      builder.addAttribute(attribute.name).apply {
        description = attribute.description
      }
    }

    properties.forEach { property ->
      builder.addProperty(property.name).apply {
        description = property.description
        type = property.type
        required = property.required
        url = property.url ?: builder.url
        updateRequires = property.updateRequires ?: ""
      }
    }

    return builder
  }

  fun saveResourceTypes() {
    awsServerless20161031ResourceTypes.map { it.toResourceTypeBuilder().toResourceType() }
    awsServerless20161031ResourceTypes.map { it.toResourceTypeBuilder().toResourceTypeDescription() }

    val limits = fetchLimits()
    val resourceTypeLocations = fetchResourceTypeLocations()
    val predefinedParameters = fetchPredefinedParameters()

    val resourceTypes = resourceTypeLocations.pmap(numThreads = 10) {
      val builder = ResourceTypeBuilder(it.name, it.location)
      fetchResourceType(builder)

      // Check everything is set
      builder.toResourceType()
      builder.toResourceTypeDescription()

      builder
    }

    fetchResourceAttributes(resourceTypes)

    val allBuilders = resourceTypes + awsServerless20161031ResourceTypes.map { it.toResourceTypeBuilder() }

    val metadata = CloudFormationMetadata(
        resourceTypes = allBuilders.map { Pair(it.name, it.toResourceType()) }.toMap(),
        predefinedParameters = predefinedParameters,
        limits = limits
    )

    val descriptions = CloudFormationResourceTypesDescription(
        resourceTypes = allBuilders.map { Pair(it.name, it.toResourceTypeDescription()) }.toMap()
    )

    FileOutputStream(File("src/main/resources/cloudformation-metadata.xml")).use { outputStream -> MetadataSerializer.toXML(metadata, outputStream) }
    FileOutputStream(File("src/main/resources/cloudformation-descriptions.xml")).use { outputStream -> MetadataSerializer.toXML(descriptions, outputStream) }
  }

  private fun fetchResourceAttributes(resourceTypes: List<ResourceTypeBuilder>) {
    val fnGetAttrDocUrl = URL("https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html")
    val doc = getDocumentFromUrl(fnGetAttrDocUrl)

    val attribBlock = doc.getElementById("intrinsic-function-reference-getatt-attrib")!!
    val tableElement = attribBlock.nextElementSibling().nextElementSibling()
    assert(tableElement.className() == "table")

    val table = parseTable(tableElement)

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
        val builder = resourceTypes.single { it.name == _resourceTypeName }
        builder.addAttribute(_attribute).description = _description
      }

      if (resourceTypeName == "AWS::DirectoryService::MicrosoftAD and AWS::DirectoryService::SimpleAD") {
        addAttribute("AWS::DirectoryService::MicrosoftAD", attribute, description)
        addAttribute("AWS::DirectoryService::SimpleAD", attribute, description)
        continue
      }

      addAttribute(resourceTypeName, attribute, description)
    }
  }

  private fun downloadDocument(url: URL): Document {
    println("Downloading $url")
    for (retry in 1..4) {
      try {
        return Jsoup.parse(url, FETCH_TIMEOUT_MS)
      } catch (ignored: IOException) {
      }

      println("retry...")
    }

    throw RuntimeException("Could not download from $url")
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

  @JvmStatic
  fun main(args: Array<String>) {
    val builder = ResourceTypeBuilder(
        "AWS::Cognito::UserPool",
        "https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-resource-cognito-userpool.html")
    fetchResourceType(builder)
  }

  private fun fetchResourceType(builder: ResourceTypeBuilder) {
    println(builder.name)

    val doc = getDocumentFromUrl(URL(builder.url))

    val descriptionElement = doc.select("div").single { it.attr("id") == "main-col-body" }
    descriptionElement.getElementsByAttributeValueMatching("id", "language-filter").forEach { it.remove() }
    descriptionElement.getElementsByAttributeValueMatching("summary", "Breadcrumbs").forEach { it.remove() }
    builder.description = cleanupHtml(descriptionElement.toString())

    val vlists = doc.select("div.variablelist")
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

        if (sectionTitle == "Return Value" || sectionTitle == "Return Values") {
          for (term in vlist.select("span.term")) {
            if (term.parent().parent().parent() !== vlist) {
              continue
            }

            val descr = term.parent().nextElementSibling()
            var name = term.text()

            if (name == "DomainArn (deprecated)") {
              name = "DomainArn"
            }

            builder.addAttribute(name).description = descr.text()
          }

          continue
        }

        if ("Properties" != sectionTitle && "Members" != sectionTitle) {
          error("Unknown section $sectionTitle")
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
            "Property anchor id ($propertyId) should have a property name ($name) as substring in ${builder.url}"
          }
          val docUrl = doc.baseUri() + "#" + propertyId

          val propertyBuilder = builder.addProperty(name)
          propertyBuilder.url = docUrl

          propertyBuilder.updateRequires = ""

          var requiredValue: String? = null
          var typeValue: String? = null

          propertyBuilder.description = ""

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

              when (fieldName) {
                "Required" -> requiredValue = fieldValue
                "Type" -> typeValue = element.toString().replace("Type:", "")
                "Update requires" -> propertyBuilder.updateRequires = element.toString().replace("Update requires:", "")
              }
            } else {
              propertyBuilder.description = element.toString()
            }
          }

          propertyBuilder.type = if (typeValue != null) {
            typeValue
          } else if (builder.name == "AWS::Redshift::Cluster" && name == "SnapshotClusterIdentifier") {
            "String"
          } else if (builder.name == "AWS::Cognito::IdentityPool" && name == "SupportedLoginProviders") {
            "String"
          } else if (builder.name == "AWS::ApiGateway::VpcLink" && name == "TargetArns") {
            "List of String"
          } else if (builder.name == "AWS::SNS::TopicPolicy" && name == "PolicyDocument") {
            "JSON or YAML"
          } else if (builder.name == "AWS::Route53::RecordSet" && name == "Region") {
            "String"
          } else {
            // TODO
//            if (resourceTypeName == "AWS::Route53::RecordSet" || name == "ScheduleExpression" && resourceTypeName == "AWS::Events::Rule") "" else {
//            }

            throw RuntimeException("Type is not found in property $name in ${builder.url}")
          }

          // TODO Handle "Required if NetBiosNameServers is specified; optional otherwise" in https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-resource-ec2-dhcp-options.html
          // TODO Handle "Yes, for VPC security groups" in https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-ec2-security-group.html
          // TODO Handle "Can be used instead of GroupId for EC2 security groups" in https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-ec2-security-group-ingress.html
          // TODO Handle "Yes, for VPC security groups; can be used instead of GroupName for EC2 security groups" in https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-ec2-security-group-ingress.html
          // TODO Handle "Yes, for ICMP and any protocol that uses ports" in https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-ec2-security-group-ingress.html
          // TODO https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-elasticache-cache-cluster.html: If your cache cluster isn't in a VPC, you must specify this property
          // TODO https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-elasticache-cache-cluster.html: If your cache cluster is in a VPC, you must specify this property

          propertyBuilder.required =
              if (builder.name == "AWS::Batch::JobDefinition" && name == "Parameters") {
                // Most likely a documentation bug, it contradicts examples in the same article
                // see https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-resource-batch-jobdefinition.html
                false
              } else if (builder.name == "AWS::Kinesis::Stream" && name == "StreamEncryption") {
                // Most likely a documentation bug, this property was introduced later
                // and will break existing code if it is mandatory
                false
              } else if (builder.name == "AWS::Neptune::DBCluster" && name == "IamAuthEnabled") {
                // Most likely a documentation bug, this property was introduced later
                false
              } else if (requiredValue != null) {
                if (requiredValue.equals("Yes", ignoreCase = true)) {
                  true
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
                  false
                } else {
                  throw RuntimeException("Unknown value for required in property $name in ${builder.url}: $requiredValue")
                }
              } else {
                // TODO
                if (builder.name != "AWS::RDS::DBParameterGroup" &&
                    builder.name != "AWS::Route53::RecordSet" &&
                    builder.name != "AWS::RDS::DBSecurityGroupIngress") {
                  throw RuntimeException("Required is not found in property $name in ${builder.url}")
                }

                false
              }

          if (builder.name == "AWS::AutoScaling::AutoScalingGroup" && name == "NotificationConfigurations") {
            builder.addProperty("NotificationConfiguration").apply {
              type = propertyBuilder.description
              description = propertyBuilder.description
              required = propertyBuilder.required
              updateRequires = propertyBuilder.updateRequires
              url = propertyBuilder.url
            }
          }
        }
      }
    } else {
        if (builder.name != "AWS::CloudFormation::WaitConditionHandle" &&
            builder.name != "AWS::SDB::Domain" &&
            builder.name != "AWS::CodeDeploy::Application" &&
            builder.name != "AWS::ECS::Cluster") {
          throw RuntimeException("No properties found in ${builder.url}")
        }
    }

    // De-facto changes not covered in documentation

    if (builder.name == "AWS::ElasticBeanstalk::Application") {
      // Not in official documentation yet, found in examples
      builder.addProperty("ConfigurationTemplates").apply {
        type = "Unknown"
        required = false
        url = ""
        updateRequires = ""
        description = ""
      }
      builder.addProperty("ApplicationVersions").apply {
        type = "Unknown"
        required = false
        url = ""
        updateRequires = ""
        description = ""
      }
    }

    if (builder.name == "AWS::IAM::AccessKey") {
      builder.addProperty("Status").required = false
    }

    if (builder.name == "AWS::RDS::DBInstance") {
      builder.addProperty("AllocatedStorage").required = false
    }

    // See #17, ToPort and FromPort are required with port-based ip protocols only, will implement special check later
    if (builder.name == "AWS::EC2::SecurityGroupEgress" || builder.name == "AWS::EC2::SecurityGroupIngress") {
      builder.addProperty("ToPort").required = false
      builder.addProperty("FromPort").required = false
    }
  }

  private val cleanElementIdPattern = Regex(""" ?id="[0-9a-f]{8}"""")
  private val cleanElementNamePattern = Regex(""" name="[0-9a-f]{8}"""")

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

      val row = tr.children()
          .filter { it.tagName() == "td" }
          .map { it.text() }

      result.add(row)
    }

    return result
  }

  private data class ResourceTypeLocation(val name: String, val location: String)

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

      result.add(ResourceTypeLocation(name.replace(Regex("\\s"), "").trim(), URL(url, href.trim()).toExternalForm()))
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
