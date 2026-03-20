@file:Suppress("LoopToCallChain")

import com.google.gson.JsonParser
import com.intellij.aws.cloudformation.metadata.CloudFormationLimits
import com.intellij.aws.cloudformation.metadata.CloudFormationMetadata
import com.intellij.aws.cloudformation.metadata.CloudFormationResourceTypesDescription
import com.intellij.aws.cloudformation.metadata.MetadataSerializer
import com.intellij.aws.cloudformation.metadata.ResourceTypeBuilder
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.URL
import java.util.TreeMap
import java.util.zip.GZIPInputStream

object ResourceTypesSaver {
  private const val FETCH_TIMEOUT_MS = 10000
  private const val AWS_SERVERLESS_2016_10_31_TRANSFORM_NAME = "AWS::Serverless-2016-10-31"
  private const val AWS_SERVERLESS_RESOURCES_DOC_URL = "https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/sam-specification-resources-and-properties.html"
  private const val AWS_SERVERLESS_GLOBALS_DOC_URL = "https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/sam-specification-template-anatomy-globals.html"
  private val PSEUDO_PARAMETER_TOKEN_REGEX = Regex("""AWS::[A-Za-z0-9]+(?![:A-Za-z0-9-])""")

  private class MissingAwsDocumentationException(message: String) : RuntimeException(message)

  fun saveResourceTypes() {
    CrawlerPaths.metadataResourceDir.mkdirs()

    val limits = fetchLimits()
    val resourceTypeLocations = fetchResourceTypeLocations()
    val predefinedParameters = fetchPredefinedParameters()
    val serverlessGlobals = fetchServerlessGlobals()

    val unsupportedTypes = setOf( // broken links on website
      "AWS::M2::Application",
      "AMZN::SDC::Deployment",
      "AWS::CodeTest::PersistentConfiguration",
      "AWS::CodeTest::Series",
      "AWS::GammaDilithium::JobDefinition",
      "AWS::IoTThingsGraph::FlowTemplate"
    )

    val supportedTypeLocations = resourceTypeLocations.filter {
      !unsupportedTypes.contains(it.name)
    }

    val resourceTypes = supportedTypeLocations.pmap(numThreads = 10) {
      val location = when (it.name) {
        "AWS::CloudWatch::Dashboard" -> "https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-cw-dashboard.html"
        "AWS::ElasticBeanstalk::ConfigurationTemplate" -> "https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-resource-beanstalk-configurationtemplate.html"
        else -> it.location
      }

      try {
        val builder = ResourceTypeBuilder(name = it.name, url = location)
        builder.transform = it.transform
        fetchResourceType(builder)

        // Check everything is set
        builder.toResourceType()
        builder.toResourceTypeDescription()

        builder
      } catch (e: MissingAwsDocumentationException) {
        println("Skipping ${it.name}: ${e.message}")
        null
      } catch (e: Throwable) {
        throw Exception("Unable to parse resource type ${it.name} from $location: ${e.message}", e)
      }
    }.filterNotNull()

    val metadata = CloudFormationMetadata(
      resourceTypes = TreeMap(resourceTypes.associate { Pair(it.name, it.toResourceType()) }),
      predefinedParameters = predefinedParameters,
      limits = limits,
      serverlessGlobals = TreeMap(serverlessGlobals.mapValues { (_, properties) -> properties.sorted() })
    )

    val descriptions = CloudFormationResourceTypesDescription(
        resourceTypes = resourceTypes.associate { Pair(it.name, it.toResourceTypeDescription()) }
    )

    File(CrawlerPaths.metadataResourceDir, "cloudformation-metadata.xml")
      .outputStream().use { outputStream -> MetadataSerializer.toXML(metadata, outputStream) }
    File(CrawlerPaths.metadataResourceDir, "cloudformation-descriptions.xml")
      .outputStream().use { outputStream -> MetadataSerializer.toXML(descriptions, outputStream) }
  }

