package org.jetbrains.qodana.staticAnalysis.sarif.fingerprints

import com.google.common.hash.Hashing
import com.jetbrains.qodana.sarif.baseline.BaselineCalculation
import com.jetbrains.qodana.sarif.model.Result

/**
 * Differences to [BaselineEqualityV2]:
 * - Sha256
 * - Includes user facing strings
 * - Does not include "additional data", see [BaselineEqualityV2.ADDITIONAL_FINGERPRINT_DATA]
 *
 * This is useful for de-duplicating results in a run, because there are inspections
 * that generate multiple results for the same exact location in code, but different
 * message (i.e. JvmCodeCoverage on Kotlin Classes vs Kotlin Constructors)
 */
internal data object BaselineEqualityV1 : FingerprintCalculator {
  override val name: String
    get() = BaselineCalculation.EQUAL_INDICATOR
  override val version: Int
    get() = 1

  override fun calculate(result: Result): String = Hashing.sha256().hash { hasher ->
    result.locations.forEachNotNull { it.hash(hasher) }
    result.graphs.forEachNotNull { graph ->
      graph.nodes.forEachNotNull { it.hash(hasher) }
      graph.edges.forEachNotNull { it.hash(hasher) }

      hasher.put(graph.description?.text)
    }

    hasher.put(result.ruleId)
    hasher.put(result.message?.text)
  }
}
