package com.intellij.aws.cloudformation.references

import com.intellij.aws.cloudformation.CloudFormationResolve
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement

class CloudFormationMappingSecondLevelKeyReference(element: JsonStringLiteral, private val mappingName: String, private val topLevelKey: String) : CloudFormationReferenceBase(element) {
  override fun resolve(): PsiElement? {
    val entityName = CloudFormationResolve.getTargetName(myElement as JsonStringLiteral)
    return CloudFormationResolve.resolveSecondLevelMappingKey(myElement.getContainingFile(), mappingName, topLevelKey, entityName)
  }

  override fun getCompletionVariants(): List<String> =
      CloudFormationResolve.getSecondLevelMappingKeys(myElement.containingFile, mappingName, topLevelKey) ?: emptyList()
}
