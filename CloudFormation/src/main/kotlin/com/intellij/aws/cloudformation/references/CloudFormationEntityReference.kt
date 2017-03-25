package com.intellij.aws.cloudformation.references

import com.intellij.aws.cloudformation.CloudFormationMetadataProvider
import com.intellij.aws.cloudformation.CloudFormationParser
import com.intellij.aws.cloudformation.CloudFormationResolve
import com.intellij.aws.cloudformation.CloudFormationSection
import com.intellij.psi.PsiElement

class CloudFormationEntityReference(element: PsiElement,
                                    private val myPossibleSections: Collection<CloudFormationSection>,
                                    private val myExcludeFromVariants: Collection<String>?,
                                    private val referenceValue: String? = null) : CloudFormationReferenceBase(element) {

  init {
    assert(myPossibleSections.isNotEmpty())
  }

  override fun resolve(): PsiElement? {
    val value = referenceValue ?: scalarNode.value

    if (myExcludeFromVariants != null && myExcludeFromVariants.contains(value)) {
      return null
    }

    val parsed = CloudFormationParser.parse(element.containingFile)
    val node = CloudFormationResolve.resolveEntity(parsed, value, myPossibleSections) ?: return null
    return parsed.getPsiElement(node)
  }

  override fun getCompletionVariants(): List<String> {
    val parsed = CloudFormationParser.parse(element.containingFile)
    val entities = CloudFormationResolve.getEntities(parsed, myPossibleSections).toMutableSet()

    if (myPossibleSections.contains(CloudFormationSection.Parameters)) {
      entities.addAll(CloudFormationMetadataProvider.METADATA.predefinedParameters)
    }

    if (myExcludeFromVariants != null) {
      entities.removeAll(myExcludeFromVariants)
    }

    return entities.toList()
  }
}
