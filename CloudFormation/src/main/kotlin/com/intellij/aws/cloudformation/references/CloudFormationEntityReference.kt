package com.intellij.aws.cloudformation.references

import com.intellij.aws.cloudformation.CloudFormationMetadataProvider
import com.intellij.aws.cloudformation.CloudFormationResolve
import com.intellij.aws.cloudformation.CloudFormationSections
import com.intellij.json.psi.JsonLiteral
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement

class CloudFormationEntityReference(element: JsonLiteral,
                                    private val myPossibleSections: Collection<CloudFormationSections>,
                                    private val myExcludeFromVariants: Collection<String>?) : CloudFormationReferenceBase(element) {

  init {
    assert(myPossibleSections.isNotEmpty())
  }

  override fun resolve(): PsiElement? {
    val entityName = StringUtil.stripQuotesAroundValue(StringUtil.notNullize(myElement.text))
    return CloudFormationResolve.resolveEntity(myElement.containingFile, entityName, myPossibleSections)
  }

  override fun getCompletionVariants(): List<String> {
    val entities = CloudFormationResolve.getEntities(myElement.containingFile, myPossibleSections.map { it.id }).toMutableSet()

    if (myPossibleSections.contains(CloudFormationSections.Parameters)) {
      entities.addAll(CloudFormationMetadataProvider.METADATA.predefinedParameters)
    }

    if (myExcludeFromVariants != null) {
      entities.removeAll(myExcludeFromVariants)
    }

    return entities.toList()
  }
}
