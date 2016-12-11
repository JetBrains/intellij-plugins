package com.intellij.aws.cloudformation

import com.intellij.aws.cloudformation.references.CloudFormationReferenceBase
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
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

            // Disable all other items from JavaScript
            rs.stopHere()

            val parsed = CloudFormationParser.parse(position.containingFile)
            val parent = if (position.parent is JsonStringLiteral) position.parent else position

            val quoteResult = false // parent instanceof JSReferenceExpression;

            val resourceTypeValuePositionMatch = CloudFormationPsiUtils.ResourceTypeValueMatch.match(parent, parsed)
            if (resourceTypeValuePositionMatch != null) {
              CloudFormationMetadataProvider.METADATA.resourceTypes.values.forEach { resourceType ->
                rs.addElement(createLookupElement(resourceType.name, quoteResult))
              }

              return
            }

            val resourcePropertyNameMatch = CloudFormationPsiUtils.ResourcePropertyNameMatch.match(parent, parsed)
            if (resourcePropertyNameMatch != null) {
              val resourceTypeMetadata = CloudFormationMetadataProvider.METADATA.findResourceType(resourcePropertyNameMatch.resource.type?.id ?: "") ?: return
              for (propertyMetadata in resourceTypeMetadata.properties.values) {
                if (resourcePropertyNameMatch.resource.properties != null &&
                    resourcePropertyNameMatch.resource.properties.properties.any { it.name.id == propertyMetadata.name}) {
                  continue
                }

                rs.addElement(createLookupElement(propertyMetadata.name, quoteResult))
              }
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

  private fun completeAttribute(file: PsiFile, rs: CompletionResultSet, quoteResult: Boolean, resourceName: String) {
    val resource = CloudFormationResolve.resolveEntity(file, resourceName, CloudFormationSections.ResourcesSingletonList) ?: return

    val resourceProperties = resource.value as? JsonObject ?: return

    val typeProperty = resourceProperties.findProperty(CloudFormationConstants.TypePropertyName)
    if (typeProperty == null || typeProperty.value == null) {
      return
    }

    val resourceTypeName = StringUtil.stripQuotesAroundValue(typeProperty.value!!.text)

    val resourceType = CloudFormationMetadataProvider.METADATA.findResourceType(resourceTypeName) ?: return

    for (attribute in resourceType.attributes.values) {
      rs.addElement(createLookupElement(attribute.name, quoteResult))
    }
  }

  private fun createLookupElement(value: String, quote: Boolean): LookupElement {
    val id = if (quote) "\"$value\"" else value
    return LookupElementBuilder.create(id)
  }
}
