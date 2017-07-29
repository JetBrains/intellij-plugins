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
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLScalar
import org.jetbrains.yaml.psi.YAMLSequence
import org.jetbrains.yaml.psi.YAMLValue
import org.jetbrains.yaml.psi.impl.YAMLBlockMappingImpl
import org.jetbrains.yaml.psi.impl.YAMLCompoundValueImpl
import java.util.ArrayList
import java.util.Optional

class YamlCloudFormationParser private constructor () {
  private val myProblems = ArrayList<CloudFormationProblem>()
  private val node2psi = mutableMapOf<CfnNode, PsiElement>()
  private val psi2node: Multimap<PsiElement, CfnNode> = ArrayListMultimap.create()

  private fun <T : CfnNode> T.registerNode(psiElement: PsiElement): T {
    if (psi2node.containsKey(psiElement)) {
      if (psi2node.get(psiElement).singleOrNull() is CfnScalarValueNode && this is CfnFunctionNode) {
        // known exception: !Ref "xxx" or !Sub "xxx"
      } else if (psi2node.get(psiElement).singleOrNull() is CfnScalarValueNode && this is CfnNamedNode) {
        // Properties:
        //   Ty <-- here, it'll be both CfnResourcePropertyNode and CfnScalarValueNode
        //   A: B
      } else {
        error("Psi Elements map already has $psiElement")
      }
    }

    assert(!node2psi.containsKey(this)) { "Nodes map already has $psiElement" }

    psi2node.put(psiElement, this)
    node2psi.put(this, psiElement)

    return this
  }

  private fun addProblem(element: PsiElement, description: String) {
    myProblems.add(CloudFormationProblem(element, description))
  }

  private fun addProblemOnNameElement(element: PsiElement, description: String) {
    val keyValue = element as? YAMLKeyValue
    addProblem(keyValue?.key ?: element, description)
  }

  private fun root(root: YAMLMapping): CfnRootNode {
    val sections = root.cfnKeyValues().mapNotNull { property ->
      val name = property.keyText
      val value = property.value

      if (name.isEmpty() || value == null) {
        return@mapNotNull null
      }

      val section = CloudFormationSection.id2enum[name]

      return@mapNotNull when (section) {
        CloudFormationSection.FormatVersion -> { formatVersion(value); null }
        CloudFormationSection.Transform -> { checkAndGetStringValue(value); null }
        CloudFormationSection.Description -> { checkAndGetStringValue(value); null }
        CloudFormationSection.Parameters -> parameters(property)
        CloudFormationSection.Resources -> resources(property)
        CloudFormationSection.Conditions -> conditions(property)
        CloudFormationSection.Metadata -> metadata(property)
        CloudFormationSection.Outputs -> outputs(property)
        CloudFormationSection.Mappings -> mappings(property)
        else -> {
          addProblemOnNameElement(
              property.owner,
              CloudFormationBundle.getString("format.unknown.section", name))
          null
        }
      }
    }

    // Duplicate keys should be handled by YAML support,
    // TODO known issue: https://youtrack.jetbrains.com/issue/RUBY-19094
    return CfnRootNode(
        lookupSection<CfnMetadataNode>(sections),
        lookupSection<CfnParametersNode>(sections),
        lookupSection<CfnMappingsNode>(sections),
        lookupSection<CfnConditionsNode>(sections),
        lookupSection<CfnResourcesNode>(sections),
        lookupSection<CfnOutputsNode>(sections)
    ).registerNode(root)
  }

  private fun metadata(metadata: CfnKeyValue): CfnMetadataNode {
    val mapping = checkAndGetMapping(metadata.value!!)
    return CfnMetadataNode(keyName(metadata), mapping?.let { expression(it, AllowFunctions.False) } as? CfnObjectValueNode).registerNode(metadata.owner)
  }

  private fun conditions(conditions: CfnKeyValue): CfnConditionsNode = parseNameValues(
      conditions,
      { node -> CfnConditionNode(keyName(node), node.value?.let { expression(it, AllowFunctions.True) }).registerNode(node.owner) },
      { nameNode, list -> CfnConditionsNode(nameNode, list) }
  )

