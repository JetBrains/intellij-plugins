package com.intellij.aws.cloudformation.references

import com.intellij.aws.cloudformation.CloudFormationParser
import com.intellij.aws.cloudformation.CloudFormationResolve
import com.intellij.psi.PsiElement

class CloudFormationMappingFirstLevelKeyReference(element: PsiElement, private val mappingName: String) : CloudFormationReferenceBase(element) {
  override fun resolve(): PsiElement? {
    val parsed = CloudFormationParser.parse(myElement.containingFile)
    val firstLevelMappingKey = CloudFormationResolve.resolveFirstLevelMappingKey(parsed, mappingName, scalarNode.value) ?: return null
    return parsed.getPsiElement(firstLevelMappingKey)
  }

  override fun getCompletionVariants(): List<String> {
    val parsed = CloudFormationParser.parse(myElement.containingFile)
    return CloudFormationResolve.getTopLevelMappingKeys(parsed, mappingName) ?: emptyList()
  }
}