  private fun downloadDocumentHandlingPartialFiles(url: URL): Document {
    val doc = downloadDocument(url)

    if (doc.select("div").any { it.attr("id") == "main-col-body" }) {
      return doc
    }

    val partialLocation = doc.location().removeSuffix(".html") + ".partial.html"
    val partialDoc = downloadDocument(toURL(partialLocation))

    if (partialDoc.select("div").any { it.attr("id") == "main-col-body" }) {
      return partialDoc
    }

    if (isCloudFormationIndexRedirect(doc) && isCloudFormationIndexRedirect(partialDoc)) {
      throw MissingAwsDocumentationException("Documentation page is unavailable")
    }

    error("Could not fetch a valid AWS document page $url from both $doc and $partialDoc")
  }

  private fun isCloudFormationIndexRedirect(doc: Document): Boolean {
    val refreshContent = doc.select("meta[http-equiv=refresh]").firstOrNull()?.attr("content")
    return refreshContent?.contains("introduction.html") == true
  }

  private fun downloadDocument(url: URL): Document {
    println("Downloading $url")
    repeat(4) {
      try {
        return Jsoup.parse(url, FETCH_TIMEOUT_MS)
      } catch (_: IOException) {
      }

      println("retry...")
    }

    throw RuntimeException("Could not download from $url")
  }

