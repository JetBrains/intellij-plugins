package org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.psi

import com.jetbrains.qodana.sarif.model.PropertyBag
import org.jetbrains.qodana.staticAnalysis.sarif.QODANA_FINGERPRINT_SOURCE
import org.jetbrains.qodana.staticAnalysis.sarif.QODANA_XML_SEMANTIC_FINGERPRINT_APPLIED
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.AdditionalFingerprintDataHolder
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.StructuralFingerprintSignals
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.mergeAdditionalFingerprintData

internal enum class FingerprintSource(val sarifValue: String) {
  PROBLEM_DESCRIPTOR_PSI("problemDescriptorPsi"),
  DOM_PROBLEM_DESCRIPTOR("domProblemDescriptor"),
}

internal data class PsiFingerprintData(
  val structuralSignals: StructuralFingerprintSignals?,
  val additionalFingerprintData: AdditionalFingerprintDataHolder = AdditionalFingerprintDataHolder.EMPTY,
  val source: FingerprintSource? = null,
) {
  fun storeIn(props: PropertyBag) {
    props.mergeAdditionalFingerprintData(additionalFingerprintData)

    if (additionalFingerprintData.xmlSemanticIdentity != null) {
      props[QODANA_XML_SEMANTIC_FINGERPRINT_APPLIED] = true
      source?.let { props[QODANA_FINGERPRINT_SOURCE] = it.sarifValue }
    }
  }
}
