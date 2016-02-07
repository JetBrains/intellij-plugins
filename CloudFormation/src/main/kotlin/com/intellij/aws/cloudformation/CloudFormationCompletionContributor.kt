package com.intellij.aws.cloudformation

import com.intellij.aws.cloudformation.references.CloudFormationReferenceBase
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.JsonLanguage
import com.intellij.json.psi.JsonArray
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.text.StringUtil
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.ObjectUtils
import com.intellij.util.ProcessingContext

class CloudFormationCompletionContributor : CompletionContributor() {
  init {
    extend(CompletionType.BASIC,
        PlatformPatterns.psiElement().withLanguage(JsonLanguage.INSTANCE),
        object : CompletionProvider<CompletionParameters>() {
          public override fun addCompletions(parameters: CompletionParameters,
                                             context: ProcessingContext,
                                             rs: CompletionResultSet) {
            val position = parameters.position

            if (!CloudFormationPsiUtils.isCloudFormationFile(position)) {
              return
            }

            var parent = position
            if (parent.parent is JsonStringLiteral) {
              parent = parent.parent
            }

            val quoteResult = false // parent instanceof JSReferenceExpression;

            if (CloudFormationPsiUtils.isResourceTypeValuePosition(parent)) {
              completeResourceType(rs, quoteResult)
            } else if (CloudFormationPsiUtils.isResourcePropertyNamePosition(parent)) {
              completeResourceProperty(rs, parent, quoteResult)
            }

            completeResourceTopLevelProperty(rs, parent, quoteResult)

            val attResourceName = getResourceNameFromGetAttAttributePosition(parent)
            if (attResourceName != null) {
              completeAttribute(parent.containingFile, rs, quoteResult, attResourceName)
            }

            for (reference in parent.references) {
              val cfnReference = ObjectUtils.tryCast(reference, CloudFormationReferenceBase::class.java)
              if (cfnReference != null) {
                for (v in cfnReference.getCompletionVariants()) {
                  rs.addElement(createLookupElement(v, quoteResult))
                }
              }
            }

            // Disable all other items from JavaScript
            rs.stopHere()
          }
        })
  }

  private fun completeResourceTopLevelProperty(rs: CompletionResultSet, element: PsiElement, quoteResult: Boolean) {
    val propertyName = ObjectUtils.tryCast(element, JsonStringLiteral::class.java) ?: return

    val property = ObjectUtils.tryCast(propertyName.parent, JsonProperty::class.java)
    if (property == null || property.nameElement !== propertyName) {
      return
    }

    val resourceExpression = ObjectUtils.tryCast(property.parent, JsonObject::class.java) ?: return

    val resourceProperty = ObjectUtils.tryCast(resourceExpression.parent, JsonProperty::class.java) ?: return

    val resourcesExpression = ObjectUtils.tryCast(resourceProperty.parent, JsonObject::class.java) ?: return

    val resourcesProperty = ObjectUtils.tryCast(resourcesExpression.parent, JsonProperty::class.java)
    if (resourcesProperty == null || CloudFormationSections.Resources != StringUtil.stripQuotesAroundValue(resourcesProperty.name)) {
      return
    }

    val root = CloudFormationPsiUtils.getRootExpression(resourceProperty.containingFile)
    if (root !== resourcesProperty.parent) {
      return
    }

    for (name in CloudFormationConstants.AllTopLevelResourceProperties) {
      if (resourceExpression.findProperty(name) == null) {
        rs.addElement(createLookupElement(name, quoteResult))
      }
    }
  }

  private fun getResourceNameFromGetAttAttributePosition(element: PsiElement): String? {
    val attributeExpression = ObjectUtils.tryCast(element, JsonStringLiteral::class.java) ?: return null

    val getattParameters = ObjectUtils.tryCast(attributeExpression.parent, JsonArray::class.java)
    if (getattParameters == null || getattParameters.valueList.size != 2) {
      return null
    }

    val getattProperty = ObjectUtils.tryCast(getattParameters.parent, JsonProperty::class.java)
    if (getattProperty == null || CloudFormationIntrinsicFunctions.FnGetAtt != getattProperty.name) {
      return null
    }

    val getattFunc = ObjectUtils.tryCast(getattProperty.parent, JsonObject::class.java)
    if (getattFunc == null || getattFunc.propertyList.size != 1) {
      return null
    }

    val text = getattParameters.valueList[0].text
    return StringUtil.stripQuotesAroundValue(text)
  }

  private fun completeResourceType(rs: CompletionResultSet, quoteResult: Boolean) {
    for (resourceType in CloudFormationMetadataProvider.METADATA.resourceTypes) {
      rs.addElement(createLookupElement(resourceType.name, quoteResult))
    }
  }

  private fun completeAttribute(file: PsiFile, rs: CompletionResultSet, quoteResult: Boolean, resourceName: String) {
    val resource = CloudFormationResolve.resolveEntity(file, resourceName, CloudFormationSections.ResourcesSingletonList) ?: return

    val resourceProperties = ObjectUtils.tryCast(resource.value, JsonObject::class.java) ?: return

    val typeProperty = resourceProperties.findProperty(CloudFormationConstants.TypePropertyName)
    if (typeProperty == null || typeProperty.value == null) {
      return
    }

    val resourceTypeName = StringUtil.stripQuotesAroundValue(typeProperty.value!!.text)

    val resourceType = CloudFormationMetadataProvider.METADATA.findResourceType(resourceTypeName) ?: return

    for (attribute in resourceType.attributes) {
      rs.addElement(createLookupElement(attribute.name, quoteResult))
    }
  }

  private fun completeResourceProperty(rs: CompletionResultSet, propertyNameElement: PsiElement, quoteResult: Boolean) {
    val propertyName = ObjectUtils.tryCast(propertyNameElement, JsonStringLiteral::class.java) ?: return

    val property = ObjectUtils.tryCast(propertyName.parent, JsonProperty::class.java)
    if (property == null || property.nameElement !== propertyName) {
      return
    }

    val propertiesExpression = ObjectUtils.tryCast(property.parent, JsonObject::class.java) ?: return

    val resourceElement = CloudFormationPsiUtils.getResourceElementFromPropertyName(propertyName) ?: return

    val resourceValue = ObjectUtils.tryCast(resourceElement.value, JsonObject::class.java) ?: return

    val typeProperty = resourceValue.findProperty(CloudFormationConstants.TypePropertyName) ?: return

    val typeValue = ObjectUtils.tryCast(typeProperty.value, JsonStringLiteral::class.java) ?: return

    val type = CloudFormationResolve.getTargetName(typeValue)

    val resourceTypeMetadata = CloudFormationMetadataProvider.METADATA.findResourceType(type) ?: return

    for (propertyMetadata in resourceTypeMetadata.properties) {
      if (propertiesExpression.findProperty(propertyMetadata.name) != null) {
        continue
      }

      rs.addElement(createLookupElement(propertyMetadata.name, quoteResult))
    }
  }

  private fun createLookupElement(`val`: String, quote: Boolean): LookupElement {
    val id = if (quote) "\"" + `val` + "\"" else `val`
    return LookupElementBuilder.create(id)
  }

}