  private fun outputs(outputs: CfnKeyValue): CfnOutputsNode = parseNameValues(
      outputs,
      { output -> CfnOutputNode(keyName(output), output.value?.let { expression(it, AllowFunctions.True) }).registerNode(output.owner) },
      { nameNode, list -> CfnOutputsNode(nameNode, list) }
  )

  private fun parameters(parameters: CfnKeyValue): CfnParametersNode = parseNameValues(
      parameters,
      { parameter -> parameter(parameter) },
      { nameNode, list -> CfnParametersNode(nameNode, list) }
  )

  private fun parameter(parameter: CfnKeyValue): CfnParameterNode = parseNameValues(
      parameter,
      { node -> CfnNameValueNode(keyName(node), node.value?.let { expression(it, AllowFunctions.False) }).registerNode(node.owner) },
      { nameNode, list -> CfnParameterNode(nameNode, list) }
  )

  private fun <ResultNodeType : CfnNode, ValueNodeType: CfnNode> parseNameValues(
      keyValueElement: CfnKeyValue,
      valueFactory: (CfnKeyValue) -> ValueNodeType,
      resultFactory: (CfnScalarValueNode?, List<ValueNodeType>) -> ResultNodeType): ResultNodeType
  {
    val keyElement = keyValueElement.key
    val nameNode = if (keyElement == null) null else CfnScalarValueNode(keyValueElement.keyText).registerNode(keyElement)

    val obj = checkAndGetMapping(keyValueElement.value)

    val list = obj?.cfnKeyValues()?.mapNotNull { keyValue ->
      if (keyValue.keyText.isEmpty()) {
        addProblemOnNameElement(keyValue.owner, "A non-empty key is expected")
        return@mapNotNull null
      }

      return@mapNotNull valueFactory(keyValue)
    } ?: emptyList()

    return resultFactory(nameNode, list).registerNode(keyValueElement.owner)
  }

  private fun mappings(mappings: CfnKeyValue): CfnMappingsNode = parseNameValues(
      mappings,
      { mapping -> firstLevelMapping(mapping) },
      { nameNode, list -> CfnMappingsNode(nameNode, list) }
  )

  private fun firstLevelMapping(mapping: CfnKeyValue): CfnFirstLevelMappingNode = parseNameValues(
      mapping,
      { secondLevel -> secondLevelMapping(secondLevel) },
      { nameNode, list -> CfnFirstLevelMappingNode(nameNode, list) }
  )

  private fun secondLevelMapping(mapping: CfnKeyValue): CfnSecondLevelMappingNode = parseNameValues(
      mapping,
      { node -> CfnMappingValue(keyName(node), node.value?.let { expression(it, AllowFunctions.False) }).registerNode(node.owner) },
      { nameNode, list -> CfnSecondLevelMappingNode(nameNode, list) }
  )

  private fun keyName(property: CfnKeyValue): CfnScalarValueNode? {
    if (property.key != null) {
      return CfnScalarValueNode(property.keyText).registerNode(property.key)
    } else {
      addProblem(property.owner, "Expected a name")
      return CfnScalarValueNode("").registerNode(property.owner)
    }
  }

  private fun resources(resources: CfnKeyValue): CfnResourcesNode = parseNameValues(
      resources,
      { resource -> resource(resource) },
      { nameNode, list -> CfnResourcesNode(nameNode, list) }
  )

