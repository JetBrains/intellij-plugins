package org.jetbrains.qodana.staticAnalysis.sarif.fingerprints

import com.google.common.hash.Hashing
import com.jetbrains.qodana.sarif.baseline.BaselineCalculation.EXTRACTION_AND_REFACTOR_TOLERANT_INDICATOR
import com.jetbrains.qodana.sarif.baseline.BaselineCalculation.MOVE_AND_REFACTOR_TOLERANT_INDICATOR
import com.jetbrains.qodana.sarif.baseline.BaselineCalculation.SCOPE_AND_SHAPE_TOLERANT_INDICATOR
import com.jetbrains.qodana.sarif.model.Result

/** PSI-derived signals extracted during problem consumption and fed directly to the structural fingerprint calculators */
data class StructuralFingerprintSignals(
  val astShape: String?,
  val enclosingScopeName: String?,
  val enclosingScopeType: String?,
  val problemElementType: String?,
)

/** Structural fingerprint tolerant to function extraction/file renamings and refactorings */
internal data object ExtractionAndRefactorTolerantIndicator : StructuralFingerprintCalculator {
  override val name: String = EXTRACTION_AND_REFACTOR_TOLERANT_INDICATOR
  override val version: Int = 1

  override fun calculate(result: Result, signals: StructuralFingerprintSignals): String? {
    val enclosingScopeName = signals.enclosingScopeName ?: return null
    return structuralFingerprint(result, signals.astShape, extras = listOf(enclosingScopeName))
  }
}

/** Structural fingerprint tolerant to moving files and refactorings inside the same file and func */
internal data object MoveAndRefactorTolerantIndicator : StructuralFingerprintCalculator {
  override val name: String = MOVE_AND_REFACTOR_TOLERANT_INDICATOR
  override val version: Int = 1

  override fun calculate(result: Result, signals: StructuralFingerprintSignals): String? {
    val astShape = signals.astShape ?: return null
    val enclosingScopeName = signals.enclosingScopeName ?: return null
    return structuralFingerprint(result, astShape, extras = listOf(result.fileName(), enclosingScopeName))
  }
}

/**
 * Structural fingerprint tolerant to a heavy refactoring inside one file. It reads the problem element type
 * alone, so it is the coarsest of the indicators.
 *
 * The name is a literal, because `BaselineCalculation` of the pinned qodana-sarif holds no constant for it.
 */
internal data object ScopeAndShapeTolerantIndicator : StructuralFingerprintCalculator {
  override val name: String = SCOPE_AND_SHAPE_TOLERANT_INDICATOR
  override val version: Int = 1

  override fun calculate(result: Result, signals: StructuralFingerprintSignals): String? {
    val problemElementType = signals.problemElementType ?: return null
    return structuralFingerprint(result, problemElementType, extras = listOf(result.fileName()))
  }
}

/**
 * Shared base of every structural fingerprint: ruleId + additionalFingerprintData + [structure], followed by
 * the variant-specific [extras]. Null parts are skipped by the hasher.
 *
 * [structure] is the astShape for most indicators, and the problem element type for the scope-and-shape one.
 */
private fun structuralFingerprint(result: Result, structure: String?, extras: List<String?>): String =
  Hashing.fingerprint2011().hash { hasher ->
    hasher.put(result.ruleId)
    hasher.put(result.properties?.get(BaselineEqualityV2.ADDITIONAL_FINGERPRINT_DATA)?.hashCode())
    hasher.put(structure)
    extras.forEach(hasher::put)
  }

private fun Result.fileName(): String? =
  locations?.firstOrNull()?.physicalLocation?.artifactLocation?.uri
    ?.substringAfterLast('/')
    ?.substringBeforeLast('.')
