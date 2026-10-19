package org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi

import com.intellij.psi.PsiElement
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.AdditionalFingerprintDataHolder
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.areStructuralFingerprintsEnabled

internal object PsiFingerprintDataExtractor {
  fun extract(element: PsiElement, source: FingerprintSource? = null): PsiFingerprintData {
    return PsiFingerprintData(
      structuralSignals = if (areStructuralFingerprintsEnabled) PsiSignalExtractor.extractSignals(element) else null,
      additionalFingerprintData = extractXmlSemanticIdentity(element),
      source = source,
    )
  }

  private fun extractXmlSemanticIdentity(element: PsiElement): AdditionalFingerprintDataHolder {
    val identity = QodanaXmlSemanticIdentityProvider.EP_NAME.extensionList.firstNotNullOfOrNull { it.extractIdentity(element) }
    return identity?.let(AdditionalFingerprintDataHolder::fromXmlSemanticIdentity) ?: AdditionalFingerprintDataHolder.EMPTY
  }
}