  private fun resource(resourceProperty: CfnKeyValue): CfnResourceNode {
    val key = keyName(resourceProperty)

    val mapping = checkAndGetMapping(resourceProperty.value) ?:
        return CfnResourceNode(key, null, null, null, null, emptyMap()).registerNode(resourceProperty.owner)

    val topLevelProperties: MutableMap<String, CfnNamedNode> = hashMapOf()

    for (property in mapping.cfnKeyValues()) {
      val propertyKey = property.keyText.trim()

      if (!CloudFormationConstants.AllTopLevelResourceProperties.contains(propertyKey)) {
        addProblemOnNameElement(property.owner, CloudFormationBundle.getString("format.unknown.resource.property", propertyKey))
      }

      val node = when (propertyKey) {
        CloudFormationConstants.DependsOnPropertyName -> resourceDependsOn(property)
        CloudFormationConstants.TypePropertyName -> resourceType(property)
        CloudFormationConstants.ConditionPropertyName -> resourceCondition(property)
        CloudFormationConstants.PropertiesPropertyName -> resourceProperties(property)
        else -> {
          CfnNameValueNode(keyName(property), property.value?.let { expression(it, AllowFunctions.True) }).registerNode(property.owner)
        }
      }

      topLevelProperties.put(propertyKey, node)
    }

    val properties: Collection<CfnNode> = topLevelProperties.values
    return CfnResourceNode(
        key,
        lookupSection<CfnResourceTypeNode>(properties),
        lookupSection<CfnResourcePropertiesNode>(properties),
        lookupSection<CfnResourceConditionNode>(properties),
        lookupSection<CfnResourceDependsOnNode>(properties),
        topLevelProperties
    ).registerNode(resourceProperty.owner)
  }

  private fun resourceDependsOn(property: CfnKeyValue): CfnResourceDependsOnNode {
    val value = property.value
    val valuesNodes = when(value) {
      null -> emptyList()
      is YAMLSequence -> value.items.mapNotNull { checkAndGetStringElement(it.value) }
      is YAMLScalar -> checkAndGetStringElement(value)?.let { listOf(it) } ?: emptyList()
      else -> {
        addProblemOnNameElement(property.owner, "Expected a string or an array of strings")
        emptyList()
      }
    }

    return CfnResourceDependsOnNode(keyName(property), valuesNodes).registerNode(property.owner)
  }

  private class CfnKeyValue(
      val owner: PsiElement,
      val key: PsiElement?,
      val keyText: String,
      val value: YAMLValue?
  )

  private fun YAMLMapping.cfnKeyValues(): List<CfnKeyValue> {
    val fromKeyValues = keyValues.filterNotNull().map { CfnKeyValue(it, it.key, it.keyText, it.value) }

    // Handle special case when value is absent, this helps code completion
    // Example:
    // Properties:
    //   Ty<caret>
    //   A: B
    // Here "Ty" won't be a part of keyValues so we add it manually
    val fromStandaloneValues = if (this is YAMLBlockMappingImpl) {
      children.mapNotNull { it as? YAMLScalar }.map { CfnKeyValue(it, it, it.textValue, null) }
    } else {
      emptyList()
    }

    return fromKeyValues + fromStandaloneValues
  }

  private fun resourceCondition(property: CfnKeyValue): CfnResourceConditionNode =
      CfnResourceConditionNode(
          keyName(property),
          checkAndGetStringElement(property.value)).registerNode(property.owner)

  private fun resourceProperties(propertiesProperty: CfnKeyValue): CfnResourcePropertiesNode {
    val nameNode = keyName(propertiesProperty)
    val properties = propertiesProperty.value as? YAMLMapping

    val propertyNodes = (properties?.cfnKeyValues() ?: emptyList()).mapNotNull { property ->
      val yamlValueNode = property.value
      val valueNode = if (yamlValueNode == null) null else {
        expression(yamlValueNode, AllowFunctions.True)
      }

      return@mapNotNull CfnResourcePropertyNode(keyName(property), valueNode).registerNode(property.owner)
    }

    return CfnResourcePropertiesNode(nameNode, propertyNodes).registerNode(propertiesProperty.owner)
  }

  enum class AllowFunctions {
    True,
    False
  }

