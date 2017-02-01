package com.intellij.aws.cloudformation

import com.intellij.aws.cloudformation.model.CfnNamedNode
import com.intellij.aws.cloudformation.model.CfnResourceNode
import com.intellij.aws.cloudformation.model.CfnScalarValueNode
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

            val resourceTypeValuePositionMatch = ResourceTypeValueMatch.match(parent, parsed)
            if (resourceTypeValuePositionMatch != null) {
              CloudFormationMetadataProvider.METADATA.resourceTypes.values.forEach { resourceType ->
                rs.addElement(createLookupElement(resourceType.name, quoteResult))
              }

              return
            }

            val resourcePropertyNameMatch = ResourcePropertyNameMatch.match(parent, parsed)
            if (resourcePropertyNameMatch != null) {
              val resourceTypeMetadata = CloudFormationMetadataProvider.METADATA.findResourceType(resourcePropertyNameMatch.resource.typeName ?: "") ?: return
              @Suppress("LoopToCallChain")
              for ((propertyName) in resourceTypeMetadata.properties.values) {
                if (resourcePropertyNameMatch.resource.properties != null &&
                    resourcePropertyNameMatch.resource.properties.properties.any { it.name?.value == propertyName }) {
                  continue
                }

                rs.addElement(createLookupElement(propertyName, quoteResult))
              }
            }

            completeResourceTopLevelProperty(rs, parent, quoteResult, parsed)

            val attResourceName = getResourceNameFromGetAttAttributePosition(parent)
            if (attResourceName != null) {
              completeAttribute(parent.containingFile, rs, quoteResult, attResourceName)
            }

            @Suppress("LoopToCallChain")
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

  private fun completeResourceTopLevelProperty(rs: CompletionResultSet, element: PsiElement, quoteResult: Boolean, parsed: CloudFormationParsedFile) {
    val nodes = parsed.getCfnNodes(element)
    val nameNode = nodes.ofType<CfnScalarValueNode>().singleOrNull() ?: return
    val namedNode = nameNode.parent(parsed) as? CfnNamedNode ?: return
    val resourceNode = namedNode.parent(parsed) as? CfnResourceNode ?: return

    if (namedNode.name != nameNode || !resourceNode.allTopLevelProperties.values.contains(namedNode)) return

    CloudFormationConstants.AllTopLevelResourceProperties
        .filter { !resourceNode.allTopLevelProperties.containsKey(it) }
        .forEach { rs.addElement(createLookupElement(it, quoteResult)) }
  }

  private fun getResourceNameFromGetAttAttributePosition(element: PsiElement): String? {
    val attributeExpression = element as? JsonStringLiteral ?: return null

    val getattParameters = attributeExpression.parent as? JsonArray
    if (getattParameters == null || getattParameters.valueList.size != 2) {
      return null
    }

    val getattProperty = getattParameters.parent as? JsonProperty
    if (getattProperty == null || CloudFormationIntrinsicFunction.FnGetAtt.id != getattProperty.name) {
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
    val parsed = CloudFormationParser.parse(file)
    val resource = CloudFormationResolve.resolveResource(parsed, resourceName) ?: return

    val resourceTypeName = resource.typeName ?: return
    val resourceType = CloudFormationMetadataProvider.METADATA.findResourceType(resourceTypeName) ?: return

    resourceType.attributes.values.forEach {
      rs.addElement(createLookupElement(it.name, quoteResult))
    }
  }

  private fun createLookupElement(value: String, quote: Boolean): LookupElement {
    val id = if (quote) "\"$value\"" else value
    return LookupElementBuilder.create(id)
  }
}
