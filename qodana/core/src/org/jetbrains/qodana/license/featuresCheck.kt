package org.jetbrains.qodana.license

import com.intellij.codeInspection.ex.ToolsImpl
import org.jetbrains.qodana.license.QodanaLicenseType.*

private val ULTIMATE_PLUS_PLUGINS: Set<String> = setOf(
  "com.intellij.plugins.dependencyAnalysis",
  "com.intellij.taintAnalysis",
  "intellij.jvm.dfa.analysis",
  "org.jetbrains.security.package-checker"
)

private val ULTIMATE_PLUS_INSPECTIONS: Set<String> = setOf(
  "RiderSecurityErrorsInspection"
)

internal fun QodanaLicenseType.isInspectionLicensed(from: ToolsImpl): Boolean {
  if (this == ULTIMATE_PLUS || this == PREMIUM) return true
  if (this == NONE) return false
  if (from.tool.shortName in ULTIMATE_PLUS_INSPECTIONS) return false

  val pluginId = from.tool.extension?.pluginDescriptor?.pluginId?.idString
  return pluginId !in ULTIMATE_PLUS_PLUGINS
}

internal fun QodanaLicenseType.isFixesAvailable(): Boolean {
  return this == ULTIMATE_PLUS || this == ULTIMATE || this == PREMIUM
}