  private fun longFunction(value: YAMLMapping): CfnFunctionNode? {
    val cfnKeyValue = value.cfnKeyValues()

    if (cfnKeyValue.size != 1 || !CloudFormationIntrinsicFunction.fullNames.containsKey(cfnKeyValue.single().keyText)) {
      return null
    }

    val single = cfnKeyValue.single()
    val nameNode = keyName(single)!!
    val functionId = CloudFormationIntrinsicFunction.fullNames[single.keyText]!!

    val yamlValueNode = single.value
    if (yamlValueNode is YAMLSequence) {
      val shortFunctionCall = shortFunction(yamlValueNode) ?: return null
      if (shortFunctionCall.isPresent) {
        return CfnFunctionNode(nameNode, functionId, listOf(shortFunctionCall.get())).registerNode(value)
      } else {
        val items = yamlValueNode.items.mapNotNull {
          val itemValue = it.value
          if (itemValue != null) expression(itemValue, AllowFunctions.True) else null
        }

        return CfnFunctionNode(nameNode, functionId, items).registerNode(value)
      }
    } else if (yamlValueNode == null) {
      return CfnFunctionNode(nameNode, functionId, listOf()).registerNode(value)
    } else {
      return CfnFunctionNode(nameNode, functionId, listOf(expression(yamlValueNode, AllowFunctions.True))).registerNode(value)
    }
  }

  private fun shortFunction(value: YAMLValue): Optional<CfnFunctionNode>? {
    val tag = value.getFirstTag() ?: return Optional.empty()

    val functionName = tag.text.trimStart('!')
    val functionId = CloudFormationIntrinsicFunction.shortNames[functionName]

    if (functionId == null) {
      addProblem(tag, "Unknown CloudFormation function: $functionName")
      return null
    }

    val tagNode = CfnScalarValueNode(functionName).registerNode(tag)

    return when {
      value is YAMLScalar -> {
        val parameterNode = CfnScalarValueNode(value.textValue).registerNode(value)
        return CfnFunctionNode(tagNode, functionId, listOf(parameterNode)).registerNode(value).toOptionalValue()
      }

      value.javaClass == YAMLCompoundValueImpl::class.java -> {
        addProblem(value, "Too many values")

        val parameterNode = CfnScalarValueNode((value as YAMLCompoundValueImpl).textValue).registerNode(value)
        return CfnFunctionNode(tagNode, functionId, listOf(parameterNode)).registerNode(value).toOptionalValue()
      }

      value is YAMLSequence -> {
        val items = value.items.mapNotNull {
          val itemValue = it.value
          if (itemValue != null) expression(itemValue, AllowFunctions.True) else null
        }
        return CfnFunctionNode(tagNode, functionId, items).registerNode(value).toOptionalValue()
      }

      value is YAMLMapping -> {
        val nestedLongFunctionCall = longFunction(value)
        if (nestedLongFunctionCall != null) {
          // Only exception for having a mapping here
          return CfnFunctionNode(tagNode, functionId, listOf(nestedLongFunctionCall)).toOptionalValue()
        } else {
          addProblem(tag, "CloudFormation function expects a scalar value or a sequence")
          null
        }
      }

      else -> {
        addProblem(value, CloudFormationBundle.getString("format.unknown.value", value.javaClass.simpleName))
        null
      }
    }
  }

  private fun expression(value: YAMLValue, allowFunctions: AllowFunctions): CfnExpressionNode? {
    val shortFunctionCall = (if (allowFunctions == AllowFunctions.True) shortFunction(value) else Optional.empty()) ?: return null
    if (shortFunctionCall.isPresent) {
      return shortFunctionCall.get()
    }

    return when {
      value is YAMLScalar -> CfnScalarValueNode(value.textValue).registerNode(value)
      value.javaClass == YAMLCompoundValueImpl::class.java -> {
        addProblem(value, "Too many values")
        CfnScalarValueNode((value as YAMLCompoundValueImpl).textValue).registerNode(value)
      }
      value is YAMLSequence -> {
        val items = value.items.mapNotNull {
          val itemValue = it.value
          if (itemValue != null) expression(itemValue, allowFunctions) else null
        }
        CfnArrayValueNode(items).registerNode(value)
      }
      value is YAMLMapping -> {
        val longFunction = if (allowFunctions == AllowFunctions.True) longFunction(value) else null
        if (longFunction != null) {
          longFunction
        } else {
          val properties = value.cfnKeyValues().map {
            val nameNode = keyName(it)

            val yamlValueNode = it.value
            val valueNode = if (yamlValueNode == null) null else {
              expression(yamlValueNode, allowFunctions)
            }

            CfnNameValueNode(nameNode, valueNode).registerNode(it.owner)
          }

          CfnObjectValueNode(properties).registerNode(value)
        }
      }
      else -> {
        addProblem(value, CloudFormationBundle.getString("format.unknown.value", value.javaClass.simpleName))
        null
      }
    }
  }

