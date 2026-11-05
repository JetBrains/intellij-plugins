package org.jetbrains.qodana.staticAnalysis.sarif.fingerprints

import com.intellij.openapi.application.ApplicationManager
import com.jetbrains.qodana.sarif.model.Result
import com.jetbrains.qodana.sarif.model.VersionedMap
import org.jetbrains.qodana.staticAnalysis.inspections.runner.qodanaProductCode

/** Product codes of the linters that use the structural indicators by default. */
private val STRUCTURAL_FINGERPRINTS_PRODUCT_CODES: Set<String> = setOf("QDJVM", "QDJS")

/**
 * Enables the structural baseline-matching indicators (shift-tolerant + structural) on top of the
 * always-present equality indicators. On by default for the QDJVM and QDJS linters, off for the others.
 * Set `-Dqodana.structural.fingerprints=true|false` to override the default.
 */
internal val areStructuralFingerprintsEnabled: Boolean by lazy {
  val productCode = if (ApplicationManager.getApplication() != null) qodanaProductCode() else null
  areStructuralFingerprintsEnabled(System.getProperty("qodana.structural.fingerprints"), productCode)
}

internal fun areStructuralFingerprintsEnabled(property: String?, productCode: String?): Boolean =
  property?.toBoolean() ?: (productCode in STRUCTURAL_FINGERPRINTS_PRODUCT_CODES)

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
