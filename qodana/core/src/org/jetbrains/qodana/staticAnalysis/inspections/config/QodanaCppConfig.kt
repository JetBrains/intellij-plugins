package org.jetbrains.qodana.staticAnalysis.inspections.config

data class QodanaCppConfig(
  val buildSystem: String? = null,
  val cmakePreset: String? = null,
)
