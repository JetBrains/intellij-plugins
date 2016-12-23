package com.intellij.aws.cloudformation

import com.google.common.collect.HashBiMap
import com.intellij.aws.cloudformation.model.CfnArrayValueNode
import com.intellij.aws.cloudformation.model.CfnBooleanValueNode
import com.intellij.aws.cloudformation.model.CfnExpressionNode
import com.intellij.aws.cloudformation.model.CfnFunctionNode
import com.intellij.aws.cloudformation.model.CfnMissingOrInvalidValueNode
import com.intellij.aws.cloudformation.model.CfnNameValueNode
import com.intellij.aws.cloudformation.model.CfnNamedNode
import com.intellij.aws.cloudformation.model.CfnNode
import com.intellij.aws.cloudformation.model.CfnNumberValueNode
import com.intellij.aws.cloudformation.model.CfnObjectValueNode
import com.intellij.aws.cloudformation.model.CfnResourceNode
import com.intellij.aws.cloudformation.model.CfnResourcePropertiesNode
import com.intellij.aws.cloudformation.model.CfnResourcePropertyNode
import com.intellij.aws.cloudformation.model.CfnResourceTypeNode
import com.intellij.aws.cloudformation.model.CfnResourcesNode
import com.intellij.aws.cloudformation.model.CfnRootNode
import com.intellij.aws.cloudformation.model.CfnStringValueNode
import com.intellij.json.psi.JsonArray
import com.intellij.json.psi.JsonBooleanLiteral
import com.intellij.json.psi.JsonNumberLiteral
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.json.psi.JsonValue
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import java.util.ArrayList
import java.util.HashSet
import java.util.regex.Pattern

class CloudFormationParser private constructor () {
  private val myProblems = ArrayList<CloudFormationParsedFile.ParseProblem>()
  private val myNodesMap = HashBiMap.create<PsiElement, CfnNode>()

  private fun <T : CfnNode> T.registerNode(psiElement: PsiElement): T {
    myNodesMap.put(psiElement, this)
    return this
  }

  private fun addProblem(element: PsiElement, description: String) {
    myProblems.add(CloudFormationParsedFile.ParseProblem(element, description))
  }

  private fun addProblemOnNameElement(property: JsonProperty, description: String) {
    addProblem(
        if (property.firstChild != null) property.firstChild else property,
        description)
  }

  private fun root(root: JsonObject): CfnRootNode {
    var resourcesNode: CfnResourcesNode? = null

    for (property in root.propertyList) {
      val name = property.name
      val value = property.value

      if (name.isEmpty() || value == null) {
        continue
      }

      if (CloudFormationSections.FormatVersion == name) {
        formatVersion(value)
      } else if (CloudFormationSections.Transform == name) {
        checkAndGetQuotedStringText(value)
      } else if (CloudFormationSections.Description == name) {
        description(value)
      } else if (CloudFormationSections.Parameters == name) {
        parameters(value)
      } else if (CloudFormationSections.Resources == name) {
        if (resourcesNode == null) {
          resourcesNode = resources(property)
        }
      } else if (CloudFormationSections.Conditions == name) {
        // TODO
      } else if (CloudFormationSections.Metadata == name) {
        // Generic content inside, no need to check
        // See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/metadata-section-structure.html
      } else if (CloudFormationSections.Outputs == name) {
        outputs(value)
      } else if (CloudFormationSections.Mappings == name) {
        mappings(value)
      } else {
        addProblemOnNameElement(
            property,
            CloudFormationBundle.getString("format.unknown.section", property.name))
      }
    }

    if (root.findProperty(CloudFormationSections.Resources) == null) {
      addProblem(root, CloudFormationBundle.getString("format.resources.section.required"))
    }

    return CfnRootNode(resourcesNode).registerNode(root)
  }

  private fun outputs(outputsExpression: JsonValue) {
    val obj = checkAndGetObject(outputsExpression) ?: return

    for (property in obj.propertyList) {
      val name = property.name
      val value = property.value
      if (name.isEmpty() || value == null) {
        continue
      }

      keyName(property)
      expression(value)
    }

    if (obj.propertyList.size == 0) {
      addProblemOnNameElement(
          obj.parent as JsonProperty,
          CloudFormationBundle.getString("format.no.outputs.declared"))
    }

    if (obj.propertyList.size > CloudFormationMetadataProvider.METADATA.limits.maxOutputs) {
      addProblemOnNameElement(
          obj.parent as JsonProperty,
          CloudFormationBundle.getString("format.max.outputs.exceeded", CloudFormationMetadataProvider.METADATA.limits.maxOutputs))
    }
  }

