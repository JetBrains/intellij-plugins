package org.jetbrains.qodana.inspectionKts

import com.intellij.openapi.util.registry.Registry

object InspectionKtsRegistry {
  private const val QODANA_PLUGIN_ID = "org.intellij.qodana"

  val useAllDistributionForInspectionKtsDependencies: Boolean
    get() = Registry.`is`("qd.inspection.kts.all.distribution.for.dependencies", false)

  val limitedInspectionKtsDependencies: Boolean
    get() = Registry.`is`("qd.inspection.kts.limited.dependencies", true)
}
