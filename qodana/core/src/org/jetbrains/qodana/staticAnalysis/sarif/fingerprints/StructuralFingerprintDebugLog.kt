package org.jetbrains.qodana.staticAnalysis.sarif.fingerprints

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.jetbrains.qodana.sarif.baseline.BaselineCalculation
import com.jetbrains.qodana.sarif.model.Result

/**
 * Debug log of the structural fingerprints and of the duplicate results that share a hash.
 *
 * It is off by default.
 * To turn it on, add `#org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.StructuralFingerprintDebugLog` to `idea.log.debug.categories`.
 */
internal object StructuralFingerprintDebugLog {
  private val LOG = logger<StructuralFingerprintDebugLog>()

  val isEnabled: Boolean
    get() = LOG.isDebugEnabled

  fun log(message: () -> String) {
    LOG.debug { "QODANA STRUCTURAL FINGERPRINT DEBUG: ${message()}" }
  }
}

internal fun StructuralFingerprintSignals.structuralSignalsDebugSummary(): String =
  "astShape=${astShape != null}, enclosingScopeName=${enclosingScopeName ?: "<null>"}, enclosingScopeType=${enclosingScopeType ?: "<null>"}"

internal fun Result.structuralFingerprintDebugSummary(): String {
  val fingerprints = partialFingerprints
  return "equal/v1=${fingerprints?.get(BaselineCalculation.EQUAL_INDICATOR, 1) != null}, " +
         "equal/v2=${fingerprints?.get(BaselineCalculation.EQUAL_INDICATOR, 2) != null}, " +
         "shiftTolerant/v1=${fingerprints?.get(BaselineCalculation.SHIFT_TOLERANT_INDICATOR, 1) != null}, " +
         "moveAndRefactorTolerant/v1=${fingerprints?.get(BaselineCalculation.MOVE_AND_REFACTOR_TOLERANT_INDICATOR, 1) != null}, " +
         "extractionAndRefactorTolerant/v1=${fingerprints?.get(BaselineCalculation.EXTRACTION_AND_REFACTOR_TOLERANT_INDICATOR, 1) != null}, " +
         "enclosingScope/v1=${fingerprints?.get(BaselineCalculation.ENCLOSING_SCOPE_INDICATOR, 1) != null}"
}
