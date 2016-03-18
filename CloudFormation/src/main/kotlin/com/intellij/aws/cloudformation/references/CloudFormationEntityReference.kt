package com.intellij.aws.cloudformation.references

import com.intellij.aws.cloudformation.CloudFormationMetadataProvider
import com.intellij.aws.cloudformation.CloudFormationResolve
import com.intellij.aws.cloudformation.CloudFormationSections
import com.intellij.json.psi.JsonLiteral
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement

class CloudFormationEntityReference(element: JsonLiteral,
                                    private val myPossibleSections: Collection<String>,
                                    private val myExcludeFromVariants: Collection<String>?) : CloudFormationReferenceBase(element) {

  init {
    assert(myPossibleSections.size > 0)
  }

  override fun resolve(): PsiElement? {
    val entityName = StringUtil.stripQuotesAroundValue(StringUtil.notNullize(myElement.text))
    return CloudFormationResolve.resolveEntity(myElement.containingFile, entityName, myPossibleSections)
  }

  override fun getCompletionVariants(): List<String> {
    val entities = CloudFormationResolve.getEntities(myElement.containingFile, myPossibleSections).toMutableSet()

    if (myPossibleSections.contains(CloudFormationSections.Parameters)) {
      entities.addAll(CloudFormationMetadataProvider.METADATA.predefinedParameters)
    }

    if (myExcludeFromVariants != null) {
      entities.removeAll(myExcludeFromVariants)
    }

    return entities.toList()
  }
}
