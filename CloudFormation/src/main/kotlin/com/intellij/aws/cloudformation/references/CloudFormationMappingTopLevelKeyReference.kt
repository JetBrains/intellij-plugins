package com.intellij.aws.cloudformation.references

import com.intellij.aws.cloudformation.CloudFormationResolve
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement

class CloudFormationMappingTopLevelKeyReference(element: JsonStringLiteral, private val mappingName: String) : CloudFormationReferenceBase(element) {
  override fun resolve(): PsiElement? {
    val entityName = CloudFormationResolve.getTargetName(myElement as JsonStringLiteral)
    return CloudFormationResolve.resolveTopLevelMappingKey(myElement.getContainingFile(), mappingName, entityName)
  }

  override fun getCompletionVariants(): List<String> =
      CloudFormationResolve.getTopLevelMappingKeys(myElement.containingFile, mappingName) ?: emptyList()
}