  private fun getDocumentFromUrl(url: URL): Document {
    val doc = downloadDocumentHandlingPartialFiles(url)

    // Fix all links to be absolute URLs, this helps IDEA to navigate to them (opening external browser)
    val select = doc.select("a")
    for (e in select) {
      val absUrl = e.absUrl("href").replace(".partial.html", ".html")
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

    val doc = getDocumentFromUrl(toURL(builder.url))
    val serverlessSyntaxProperties =
      if (builder.transform == AWS_SERVERLESS_2016_10_31_TRANSFORM_NAME) {
        parseServerlessSyntaxPropertyNames(doc)
      }
      else {
        null
      }

    val descriptionElement = doc.select("div").single { it.attr("id") == "main-col-body" }
    descriptionElement.getElementsByAttributeValueMatching("id", "language-filter").forEach { it.remove() }
    descriptionElement.getElementsByAttributeValueMatching("summary", "Breadcrumbs").forEach { it.remove() }
    builder.description = cleanupHtml(descriptionElement.toString())

    val vlists = doc.select("div.variablelist")
    if (!vlists.isEmpty()) {
      for (vlist in vlists) {
        var cur: Element? = vlist
        while (cur != null) {
          if (cur.tagName() == "h2") {
            break
          }
          cur = cur.previousElementSibling()
        }

        if (cur == null) continue
        assert(cur.tagName() == "h2")

        val sectionTitle = cur.text()

        if (sectionTitle.equals("Return Value", true) || sectionTitle.equals("Return Values", true)) {
          for (term in vlist.select("span.term")) {
            if (term.parent()?.parent()?.parent() !== vlist) {
              continue
            }

            val descr = term.parent()?.nextElementSibling()
            var name = term.text()

            if (name == "DomainArn (deprecated)") {
              name = "DomainArn"
            }

            builder.addAttribute(name).description = descr?.text()
          }

          continue
        }

        if ("Properties" != sectionTitle && "Members" != sectionTitle) {
          error("Unknown section $sectionTitle")
        }

        val ignoredServerlessProperties = LinkedHashSet<String>()

        for (term in vlist.select("span.term")) {
          if (term.parent()?.parent()?.parent() !== vlist) {
            continue
          }

          val descr = term.parent()?.nextElementSibling()
          //descr.children().
          val descrElements = descr?.select("p")

          val name = term.text()
          if (serverlessSyntaxProperties != null && name !in serverlessSyntaxProperties) {
            ignoredServerlessProperties.add(name)
            continue
          }

          val href = term.previousElementSibling() ?: term.parent()
          assert(href?.tagName() == "a" || href?.tagName() == "dt")
          assert(href?.hasAttr("id") ?: false)

          val propertyId = href?.attr("id")
          assert(propertyId?.contains(name, ignoreCase = true)?:false) {
            "Property anchor id ($propertyId) should have a property name ($name) as substring in ${builder.url}"
          }
          val docUrl = doc.location().replace(".partial.html", ".html") + "#" + propertyId

          val propertyBuilder = builder.addProperty(name)
          propertyBuilder.url = docUrl

          propertyBuilder.updateRequires = ""

          var requiredValue: String? = null
          var typeValue: String? = null

          propertyBuilder.description = ""

          if (descrElements != null) {
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
          }

          propertyBuilder.type = typeValue ?: when (builder.name to name) {
            "AWS::Redshift::Cluster" to "SnapshotClusterIdentifier" -> "String"
            "AWS::Cognito::IdentityPool" to "SupportedLoginProviders" -> "String"
            "AWS::ApiGateway::VpcLink" to "TargetArns" -> "List of String"
            "AWS::SNS::TopicPolicy" to "PolicyDocument" -> "JSON or YAML"
            "AWS::Route53::RecordSet" to "Region" -> "String"
            else -> {
              // TODO
//            if (resourceTypeName == "AWS::Route53::RecordSet" || name == "ScheduleExpression" && resourceTypeName == "AWS::Events::Rule") "" else {
//            }

              throw RuntimeException("Type is not found in property $name in ${builder.url}")
            }
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
                if (requiredValue.equals("Yes", ignoreCase = true) ||
                    requiredValue.equals("true", ignoreCase = true)) {
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
                    builder.name != "AWS::EC2::EC2Fleet" &&
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

        if (ignoredServerlessProperties.isNotEmpty()) {
          println(
            "Skipping AWS SAM documented properties not present in syntax for ${builder.name}: ${ignoredServerlessProperties.joinToString()}"
          )
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

      result.add(tr.children()
                   .asSequence()
                   .filter { it.tagName() == "td" }
                   .map { it.text() }
                   .toList())
    }

    return result
  }

  internal data class ResourceTypeLocation(
    val name: String,
    val location: String,
    val transform: String? = null
  )

  private fun fetchResourceTypeLocations(url: String): Map<String, ResourceTypeLocation> {
    val content = try {
      toURL(url).openStream().use { stream ->
        GZIPInputStream(stream).bufferedReader().readText()
      }
    } catch (t: Throwable) {
      throw IllegalStateException("Unable to fetch $url", t)
    }

    val root = JsonParser.parseString(content)

    val resourceTypes = root.asJsonObject["ResourceTypes"].asJsonObject

    return resourceTypes.entrySet()
        .mapNotNull { (key, resourceTypeJson) ->
          val documentationElement = resourceTypeJson.asJsonObject["Documentation"]
          if (documentationElement == null) {
            return@mapNotNull null
          } else {
            key to ResourceTypeLocation(
                name = key,
                location = documentationElement.asString.replace("http://", "https://")
            )
          }
        }
        .toMap()
  }

  private fun fetchResourceTypeLocations(): List<ResourceTypeLocation> {
    // from https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/cfn-resource-specification.html
    val urls = listOf(
//        "https://d1mta8qj7i28i2.cloudfront.net/latest/gzip/CloudFormationResourceSpecification.json", // EU (Frankfurt)
        "https://d3teyb21fexa9r.cloudfront.net/latest/gzip/CloudFormationResourceSpecification.json", // EU (Ireland)
        "https://d68hl49wbnanq.cloudfront.net/latest/gzip/CloudFormationResourceSpecification.json", // US West (N. California)
        "https://d201a2mn26r7lk.cloudfront.net/latest/gzip/CloudFormationResourceSpecification.json" //  US West (Oregon)
    )

    val standardResourceLocations = urls
      .map { fetchResourceTypeLocations(it) }
      .fold(mapOf<String, ResourceTypeLocation>()) { acc, map -> acc + map }
      .map { it.value }

    val serverlessResourceLocations = fetchServerlessResourceTypeLocations()

    return (standardResourceLocations + serverlessResourceLocations)
        .sortedBy { it.name }
  }

  private fun fetchServerlessResourceTypeLocations(): List<ResourceTypeLocation> {
    val doc = getDocumentFromUrl(toURL(AWS_SERVERLESS_RESOURCES_DOC_URL))
    val resourceLocations = parseServerlessResourceTypeLocations(doc)

    check(resourceLocations.isNotEmpty()) {
      "Could not extract AWS SAM resource types from $AWS_SERVERLESS_RESOURCES_DOC_URL"
    }

    return resourceLocations
  }

  internal fun parseServerlessResourceTypeLocations(doc: Document): List<ResourceTypeLocation> {
    return doc.select("a[href]")
      .asSequence()
      .mapNotNull { link ->
        val name = link.text().trim()
        val href = link.absUrl("href")

        if (!name.startsWith("AWS::Serverless::") || !href.contains("sam-resource-")) {
          return@mapNotNull null
        }

        serverlessLocation(name, href)
      }
      .distinctBy { it.name }
      .sortedBy { it.name }
      .toList()
  }

  internal fun parseServerlessSyntaxPropertyNames(doc: Document): Set<String> {
    val syntaxHeader = doc.select("h2").firstOrNull { it.text().equals("Syntax", ignoreCase = true) }
                       ?: error("Could not locate SAM syntax section in ${doc.location()}")
    var current: Element? = syntaxHeader.nextElementSibling()
    while (current != null && current.tagName() != "h2") {
      val yamlSnippet = current.select("pre.programlisting, div.programlisting, pre")
        .asSequence()
        .map { it.wholeText().ifBlank { it.text() }.trim() }
        .firstOrNull { it.isNotEmpty() }
      if (yamlSnippet != null) {
        return parseServerlessSyntaxPropertyNames(yamlSnippet)
      }

      current = current.nextElementSibling()
    }

    error("Could not locate SAM YAML syntax snippet in ${doc.location()}")
  }

  internal fun parseServerlessSyntaxPropertyNames(snippet: String): Set<String> {
    val yamlKeyLines = snippet.lines()
      .mapIndexedNotNull { index, line -> parseYamlKeyLine(index, line) }

    check(yamlKeyLines.isNotEmpty()) {
      "Could not parse any YAML keys from SAM syntax snippet"
    }

    val parent = yamlKeyLines.firstOrNull { it.name == "Properties" } ?: yamlKeyLines.first()
    val propertyNames = LinkedHashSet<String>()
    var childIndent: Int? = null

    for (keyLine in yamlKeyLines) {
      if (keyLine.lineIndex <= parent.lineIndex) {
        continue
      }

      if (keyLine.indent <= parent.indent) {
        break
      }

      if (childIndent == null) {
        childIndent = keyLine.indent
      }

      if (keyLine.indent == childIndent) {
        propertyNames.add(keyLine.name)
      }
    }

    check(propertyNames.isNotEmpty()) {
      "Could not parse direct properties from SAM syntax snippet"
    }

    return propertyNames
  }

  private data class YamlKeyLine(
    val lineIndex: Int,
    val indent: Int,
    val name: String,
  )

  private fun parseYamlKeyLine(index: Int, line: String): YamlKeyLine? {
    val normalizedLine = line.replace('\t', ' ').trimEnd()
    if (normalizedLine.isBlank()) {
      return null
    }

    val indent = normalizedLine.indexOfFirst { !it.isWhitespace() }
    if (indent < 0) {
      return null
    }

    val trimmed = normalizedLine.trim()
    if (trimmed.startsWith("- ")) {
      return null
    }

    val delimiterIndex = trimmed.indexOf(':')
    if (delimiterIndex <= 0) {
      return null
    }

    return YamlKeyLine(
      lineIndex = index,
      indent = indent,
      name = trimmed.substring(0, delimiterIndex).trim(),
    )
  }

  private fun fetchServerlessGlobals(): Map<String, Set<String>> {
    val doc = getDocumentFromUrl(toURL(AWS_SERVERLESS_GLOBALS_DOC_URL))
    val globals = parseServerlessGlobals(doc)

    check(globals.isNotEmpty()) {
      "Could not extract AWS SAM Globals support from $AWS_SERVERLESS_GLOBALS_DOC_URL"
    }

    return globals
  }

  internal fun parseServerlessGlobals(doc: Document): Map<String, Set<String>> {
    val snippet = doc.select("pre, code, div.programlisting, div.highlighter-rouge")
      .asSequence()
      .map { element -> element.wholeText().ifBlank { element.text() }.trim() }
      .filter { it.contains("Globals:") && it.contains("Function:") }
      .maxByOrNull { it.length }
      ?: error("Could not locate SAM Globals support snippet in ${doc.location()}")

    return parseServerlessGlobalsSnippet(snippet)
  }

  internal fun parseServerlessGlobalsSnippet(snippet: String): Map<String, Set<String>> {
    val lines = snippet.lines().map { it.replace('\t', ' ').trimEnd() }
    val globalsIndex = lines.indexOfFirst { it.trim() == "Globals:" }

    check(globalsIndex >= 0) {
      "Could not locate 'Globals:' in SAM Globals snippet"
    }

    val globals = linkedMapOf<String, LinkedHashSet<String>>()
    var currentSection: String? = null
    var sectionIndent = -1
    var propertyIndent: Int? = null

    for (line in lines.drop(globalsIndex + 1)) {
      if (line.isBlank()) {
        continue
      }

      val indent = line.indexOfFirst { !it.isWhitespace() }
      if (indent < 0) {
        continue
      }

      val trimmed = line.trim()
      if (!trimmed.endsWith(':')) {
        continue
      }

      val name = trimmed.removeSuffix(":")
      if (sectionIndent < 0) {
        sectionIndent = indent
      }

      if (indent < sectionIndent) {
        break
      }

      if (indent == sectionIndent) {
        currentSection = name
        propertyIndent = null
        globals.getOrPut(name) { linkedSetOf() }
        continue
      }

      val activeSection = currentSection ?: continue
      if (propertyIndent == null) {
        propertyIndent = indent
      }

      if (indent == propertyIndent) {
        globals.getValue(activeSection).add(name)
      }
    }

    return globals
  }

  private fun serverlessLocation(name: String, url: String): ResourceTypeLocation {
    // Serverless types must have "Transform:" attribute
    return ResourceTypeLocation(name, url, AWS_SERVERLESS_2016_10_31_TRANSFORM_NAME)
  }

  private fun fetchPredefinedParameters(): List<String> {
    val url = toURL("https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/pseudo-parameter-reference.html")
    val doc = getDocumentFromUrl(url)

    val extracted =
      (doc.select("h1, h2, h3, h4, h5, h6") + doc.select("div#main-col-body code"))
        .asSequence()
        .flatMap { element ->
          PSEUDO_PARAMETER_TOKEN_REGEX.findAll(element.text()).map { it.value }
        }
        .toSet()

    check(extracted.isNotEmpty()) {
      "Could not extract predefined pseudo-parameters from ${url}"
    }

    return extracted.sorted()
  }

  private fun fetchLimits(): CloudFormationLimits {
    val fnGetAttrDocUrl = toURL("https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/cloudformation-limits.html")
    val doc = getDocumentFromUrl(fnGetAttrDocUrl)

    val tableElement = doc.select("div.table-contents").first()!!

    val table = parseTable(tableElement)

    val limits = table.filter { it.size == 4 }.associate { it[0] to it[2] }

    return CloudFormationLimits(
        maxMappings = Integer.parseInt(limits.getValue("Mappings").replace(" mappings", "")),
        maxParameters = Integer.parseInt(limits.getValue("Parameters").replace(" parameters", "")),
        maxOutputs = Integer.parseInt(limits.getValue("Outputs").replace(" outputs", ""))
    )
  }

  private fun toURL(url: String): URL = URI(url).toURL()
}
