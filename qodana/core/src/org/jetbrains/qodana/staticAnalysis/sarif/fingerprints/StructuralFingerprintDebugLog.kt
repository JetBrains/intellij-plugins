package org.jetbrains.qodana.staticAnalysis.sarif.fingerprints

import com.intellij.openapi.diagnostic.logger
import com.jetbrains.qodana.sarif.baseline.BaselineCalculation
import com.jetbrains.qodana.sarif.model.Result
import org.jetbrains.qodana.util.QodanaMessageReporter

internal object StructuralFingerprintDebugLog {
  private val LOG = logger<StructuralFingerprintDebugLog>()

  fun log(message: String) {
    val fullMessage = "QODANA STRUCTURAL FINGERPRINT DEBUG: $message"
    LOG.warn(fullMessage)
    println(fullMessage)
    System.err.println(fullMessage)
    QodanaMessageReporter.DEFAULT.reportError(fullMessage)
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