  private fun resourceType(typeProperty: CfnKeyValue): CfnResourceTypeNode {
    val nameNode = keyName(typeProperty)
    val value = checkAndGetStringValue(typeProperty.value) ?:
        return CfnResourceTypeNode(nameNode, null).registerNode(typeProperty.owner)

    val valueNode = CfnScalarValueNode(value).registerNode(typeProperty.value!!)
    return CfnResourceTypeNode(nameNode, valueNode).registerNode(typeProperty.owner)
  }

  private fun checkAndGetMapping(expression: YAMLValue?): YAMLMapping? {
    if (expression == null) return null

    val obj = expression as? YAMLMapping
    if (obj == null) {
      addProblem(expression, "Expected YAML mapping")
      return null
    }

    return obj
  }

  private fun formatVersion(value: YAMLValue) {
    val version = checkAndGetStringValue(value) ?: return

    if (!CloudFormationConstants.SupportedTemplateFormatVersions.contains(version)) {
      val supportedVersions = StringUtil.join(CloudFormationConstants.SupportedTemplateFormatVersions, ", ")
      addProblem(value, CloudFormationBundle.getString("format.unknownVersion", supportedVersions))
    }
  }

  private fun checkAndGetScalarNode(expression: YAMLValue?): YAMLScalar? {
    if (expression == null) {
      // Do not threat value absence as error
      return null
    }

    val scalar = expression as? YAMLScalar
    if (scalar == null) {
      addProblem(expression, "A string literal is expected")
      return null
    }

    val tag = scalar.tag
    if (tag != null) {
      addProblem(expression, "Unexpected tag: ${tag.text}")
      return null
    }

    return scalar
  }

  private fun checkAndGetStringElement(expression: YAMLValue?): CfnScalarValueNode? {
    val scalar = checkAndGetScalarNode(expression) ?: return null
    return CfnScalarValueNode(scalar.textValue).registerNode(scalar)
  }

  private fun checkAndGetStringValue(expression: YAMLValue?): String? {
    val scalar = checkAndGetScalarNode(expression) ?: return null
    return scalar.textValue
  }

  fun file(psiFile: PsiFile): CfnRootNode {
    assert(CloudFormationPsiUtils.isCloudFormationFile(psiFile)) { psiFile.name + " is not a cfn file" }

    val yamlFile = psiFile as? YAMLFile ?: error("Not a YAML file")
    if (yamlFile.documents.isEmpty()) {
      error("YAML file is empty to parse")
    }

    for (doc in yamlFile.documents.drop(1)) {
      addProblem(doc, "Unexpected YAML document")
    }

    val yamlDocument = yamlFile.documents.single()
    val topLevelValue = yamlDocument.topLevelValue
    if (topLevelValue == null) {
      addProblem(yamlDocument, "Expected non-empty YAML document")
      return CfnRootNode(null, null, null, null, null, null).registerNode(yamlDocument)
    }

    val yamlMapping = topLevelValue as? YAMLMapping
    if (yamlMapping == null) {
      addProblem(topLevelValue, "Expected YAML mapping")
      return CfnRootNode(null, null, null, null, null, null).registerNode(topLevelValue)
    }

    return root(yamlMapping)
  }

  companion object {
    fun parse(psiFile: PsiFile): CloudFormationParsedFile {
      val parser = YamlCloudFormationParser()
      val rootNode = parser.file(psiFile)

      return CloudFormationParsedFile(parser.myProblems, parser.node2psi, parser.psi2node, rootNode, psiFile, psiFile.modificationStamp)
    }
  }
}
