package com.intellij.aws.cloudformation

import com.google.common.collect.HashBiMap
import com.intellij.aws.cloudformation.model.CfnArrayValueNode
import com.intellij.aws.cloudformation.model.CfnExpressionNode
import com.intellij.aws.cloudformation.model.CfnFirstLevelMappingNode
import com.intellij.aws.cloudformation.model.CfnFunctionNode
import com.intellij.aws.cloudformation.model.CfnMappingValue
import com.intellij.aws.cloudformation.model.CfnMappingsNode
import com.intellij.aws.cloudformation.model.CfnNameValueNode
import com.intellij.aws.cloudformation.model.CfnNamedNode
import com.intellij.aws.cloudformation.model.CfnNode
import com.intellij.aws.cloudformation.model.CfnObjectValueNode
import com.intellij.aws.cloudformation.model.CfnOutputNode
import com.intellij.aws.cloudformation.model.CfnOutputsNode
import com.intellij.aws.cloudformation.model.CfnParameterNode
import com.intellij.aws.cloudformation.model.CfnParametersNode
import com.intellij.aws.cloudformation.model.CfnResourceNode
import com.intellij.aws.cloudformation.model.CfnResourcePropertiesNode
import com.intellij.aws.cloudformation.model.CfnResourcePropertyNode
import com.intellij.aws.cloudformation.model.CfnResourceTypeNode
import com.intellij.aws.cloudformation.model.CfnResourcesNode
import com.intellij.aws.cloudformation.model.CfnRootNode
import com.intellij.aws.cloudformation.model.CfnScalarValueNode
import com.intellij.aws.cloudformation.model.CfnSecondLevelMappingNode
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

class JsonCloudFormationParser private constructor () {
  private val myProblems = ArrayList<CloudFormationProblem>()
  private val myNodesMap = HashBiMap.create<PsiElement, CfnNode>()

  private fun <T : CfnNode> T.registerNode(psiElement: PsiElement): T {
    myNodesMap.put(psiElement, this)
    return this
  }

  private fun addProblem(element: PsiElement, description: String) {
    myProblems.add(CloudFormationProblem(element, description))
  }

  private fun addProblemOnNameElement(property: JsonProperty, description: String) {
    addProblem(
        if (property.firstChild != null) property.firstChild else property,
        description)
  }

  private fun root(root: JsonObject): CfnRootNode {
    val sections = root.propertyList.mapNotNull { property ->
      val name = property.name
      val value = property.value

      if (name.isEmpty() || value == null) {
        return@mapNotNull null
      }

      val section = CloudFormationSection.id2enum[name]

      return@mapNotNull when (section) {
        CloudFormationSection.FormatVersion -> { formatVersion(value); null }
        CloudFormationSection.Transform -> { checkAndGetUnquotedStringText(value); null }
        CloudFormationSection.Description -> { checkAndGetUnquotedStringText(value); null }
        CloudFormationSection.Parameters -> parameters(property)
        CloudFormationSection.Resources -> resources(property)
        CloudFormationSection.Conditions -> {
          // TODO
          null
        }
        CloudFormationSection.Metadata -> {
          // Generic content inside, no need to check
          // See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/metadata-section-structure.html
          null
        }
        CloudFormationSection.Outputs -> outputs(property)
        CloudFormationSection.Mappings -> mappings(property)
        else -> {
          addProblemOnNameElement(
              property,
              CloudFormationBundle.getString("format.unknown.section", name))
          null
        }
      }
    }

    // Duplicate keys should be handled by YAML support,
    // TODO known issue: https://youtrack.jetbrains.com/issue/RUBY-19094
    return CfnRootNode(
        lookupSection<CfnParametersNode>(sections),
        lookupSection<CfnResourcesNode>(sections),
        lookupSection<CfnOutputsNode>(sections),
        lookupSection<CfnMappingsNode>(sections)
    ).registerNode(root)
  }

  private fun <ResultNodeType : CfnNode, ValueNodeType: CfnNode> parseNameValues(
      property: JsonProperty,
      valueFactory: (JsonProperty) -> ValueNodeType,
      resultFactory: (CfnScalarValueNode?, List<ValueNodeType>) -> ResultNodeType): ResultNodeType
  {
    val nameNode = keyName(property)

    val obj = checkAndGetObject(property.value!!) ?: return resultFactory(nameNode, emptyList()).registerNode(property)

    val list = obj.propertyList.mapNotNull { value ->
      if (value.name.isEmpty()) {
        addProblemOnNameElement(value, "A non-empty key is expected")
        return@mapNotNull null
      }

      if (value.value == null) {
        addProblemOnNameElement(value, "A value is expected")
        return@mapNotNull null
      }

      return@mapNotNull valueFactory(value)
    }

    return resultFactory(nameNode, list).registerNode(property)
  }

