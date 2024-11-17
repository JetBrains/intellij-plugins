package org.jetbrains.qodana.staticAnalysis.sarif.fingerprints

import com.jetbrains.qodana.sarif.model.Result
import com.jetbrains.qodana.sarif.model.VersionedMap

internal sealed interface FingerprintCalculator {
  val name: String
  val version: Int

  fun calculate(result: Result): String
}

private fun Result.addFingerprints(vararg calculators: FingerprintCalculator) {
  val prints = partialFingerprints ?: VersionedMap()

  calculators.forEach {
    prints.put(it.name, it.version, it.calculate(this))
  }

  partialFingerprints = prints
}

internal fun Result.fingerprintOf(calculator: FingerprintCalculator): String? =
  partialFingerprints?.get(calculator.name, calculator.version)

fun Result.withPartialFingerprints() = apply {
  addFingerprints(BaselineEqualityV1, BaselineEqualityV2)
}
