package org.jetbrains.qodana.staticAnalysis.sarif.fingerprints

import com.google.common.hash.Hashing
import com.jetbrains.qodana.sarif.baseline.BaselineCalculation.EXTRACTION_AND_REFACTOR_TOLERANT_INDICATOR
import com.jetbrains.qodana.sarif.baseline.BaselineCalculation.MOVE_AND_REFACTOR_TOLERANT_INDICATOR
import com.jetbrains.qodana.sarif.model.Result

/** PSI-derived signals extracted during problem consumption and fed directly to the structural fingerprint calculators */
data class StructuralFingerprintSignals(
  val astShape: String?,
  val enclosingScopeName: String?,
  val enclosingScopeType: String?,
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
    if (signals.astShape == null && signals.enclosingScopeName == null) return null
    return structuralFingerprint(result, signals.astShape, extras = listOf(result.fileName(), signals.enclosingScopeName))
  }
}

/**
 * Shared base of every structural fingerprint: ruleId + additionalFingerprintData + astShape, followed by the
 * variant-specific [extras]. Null parts are skipped by the hasher.
 */
private fun structuralFingerprint(result: Result, astShape: String?, extras: List<String?>): String =
  Hashing.fingerprint2011().hash { hasher ->
    hasher.put(result.ruleId)
    hasher.put(result.properties?.get(BaselineEqualityV2.ADDITIONAL_FINGERPRINT_DATA)?.hashCode())
    hasher.put(astShape)
    extras.forEach(hasher::put)
  }

private fun Result.fileName(): String? =
  locations?.firstOrNull()?.physicalLocation?.artifactLocation?.uri
    ?.substringAfterLast('/')
    ?.substringBeforeLast('.')
