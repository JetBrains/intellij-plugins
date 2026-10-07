package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.openapi.util.Key
import org.jetbrains.qodana.staticAnalysis.profile.DerivedInspection

/** Supplies a derived inspection and result properties through a problem descriptor. */
class DerivedInspectionMetadata(
  val inspection: DerivedInspection,
  val properties: Map<String, String>,
) {
  companion object {
    val KEY: Key<DerivedInspectionMetadata> = Key.create("qodana.derived.inspection")
  }
}
