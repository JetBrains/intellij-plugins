package org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi

import com.jetbrains.qodana.sarif.model.PropertyBag
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.AdditionalFingerprintDataHolder
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.StructuralFingerprintSignals
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.mergeAdditionalFingerprintData

internal data class PsiFingerprintData(
  val structuralSignals: StructuralFingerprintSignals?,
  val additionalFingerprintData: AdditionalFingerprintDataHolder = AdditionalFingerprintDataHolder.EMPTY,
) {
  fun storeIn(props: PropertyBag) {
    props.mergeAdditionalFingerprintData(additionalFingerprintData)
  }
}
