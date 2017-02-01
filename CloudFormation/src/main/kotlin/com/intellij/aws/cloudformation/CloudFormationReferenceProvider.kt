package com.intellij.aws.cloudformation

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
    fun buildFromElement(element: PsiElement): Collection<PsiReference> {
      val parsed = CloudFormationParser.parse(element.containingFile)
      // val scalarNode = parsed.getCfnNodes(element).ofType<CfnScalarValueNode>().singleOrNull() ?: return null

      // TODO Cache!!!
      val inspectionResult = CloudFormationInspections.inspectFile(parsed)
      val references = inspectionResult.references.get(element)
      return references
    }
  }
}
