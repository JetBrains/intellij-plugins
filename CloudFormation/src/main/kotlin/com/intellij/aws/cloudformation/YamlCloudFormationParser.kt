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
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.impl.source.tree.CompositeElement
import com.intellij.psi.impl.source.tree.TreeElement
import org.jetbrains.yaml.YAMLElementTypes
import org.jetbrains.yaml.YAMLTokenTypes
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLScalar
import org.jetbrains.yaml.psi.YAMLSequence
import org.jetbrains.yaml.psi.YAMLValue
import org.jetbrains.yaml.psi.impl.YAMLCompoundValueImpl
import org.jetbrains.yaml.psi.impl.YAMLQuotedTextImpl
import java.util.ArrayList

class YamlCloudFormationParser private constructor () {
  private val myProblems = ArrayList<CloudFormationProblem>()
  private val myNodesMap = HashBiMap.create<PsiElement, CfnNode>()

  private fun <T : CfnNode> T.registerNode(psiElement: PsiElement): T {
    myNodesMap.put(psiElement, this)
    return this
  }

  private fun addProblem(element: PsiElement, description: String) {
    myProblems.add(CloudFormationProblem(element, description))
  }

  private fun addProblemOnNameElement(property: YAMLKeyValue, description: String) =
    addProblem(property.key ?: property, description)


