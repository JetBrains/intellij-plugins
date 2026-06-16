package org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi

import com.intellij.codeInspection.CommonProblemDescriptor
import com.intellij.psi.PsiElement
import com.intellij.util.xml.highlighting.DomElementProblemDescriptor

internal object DomProblemFingerprintPsiResolver {
  fun resolve(descriptor: CommonProblemDescriptor): PsiElement? {
    val domElement = (descriptor as? DomElementProblemDescriptor)?.domElement ?: return null
    return domElement.xmlTag ?: domElement.xmlElement
  }
}