  private fun parameters(parametersExpression: JsonValue) {
    val obj = checkAndGetObject(parametersExpression) ?: return

    if (obj.propertyList.size == 0) {
      addProblemOnNameElement(
          obj.parent as JsonProperty,
          CloudFormationBundle.getString("format.no.parameters.declared"))
    }

    if (obj.propertyList.size > CloudFormationMetadataProvider.METADATA.limits.maxParameters) {
      addProblemOnNameElement(
          obj.parent as JsonProperty,
          CloudFormationBundle.getString("format.max.parameters.exceeded", CloudFormationMetadataProvider.METADATA.limits.maxParameters))
    }
  }

  private fun mappings(mappingsExpression: JsonValue) {
    val obj = checkAndGetObject(mappingsExpression) ?: return

    if (obj.propertyList.size == 0) {
      addProblemOnNameElement(
          obj.parent as JsonProperty,
          CloudFormationBundle.getString("format.no.mappings.declared"))
    }

    if (obj.propertyList.size > CloudFormationMetadataProvider.METADATA.limits.maxMappings) {
      addProblemOnNameElement(
          obj.parent as JsonProperty,
          CloudFormationBundle.getString("format.max.mappings.exceeded", CloudFormationMetadataProvider.METADATA.limits.maxMappings))
    }
  }

  private fun stringValue(expression: JsonValue) {
    checkAndGetQuotedStringText(expression)
  }

  private fun keyName(property: JsonProperty): CfnStringValueNode {
    if (AlphanumericStringPattern.matcher(property.name).matches()) {
      return CfnStringValueNode(property.name).registerNode(property.nameElement)
    } else {
      addProblemOnNameElement(
          property,
          CloudFormationBundle.getString("format.invalid.key.name"))
      return CfnStringValueNode("").registerNode(property.nameElement)
    }
  }

  private fun description(value: JsonValue) {
    checkAndGetQuotedStringText(value)
  }

  private fun resources(property: JsonProperty): CfnResourcesNode? {
    val nameNode = CfnStringValueNode(property.name).registerNode(property.nameElement)
    val obj = checkAndGetObject(property.value!!) ?: return CfnResourcesNode(nameNode, emptyList()).registerNode(property.value!!)

    val resourcesList = obj.propertyList.mapNotNull { property ->
      val resourceName = property.name
      val resourceObj = property.value

      if (resourceName.isEmpty() || resourceObj == null) {
        return@mapNotNull null
      }

      return@mapNotNull resource(property)
    }

    return CfnResourcesNode(nameNode, resourcesList).registerNode(property)
  }

