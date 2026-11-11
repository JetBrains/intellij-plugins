package org.jetbrains.qodana.staticAnalysis.sarif.fingerprints

import com.jetbrains.qodana.sarif.model.Result
import com.jetbrains.qodana.sarif.model.VersionedMap

/**
 * Enables the structural baseline-matching indicators (shift-tolerant + structural) on top of the
 * always-present equality indicators.
 *
 * `-Dqodana.structural.fingerprints=true|false` decides alone when it is set. Without it,
 * `-Dqodana.structural.fingerprints.default=true|false` decides. The linter distribution sets that default,
 * and the value is `false` when neither property is set.
 */
internal val areStructuralFingerprintsEnabled: Boolean by lazy {
  areStructuralFingerprintsEnabled(
    property = System.getProperty("qodana.structural.fingerprints"),
    default = System.getProperty("qodana.structural.fingerprints.default"),
  )
}

internal fun areStructuralFingerprintsEnabled(property: String?, default: String?): Boolean =
  property?.toBoolean() ?: default.toBoolean()

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
    addFingerprints(ShiftTolerantIndicator)
  }
}

fun Result.addStructuralFingerprints(signals: StructuralFingerprintSignals): Result = apply {
  val prints = partialFingerprints ?: VersionedMap()
  for (calculator in listOf(
    ScopeAndShapeTolerantIndicator,
    ExtractionAndRefactorTolerantIndicator,
    MoveAndRefactorTolerantIndicator,
    EnclosingScopeIndicator,
  )) {
    calculator.calculate(this, signals)?.let { prints.put(calculator.name, calculator.version, it) }
  }
  partialFingerprints = prints
}
