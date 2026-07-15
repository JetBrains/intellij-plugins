package org.jetbrains.qodana.staticAnalysis.sarif.fingerprints

import com.jetbrains.qodana.sarif.baseline.BaselineCalculation
import com.jetbrains.qodana.sarif.model.Result

/** This indicator is an important part of the Collision resolver Cascade in Qodana-Sarif library. */
internal data object EnclosingScopeIndicator : StructuralFingerprintCalculator {
  override val name: String
    get() = BaselineCalculation.ENCLOSING_SCOPE_INDICATOR
  override val version: Int
    get() = 1

  override fun calculate(result: Result, signals: StructuralFingerprintSignals): String? {
    val enclosingScopeName = signals.enclosingScopeName
    val enclosingScopeType = signals.enclosingScopeType

    return if (enclosingScopeName != null && enclosingScopeType != null) "$enclosingScopeType#$enclosingScopeName" else null
  }
}
