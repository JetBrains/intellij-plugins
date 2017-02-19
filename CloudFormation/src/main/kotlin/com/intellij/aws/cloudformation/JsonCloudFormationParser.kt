package com.intellij.aws.cloudformation

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import com.intellij.aws.cloudformation.model.CfnArrayValueNode
import com.intellij.aws.cloudformation.model.CfnConditionNode
import com.intellij.aws.cloudformation.model.CfnConditionsNode
import com.intellij.aws.cloudformation.model.CfnExpressionNode
import com.intellij.aws.cloudformation.model.CfnFirstLevelMappingNode
import com.intellij.aws.cloudformation.model.CfnFunctionNode
import com.intellij.aws.cloudformation.model.CfnMappingValue
import com.intellij.aws.cloudformation.model.CfnMappingsNode
import com.intellij.aws.cloudformation.model.CfnMetadataNode
import com.intellij.aws.cloudformation.model.CfnNameValueNode
import com.intellij.aws.cloudformation.model.CfnNamedNode
import com.intellij.aws.cloudformation.model.CfnNode
import com.intellij.aws.cloudformation.model.CfnObjectValueNode
import com.intellij.aws.cloudformation.model.CfnOutputNode
import com.intellij.aws.cloudformation.model.CfnOutputsNode
import com.intellij.aws.cloudformation.model.CfnParameterNode
import com.intellij.aws.cloudformation.model.CfnParametersNode
import com.intellij.aws.cloudformation.model.CfnResourceConditionNode
import com.intellij.aws.cloudformation.model.CfnResourceDependsOnNode
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
import com.intellij.json.psi.JsonReferenceExpression
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.json.psi.JsonValue
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import java.util.ArrayList

class JsonCloudFormationParser private constructor () {
  private val myProblems = ArrayList<CloudFormationProblem>()
  private val node2psi = mutableMapOf<CfnNode, PsiElement>()
  private val psi2node: Multimap<PsiElement, CfnNode> = ArrayListMultimap.create()