  private fun root(root: YAMLMapping): CfnRootNode {
    val sections = root.keyValues.mapNotNull { property ->
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

  private fun outputs(outputs: YAMLKeyValue): CfnOutputsNode = parseNameValues(
      outputs,
      { output -> CfnOutputNode(keyName(output), expression(output.value!!)) },
      { nameNode, list -> CfnOutputsNode(nameNode, list) }
  )

  private fun parameters(parameters: YAMLKeyValue): CfnParametersNode = parseNameValues(
      parameters,
      { parameter -> parameter(parameter) },
      { nameNode, list -> CfnParametersNode(nameNode, list) }
  )

  private fun parameter(parameter: YAMLKeyValue): CfnParameterNode = parseNameValues(
      parameter,
      { node -> CfnNameValueNode(keyName(node), node.value?.let { expression(it) }) },
      { nameNode, list -> CfnParameterNode(nameNode, list) }
  )

  private fun <ResultNodeType : CfnNode, ValueNodeType: CfnNode> parseNameValues(
      keyValueElement: YAMLKeyValue,
      valueFactory: (YAMLKeyValue) -> ValueNodeType,
      resultFactory: (CfnScalarValueNode?, List<ValueNodeType>) -> ResultNodeType): ResultNodeType
  {
    val keyElement = keyValueElement.key
    val nameNode = if (keyElement == null) null else CfnScalarValueNode(keyValueElement.keyText).registerNode(keyElement)

    val obj = checkAndGetMapping(keyValueElement.value!!) ?: return resultFactory(nameNode, emptyList()).registerNode(keyValueElement)

    val list = obj.keyValues.mapNotNull { value ->
      if (value.keyText.isEmpty()) {
        addProblemOnNameElement(value, "A non-empty key is expected")
        return@mapNotNull null
      }

      if (value.value == null) {
        addProblemOnNameElement(value, "A value is expected")
        return@mapNotNull null
      }

      return@mapNotNull valueFactory(value)
    }

    return resultFactory(nameNode, list).registerNode(keyValueElement)
  }

  private fun mappings(mappings: YAMLKeyValue): CfnMappingsNode = parseNameValues(
      mappings,
      { mapping -> firstLevelMapping(mapping) },
      { nameNode, list -> CfnMappingsNode(nameNode, list) }
  )

  private fun firstLevelMapping(mapping: YAMLKeyValue): CfnFirstLevelMappingNode = parseNameValues(
      mapping,
      { mapping -> secondLevelMapping(mapping) },
      { nameNode, list -> CfnFirstLevelMappingNode(nameNode, list) }
  )

  private fun secondLevelMapping(mapping: YAMLKeyValue): CfnSecondLevelMappingNode = parseNameValues(
      mapping,
      { node -> CfnMappingValue(keyName(node), checkAndGetStringElement(node.value)) },
      { nameNode, list -> CfnSecondLevelMappingNode(nameNode, list) }
  )

  private fun keyName(property: YAMLKeyValue): CfnScalarValueNode? {
    if (property.key != null) {
      return CfnScalarValueNode(property.keyText).registerNode(property.key!!)
    } else {
      addProblem(property, "Expected a name")
      return CfnScalarValueNode("").registerNode(property)
    }
  }

  private fun resources(resources: YAMLKeyValue): CfnResourcesNode = parseNameValues(
      resources,
      { resource -> resource(resource) },
      { nameNode, list -> CfnResourcesNode(nameNode, list) }
  )

  private fun resource(resourceProperty: YAMLKeyValue): CfnResourceNode {
    val key = keyName(resourceProperty)

    val value = resourceProperty.value ?: return CfnResourceNode(key, null, emptyMap(), null).registerNode(resourceProperty)
    val obj = checkAndGetMapping(value) ?: return CfnResourceNode(key, null, emptyMap(), null).registerNode(resourceProperty)

    val typeNode: CfnResourceTypeNode?
    val topLevelProperties: MutableMap<String, CfnNamedNode> = hashMapOf()

    val typeProperty = obj.getKeyValueByKey(CloudFormationConstants.TypePropertyName)
    if (typeProperty != null) {
      typeNode = resourceType(typeProperty)
      topLevelProperties.put(typeProperty.keyText, typeNode)
    } else {
      typeNode = null
    }

    val properties: CfnResourcePropertiesNode?

    val propertiesProperty = obj.getKeyValueByKey(CloudFormationConstants.PropertiesPropertyName)
    if (propertiesProperty != null && propertiesProperty.value is YAMLMapping) {
      properties = resourceProperties(propertiesProperty)
      topLevelProperties.put(propertiesProperty.keyText, properties)
    } else {
      if (propertiesProperty != null && propertiesProperty.value !is YAMLMapping) {
        addProblemOnNameElement(propertiesProperty, "Expected a properties list")
      }

      properties = null
    }

    for (property in obj.keyValues) {
      val propertyName = property.keyText.trim()

      if (!CloudFormationConstants.AllTopLevelResourceProperties.contains(propertyName)) {
        addProblemOnNameElement(property, CloudFormationBundle.getString("format.unknown.resource.property", propertyName))
      }

      if (!topLevelProperties.containsKey(propertyName)) {
        val nameNode = keyName(property)

        val valueElement = property.value
        val valueNode = valueElement?.let { expression(valueElement) }

        topLevelProperties.put(propertyName, CfnNameValueNode(nameNode, valueNode).registerNode(property))
      }
    }

    return CfnResourceNode(key, typeNode, topLevelProperties, properties).registerNode(resourceProperty)
  }

  private fun resourceProperties(propertiesProperty: YAMLKeyValue): CfnResourcePropertiesNode {
    val nameNode = keyName(propertiesProperty)
    val properties = propertiesProperty.value as YAMLMapping

    val propertyNodes = properties.keyValues.mapNotNull { property ->
      val propertyName = property.name
      if (propertyName == CloudFormationConstants.CommentResourcePropertyName) {
        return@mapNotNull null
      }

      val propertyNameNode = keyName(property)

      val yamlValueNode = property.value
      val valueNode = if (yamlValueNode == null) null else {
        expression(yamlValueNode)
      }

      return@mapNotNull CfnResourcePropertyNode(propertyNameNode, valueNode).registerNode(property)
    }

    return CfnResourcePropertiesNode(nameNode, propertyNodes).registerNode(propertiesProperty)
  }

  private fun YAMLScalar.cfnPatchedTextValue(): String {
    if (this is YAMLQuotedTextImpl) {
      if (node.firstChildNode == null) {
        return ""
      }

      val clone = node.clone() as ASTNode

      val startNode = if (clone.firstChildNode.elementType == YAMLTokenTypes.TAG) {
        var current: ASTNode? = clone.firstChildNode
        while (current != null && (current.elementType == YAMLTokenTypes.TAG || current.elementType == TokenType.WHITE_SPACE)) {
          current = current.treeNext
        }
        current
      } else {
        clone.firstChildNode
      } ?: return ""

      val composite = CompositeElement(YAMLElementTypes.SCALAR_QUOTED_STRING)
      composite.rawAddChildrenWithoutNotifications(startNode as TreeElement)
      return YAMLQuotedTextImpl(composite).textValue
    } else {
      return textValue
    }
  }

  private fun expression(value: YAMLValue): CfnExpressionNode? {
    val tag = value.tag

    if (tag != null) {
      val functionName = tag.text.trimStart('!')
      val functionId = CloudFormationIntrinsicFunction.shortNames[functionName]

      if (functionId == null) {
        addProblem(tag, "Unknown CloudFormation function: $functionName")
        return null
      }

      val tagNode = CfnScalarValueNode(functionName).registerNode(tag)

      return when {
        value is YAMLScalar -> {
          val parameterNode = CfnScalarValueNode(value.cfnPatchedTextValue()).registerNode(value)
          CfnFunctionNode(tagNode, functionId, listOf(parameterNode)).registerNode(value)
        }

        value.javaClass == YAMLCompoundValueImpl::class.java -> {
          addProblem(value, "Too many values")
          null
        }

        value is YAMLSequence -> {
          val items = value.items.mapNotNull {
            val itemValue = it.value
            if (itemValue != null) expression(itemValue) else null
          }
          CfnFunctionNode(tagNode, functionId, items).registerNode(value)
        }

        value is YAMLMapping -> {
          addProblem(tag, "CloudFormation function expects a scalar value or a sequence")
          null
        }

        else -> {
          addProblem(value, CloudFormationBundle.getString("format.unknown.value", value.javaClass.simpleName))
          null
        }
      }
    }

    return when {
      value is YAMLScalar -> CfnScalarValueNode(value.cfnPatchedTextValue()).registerNode(value)
      value.javaClass == YAMLCompoundValueImpl::class.java -> {
        addProblem(value, "Too many values")
        null
      }
      value is YAMLSequence -> {
        val items = value.items.mapNotNull {
          val itemValue = it.value
          if (itemValue != null) expression(itemValue) else null
        }
        CfnArrayValueNode(items).registerNode(value)
      }
      value is YAMLMapping -> {
        if (value.keyValues.size == 1 && CloudFormationIntrinsicFunction.fullNames.containsKey(value.keyValues.single().keyText)) {
          val single = value.keyValues.single()
          val nameNode = keyName(single)!!
          val functionId = CloudFormationIntrinsicFunction.fullNames[single.keyText]!!

          val yamlValueNode = single.value
          if (yamlValueNode is YAMLSequence) {
            val items = yamlValueNode.items.mapNotNull {
              val itemValue = it.value
              if (itemValue != null) expression(itemValue) else null
            }
            CfnFunctionNode(nameNode, functionId, items).registerNode(value)
          } else if (yamlValueNode == null){
            CfnFunctionNode(nameNode, functionId, listOf()).registerNode(value)
          } else {
            CfnFunctionNode(nameNode, functionId, listOf(expression(yamlValueNode))).registerNode(value)
          }
        } else {
          val properties = value.keyValues.map {
            val nameNode = keyName(it)

            val yamlValueNode = it.value
            val valueNode = if (yamlValueNode == null) null else {
              expression(yamlValueNode)
            }

            CfnNameValueNode(nameNode, valueNode).registerNode(it)
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

  private fun resourceType(typeProperty: YAMLKeyValue): CfnResourceTypeNode {
    val nameNode = keyName(typeProperty)
    val value = checkAndGetStringValue(typeProperty.value) ?:
        return CfnResourceTypeNode(nameNode, null).registerNode(typeProperty)

    val valueNode = CfnScalarValueNode(value).registerNode(typeProperty.value!!)
    return CfnResourceTypeNode(nameNode, valueNode).registerNode(typeProperty)
  }

  private fun checkAndGetMapping(expression: YAMLValue): YAMLMapping? {
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
    return CfnScalarValueNode(scalar.cfnPatchedTextValue()).registerNode(scalar)
  }

  private fun checkAndGetStringValue(expression: YAMLValue?): String? {
    val scalar = checkAndGetScalarNode(expression) ?: return null
    return scalar.cfnPatchedTextValue()
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
      return CfnRootNode(null, null, null, null).registerNode(yamlDocument)
    }

    val yamlMapping = topLevelValue as? YAMLMapping
    if (yamlMapping == null) {
      addProblem(topLevelValue, "Expected YAML mapping")
      return CfnRootNode(null, null, null, null).registerNode(topLevelValue)
    }

    return root(yamlMapping)
  }

  companion object {
    fun parse(psiFile: PsiFile): CloudFormationParsedFile {
      val parser = YamlCloudFormationParser()
      val rootNode = parser.file(psiFile)

      return CloudFormationParsedFile(parser.myProblems, parser.myNodesMap, rootNode)
    }
  }
}