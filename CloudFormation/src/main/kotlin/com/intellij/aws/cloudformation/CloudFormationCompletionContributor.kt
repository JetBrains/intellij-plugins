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
              val cfnReference = reference as? CloudFormationReferenceBase
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
    val propertyName = element as? JsonStringLiteral ?: return

    val property = propertyName.parent as? JsonProperty
    if (property == null || property.nameElement !== propertyName) {
      return
    }

    val resourceExpression = property.parent as? JsonObject ?: return

    val resourceProperty = resourceExpression.parent as? JsonProperty ?: return

    val resourcesExpression = resourceProperty.parent as? JsonObject ?: return

    val resourcesProperty = resourcesExpression.parent as? JsonProperty
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
    val attributeExpression = element as? JsonStringLiteral ?: return null

    val getattParameters = attributeExpression.parent as? JsonArray
    if (getattParameters == null || getattParameters.valueList.size != 2) {
      return null
    }

    val getattProperty = getattParameters.parent as? JsonProperty
    if (getattProperty == null || CloudFormationIntrinsicFunctions.FnGetAtt != getattProperty.name) {
      return null
    }

    val getattFunc = getattProperty.parent as? JsonObject
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

    val resourceProperties = resource.value as? JsonObject ?: return

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
    val propertyName = propertyNameElement as? JsonStringLiteral ?: return

    val property = propertyName.parent as? JsonProperty
    if (property == null || property.nameElement !== propertyName) {
      return
    }

    val propertiesExpression = property.parent as? JsonObject ?: return

    val resourceElement = CloudFormationPsiUtils.getResourceElementFromPropertyName(propertyName) ?: return

    val resourceValue = resourceElement.value as? JsonObject ?: return

    val typeProperty = resourceValue.findProperty(CloudFormationConstants.TypePropertyName) ?: return

    val typeValue = typeProperty.value as? JsonStringLiteral ?: return

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