  private fun outputs(outputs: JsonProperty): CfnOutputsNode = parseNameValues(
      outputs,
      { output -> CfnOutputNode(keyName(output), expression(output.value!!)) },
      { nameNode, list -> CfnOutputsNode(nameNode, list) }
  )

  private fun parameters(parameters: JsonProperty): CfnParametersNode = parseNameValues(
      parameters,
      { parameter -> parameter(parameter) },
      { nameNode, list -> CfnParametersNode(nameNode, list) }
  )

  private fun parameter(parameter: JsonProperty): CfnParameterNode = parseNameValues(
      parameter,
      { node -> CfnNameValueNode(keyName(node), node.value?.let { expression(it) }) },
      { nameNode, list -> CfnParameterNode(nameNode, list) }
  )

  private fun mappings(mappings: JsonProperty): CfnMappingsNode = parseNameValues(
      mappings,
      { mapping -> firstLevelMapping(mapping) },
      { nameNode, list -> CfnMappingsNode(nameNode, list) }
  )

  private fun firstLevelMapping(mapping: JsonProperty): CfnFirstLevelMappingNode = parseNameValues(
      mapping,
      { mapping -> secondLevelMapping(mapping) },
      { nameNode, list -> CfnFirstLevelMappingNode(nameNode, list) }
  )

  private fun secondLevelMapping(mapping: JsonProperty): CfnSecondLevelMappingNode = parseNameValues(
      mapping,
      { node -> CfnMappingValue(keyName(node), checkAndGetStringElement(node.value)) },
      { nameNode, list -> CfnSecondLevelMappingNode(nameNode, list) }
  )

  private fun keyName(property: JsonProperty): CfnScalarValueNode {
    return CfnScalarValueNode(property.name).registerNode(property.nameElement)
  }

  private fun resources(property: JsonProperty): CfnResourcesNode? {
    val nameNode = CfnScalarValueNode(property.name).registerNode(property.nameElement)
    val obj = checkAndGetObject(property.value!!) ?: return CfnResourcesNode(nameNode, emptyList()).registerNode(property)

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
      typeNode = null
    }

    val properties: CfnResourcePropertiesNode?

    val propertiesProperty = obj.findProperty(CloudFormationConstants.PropertiesPropertyName)
    if (propertiesProperty != null && propertiesProperty.value is JsonObject) {
      properties = resourceProperties(propertiesProperty)
      topLevelProperties.put(propertiesProperty.name, properties)
    } else {
      if (propertiesProperty != null && propertiesProperty.value !is JsonObject) {
        addProblemOnNameElement(propertiesProperty, CloudFormationBundle.getString("format.properties.property.should.properties.list"))
      }

      properties = null
    }

    for (property in obj.propertyList) {
      val propertyName = property.name

      if (!CloudFormationConstants.AllTopLevelResourceProperties.contains(propertyName)) {
        addProblemOnNameElement(property, CloudFormationBundle.getString("format.unknown.resource.property", propertyName))
      }

      if (!topLevelProperties.containsKey(propertyName)) {
        val nameNode = CfnScalarValueNode(propertyName).registerNode(property.nameElement)

        val valueElement = property.value
        val valueNode = if (valueElement != null) {
          CfnScalarValueNode(propertyName).registerNode(valueElement)
        } else {
          CfnScalarValueNode("")
        }

        topLevelProperties.put(propertyName, CfnNameValueNode(nameNode, valueNode).registerNode(property))
      }
    }

