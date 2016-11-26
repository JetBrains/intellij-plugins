package com.intellij.aws.cloudformation

import com.google.common.collect.HashBiMap
import com.intellij.aws.cloudformation.model.CfnNameNode
import com.intellij.aws.cloudformation.model.CfnNode
import com.intellij.aws.cloudformation.model.CfnPropertiesNode
import com.intellij.aws.cloudformation.model.CfnProperty
import com.intellij.aws.cloudformation.model.CfnResourceNode
import com.intellij.aws.cloudformation.model.CfnResourcesNode
import com.intellij.aws.cloudformation.model.CfnRootNode
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
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

class CloudFormationParser(private val myInspectionManager: InspectionManager, private val myOnTheFly: Boolean) {
  private val myProblems = ArrayList<ProblemDescriptor>()
  private val myNodesMap = HashBiMap.create<PsiElement, CfnNode>()

  val problems: List<ProblemDescriptor>
    get() = myProblems

  fun getCfnNode(psiElement: PsiElement): CfnNode? = myNodesMap[psiElement]
  fun getPsiElement(node: CfnNode): PsiElement? = myNodesMap.inverse()[node]

  private fun <T : CfnNode> T.registerNode(psiElement: PsiElement): T {
    myNodesMap.put(psiElement, this)
    return this
  }

  private fun addProblem(element: PsiElement, description: String) {
    myProblems.add(myInspectionManager.createProblemDescriptor(
        element,
        description,
        myOnTheFly,
        LocalQuickFix.EMPTY_ARRAY,
        ProblemHighlightType.GENERIC_ERROR_OR_WARNING))
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
          resourcesNode = resources(value)
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
      stringValue(value)
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
    val literalExpression = expression as? JsonStringLiteral
    if (literalExpression != null) {
      // TODO
    }
  }

  private fun keyName(property: JsonProperty): CfnNameNode {
    if (AlphanumericStringPattern.matcher(property.name).matches()) {
      return CfnNameNode(property.name).registerNode(property)
    } else {
      addProblemOnNameElement(
          property,
          CloudFormationBundle.getString("format.invalid.key.name"))
      return CfnNameNode("").registerNode(property)
    }
  }

  private fun description(value: JsonValue) {
    checkAndGetQuotedStringText(value)
  }

  private fun resources(value: JsonValue): CfnResourcesNode? {
    val obj = checkAndGetObject(value) ?: return CfnResourcesNode(emptyList()).registerNode(value)

    val resourcesList = obj.propertyList.mapNotNull { property ->
      val resourceName = property.name
      val resourceObj = property.value

      if (resourceName.isEmpty() || resourceObj == null) {
        return@mapNotNull null
      }

      return@mapNotNull resource(property)
    }

    return CfnResourcesNode(resourcesList).registerNode(value)
  }

  private fun resource(resourceProperty: JsonProperty): CfnResourceNode {
    val key = keyName(resourceProperty)

    val value = resourceProperty.value ?: return CfnResourceNode(key, null, null).registerNode(resourceProperty)
    val obj = checkAndGetObject(value) ?: return CfnResourceNode(key, null, null).registerNode(resourceProperty)

    val typeNode: CfnNameNode?

    val typeProperty = obj.findProperty(CloudFormationConstants.TypePropertyName)
    if (typeProperty != null) {
      typeNode = resourceType(typeProperty)
    } else {
      addProblemOnNameElement(resourceProperty, CloudFormationBundle.getString("format.type.property.required"))
      typeNode = null
    }

    for (property in obj.propertyList) {
      val propertyName = property.name

      if (!CloudFormationConstants.AllTopLevelResourceProperties.contains(propertyName)) {
        addProblemOnNameElement(property, CloudFormationBundle.getString("format.unknown.resource.property", propertyName))
      }
    }

    val properties: CfnPropertiesNode?

    val propertiesProperty = obj.findProperty(CloudFormationConstants.PropertiesPropertyName)
    if (propertiesProperty != null && propertiesProperty.value is JsonObject) {
      properties = resourceProperties(propertiesProperty, typeNode?.id ?: "")
    } else {
      if (propertiesProperty != null && propertiesProperty.value !is JsonObject) {
        addProblemOnNameElement(propertiesProperty, CloudFormationBundle.getString("format.properties.property.should.properties.list"))
      }

      val resourceTypeMetadata = CloudFormationMetadataProvider.METADATA.findResourceType(typeNode?.id ?: "")
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

    return CfnResourceNode(key, typeNode, properties).registerNode(resourceProperty)
  }

  private fun resourceProperties(propertiesProperty: JsonProperty, rawResourceTypeName: String): CfnPropertiesNode {
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
      return@mapNotNull CfnProperty(propertyNameNode).registerNode(property)
    }

    if (!requiredProperties.isEmpty()) {
      val requiredPropertiesString = StringUtil.join(requiredProperties, " ")
      addProblemOnNameElement(propertiesProperty,
          CloudFormationBundle.getString("format.required.resource.properties.are.not.set", requiredPropertiesString))
    }

    return CfnPropertiesNode(propertyNodes).registerNode(propertiesProperty)
  }

  private fun resourceType(typeProperty: JsonProperty): CfnNameNode {
    val value = checkAndGetUnquotedStringText(typeProperty.value) ?: return CfnNameNode("").registerNode(typeProperty)

    if (!isCustomResourceType(value) && CloudFormationMetadataProvider.METADATA.findResourceType(value) == null) {
      addProblem(typeProperty, CloudFormationBundle.getString("format.unknown.type", value))
    }

    return CfnNameNode(value).registerNode(typeProperty)
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
      myProblems.add(
          myInspectionManager.createProblemDescriptor(
              value,
              CloudFormationBundle.getString("format.unknownVersion", supportedVersions),
              myOnTheFly,
              LocalQuickFix.EMPTY_ARRAY,
              ProblemHighlightType.GENERIC_ERROR_OR_WARNING))
    }
  }

  private fun checkAndGetQuotedStringText(expression: JsonValue?): String? {
    if (expression == null) {
      // Do not threat value absence as error
      return null
    }

    val literal = expression as? JsonStringLiteral
    if (literal == null) {
      myProblems.add(
          myInspectionManager.createProblemDescriptor(
              expression,
              CloudFormationBundle.getString("format.expected.quoted.string"),
              myOnTheFly,
              LocalQuickFix.EMPTY_ARRAY,
              ProblemHighlightType.GENERIC_ERROR_OR_WARNING))

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
  }
}
