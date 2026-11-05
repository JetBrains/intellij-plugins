package org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi

import com.intellij.psi.PsiElement
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.AdditionalFingerprintDataHolder

internal object PsiFingerprintDataExtractor {
  fun extract(element: PsiElement): PsiFingerprintData {
    return PsiFingerprintData(
      structuralSignals = PsiSignalExtractor.extractSignals(element),
      additionalFingerprintData = extractXmlSemanticIdentity(element),
    )
  }

  private fun extractXmlSemanticIdentity(element: PsiElement): AdditionalFingerprintDataHolder {
    val identity = QodanaXmlSemanticIdentityProvider.EP_NAME.extensionList.firstNotNullOfOrNull { it.extractIdentity(element) }
    return identity?.let(AdditionalFingerprintDataHolder::fromXmlSemanticIdentity) ?: AdditionalFingerprintDataHolder.EMPTY
  }
}
