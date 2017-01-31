package com.intellij.aws.cloudformation

import com.intellij.aws.cloudformation.references.CloudFormationEntityReference
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext

class CloudFormationReferenceProvider : PsiReferenceProvider() {

  override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
    if (!CloudFormationPsiUtils.isCloudFormationFile(element)) {
      return PsiReference.EMPTY_ARRAY
    }

    val references = buildFromElement(element)
    return if (references.isEmpty()) PsiReference.EMPTY_ARRAY else references.toTypedArray()
  }

  companion object {
    val ParametersAndResourcesSections = listOf(CloudFormationSection.Parameters, CloudFormationSection.Resources)

    fun buildFromElement(element: PsiElement): List<PsiReference> {
      val parsed = CloudFormationParser.parse(element.containingFile)
      // val scalarNode = parsed.getCfnNodes(element).ofType<CfnScalarValueNode>().singleOrNull() ?: return null

      // TODO Cache!!!
      val inspectionResult = CloudFormationInspections.inspectFile(parsed)
      val references = inspectionResult.references.get(element)
      if (references.isEmpty()) return emptyList()

      return references.map { reference ->
        when (reference.type) {
          ReferenceType.Parameter -> CloudFormationEntityReference(element, CloudFormationSection.ParametersSingletonList, reference.excludeFromCompletion)
          ReferenceType.Mapping -> CloudFormationEntityReference(element, CloudFormationSection.MappingsSingletonList, reference.excludeFromCompletion)
          ReferenceType.Condition -> CloudFormationEntityReference(element, CloudFormationSection.ConditionsSingletonList, reference.excludeFromCompletion)
          ReferenceType.Resource -> CloudFormationEntityReference(element, CloudFormationSection.ResourcesSingletonList, reference.excludeFromCompletion)
          ReferenceType.Ref -> CloudFormationEntityReference(element, ParametersAndResourcesSections, reference.excludeFromCompletion)
        }
      }
    }
  }
}