  private fun resource(resourceProperty: JsonProperty): CfnResourceNode {
    val key = keyName(resourceProperty)

    val value = resourceProperty.value ?: return CfnResourceNode(key, null, emptyMap(), null).registerNode(resourceProperty)
    val obj = checkAndGetObject(value) ?: return CfnResourceNode(key, null, emptyMap(), null).registerNode(resourceProperty)

    val typeNode: CfnResourceTypeNode?
    val topLevelProperties: MutableMap<String, CfnNamedNode> = hashMapOf()

    val typeProperty = obj.findProperty(CloudFormationConstants.TypePropertyName)
    if (typeProperty != null) {
      typeNode = resourceType(typeProperty)
      topLevelProperties.put(typeProperty.name, typeNode)
    } else {
      addProblemOnNameElement(resourceProperty, CloudFormationBundle.getString("format.type.property.required"))
      typeNode = null
    }

    val properties: CfnResourcePropertiesNode?

    val propertiesProperty = obj.findProperty(CloudFormationConstants.PropertiesPropertyName)
    if (propertiesProperty != null && propertiesProperty.value is JsonObject) {
      properties = resourceProperties(propertiesProperty, typeNode?.value?.value ?: "")
      topLevelProperties.put(propertiesProperty.name, properties)
    } else {
      if (propertiesProperty != null && propertiesProperty.value !is JsonObject) {
        addProblemOnNameElement(propertiesProperty, CloudFormationBundle.getString("format.properties.property.should.properties.list"))
      }

      val resourceTypeMetadata = CloudFormationMetadataProvider.METADATA.findResourceType(typeNode?.value?.value ?: "")
      if (resourceTypeMetadata != null) {
        val requiredProperties = resourceTypeMetadata.requiredProperties
        if (!requiredProperties.isEmpty()) {
          val requiredPropertiesString = StringUtil.join(requiredProperties, " ")
          addProblemOnNameElement(
              resourceProperty,
              CloudFormationBundle.getString("format.required.resource.properties.are.not.set", requiredPropertiesString))
        }
      }

      properties = null
    }

    for (property in obj.propertyList) {
      val propertyName = property.name

      if (!CloudFormationConstants.AllTopLevelResourceProperties.contains(propertyName)) {
        addProblemOnNameElement(property, CloudFormationBundle.getString("format.unknown.resource.property", propertyName))
      }

      if (!topLevelProperties.containsKey(propertyName)) {
        val nameNode = CfnStringValueNode(propertyName).registerNode(property.nameElement)

        val valueElement = property.value
        val valueNode = if (valueElement != null) {
          CfnStringValueNode(propertyName).registerNode(valueElement)
        } else {
          CfnStringValueNode("")
        }

        topLevelProperties.put(propertyName, CfnNameValueNode(nameNode, valueNode).registerNode(property))
      }
    }

    return CfnResourceNode(key, typeNode, topLevelProperties, properties).registerNode(resourceProperty)
  }

  private fun resourceProperties(propertiesProperty: JsonProperty, rawResourceTypeName: String): CfnResourcePropertiesNode {
    val nameNode = CfnStringValueNode(propertiesProperty.name).registerNode(propertiesProperty.nameElement)
    val properties = propertiesProperty.value as JsonObject

    val resourceTypeName = if (rawResourceTypeName.startsWith(CloudFormationConstants.CustomResourceTypePrefix)) {
      CloudFormationConstants.CustomResourceType
    } else {
      rawResourceTypeName
    }

    val resourceType = CloudFormationMetadataProvider.METADATA.findResourceType(resourceTypeName)
    val requiredProperties = HashSet(resourceType?.requiredProperties ?: emptyList())

    val propertyNodes = properties.propertyList.mapNotNull { property ->
      val propertyName = property.name
      if (propertyName == CloudFormationConstants.CommentResourcePropertyName) {
        return@mapNotNull null
      }

      if (resourceType != null) {
        if (resourceType.findProperty(propertyName) == null && !isCustomResourceType(resourceTypeName)) {
          addProblemOnNameElement(property, CloudFormationBundle.getString("format.unknown.resource.type.property", propertyName))
        }
      }

      requiredProperties.remove(propertyName)

      val propertyNameNode = keyName(property)

      val jsonValueNode = property.value
      val valueNode = if (jsonValueNode != null) {
        expression(jsonValueNode)
      } else {
        CfnMissingOrInvalidValueNode()
      }

      return@mapNotNull CfnResourcePropertyNode(propertyNameNode, valueNode).registerNode(property)
    }

    if (!requiredProperties.isEmpty()) {
      val requiredPropertiesString = StringUtil.join(requiredProperties, " ")
      addProblemOnNameElement(propertiesProperty,
          CloudFormationBundle.getString("format.required.resource.properties.are.not.set", requiredPropertiesString))
    }

    return CfnResourcePropertiesNode(nameNode, propertyNodes).registerNode(propertiesProperty)
  }

