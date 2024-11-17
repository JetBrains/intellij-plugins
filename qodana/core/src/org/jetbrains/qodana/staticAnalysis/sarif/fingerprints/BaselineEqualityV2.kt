package org.jetbrains.qodana.staticAnalysis.sarif.fingerprints

import com.google.common.hash.Hashing
import com.jetbrains.qodana.sarif.baseline.BaselineCalculation
import com.jetbrains.qodana.sarif.model.Result

/**
 * Unlike [BaselineEqualityV1], this is not useful for de-duplication, but
 * very useful for baseline calculation.
 * There are inspections that report different texts for the same failure,
 * depending on when inspection is run, i.e. "Lib X is vulnerable, use version 1" and
 * later "... use version 1.1"
 */
internal data object BaselineEqualityV2 : FingerprintCalculator {

  const val ADDITIONAL_FINGERPRINT_DATA = "${BaselineCalculation.EQUAL_INDICATOR}/2/additionalData"

  override val name: String
    get() = BaselineCalculation.EQUAL_INDICATOR
  override val version: Int
    get() = 2

  override fun calculate(result: Result): String = Hashing.fingerprint2011().hash { hasher ->
    hasher.put(result.ruleId)

    result.locations.forEachNotNull { it.hash(hasher) }
    if (result.locations == null || result.locations.isEmpty()) {
      hasher.put(result.message?.text)
    }
    result.graphs.forEachNotNull { graph ->
      graph.nodes.forEachNotNull { it.hash(hasher) }
      graph.edges.forEachNotNull { it.hash(hasher) }
    }
    hasher.put(result.properties?.get(ADDITIONAL_FINGERPRINT_DATA)?.hashCode())
  }
}