    return CfnResourceNode(key, typeNode, topLevelProperties, properties).registerNode(resourceProperty)
  }

  private fun resourceProperties(propertiesProperty: JsonProperty): CfnResourcePropertiesNode {
    val nameNode = CfnScalarValueNode(propertiesProperty.name).registerNode(propertiesProperty.nameElement)
    val properties = propertiesProperty.value as JsonObject

    val propertyNodes = properties.propertyList.mapNotNull { property ->
      val propertyName = property.name
      // TODO make a node?
      if (propertyName == CloudFormationConstants.CommentResourcePropertyName) {
        return@mapNotNull null
      }

      val propertyNameNode = keyName(property)

      val jsonValueNode = property.value
      val valueNode = if (jsonValueNode == null) null else {
        expression(jsonValueNode)
      }

      return@mapNotNull CfnResourcePropertyNode(propertyNameNode, valueNode).registerNode(property)
    }

    return CfnResourcePropertiesNode(nameNode, propertyNodes).registerNode(propertiesProperty)
  }

  private fun expression(value: JsonValue): CfnExpressionNode? {
    return when (value) {
      is JsonStringLiteral -> CfnScalarValueNode(value.value).registerNode(value)
      is JsonBooleanLiteral -> CfnScalarValueNode(value.text).registerNode(value)
      is JsonNumberLiteral -> CfnScalarValueNode(value.text).registerNode(value)
      is JsonArray -> {
        val items = value.valueList.mapNotNull { expression(it) }
        CfnArrayValueNode(items).registerNode(value)
      }
      is JsonObject -> {
        if (value.propertyList.size == 1 && CloudFormationIntrinsicFunction.fullNames.contains(value.propertyList.single().name)) {
          val single = value.propertyList.single()
          val nameNode = CfnScalarValueNode(single.name).registerNode(single.nameElement)
          val functionId = CloudFormationIntrinsicFunction.fullNames[single.name]!!

          val jsonValueNode = single.value
          if (jsonValueNode is JsonArray) {
            val items = jsonValueNode.valueList.map { expression(it) }
            CfnFunctionNode(nameNode, functionId, items).registerNode(value)
          } else if (jsonValueNode == null){
            CfnFunctionNode(nameNode, functionId, listOf()).registerNode(value)
          } else {
            CfnFunctionNode(nameNode, functionId, listOf(expression(jsonValueNode))).registerNode(value)
          }
        } else {
          val properties = value.propertyList.map {
            val nameNode = CfnScalarValueNode(it.name).registerNode(it.nameElement)

            val jsonValueNode = it.value
            val valueNode = if (jsonValueNode == null) null else {
              expression(jsonValueNode)
            }

            Pair(nameNode, valueNode)
          }

          CfnObjectValueNode(properties).registerNode(value)
        }
      }
      else -> {
        addProblem(value, CloudFormationBundle.getString("format.unknown.value", value.javaClass.simpleName))
        return null
      }
    }
  }

  private fun resourceType(typeProperty: JsonProperty): CfnResourceTypeNode {
    val nameNode = CfnScalarValueNode(typeProperty.name).registerNode(typeProperty.nameElement)
    val value = checkAndGetUnquotedStringText(typeProperty.value) ?:
        return CfnResourceTypeNode(nameNode, CfnScalarValueNode("")).registerNode(typeProperty)

    val valueNode = CfnScalarValueNode(value).registerNode(typeProperty.value!!)
    return CfnResourceTypeNode(nameNode, valueNode).registerNode(typeProperty)
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
    val version = checkAndGetUnquotedStringText(value) ?: return

    if (!CloudFormationConstants.SupportedTemplateFormatVersions.contains(version)) {
      val supportedVersions = StringUtil.join(CloudFormationConstants.SupportedTemplateFormatVersions, ", ")
      addProblem(value, CloudFormationBundle.getString("format.unknownVersion", supportedVersions))
    }
  }

  private fun checkAndGetScalarNode(expression: JsonValue?): JsonStringLiteral? {
    if (expression == null) {
      // Do not threat value absence as error
      return null
    }

    val literal = expression as? JsonStringLiteral
    if (literal == null) {
      addProblem(expression, CloudFormationBundle.getString("format.expected.quoted.string"))
      return null
    }

    return literal
  }

  private fun checkAndGetStringElement(expression: JsonValue?): CfnScalarValueNode? {
    val scalar = checkAndGetScalarNode(expression) ?: return null
    return CfnScalarValueNode(scalar.value).registerNode(scalar)
  }

  private fun checkAndGetUnquotedStringText(expression: JsonValue?): String? {
    val literal = checkAndGetScalarNode(expression)
    return literal?.value
  }

  fun file(psiFile: PsiFile): CfnRootNode {
    assert(CloudFormationPsiUtils.isCloudFormationFile(psiFile)) { psiFile.name + " is not a cfn file" }

    val root = CloudFormationPsiUtils.getRootExpression(psiFile) ?: error("Could not get root expression")
    return root(root)
  }

  companion object {
    fun parse(psiFile: PsiFile): CloudFormationParsedFile {
      val parser = JsonCloudFormationParser()
      val rootNode = parser.file(psiFile)

      return CloudFormationParsedFile(parser.myProblems, parser.myNodesMap, rootNode)
    }
  }
}