package com.intellij.aws.cloudformation.references

import com.intellij.aws.cloudformation.CloudFormationParser
import com.intellij.aws.cloudformation.CloudFormationResolve
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement

class CloudFormationMappingSecondLevelKeyReference(element: JsonStringLiteral, private val mappingName: String, private val topLevelKey: String) : CloudFormationReferenceBase(element) {
  override fun resolve(): PsiElement? {
    val parsed = CloudFormationParser.parse(element.containingFile)

    val key = CloudFormationResolve.resolveSecondLevelMappingKey(parsed, mappingName, topLevelKey, scalarNode.value) ?: return null
    return parsed.getPsiElement(key)
  }

  override fun getCompletionVariants(): List<String> {
    val parsed = CloudFormationParser.parse(element.containingFile)
    return CloudFormationResolve.getSecondLevelMappingKeys(parsed, mappingName, topLevelKey) ?: emptyList()
  }
}