  private fun <T : CfnNode> T.registerNode(psiElement: PsiElement): T {
    assert(!psi2node.containsKey(psiElement)) { "Psi Elements map already has $psiElement" } // No known exceptions in JSON
    assert(!node2psi.containsKey(this)) { "Nodes map already has $psiElement" }

    psi2node.put(psiElement, this)
    node2psi.put(this, psiElement)

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
        CloudFormationSection.Conditions -> conditions(property)
        CloudFormationSection.Metadata -> metadata(property)
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

    return CfnRootNode(
        lookupSection<CfnMetadataNode>(sections),
        lookupSection<CfnParametersNode>(sections),
        lookupSection<CfnMappingsNode>(sections),
        lookupSection<CfnConditionsNode>(sections),
        lookupSection<CfnResourcesNode>(sections),
        lookupSection<CfnOutputsNode>(sections)
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

  private fun metadata(metadata: JsonProperty): CfnMetadataNode {
    val valueNode = checkAndGetObject(metadata.value)
    return CfnMetadataNode(keyName(metadata), valueNode?.let { expression(valueNode, AllowFunctions.False) } as? CfnObjectValueNode).registerNode(metadata)
  }

  private fun conditions(conditions: JsonProperty): CfnConditionsNode = parseNameValues(
      conditions,
      { node -> CfnConditionNode(keyName(node), expression(node.value!!, AllowFunctions.True)).registerNode(node) },
      { nameNode, list -> CfnConditionsNode(nameNode, list) }
  )

  private fun outputs(outputs: JsonProperty): CfnOutputsNode = parseNameValues(
      outputs,
      { output -> CfnOutputNode(keyName(output), expression(output.value!!, AllowFunctions.True)).registerNode(output) },
      { nameNode, list -> CfnOutputsNode(nameNode, list) }
  )

  private fun parameters(parameters: JsonProperty): CfnParametersNode = parseNameValues(
      parameters,
      { parameter -> parameter(parameter) },
      { nameNode, list -> CfnParametersNode(nameNode, list) }
  )

  private fun parameter(parameter: JsonProperty): CfnParameterNode = parseNameValues(
      parameter,
      { node -> CfnNameValueNode(keyName(node), node.value?.let { expression(it, AllowFunctions.False) }).registerNode(node) },
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
      { node -> CfnMappingValue(keyName(node), checkAndGetStringElement(node.value)).registerNode(node) },
      { nameNode, list -> CfnSecondLevelMappingNode(nameNode, list) }
  )

  private fun keyName(property: JsonProperty): CfnScalarValueNode {
    return CfnScalarValueNode(property.name).registerNode(property.nameElement)
  }

  private fun resources(resources: JsonProperty): CfnResourcesNode = parseNameValues(
      resources,
      { resource -> resource(resource) },
      { nameNode, list -> CfnResourcesNode(nameNode, list) }
  )

  private fun resource(resourceProperty: JsonProperty): CfnResourceNode {
    val key = keyName(resourceProperty)

    val obj = checkAndGetObject(resourceProperty.value) ?:
        return CfnResourceNode(key, null, null, null, null, emptyMap()).registerNode(resourceProperty)

    val topLevelProperties: MutableMap<String, CfnNamedNode> = hashMapOf()

    for (property in obj.propertyList) {
      val propertyName = property.name

      if (!CloudFormationConstants.AllTopLevelResourceProperties.contains(propertyName)) {
        addProblemOnNameElement(property, CloudFormationBundle.getString("format.unknown.resource.property", propertyName))
      }

      val node = when (propertyName) {
        CloudFormationConstants.DependsOnPropertyName -> resourceDependsOn(property)
        CloudFormationConstants.TypePropertyName -> resourceType(property)
        CloudFormationConstants.ConditionPropertyName -> resourceCondition(property)
        CloudFormationConstants.PropertiesPropertyName -> resourceProperties(property)
        else -> {
          CfnNameValueNode(keyName(property), property.value?.let { expression(it, AllowFunctions.True) }).registerNode(property)
        }
      }

      topLevelProperties.put(propertyName, node)
    }

    val properties: Collection<CfnNode> = topLevelProperties.values
    return CfnResourceNode(
        key,
        lookupSection<CfnResourceTypeNode>(properties),
        lookupSection<CfnResourcePropertiesNode>(properties),
        lookupSection<CfnResourceConditionNode>(properties),
        lookupSection<CfnResourceDependsOnNode>(properties),
        topLevelProperties
    ).registerNode(resourceProperty)
  }

  private fun resourceDependsOn(property: JsonProperty): CfnResourceDependsOnNode {
    val value = property.value
    val valuesNodes = when(value) {
      null -> emptyList()
      is JsonArray -> value.valueList.mapNotNull { checkAndGetStringElement(it) }
      is JsonStringLiteral -> listOf(CfnScalarValueNode(value.value).registerNode(value))
      else -> {
        addProblemOnNameElement(property, "Expected a string or an array of strings")
        emptyList()
      }
    }

    return CfnResourceDependsOnNode(keyName(property), valuesNodes).registerNode(property)
  }

  private fun resourceCondition(property: JsonProperty): CfnResourceConditionNode =
      CfnResourceConditionNode(keyName(property), checkAndGetStringElement(property.value)).registerNode(property)

  private fun resourceProperties(propertiesProperty: JsonProperty): CfnResourcePropertiesNode {
    val value = checkAndGetObject(propertiesProperty.value)
        ?: return CfnResourcePropertiesNode(keyName(propertiesProperty), emptyList()).registerNode(propertiesProperty)

    val propertyNodes = value.propertyList.mapNotNull { property ->
      // TODO make a node?
      if (property.name == CloudFormationConstants.CommentResourcePropertyName) {
        return@mapNotNull null
      }

      return@mapNotNull CfnResourcePropertyNode(keyName(property), property.value?.let { expression(it, AllowFunctions.True) }).registerNode(property)
    }

    return CfnResourcePropertiesNode(keyName(propertiesProperty), propertyNodes).registerNode(propertiesProperty)
  }

  enum class AllowFunctions {
    True,
    False
  }

  private fun expression(value: JsonValue, allowFunctions: AllowFunctions): CfnExpressionNode? {
    return when (value) {
      is JsonStringLiteral -> CfnScalarValueNode(value.value).registerNode(value)
      is JsonBooleanLiteral -> CfnScalarValueNode(value.text).registerNode(value)
      is JsonNumberLiteral -> CfnScalarValueNode(value.text).registerNode(value)
      is JsonArray -> {
        val items = value.valueList.mapNotNull { expression(it, allowFunctions) }
        CfnArrayValueNode(items).registerNode(value)
      }
      is JsonReferenceExpression -> {
        addProblem(value, "Expected an expression")
        null
      }
      is JsonObject -> {
        if (allowFunctions == AllowFunctions.True &&
            value.propertyList.size == 1 &&
            CloudFormationIntrinsicFunction.fullNames.contains(value.propertyList.single().name)) {
          val single = value.propertyList.single()
          val nameNode = CfnScalarValueNode(single.name).registerNode(single.nameElement)
          val functionId = CloudFormationIntrinsicFunction.fullNames[single.name]!!

          val jsonValueNode = single.value
          if (jsonValueNode is JsonArray) {
            val items = jsonValueNode.valueList.map { expression(it, allowFunctions) }
            CfnFunctionNode(nameNode, functionId, items).registerNode(value)
          } else if (jsonValueNode == null){
            CfnFunctionNode(nameNode, functionId, listOf()).registerNode(value)
          } else {
            CfnFunctionNode(nameNode, functionId, listOf(expression(jsonValueNode, allowFunctions))).registerNode(value)
          }
        } else {
          val properties = value.propertyList.map {
            val nameNode = CfnScalarValueNode(it.name).registerNode(it.nameElement)

            val jsonValueNode = it.value
            val valueNode = if (jsonValueNode == null) null else {
              expression(jsonValueNode, allowFunctions)
            }

            CfnNameValueNode(nameNode, valueNode).registerNode(it)
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
    return CfnResourceTypeNode(
        keyName(typeProperty),
        checkAndGetStringElement(typeProperty.value)
    ).registerNode(typeProperty)
  }

  private fun checkAndGetObject(expression: JsonValue?): JsonObject? {
    if (expression == null) return null

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

      return CloudFormationParsedFile(parser.myProblems, parser.node2psi, parser.psi2node, rootNode, psiFile, psiFile.modificationStamp)
    }
  }
}
