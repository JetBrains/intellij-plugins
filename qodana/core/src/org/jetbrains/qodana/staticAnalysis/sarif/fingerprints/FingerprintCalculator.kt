package org.jetbrains.qodana.staticAnalysis.sarif.fingerprints

import com.jetbrains.qodana.sarif.model.Result
import com.jetbrains.qodana.sarif.model.VersionedMap

/**
 * Enables the structural baseline-matching indicators (shift-tolerant + structural) on top of the
 * always-present equality indicators. Off by default.
 * Set `-Dqodana.structural.fingerprints=true` for the internal usage.
 */
internal val areStructuralFingerprintsEnabled: Boolean =
  System.getProperty("qodana.structural.fingerprints")?.toBoolean() ?: false

internal sealed interface FingerprintCalculator {
  val name: String
  val version: Int

  fun calculate(result: Result): String
}

/** Calculators whose hash is derived from PSI-extracted [StructuralFingerprintSignals]. */
internal sealed interface StructuralFingerprintCalculator {
  val name: String
  val version: Int
  fun calculate(result: Result, signals: StructuralFingerprintSignals): String?
}

private fun Result.addFingerprints(vararg calculators: FingerprintCalculator) {
  val prints = partialFingerprints ?: VersionedMap()

  calculators.forEach {
    it.calculate(this).let { hash -> prints.put(it.name, it.version, hash) }
  }

  partialFingerprints = prints
}

internal fun Result.fingerprintOf(calculator: FingerprintCalculator): String? =
  partialFingerprints?.get(calculator.name, calculator.version)

fun Result.withPartialFingerprints(): Result = apply {
  addFingerprints(BaselineEqualityV1, BaselineEqualityV2)
  if (areStructuralFingerprintsEnabled) {
    addFingerprints(ShiftTolerantEquality)
  }
}

fun Result.addStructuralFingerprints(signals: StructuralFingerprintSignals): Result = apply {
  val prints = partialFingerprints ?: VersionedMap()
  for (calculator in listOf(
    ExtractionAndRefactorTolerantIndicator,
    MoveAndRefactorTolerantIndicator,
    EnclosingScopeIndicator,
  )) {
    calculator.calculate(this, signals)?.let { prints.put(calculator.name, calculator.version, it) }
  }
  partialFingerprints = prints
}
