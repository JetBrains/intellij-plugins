package org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi

import com.intellij.psi.PsiElement
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.areStructuralFingerprintsEnabled

internal object PsiFingerprintDataExtractor {
  fun extract(element: PsiElement): PsiFingerprintData {
    return PsiFingerprintData(
      structuralSignals = if (areStructuralFingerprintsEnabled) PsiSignalExtractor.extractSignals(element) else null,
      additionalFingerprintData = XmlSemanticIdentityExtractor.extractAdditionalFingerprintData(element),
    )
  }
}
