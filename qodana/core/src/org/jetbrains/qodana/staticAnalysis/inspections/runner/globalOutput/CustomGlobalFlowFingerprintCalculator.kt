package org.jetbrains.qodana.staticAnalysis.inspections.runner.globalOutput

import com.intellij.openapi.extensions.ExtensionPointName
import com.jetbrains.qodana.sarif.model.Result
import com.jetbrains.qodana.sarif.model.VersionedMap

/**
 * Should be defined whenever a certain flow inspection needs to redefine its fingerprint.
 * Currently, fingerprints of flow inspections depend on all inner propagation nodes,
 * which may result in problems with the baseline if the inspection starts reporting paths more carefully.
 */
interface CustomGlobalFlowFingerprintCalculator {
  companion object {
    val EP_NAME: ExtensionPointName<CustomGlobalFlowFingerprintCalculator> = ExtensionPointName("org.intellij.qodana.globalFlowFingerprintCalculator")

    fun Result.withCustomPartialFingerprints(): Result = apply {
      EP_NAME.extensionList
        .mapNotNull { it.computeFingerprints(this) }
        .singleOrNull()?.let {
          partialFingerprints = it
        }
    }
  }

  fun computeFingerprints(result: Result): VersionedMap<String>?
}