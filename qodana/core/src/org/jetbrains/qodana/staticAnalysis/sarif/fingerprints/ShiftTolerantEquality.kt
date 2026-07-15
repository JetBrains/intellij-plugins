package org.jetbrains.qodana.staticAnalysis.sarif.fingerprints

import com.google.common.hash.Hashing
import com.jetbrains.qodana.sarif.baseline.BaselineCalculation
import com.jetbrains.qodana.sarif.model.Result

/**
 * This is a useful hash for results in a run whose line and/or column positions have shifted.
 */
internal data object ShiftTolerantEquality : FingerprintCalculator {
  override val name: String
    get() = BaselineCalculation.SHIFT_TOLERANT_INDICATOR
  override val version: Int
    get() = 1

  override fun calculate(result: Result): String = Hashing.fingerprint2011().hash { hasher ->
    hasher.put(result.ruleId)
    hasher.put(result.level.value())
    hasher.put(result.properties?.get(BaselineEqualityV2.ADDITIONAL_FINGERPRINT_DATA)?.hashCode())
    result.locations.forEachNotNull { location ->
      val physical = location.physicalLocation

      if (physical != null) {
        physical.artifactLocation?.let { artifact ->
          artifact.uri?.let { hasher.put(it) }
          artifact.uriBaseId?.let { hasher.put(it) }
        }
        physical.region?.let { region ->
          region.charLength?.let { hasher.put(it) }
          region.snippet?.text?.let { hasher.put(it) }
        }
      } else {
        location.logicalLocations?.forEach { logical ->
          logical.name?.let { hasher.put(it) }
          logical.kind?.let { hasher.put(it) }
        }
      }
    }
  }
}
