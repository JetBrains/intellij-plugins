package com.intellij.aws.cloudformation

import com.intellij.aws.cloudformation.model.CfnFunctionNode
import com.intellij.aws.cloudformation.model.CfnNamedNode
import com.intellij.aws.cloudformation.model.CfnResourceNode
import com.intellij.aws.cloudformation.model.CfnScalarValueNode
import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.JsonLanguage
import com.intellij.json.psi.JsonReferenceExpression
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.YAMLLanguage
import org.jetbrains.yaml.psi.YAMLScalar
import org.jetbrains.yaml.psi.impl.YAMLCompoundValueImpl

class CloudFormationCompletionProvider : CompletionProvider<CompletionParameters>() {
          public override fun addCompletions(parameters: CompletionParameters,
                                             context: ProcessingContext,
                                             rsParent: CompletionResultSet) {
            val position = parameters.position

            if (!CloudFormationPsiUtils.isCloudFormationFile(position)) {
              return
            }

            // Disable all other items from JavaScript
            rsParent.stopHere()

            val rs = rsParent.caseInsensitive()

            val parsed = CloudFormationParser.parse(position.containingFile)
            val inspected = CloudFormationInspections.inspectFile(parsed)

            val parentSub = if (position.parent is JsonStringLiteral || position.parent is YAMLScalar || position.parent is JsonReferenceExpression) position.parent else position
            val parent = if (parentSub.parent != null && parentSub.parent.javaClass == YAMLCompoundValueImpl::class.java) {
              parentSub.parent
            } else parentSub

            val prefixFirstChar = rs.prefixMatcher.prefix.firstOrNull()
            val quote = when (parent) {
              is JsonReferenceExpression -> "\""
              else -> if (prefixFirstChar == '"' || prefixFirstChar == '\'') "$prefixFirstChar" else ""
            }

            val resourceTypeValuePositionMatch = ResourceTypeValueMatch.match(parent, parsed)
            if (resourceTypeValuePositionMatch != null) {
              CloudFormationMetadataProvider.METADATA.resourceTypes.values.forEach { resourceType ->
                rs.addElement(createLookupElement(resourceType.name, quote))
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

                rs.addElement(createLookupElement(propertyName, quote))
              }
            }

            completeResourceTopLevelProperty(rs, parent, quote, parsed)

            val attResourceName = getResourceNameFromGetAttAttributePosition(parent, parsed)
            if (attResourceName != null) {
              completeAttribute(parent.containingFile, rs, quote, attResourceName)
            }

            val references = inspected.references[parent]
            @Suppress("LoopToCallChain")
            for (reference in references) {
              for (v in reference.getCompletionVariants()) {
                rs.addElement(createLookupElement(v, quote))
              }
            }
          }

  private fun completeResourceTopLevelProperty(rs: CompletionResultSet, element: PsiElement, quote: String, parsed: CloudFormationParsedFile) {
    val nodes = parsed.getCfnNodes(element)
    val nameNode = nodes.ofType<CfnScalarValueNode>().singleOrNull() ?: return
    val namedNode = nameNode.parent(parsed) as? CfnNamedNode ?: return
    val resourceNode = namedNode.parent(parsed) as? CfnResourceNode ?: return

    if (namedNode.name != nameNode || !resourceNode.allTopLevelProperties.values.contains(namedNode)) return

    CloudFormationConstants.AllTopLevelResourceProperties
        .filter { !resourceNode.allTopLevelProperties.containsKey(it) }
        .forEach { rs.addElement(createLookupElement(it, quote)) }
  }

  private fun getResourceNameFromGetAttAttributePosition(element: PsiElement, parsed: CloudFormationParsedFile): String? {
    val nodes = parsed.getCfnNodes(element)
    val attrNode = nodes.ofType<CfnScalarValueNode>().singleOrNull() ?: return null
    val functionNode = attrNode.parent(parsed) as? CfnFunctionNode ?: return null

    if (functionNode.functionId != CloudFormationIntrinsicFunction.FnGetAtt ||
        functionNode.args.size != 2 || functionNode.args[1] !== attrNode) {
      return null
    }

    return (functionNode.args[0] as? CfnScalarValueNode)?.value
  }

  private fun completeAttribute(file: PsiFile, rs: CompletionResultSet, quote: String, resourceName: String) {
    val parsed = CloudFormationParser.parse(file)
    val resource = CloudFormationResolve.resolveResource(parsed, resourceName) ?: return

    val resourceTypeName = resource.typeName ?: return
    val resourceType = CloudFormationMetadataProvider.METADATA.findResourceType(resourceTypeName) ?: return

    resourceType.attributes.values.forEach {
      rs.addElement(createLookupElement(it.name, quote))
    }
  }

  private fun createLookupElement(value: String, quote: String): LookupElement {
    val id = "$quote$value$quote"
    return LookupElementBuilder.create(id)
  }
}

class JsonCloudFormationCompletionContributor : CompletionContributor() {
  init { extend(CompletionType.BASIC, PlatformPatterns.psiElement().withLanguage(JsonLanguage.INSTANCE), CloudFormationCompletionProvider()) }
}

class YamlCloudFormationCompletionContributor : CompletionContributor() {
  init { extend(CompletionType.BASIC, PlatformPatterns.psiElement().withLanguage(YAMLLanguage.INSTANCE), CloudFormationCompletionProvider()) }
}