  private fun expression(value: JsonValue): CfnExpressionNode {
    return when (value) {
      is JsonStringLiteral -> CfnStringValueNode(value.value).registerNode(value)
      is JsonBooleanLiteral -> CfnBooleanValueNode(value.value).registerNode(value)
      is JsonNumberLiteral -> CfnNumberValueNode(value.text).registerNode(value)
      is JsonArray -> {
        val items = value.valueList.map { expression(it) }
        CfnArrayValueNode(items).registerNode(value)
      }
      is JsonObject -> {
        if (value.propertyList.size == 1 && CloudFormationIntrinsicFunctions.allNames.contains(value.propertyList.single().name)) {
          val single = value.propertyList.single()
          val nameNode = CfnStringValueNode(single.name).registerNode(single.nameElement)

          val jsonValueNode = single.value
          if (jsonValueNode is JsonArray) {
            val items = jsonValueNode.valueList.map { expression(it) }
            CfnFunctionNode(nameNode, items).registerNode(value)
          } else if (jsonValueNode == null){
            CfnFunctionNode(nameNode, listOf()).registerNode(value)
          } else {
            CfnFunctionNode(nameNode, listOf(expression(jsonValueNode))).registerNode(value)
          }
        } else {
          val properties = value.propertyList.map {
            val nameNode = CfnStringValueNode(it.name).registerNode(it.nameElement)

            val jsonValueNode = it.value
            val valueNode = if (jsonValueNode != null) {
              expression(jsonValueNode)
            } else {
              CfnMissingOrInvalidValueNode()
            }

            Pair(nameNode, valueNode)
          }

          CfnObjectValueNode(properties).registerNode(value)
        }
      }
      else -> {
        addProblem(value, CloudFormationBundle.getString("format.unknown.value", value.javaClass.simpleName))
        CfnMissingOrInvalidValueNode()
      }
    }
  }

  private fun resourceType(typeProperty: JsonProperty): CfnResourceTypeNode {
    val nameNode = CfnStringValueNode(typeProperty.name).registerNode(typeProperty.nameElement)
    val value = checkAndGetUnquotedStringText(typeProperty.value) ?:
        return CfnResourceTypeNode(nameNode, CfnStringValueNode("")).registerNode(typeProperty)

    // TODO Move to inspections
    if (!isCustomResourceType(value) && CloudFormationMetadataProvider.METADATA.findResourceType(value) == null) {
      addProblem(typeProperty, CloudFormationBundle.getString("format.unknown.type", value))
    }

    val valueNode = CfnStringValueNode(value).registerNode(typeProperty.value!!)
    return CfnResourceTypeNode(nameNode, valueNode).registerNode(typeProperty)
  }

  private fun isCustomResourceType(value: String): Boolean {
    return value == CloudFormationConstants.CustomResourceType || value.startsWith(CloudFormationConstants.CustomResourceTypePrefix)
  }

  private fun checkAndGetObject(expression: JsonValue): JsonObject? {
    val obj = expression as? JsonObject
    if (obj == null) {
      addProblem(
          expression,
          CloudFormationBundle.getString("format.expected.json.object"))

      return null
    }

    return obj
  }


  private fun formatVersion(value: JsonValue) {
    val text = checkAndGetQuotedStringText(value) ?: return

    val version = StringUtil.stripQuotesAroundValue(StringUtil.notNullize(text))
    if (!CloudFormationConstants.SupportedTemplateFormatVersions.contains(version)) {
      val supportedVersions = StringUtil.join(CloudFormationConstants.SupportedTemplateFormatVersions, ", ")
      addProblem(value, CloudFormationBundle.getString("format.unknownVersion", supportedVersions))
    }
  }

  private fun checkAndGetQuotedStringText(expression: JsonValue?): String? {
    if (expression == null) {
      // Do not threat value absence as error
      return null
    }

    val literal = expression as? JsonStringLiteral
    if (literal == null) {
      addProblem(expression, CloudFormationBundle.getString("format.expected.quoted.string"))
      return null
    }

    return literal.text
  }

  private fun checkAndGetUnquotedStringText(expression: JsonValue?): String? {
    val quoted = checkAndGetQuotedStringText(expression) ?: return null

    return StringUtil.stripQuotesAroundValue(quoted)
  }

  fun file(psiFile: PsiFile): CfnRootNode {
    assert(CloudFormationPsiUtils.isCloudFormationFile(psiFile)) { psiFile.name + " is not a cfn file" }

    val root = CloudFormationPsiUtils.getRootExpression(psiFile) ?: error("Could not get root expression")
    return root(root)
  }

  companion object {
    private val AlphanumericStringPattern = Pattern.compile("[a-zA-Z0-9]+")

    fun parse(psiFile: PsiFile): CloudFormationParsedFile {
      val parser = CloudFormationParser()
      val rootNode = parser.file(psiFile)

      return CloudFormationParsedFile(parser.myProblems, parser.myNodesMap, rootNode)
    }
  }
}