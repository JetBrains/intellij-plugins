package com.intellij.aws.cloudformation

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
import com.intellij.util.ObjectUtils
import java.util.*
import java.util.regex.Pattern

class CloudFormationFormatChecker(private val myInspectionManager: InspectionManager, private val myOnTheFly: Boolean) {

  private val myProblems = ArrayList<ProblemDescriptor>()

  val problems: List<ProblemDescriptor>
    get() = myProblems

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

  private fun root(root: JsonObject) {
    for (property in root.propertyList) {
      val name = property.name
      val value = property.value

      if (name.isEmpty() || value == null) {
        continue
      }

      if (CloudFormationSections.FormatVersion == name) {
        formatVersion(value)
      } else if (CloudFormationSections.Description == name) {
        description(value)
      } else if (CloudFormationSections.Parameters == name) {
        parameters(value)
      } else if (CloudFormationSections.Resources == name) {
        resources(value)
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
  }

  private fun outputs(outputsExpression: JsonValue) {
    val obj = checkAndGetObject(outputsExpression) ?: return

    for (property in obj.propertyList) {
      val name = property.name
      val value = property.value
      if (name.isEmpty() || value == null) {
        continue
      }

      checkKeyName(property)
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
    val literalExpression = ObjectUtils.tryCast(expression, JsonStringLiteral::class.java)
    if (literalExpression != null) {
      // TODO
    }
  }

  private fun checkKeyName(property: JsonProperty?) {
    if (property == null || property.name.isEmpty()) {
      return
    }

    if (!AlphanumericStringPattern.matcher(property.name).matches()) {
      addProblemOnNameElement(
          property,
          CloudFormationBundle.getString("format.invalid.key.name"))
    }
  }

  private fun description(value: JsonValue) {
    checkAndGetQuotedStringText(value)
  }

  private fun resources(value: JsonValue) {
    val obj = checkAndGetObject(value) ?: return

    for (property in obj.propertyList) {
      val resourceName = property.name
      val resourceObj = property.value
      if (resourceName.isEmpty() || resourceObj == null) {
        continue
      }

      checkKeyName(property)
      resource(property)
    }
  }

  private fun resource(resourceProperty: JsonProperty) {
    val value = resourceProperty.value ?: return
    val obj = checkAndGetObject(value) ?: return

    val typeProperty = obj.findProperty(CloudFormationConstants.TypePropertyName)
    if (typeProperty == null) {
      addProblemOnNameElement(resourceProperty, CloudFormationBundle.getString("format.type.property.required"))
      return
    }

    for (property in obj.propertyList) {
      val propertyName = property.name

      if (!CloudFormationConstants.AllTopLevelResourceProperties.contains(propertyName)) {
        addProblemOnNameElement(property, CloudFormationBundle.getString("format.unknown.resource.property", propertyName))
      }
    }

    resourceType(typeProperty)

    val propertiesProperty = obj.findProperty(CloudFormationConstants.PropertiesPropertyName)
    if (propertiesProperty != null) {
      resourceProperties(propertiesProperty, typeProperty)
    } else {
      val resourceType = checkAndGetUnquotedStringText(typeProperty.value)
      if (resourceType != null) {
        val resourceTypeMetadata = CloudFormationMetadataProvider.METADATA.findResourceType(resourceType)
        if (resourceTypeMetadata != null) {
          val requiredProperties = HashSet(resourceTypeMetadata.requiredProperties)
          if (!requiredProperties.isEmpty()) {
            val requiredPropertiesString = StringUtil.join(requiredProperties, " ")
            addProblemOnNameElement(
                resourceProperty,
                CloudFormationBundle.getString("format.required.resource.properties.are.not.set", requiredPropertiesString))
          }
        }
      }
    }
  }

  private fun resourceProperties(propertiesProperty: JsonProperty, typeProperty: JsonProperty) {
    val properties = ObjectUtils.tryCast(propertiesProperty.value, JsonObject::class.java)
    if (properties == null) {
      addProblemOnNameElement(propertiesProperty, CloudFormationBundle.getString("format.properties.property.should.properties.list"))
      return
    }

    var resourceTypeName = checkAndGetUnquotedStringText(typeProperty.value) ?: return

    if (resourceTypeName.startsWith(CloudFormationConstants.CustomResourceTypePrefix)) {
      resourceTypeName = CloudFormationConstants.CustomResourceType
    }

    val resourceType = CloudFormationMetadataProvider.METADATA.findResourceType(resourceTypeName) ?: return

    val requiredProperties = HashSet(resourceType.requiredProperties)

    for (property in properties.propertyList) {
      val propertyName = property.name
      if (propertyName.isEmpty()) {
        continue
      }

      if (propertyName == CloudFormationConstants.CommentResourcePropertyName) {
        continue
      }

      if (resourceType.findProperty(propertyName) == null && !isCustomResourceType(resourceTypeName)) {
        addProblemOnNameElement(property, CloudFormationBundle.getString("format.unknown.resource.type.property", propertyName))
      }

      requiredProperties.remove(propertyName)
    }

    if (!requiredProperties.isEmpty()) {
      val requiredPropertiesString = StringUtil.join(requiredProperties, " ")
      addProblemOnNameElement(propertiesProperty,
          CloudFormationBundle.getString("format.required.resource.properties.are.not.set", requiredPropertiesString))
    }
  }

  private fun resourceType(typeProperty: JsonProperty) {
    val value = checkAndGetUnquotedStringText(typeProperty.value) ?: return

    if (isCustomResourceType(value)) {
      return
    }

    if (CloudFormationMetadataProvider.METADATA.findResourceType(value) == null) {
      addProblem(typeProperty, CloudFormationBundle.getString("format.unknown.type", value))
    }
  }

  private fun isCustomResourceType(value: String): Boolean {
    return value == CloudFormationConstants.CustomResourceType || value.startsWith(CloudFormationConstants.CustomResourceTypePrefix)
  }

  private fun checkAndGetObject(expression: JsonValue): JsonObject? {
    val obj = ObjectUtils.tryCast(expression, JsonObject::class.java)
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

    val literal = ObjectUtils.tryCast(expression, JsonStringLiteral::class.java)
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

  fun file(psiFile: PsiFile) {
    assert(CloudFormationPsiUtils.isCloudFormationFile(psiFile)) { psiFile.name + " is not a cfn file" }

    val root = CloudFormationPsiUtils.getRootExpression(psiFile) ?: return

    root(root)
  }

  companion object {
    private val AlphanumericStringPattern = Pattern.compile("[a-zA-Z0-9]+")
  }
}
