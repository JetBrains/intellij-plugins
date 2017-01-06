package com.intellij.aws.cloudformation

import com.google.common.collect.HashBiMap
import com.intellij.aws.cloudformation.model.CfnArrayValueNode
import com.intellij.aws.cloudformation.model.CfnExpressionNode
import com.intellij.aws.cloudformation.model.CfnFunctionNode
import com.intellij.aws.cloudformation.model.CfnMissingOrInvalidValueNode
import com.intellij.aws.cloudformation.model.CfnNameValueNode
import com.intellij.aws.cloudformation.model.CfnNamedNode
import com.intellij.aws.cloudformation.model.CfnNode
import com.intellij.aws.cloudformation.model.CfnObjectValueNode
import com.intellij.aws.cloudformation.model.CfnOutputsNode
import com.intellij.aws.cloudformation.model.CfnResourceNode
import com.intellij.aws.cloudformation.model.CfnResourcePropertiesNode
import com.intellij.aws.cloudformation.model.CfnResourcePropertyNode
import com.intellij.aws.cloudformation.model.CfnResourceTypeNode
import com.intellij.aws.cloudformation.model.CfnResourcesNode
import com.intellij.aws.cloudformation.model.CfnRootNode
import com.intellij.aws.cloudformation.model.CfnScalarValueNode
import com.intellij.aws.cloudformation.model.CfnStringValueNode
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLScalarText
import org.jetbrains.yaml.psi.YAMLSequence
import org.jetbrains.yaml.psi.YAMLValue
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
    var resourcesNode: CfnResourcesNode? = null
    var outputsNode: CfnOutputsNode? = null

    for (property in root.keyValues) {
      val name = property.keyText
      val value = property.value

      if (name.isEmpty() || value == null) {
        continue
      }

      if (CloudFormationSections.FormatVersion == name) {
        formatVersion(value)
      } else if (CloudFormationSections.Transform == name) {
        checkAndGetStringValue(value)
      } else if (CloudFormationSections.Description == name) {
        description(value)
      } else if (CloudFormationSections.Parameters == name) {
        parameters(value)
      } else if (CloudFormationSections.Resources == name) {
        if (resourcesNode == null) {
          resourcesNode = resources(property)
        } else {
          addProblem(property, "Duplicate Resources node")
        }
      } else if (CloudFormationSections.Conditions == name) {
        // TODO
      } else if (CloudFormationSections.Metadata == name) {
        // Generic content inside, no need to check
        // See https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/metadata-section-structure.html
      } else if (CloudFormationSections.Outputs == name) {
        if (outputsNode == null) {
          outputsNode = outputs(property)
        } else {
          addProblem(property, "Duplicate Outputs node")
        }
      } else if (CloudFormationSections.Mappings == name) {
        mappings(value)
      } else {
        addProblemOnNameElement(
            property,
            CloudFormationBundle.getString("format.unknown.section", name))
      }
    }

    return CfnRootNode(resourcesNode, outputsNode).registerNode(root)
  }

  private fun outputs(outputs: YAMLKeyValue): CfnOutputsNode {
    val obj = checkAndGetMapping(outputs.value!!) ?: return CfnOutputsNode(keyName(outputs), emptyList())

    val properties = obj.keyValues.mapNotNull { property ->
      val value = property.value
      if (value == null) {
        addProblem(property, "Property value is expected")
        return@mapNotNull null
      }

      Pair(keyName(property), expression(value))
    }

    return CfnOutputsNode(keyName(outputs), properties).registerNode(outputs.value!!)
  }

  private fun parameters(parametersExpression: YAMLValue) {
    val obj = checkAndGetMapping(parametersExpression) ?: return

    if (obj.keyValues.isEmpty()) {
      addProblemOnNameElement(
          obj.parent as YAMLKeyValue,
          CloudFormationBundle.getString("format.no.parameters.declared"))
    }

    if (obj.keyValues.size > CloudFormationMetadataProvider.METADATA.limits.maxParameters) {
      addProblemOnNameElement(
          obj.parent as YAMLKeyValue,
          CloudFormationBundle.getString("format.max.parameters.exceeded", CloudFormationMetadataProvider.METADATA.limits.maxParameters))
    }
  }

  private fun mappings(mappingsExpression: YAMLValue) {
    val obj = checkAndGetMapping(mappingsExpression) ?: return

    if (obj.keyValues.isEmpty()) {
      addProblemOnNameElement(
          obj.parent as YAMLKeyValue,
          CloudFormationBundle.getString("format.no.mappings.declared"))
    }

    if (obj.keyValues.size > CloudFormationMetadataProvider.METADATA.limits.maxMappings) {
      addProblemOnNameElement(
          obj.parent as YAMLKeyValue,
          CloudFormationBundle.getString("format.max.mappings.exceeded", CloudFormationMetadataProvider.METADATA.limits.maxMappings))
    }
  }

  private fun keyName(property: YAMLKeyValue): CfnStringValueNode {
    if (property.key != null) {
      return CfnStringValueNode(property.keyText).registerNode(property.key!!)
    } else {
      addProblem(property, "Expected a name")
      return CfnStringValueNode("").registerNode(property)
    }
  }

  private fun description(value: YAMLValue) {
    checkAndGetStringValue(value)
  }

  private fun resources(property: YAMLKeyValue): CfnResourcesNode? {
    val keyElement = property.key
    val nameNode = if (keyElement == null) null else CfnStringValueNode(property.keyText).registerNode(keyElement)

    val obj = checkAndGetMapping(property.value!!) ?: return CfnResourcesNode(nameNode, emptyList()).registerNode(property)

    val resourcesList = obj.keyValues.mapNotNull { property ->
      return@mapNotNull if (property.keyText.isEmpty() || property.value == null) null else resource(property)
    }

    return CfnResourcesNode(nameNode, resourcesList).registerNode(property)
  }

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
      val propertyName = property.keyText

      if (!CloudFormationConstants.AllTopLevelResourceProperties.contains(propertyName)) {
        addProblemOnNameElement(property, CloudFormationBundle.getString("format.unknown.resource.property", propertyName))
      }

      if (!topLevelProperties.containsKey(propertyName)) {
        val nameNode = keyName(property)

        val valueElement = property.value
        val valueNode = if (valueElement == null) null else {
          val node = expression(valueElement) as? CfnScalarValueNode
          if (node == null) {
            addProblemOnNameElement(property, "Expected one value")
          }

          node
        }

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

      val jsonValueNode = property.value
      val valueNode = if (jsonValueNode != null) {
        expression(jsonValueNode)
      } else {
        CfnMissingOrInvalidValueNode()
      }

      return@mapNotNull CfnResourcePropertyNode(propertyNameNode, valueNode).registerNode(property)
    }

    return CfnResourcePropertiesNode(nameNode, propertyNodes).registerNode(propertiesProperty)
  }

  private fun expression(value: YAMLValue): CfnExpressionNode {
    return when (value) {
      is YAMLScalarText -> CfnStringValueNode(value.textValue).registerNode(value)
      // TODO boolean in yaml: is YAMLBoo -> CfnBooleanValueNode(value.value).registerNode(value)
      // TODO number in yaml: is YAMLScalar -> CfnNumberValueNode(value.text).registerNode(value)
      is YAMLSequence -> {
        val items = value.items.mapNotNull {
          val itemValue = it.value
          if (itemValue != null) expression(itemValue) else null
        }
        CfnArrayValueNode(items).registerNode(value)
      }
      is YAMLMapping -> {
        if (value.keyValues.size == 1 && CloudFormationIntrinsicFunctions.allNames.contains(value.keyValues.single().keyText)) {
          val single = value.keyValues.single()
          val nameNode = keyName(single)

          val jsonValueNode = single.value
          if (jsonValueNode is YAMLSequence) {
            val items = jsonValueNode.items.mapNotNull {
              val itemValue = it.value
              if (itemValue != null) expression(itemValue) else null
            }
            CfnFunctionNode(nameNode, items).registerNode(value)
          } else if (jsonValueNode == null){
            CfnFunctionNode(nameNode, listOf()).registerNode(value)
          } else {
            CfnFunctionNode(nameNode, listOf(expression(jsonValueNode))).registerNode(value)
          }
        } else {
          val properties = value.keyValues.map {
            val nameNode = keyName(it)

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

  private fun resourceType(typeProperty: YAMLKeyValue): CfnResourceTypeNode {
    val nameNode = keyName(typeProperty)
    val value = checkAndGetStringValue(typeProperty.value) ?:
        return CfnResourceTypeNode(nameNode, CfnStringValueNode("")).registerNode(typeProperty)

    val valueNode = CfnStringValueNode(value).registerNode(typeProperty.value!!)
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

  private fun checkAndGetStringValue(expression: YAMLValue?): String? {
    if (expression == null) {
      // Do not threat value absence as error
      return null
    }

    val literal = expression as? YAMLScalarText
    if (literal == null) {
      addProblem(expression, "A string literal is expected")
      return null
    }

    return literal.textValue
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
      return CfnRootNode(null, null).registerNode(yamlDocument)
    }

    val yamlMapping = topLevelValue as? YAMLMapping
    if (yamlMapping == null) {
      addProblem(topLevelValue, "Expected YAML mapping")
      return CfnRootNode(null, null).registerNode(topLevelValue)
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